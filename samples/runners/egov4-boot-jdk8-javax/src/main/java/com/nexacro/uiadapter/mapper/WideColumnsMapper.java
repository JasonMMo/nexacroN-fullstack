package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.WideColumns;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** MyBatis mapper for the 50-column WIDE_COLUMNS table. */
@Mapper
public interface WideColumnsMapper {

    /** All wide-column rows, optionally filtered by KEY_ID. */
    List<WideColumns> selectList(@Param("keyId") String keyId);
}
