package com.nexacro.uiadapter.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * Board POJO mirroring the TB_BOARD table.
 *
 * <p>Field scopes mirror the canonical GitLab sample
 * ({@code example.nexacro.uiadapter.pojo.Board}):
 * <ul>
 *   <li>{@code searchCondition} — private (canonical: AccessLevel.PRIVATE)</li>
 *   <li>{@code searchKeyword}, {@code searchUseYn} — protected (canonical: AccessLevel.PROTECTED)</li>
 *   <li>Domain columns — public (canonical: public fields)</li>
 *   <li>{@code hiddenInfo} — included; no {@code @ExcludeField} in
 *       {@code uiadapter-spring-core} (jakarta-only annotation)</li>
 * </ul>
 */
@Getter
@Setter
public class Board extends NexacroBase {

    /** 검색조건 */
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private String searchCondition = "";

    /** 검색Keyword */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    protected String searchKeyword = "";

    /** 검색사용여부 */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    protected String searchUseYn = "";

    // Domain columns (TB_BOARD)
    public Integer postId;
    public String  title;
    public String  contents;
    public String  regId;
    public Date    regDate;
    public String  communityId;
    public Integer hitCount;
    public Boolean isNotice;

    public String hiddenInfo;
}
