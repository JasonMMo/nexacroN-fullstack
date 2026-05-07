package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Dept POJO mirroring the TB_DEPT table (canonical schema — flat list).
 *
 * <p>Field names match the lowercase column identifiers exactly
 * (deptId / deptName / memberCount), so MyBatis underscore-camelCase
 * mapping is a no-op for this entity.
 */
@Getter
@Setter
public class Dept {
    private Integer deptId;
    private String  deptName;
    private Integer memberCount;
}
