package com.nexacro.fullstack.business.domain.large;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LargeDataService {

    public static class LargePageResult {
        private final NexacroDataset dataset;
        private final long totalCount;

        public LargePageResult(NexacroDataset dataset, long totalCount) {
            this.dataset = dataset;
            this.totalCount = totalCount;
        }

        public NexacroDataset getDataset() { return dataset; }
        public long getTotalCount() { return totalCount; }
    }

    private final LargeDataDao dao;

    public LargeDataService(LargeDataDao dao) { this.dao = dao; }

    public LargePageResult page(int page, int pageSize, String category) {
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 50;
        int offset = (page - 1) * pageSize;
        long total = dao.count(category);
        List<Map<String, Object>> rows = dao.page(offset, pageSize, category);

        NexacroDataset ds = new NexacroDataset();
        ds.setId("large");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("ROW_ID", "BIGINT", "19"));
        cols.add(new NexacroDataset.Column("CATEGORY", "STRING", "10"));
        cols.add(new NexacroDataset.Column("SEQ_NO", "INT", "10"));
        cols.add(new NexacroDataset.Column("VALUE_1", "STRING", "100"));
        cols.add(new NexacroDataset.Column("VALUE_2", "DECIMAL", "18"));
        cols.add(new NexacroDataset.Column("VALUE_3", "INT", "10"));
        cols.add(new NexacroDataset.Column("CREATED_AT", "DATETIME", "19"));
        ci.setColumns(cols);
        ds.setColumnInfo(ci);
        ds.setRows(rows);
        return new LargePageResult(ds, total);
    }
}
