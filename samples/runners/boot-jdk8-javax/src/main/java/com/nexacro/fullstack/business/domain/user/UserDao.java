package com.nexacro.fullstack.business.domain.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface UserDao {
    Map<String, Object> findById(@Param("userId") String userId);
    int updateLastLogin(@Param("userId") String userId);
}
