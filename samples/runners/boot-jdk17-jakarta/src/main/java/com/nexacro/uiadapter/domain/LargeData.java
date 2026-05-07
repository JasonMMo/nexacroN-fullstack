package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/** LargeData POJO mirroring the TB_LARGE table (canonical schema, paging demo). */
@Getter
@Setter
public class LargeData {
    private Integer largeId;
    private String  name;
    private Date    regDate;
    private String  story;
    private Integer status;
}
