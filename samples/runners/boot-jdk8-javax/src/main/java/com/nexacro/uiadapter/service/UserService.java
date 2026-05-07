package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.User;

/** User service contract — login/logout helpers. */
public interface UserService {

    /**
     * Stub login: compares stored PASSWORD_HASH against {@code "stub$" + userId}
     * (matches seed-data convention). Returns the sanitized user (no password
     * hash) on success, or {@code null} on failure.
     */
    User login(String userId, String password);
}
