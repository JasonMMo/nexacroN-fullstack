package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * Dept POJO mirroring the DEPT table.
 *
 * <p>Used both for flat list and recursive tree responses;
 * {@code depth} / {@code path} columns come from the recursive CTE
 * (tree query) and are otherwise null.
 */
@Getter
@Setter
public class Dept {
    private String  deptId;
    private String  deptName;
    private String  parentId;
    private Integer sortOrder;
    private Integer levelNo;
    // Tree-only columns populated by the recursive CTE
    private Integer depth;
    private String  path;
}
