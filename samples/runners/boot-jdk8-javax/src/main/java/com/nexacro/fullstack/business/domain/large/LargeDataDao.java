package com.nexacro.fullstack.business.domain.large;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface LargeDataDao {
    long count(@Param("category") String category);
    List<Map<String, Object>> page(@Param("offset") int offset,
                                   @Param("limit") int limit,
                                   @Param("category") String category);
}
