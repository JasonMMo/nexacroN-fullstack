package com.nexacro.uiadapter.domain;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User POJO mirroring the TB_USER table (javax lane).
 *
 * <p>Scope and validation constraints mirror the canonical GitLab
 * sample ({@code example.nexacro.uiadapter.pojo.User}): every field
 * {@code private}, class-level {@link Data} for getters/setters, and
 * {@link EqualsAndHashCode}(callSuper = true) so {@link NexacroBase}'s
 * {@code rowType} participates in equality.
 *
 * <p>javax lane uses {@code javax.validation.constraints.*} (Bean
 * Validation 2.0 / Java EE 8); the jakarta lane uses
 * {@code jakarta.validation.constraints.*}.
 *
 * <p>MyBatis {@code mapUnderscoreToCamelCase=true} maps columns:
 * USER_ID→userId, USER_NAME→userName, EN_NAME→enName, COMP_PHONE→compPhone,
 * PHONE→phone, CELL_PHONE→cellPhone, COMPANY→company, JOB_POSITION→jobPosition,
 * ASSIGNMENT→assignment, OFFICER_YN→officerYn, FAX→fax, ZIP_CODE→zipCode,
 * ADDRESS→address, COMP_ZIP_CODE→compZipCode, COMP_ADDRESS→compAddress,
 * EMAIL→email, DEPT_ID→deptId, PASSWORD→password.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends NexacroBase {

    @NotNull(message = "user id is required.")
    @Size(min = 4, max = 20, message = "Please, Enter your id at least 4 Characters.")
    private String userId;

    @NotNull(message = "user namme is required.")
    @Size(max = 20, message = "Please, Enter your name.")
    private String userName;

    @NotNull(message = "password is required.")
    @Size(min = 4, max = 50, message = "Please, Enter your password at least 4 Characters.")
    private String password;

    @Pattern(regexp = ".+@.+\\.[a-z]+", message = "{errors.validation.email}")
    private String email;

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
    private String deptId;
}
