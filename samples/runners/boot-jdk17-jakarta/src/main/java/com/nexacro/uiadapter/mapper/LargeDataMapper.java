package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.LargeData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** MyBatis mapper for LARGE_DATA paging. */
@Mapper
public interface LargeDataMapper {

    /** Total row count, optionally filtered by category. */
    int count(@Param("category") String category);

    /** One page of rows (offset/limit), optionally filtered by category. */
    List<LargeData> page(@Param("offset") int offset,
                         @Param("limit") int limit,
                         @Param("category") String category);
}
