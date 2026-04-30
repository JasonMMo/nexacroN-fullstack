package com.nexacro.fullstack.business.domain.large;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class LargeDataService {

    private final LargeDataDao largeDataDao;

    public LargeDataService(LargeDataDao largeDataDao) {
        this.largeDataDao = largeDataDao;
    }

    /**
     * Returns the total row count, optionally filtered by category.
     * Exposed for controllers that need to populate the totalCount parameter.
     */
    public int count(String category) {
        return largeDataDao.count(category);
    }

    /**
     * Returns a paged dataset (id="large").
     * page is 1-indexed; offset is derived as (page - 1) * pageSize.
     */
    public NexacroDataset page(int page, int pageSize, String category) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> rows = largeDataDao.page(offset, pageSize, category);

        NexacroDataset ds = new NexacroDataset();
        ds.setId("large");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("ROW_ID",     "int",      "20"),
            new NexacroDataset.Column("CATEGORY",   "string",   "10"),
            new NexacroDataset.Column("SEQ_NO",     "int",      "10"),
            new NexacroDataset.Column("VALUE_1",    "string",   "100"),
            new NexacroDataset.Column("VALUE_2",    "decimal",  "18"),
            new NexacroDataset.Column("VALUE_3",    "int",      "10"),
            new NexacroDataset.Column("CREATED_AT", "datetime", "20")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
    }
}
