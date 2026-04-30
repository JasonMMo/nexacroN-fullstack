package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * User POJO mirroring the USERS table.
 *
 * <p>{@code passwordHash} is included because the login flow needs to compare
 * it on the server. It is excluded from the controller-facing output dataset
 * (the service builds a sanitized {@link User} for the response).
 */
@Getter
@Setter
public class User {
    private String  userId;
    private String  userName;
    private String  passwordHash;
    private String  email;
    private String  role;
    private Boolean enabled;
    private Timestamp createdAt;
}
