package com.nexacro.fullstack.business.domain.user;

import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface UserDao {

    Map<String, Object> findById(String userId);

    int updateLastLogin(String userId);
}
