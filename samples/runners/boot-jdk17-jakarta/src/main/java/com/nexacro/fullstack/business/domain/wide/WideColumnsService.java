package com.nexacro.fullstack.business.domain.wide;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for wide-columns search endpoint (#14 search_manyColumn_data).
 *
 * <p>Ported from mvc-jdk8-javax-legacy/src/main/java/com/nexacro/example/impl/UidapterManyColumnServiceImpl.java@69cdaf1a9d on 2026-04-24.
 * Adaptations: javax→jakarta shim types; returns NexacroDataset (id="output1") instead of raw List
 * wrapped in legacy NexacroResult; column definitions align with WIDE_COLUMNS seed schema (50 cols).
 */
@Service
public class WideColumnsService {

    private final WideColumnsDao wideColumnsDao;

    public WideColumnsService(WideColumnsDao wideColumnsDao) {
        this.wideColumnsDao = wideColumnsDao;
    }

    /**
     * Execute the wide-columns search and build an output1 dataset.
     *
     * Ported from mvc-jdk8-javax-legacy/.../UidapterBoardController.search_manyColumn_data@69cdaf1a9d on 2026-04-24.
     * Adaptations: no initDataCount / largeDataJdbc init (seed-data handles it); uses map-based DAO.
     */
    public NexacroDataset search(String keyId) {
        List<Map<String, Object>> rows = wideColumnsDao.search(keyId);

        NexacroDataset ds = new NexacroDataset();
        ds.setId("output1");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();

        List<NexacroDataset.Column> cols = new ArrayList<>();
        cols.add(new NexacroDataset.Column("KEY_ID", "string", "20"));
        // COL_01..COL_10 — STRING
        for (int i = 1; i <= 10; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "string", "50"));
        }
        // COL_11..COL_20 — INT
        for (int i = 11; i <= 20; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "int", "10"));
        }
        // COL_21..COL_30 — BIGDECIMAL
        for (int i = 21; i <= 30; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "bigdecimal", "22"));
        }
        // COL_31..COL_35 — DATE
        for (int i = 31; i <= 35; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "date", "10"));
        }
        // COL_36..COL_40 — DATETIME
        for (int i = 36; i <= 40; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "datetime", "20"));
        }
        // COL_41..COL_45 — BOOLEAN
        for (int i = 41; i <= 45; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "boolean", "1"));
        }
        // COL_46..COL_50 — CLOB (string in Nexacro)
        for (int i = 46; i <= 50; i++) {
            cols.add(new NexacroDataset.Column(colName(i), "string", "0"));
        }

        ci.setColumns(cols);
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
    }

    private static String colName(int n) {
        return String.format("COL_%02d", n);
    }
}
