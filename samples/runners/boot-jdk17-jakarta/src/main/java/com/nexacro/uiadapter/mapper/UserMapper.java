package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.User;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis mapper for the USERS table. */
@Mapper
public interface UserMapper {

    /** Lookup an enabled user by id, or {@code null} if missing/disabled. */
    User selectById(String userId);

    /** Touch the user record (acts as last-login marker in this stub). */
    int updateLastLogin(String userId);
}
