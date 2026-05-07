package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** LargeData POJO mirroring the LARGE_DATA table (paging demo). */
@Getter
@Setter
public class LargeData {
    private Long       rowId;
    private String     category;
    private Integer    seqNo;
    private String     value1;
    private BigDecimal value2;
    private Integer    value3;
    private Timestamp  createdAt;
}
