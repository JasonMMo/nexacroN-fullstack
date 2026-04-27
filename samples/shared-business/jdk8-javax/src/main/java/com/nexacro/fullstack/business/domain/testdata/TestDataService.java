package com.nexacro.fullstack.business.domain.testdata;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for TestData type demo endpoints (#10 select, #11 check).
 *
 * Ported from <boot-jdk17-jakarta-legacy>/<TestDataService.java>@e49a17791d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 * JDK 8: List.of() replaced with Arrays.asList(); no var; no records.
 */
@Service
public class TestDataService {

    private final TestDataDao testDataDao;

    public TestDataService(TestDataDao testDataDao) {
        this.testDataDao = testDataDao;
    }

    /**
     * Select all test-data rows and return as a NexacroDataset (id="output1").
     * Demonstrates all Nexacro column data types: STRING, INT, BOOLEAN, LONG, FLOAT,
     * DOUBLE, BIGDECIMAL, DATE, TIME, DATETIME, BYTES.
     *
     * Ported from boot-jdk17-jakarta-legacy/.../ExampleDateTypeController.select_testDataTypeList@e49a17791d on 2026-04-24.
     * Adaptations: DB query replaces static service call; result wrapped in NexacroDataset shim;
     * JDK 8: List.of() replaced with Arrays.asList().
     */
    public NexacroDataset selectAll() {
        List<Map<String, Object>> rows = testDataDao.selectAll();
        NexacroDataset ds = new NexacroDataset();
        ds.setId("output1");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("ID",                "int",        "10"));
        cols.add(new NexacroDataset.Column("STRING_VALUE",      "string",     "200"));
        cols.add(new NexacroDataset.Column("INT_VALUE",         "int",        "10"));
        cols.add(new NexacroDataset.Column("BOOLEAN_VALUE",     "boolean",    "1"));
        cols.add(new NexacroDataset.Column("LONG_VALUE",        "bigdecimal", "20"));
        cols.add(new NexacroDataset.Column("FLOAT_VALUE",       "float",      "20"));
        cols.add(new NexacroDataset.Column("DOUBLE_VALUE",      "float",      "20"));
        cols.add(new NexacroDataset.Column("BIG_DECIMAL_VALUE", "bigdecimal", "24"));
        cols.add(new NexacroDataset.Column("DATE_VALUE",        "date",       "10"));
        cols.add(new NexacroDataset.Column("TIME_VALUE",        "time",       "8"));
        cols.add(new NexacroDataset.Column("DATETIME_VALUE",    "datetime",   "20"));
        cols.add(new NexacroDataset.Column("BYTES_VALUE",       "blob",       "0"));
        ci.setColumns(cols);
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? Collections.<Map<String, Object>>emptyList() : rows);
        return ds;
    }

    /**
     * Echo back the input dataset rows unchanged (column-type round-trip check).
     * Legacy logs each field type; here we re-wrap the incoming rows as output1.
     *
     * Ported from boot-jdk17-jakarta-legacy/.../ExampleDateTypeController.check_testDataTypeList@e49a17791d on 2026-04-24.
     * Adaptations: uses NexacroDataset rows (Map) instead of VO list;
     * JDK 8: List.of() replaced with Arrays.asList().
     */
    public NexacroDataset echo(NexacroDataset input) {
        NexacroDataset ds = new NexacroDataset();
        ds.setId("output1");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("STRING_VALUE",      "string",     "200"));
        cols.add(new NexacroDataset.Column("INT_VALUE",         "int",        "10"));
        cols.add(new NexacroDataset.Column("BOOLEAN_VALUE",     "boolean",    "1"));
        cols.add(new NexacroDataset.Column("LONG_VALUE",        "bigdecimal", "20"));
        cols.add(new NexacroDataset.Column("FLOAT_VALUE",       "float",      "20"));
        cols.add(new NexacroDataset.Column("DOUBLE_VALUE",      "float",      "20"));
        cols.add(new NexacroDataset.Column("BIG_DECIMAL_VALUE", "bigdecimal", "24"));
        cols.add(new NexacroDataset.Column("DATE_VALUE",        "date",       "10"));
        cols.add(new NexacroDataset.Column("TIME_VALUE",        "time",       "8"));
        cols.add(new NexacroDataset.Column("DATETIME_VALUE",    "datetime",   "20"));
        cols.add(new NexacroDataset.Column("BYTES_VALUE",       "blob",       "0"));
        ci.setColumns(cols);
        ds.setColumnInfo(ci);
        ds.setRows(input != null && input.getRows() != null
                ? input.getRows()
                : Collections.<Map<String, Object>>emptyList());
        return ds;
    }
}
