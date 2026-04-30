package com.nexacro.fullstack.business.domain.testdata;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * MyBatis DAO for TB_TEST_DATA_TYPE.
 */
@Mapper
public interface TestDataDao {

    /** Return all rows from TB_TEST_DATA_TYPE as plain maps. */
    List<Map<String, Object>> selectAll();
}
