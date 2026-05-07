package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.TestDataType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** MyBatis mapper for TB_TEST_DATA_TYPE. */
@Mapper
public interface TestDataMapper {

    /** All rows from TB_TEST_DATA_TYPE. */
    List<TestDataType> selectAll();
}
