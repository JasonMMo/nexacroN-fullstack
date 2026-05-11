package com.nexacro.uiadapter.domain;

/**
 * Dept POJO mirroring the TB_DEPT table.
 *
 * <p>Scope mirrors the canonical GitLab sample
 * ({@code example.nexacro.uiadapter.pojo.Dept}): every field is
 * {@code private} with explicit getter/setter pairs (no Lombok), and
 * the class extends {@link NexacroBase} so {@code _RowType_} is preserved.
 */
public class Dept extends NexacroBase {

    /** 검색조건 */
    private String searchCondition = "";

    /** 검색Keyword */
    private String searchKeyword = "";

    /** 검색사용여부 */
    private String searchUseYn = "";

    // Domain columns
    private Integer deptId;
    private String  deptName;
    private Integer memberCount;

    public String getSearchCondition() {
        return searchCondition;
    }

    public void setSearchCondition(String searchCondition) {
        this.searchCondition = searchCondition;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public String getSearchUseYn() {
        return searchUseYn;
    }

    public void setSearchUseYn(String searchUseYn) {
        this.searchUseYn = searchUseYn;
    }

    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
}
