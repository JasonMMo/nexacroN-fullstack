package com.nexacro.uiadapter.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * TestDataType POJO exercising every Nexacro column type (string, int,
 * boolean, long, float, double, big-decimal, date, time, datetime, blob).
 *
 * <p>Scope mirrors the canonical
 * {@code example.nexacro.uiadapter.pojo.ExampleDataType}: every typed
 * field is {@code public}, class-level Lombok {@link Getter}/{@link Setter}
 * supply accessors, and {@link EqualsAndHashCode}(callSuper = true) so
 * {@link NexacroBase}'s {@code rowType} participates in equality.
 *
 * <p>Note: {@code id} is our local primary-key column used by MyBatis
 * upsert dispatch (id == null → INSERT, else UPDATE). It has no analog
 * in the canonical sample but is required for our HSQLDB persistence.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TestDataType extends NexacroBase {

    private Integer id;

    public String      stringValue;
    public Integer     intValue;
    public Boolean     booleanValue;
    public Long        longValue;
    public Float       floatValue;
    public Double      doubleValue;
    public BigDecimal  bigDecimalValue;

    public Date        dateValue;
    public Date        timeValue;
    public Date        datetimeValue;

    public byte[]      bytesValue;
}
