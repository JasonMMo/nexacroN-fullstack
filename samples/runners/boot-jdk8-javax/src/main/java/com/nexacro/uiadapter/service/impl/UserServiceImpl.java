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
        if (!password.equals(u.getPassword())) {
            return null;
        }
        userMapper.updateLastLogin(userId);
        u.setPassword(null);
        return u;
    }
}
