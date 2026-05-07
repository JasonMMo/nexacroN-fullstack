package com.nexacro.uiadapter.mapper;

import com.nexacro.uiadapter.domain.User;
import org.apache.ibatis.annotations.Mapper;

/** MyBatis mapper for the TB_USER table (canonical schema). */
@Mapper
public interface UserMapper {

    /** Lookup a user by id, or {@code null} if missing. */
    User selectById(String userId);

    /** Touch the user record (acts as last-login marker in this stub). */
    int updateLastLogin(String userId);
}
