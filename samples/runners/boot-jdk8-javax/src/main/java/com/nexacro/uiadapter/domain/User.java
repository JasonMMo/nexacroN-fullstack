package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * User POJO mirroring the TB_USER table (canonical schema).
 *
 * <p>Field names use camelCase to match MyBatis
 * {@code mapUnderscoreToCamelCase=true}. Database columns map as:
 * USER_ID→userId, USER_NAME→userName, EN_NAME→enName, COMP_PHONE→compPhone,
 * PHONE→phone, CELL_PHONE→cellPhone, COMPANY→company, JOB_POSITION→jobPosition,
 * ASSIGNMENT→assignment, OFFICER_YN→officerYn, FAX→fax, ZIP_CODE→zipCode,
 * ADDRESS→address, COMP_ZIP_CODE→compZipCode, COMP_ADDRESS→compAddress,
 * EMAIL→email, DEPT_ID→deptId, PASSWORD→password.
 */
@Getter
@Setter
public class User {
    private String userId;
    private String userName;
    private String enName;
    private String compPhone;
    private String phone;
    private String cellPhone;
    private String company;
    private String jobPosition;
    private String assignment;
    private String officerYn;
    private String fax;
    private String zipCode;
    private String address;
    private String compZipCode;
    private String compAddress;
    private String email;
    private String deptId;
    private String password;
}
