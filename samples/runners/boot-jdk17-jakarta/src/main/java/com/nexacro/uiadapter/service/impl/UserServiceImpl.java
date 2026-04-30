package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.User;
import com.nexacro.uiadapter.mapper.UserMapper;
import com.nexacro.uiadapter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional
    public User login(String userId, String password) {
        if (userId == null || password == null) {
            return null;
        }
        User u = userMapper.selectById(userId);
        if (u == null) {
            return null;
        }
        String expected = "stub$" + userId;
        if (!expected.equals(u.getPasswordHash())) {
            return null;
        }
        userMapper.updateLastLogin(userId);
        // Return a sanitized copy (drop the password hash).
        User out = new User();
        out.setUserId(u.getUserId());
        out.setUserName(u.getUserName());
        out.setEmail(u.getEmail());
        out.setRole(u.getRole());
        out.setEnabled(u.getEnabled());
        return out;
    }
}
