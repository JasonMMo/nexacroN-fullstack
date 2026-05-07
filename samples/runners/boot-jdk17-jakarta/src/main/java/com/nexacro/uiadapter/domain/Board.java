package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * Board POJO mirroring the TB_BOARD table (canonical schema).
 *
 * <p>Field names use camelCase to match MyBatis
 * {@code mapUnderscoreToCamelCase=true}. Database columns map as:
 * POST_ID→postId, TITLE→title, CONTENTS→contents, REG_ID→regId,
 * REG_DATE→regDate, COMMUNITY_ID→communityId, HIDDEN_INFO→hiddenInfo,
 * HIT_COUNT→hitCount, IS_NOTICE→isNotice.
 */
@Getter
@Setter
public class Board {
    private Integer postId;
    private String  title;
    private String  contents;
    private String  regId;
    private Date    regDate;
    private String  communityId;
    private String  hiddenInfo;
    private Integer hitCount;
    private Boolean isNotice;
}
