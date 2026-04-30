package com.nexacro.fullstack.business.domain.testdata;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for TestData type demo endpoints (#10 select, #11 check).
 *
 * <p>Ported from boot-jdk17-jakarta-legacy/src/main/java/example/nexacro/uiadapter/impl/ExampleDataTypeServiceImpl.java@e49a17791d on 2026-04-24.
 * Adaptations: uses shim NexacroDataset instead of legacy NexacroResult/VO; no SqlSessionTemplate — uses @Mapper DAO.
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
     * Adaptations: DB query replaces static service call; result wrapped in NexacroDataset shim.
     */
    public NexacroDataset selectAll() {
        List<Map<String, Object>> rows = testDataDao.selectAll();
        NexacroDataset ds = new NexacroDataset();
        ds.setId("output1");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("ID",                "int",        "10"),
            new NexacroDataset.Column("STRING_VALUE",      "string",     "200"),
            new NexacroDataset.Column("INT_VALUE",         "int",        "10"),
            new NexacroDataset.Column("BOOLEAN_VALUE",     "boolean",    "1"),
            new NexacroDataset.Column("LONG_VALUE",        "bigdecimal", "20"),
            new NexacroDataset.Column("FLOAT_VALUE",       "float",      "20"),
            new NexacroDataset.Column("DOUBLE_VALUE",      "float",      "20"),
            new NexacroDataset.Column("BIG_DECIMAL_VALUE", "bigdecimal", "24"),
            new NexacroDataset.Column("DATE_VALUE",        "date",       "10"),
            new NexacroDataset.Column("TIME_VALUE",        "time",       "8"),
            new NexacroDataset.Column("DATETIME_VALUE",    "datetime",   "20"),
            new NexacroDataset.Column("BYTES_VALUE",       "blob",       "0")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
    }

    /**
     * Echo back the input dataset rows unchanged (column-type round-trip check).
     * Legacy logs each field type; here we re-wrap the incoming rows as output1.
     *
     * Ported from boot-jdk17-jakarta-legacy/.../ExampleDateTypeController.check_testDataTypeList@e49a17791d on 2026-04-24.
     * Adaptations: uses NexacroDataset rows (Map) instead of VO list.
     */
    public NexacroDataset echo(NexacroDataset input) {
        NexacroDataset ds = new NexacroDataset();
        ds.setId("output1");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("STRING_VALUE",      "string",     "200"),
            new NexacroDataset.Column("INT_VALUE",         "int",        "10"),
            new NexacroDataset.Column("BOOLEAN_VALUE",     "boolean",    "1"),
            new NexacroDataset.Column("LONG_VALUE",        "bigdecimal", "20"),
            new NexacroDataset.Column("FLOAT_VALUE",       "float",      "20"),
            new NexacroDataset.Column("DOUBLE_VALUE",      "float",      "20"),
            new NexacroDataset.Column("BIG_DECIMAL_VALUE", "bigdecimal", "24"),
            new NexacroDataset.Column("DATE_VALUE",        "date",       "10"),
            new NexacroDataset.Column("TIME_VALUE",        "time",       "8"),
            new NexacroDataset.Column("DATETIME_VALUE",    "datetime",   "20"),
            new NexacroDataset.Column("BYTES_VALUE",       "blob",       "0")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(input != null && input.getRows() != null ? input.getRows() : List.of());
        return ds;
    }
}
