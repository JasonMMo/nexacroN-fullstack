package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.User;

/** User service contract — login/logout helpers. */
public interface UserService {

    /**
     * Compares the supplied password against TB_USER.PASSWORD (canonical seed
     * stores plain values such as '1111'). Returns a sanitized {@link User}
     * with the password cleared on success, or {@code null} on failure.
     */
    User login(String userId, String password);
}
