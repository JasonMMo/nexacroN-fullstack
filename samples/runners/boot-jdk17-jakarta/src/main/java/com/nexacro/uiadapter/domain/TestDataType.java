package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * TestDataType POJO mirroring TB_TEST_DATA_TYPE — exercises every Nexacro
 * column type (string, int, boolean, long, float, double, big-decimal,
 * date, time, datetime, blob).
 */
@Getter
@Setter
public class TestDataType {
    private Integer    id;
    private String     stringValue;
    private Integer    intValue;
    private Boolean    booleanValue;
    private Long       longValue;
    private Float      floatValue;
    private Double     doubleValue;
    private BigDecimal bigDecimalValue;
    private Date       dateValue;
    private Timestamp  timeValue;
    private Timestamp  datetimeValue;
    private byte[]     bytesValue;
}
