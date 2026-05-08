package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.TestDataType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper for TB_TEST_DATA_TYPE.
 *
 * <p>Mirrors the canonical Nexacro N reference (gitlab.com/nexacron/spring-boot
 * /jakarta/uiadapter-jakarta) {@code ExampleDataTypeMapper}, exposing both
 * POJO (List&lt;TestDataType&gt;) and Map (List&lt;Map&lt;String, Object&gt;&gt;)
 * variants for select / insert / update / delete.
 */
@Mapper
public interface TestDataMapper {

    /** All rows from TB_TEST_DATA_TYPE (existing aggregate-load endpoint). */
    List<TestDataType> selectAll();

    /** Optionally-filtered list of POJOs. */
    List<TestDataType> select_datalist(@Param("search") Map<String, String> searchMap);

    /** Optionally-filtered list of Maps. */
    List<Map<String, Object>> select_datalist_map(@Param("search") Map<String, String> searchMap);

    /** Insert one POJO row. */
    int insert_data(TestDataType row);

    /** Update one POJO row by ID. */
    int update_data(TestDataType row);

    /** Delete one POJO row by ID. */
    int delete_data(@Param("id") Integer id);

    /** Insert one Map row. */
    int insert_data_map(Map<String, Object> row);

    /** Update one Map row by id key. */
    int update_data_map(Map<String, Object> row);

    /** Delete one Map row by id key. */
    int delete_data_map(@Param("id") Integer id);
}
