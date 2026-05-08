package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.TestDataType;

import java.util.List;
import java.util.Map;

/**
 * TestData (TB_TEST_DATA_TYPE) service contract.
 *
 * <p>Mirrors the canonical Nexacro N reference at
 * gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta
 * ({@code ExampleDataTypeServcice}), exposing both POJO and Map
 * variants for select / save (upsert).
 */
public interface TestDataService {

    /** All test-data-type rows (existing aggregate-load endpoint). */
    List<TestDataType> selectAll();

    /** Filtered list (POJO). {@code searchMap} keys: {@code stringValue}. */
    List<TestDataType> select_datalist(Map<String, String> searchMap);

    /** Filtered list (Map). {@code searchMap} keys: {@code stringValue}. */
    List<Map<String, Object>> select_datalist_map(Map<String, String> searchMap);

    /**
     * Upsert a list of POJO rows. Each row with {@code id == null} is inserted;
     * rows with non-null id are updated. (Delete-by-rowType is not supported in
     * the {@code List<TestDataType>} binding — use a dedicated delete endpoint
     * if needed.)
     */
    void update_datalist(List<TestDataType> dataList);

    /**
     * Upsert a list of Map rows. {@code id == null} → insert;
     * non-null → update.
     */
    void update_datalist_map(List<Map<String, Object>> dataList);
}
