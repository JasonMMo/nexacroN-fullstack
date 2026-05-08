package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.TestDataType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * MyBatis mapper for TB_TEST_DATA_TYPE.
 *
 * <p>Mirrors canonical Nexacro N example
 * (gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta) with both
 * POJO and Map binding for select / insert / update / delete.
 */
@Mapper
public interface TestDataMapper {

    /** All rows from TB_TEST_DATA_TYPE (existing aggregate-load endpoint). */
    List<TestDataType> selectAll();

    /** Filtered list (POJO). {@code searchMap} keys: {@code stringValue}. */
    List<TestDataType> select_datalist(@Param("search") Map<String, String> searchMap);

    /** Filtered list (Map). {@code searchMap} keys: {@code stringValue}. */
    List<Map<String, Object>> select_datalist_map(@Param("search") Map<String, String> searchMap);

    /** Insert one POJO row. {@code id} is generated. */
    int insert_data(TestDataType row);

    /** Update one POJO row by {@code id}. */
    int update_data(TestDataType row);

    /** Delete one row by {@code id}. */
    int delete_data(@Param("id") Integer id);

    /** Insert one Map row. {@code id} is generated. */
    int insert_data_map(Map<String, Object> row);

    /** Update one Map row by {@code id}. */
    int update_data_map(Map<String, Object> row);

    /** Delete one row by {@code id} (Map binding mirror). */
    int delete_data_map(@Param("id") Integer id);
}
