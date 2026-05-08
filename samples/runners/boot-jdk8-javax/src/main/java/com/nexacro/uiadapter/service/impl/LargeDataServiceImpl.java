package com.nexacro.uiadapter.service.impl;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataTypes;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.uiadapter.domain.LargeData;
import com.nexacro.uiadapter.mapper.LargeDataMapper;
import com.nexacro.uiadapter.service.LargeDataService;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Default {@link LargeDataService}.
 *
 * <p>Implements the canonical Nexacro N chunked-streaming pattern:
 * iterate {@link LargeDataMapper#page} in {@code firstRowCount} (first chunk)
 * then {@code chunkSize} (subsequent) batches, fill a single reused
 * {@link DataSet}, flush each batch via
 * {@link NexacroFirstRowHandler#sendDataSet(DataSet)}, and {@link DataSet#clearData()}
 * before the next batch.
 */
@Service
@RequiredArgsConstructor
public class LargeDataServiceImpl implements LargeDataService {

    private static final Logger log = LoggerFactory.getLogger(LargeDataServiceImpl.class);

    private final LargeDataMapper largeDataMapper;

    @Override
    public int count(String category) {
        return largeDataMapper.count(category);
    }

    @Override
    public List<LargeData> page(int page, int pageSize, String category) {
        int safePage = page < 1 ? 1 : page;
        int safeSize = pageSize < 1 ? 50 : pageSize;
        int offset = (safePage - 1) * safeSize;
        return largeDataMapper.page(offset, safeSize, category);
    }

    @Override
    public void selectLargeData(NexacroFirstRowHandler handler,
                                String dataSetName,
                                int firstRowCount,
                                int chunkSize) {

        int total = largeDataMapper.count(null);
        if (log.isDebugEnabled()) {
            log.debug("selectLargeData total={} firstRowCount={} chunkSize={} dsName={}",
                    total, firstRowCount, chunkSize, dataSetName);
        }

        DataSet ds = new DataSet(dataSetName);
        ds.addColumn("largeId",  DataTypes.INT);
        ds.addColumn("name",     DataTypes.STRING);
        ds.addColumn("regDate",  DataTypes.DATE);
        ds.addColumn("story",    DataTypes.STRING);
        ds.addColumn("status",   DataTypes.INT);

        int safeFirst = firstRowCount > 0 ? firstRowCount : 1000;
        int safeChunk = chunkSize    > 0 ? chunkSize    : 1000;

        int offset    = 0;
        int chunkNo   = 0;

        while (offset < total) {
            int limit = (chunkNo == 0) ? safeFirst : safeChunk;

            List<LargeData> rows = largeDataMapper.page(offset, limit, null);
            if (rows == null || rows.isEmpty()) break;

            if (chunkNo > 0) ds.clearData();

            for (LargeData row : rows) {
                int idx = ds.newRow();
                if (row.getLargeId() != null) ds.set(idx, "largeId", row.getLargeId().intValue());
                ds.set(idx, "name",    row.getName());
                ds.set(idx, "regDate", row.getRegDate());
                ds.set(idx, "story",   row.getStory());
                if (row.getStatus()  != null) ds.set(idx, "status",  row.getStatus().intValue());
            }

            try {
                handler.sendDataSet(ds);
            } catch (PlatformException e) {
                throw new RuntimeException(
                    "selectLargeData sendDataSet failed at offset=" + offset + " chunk=" + chunkNo, e);
            }

            if (handler.checkError()) {
                if (log.isDebugEnabled()) {
                    log.debug("selectLargeData abort: client closed connection at offset={}", offset);
                }
                break;
            }

            offset  += rows.size();
            chunkNo += 1;
        }

        if (log.isDebugEnabled()) {
            log.debug("selectLargeData done: chunks={} sent={}/{}", chunkNo, offset, total);
        }
    }
}
