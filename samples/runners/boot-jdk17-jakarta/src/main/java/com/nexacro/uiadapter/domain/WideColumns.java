package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * WideColumns POJO mirroring WIDE_COLUMNS — 50-column demo.
 *
 * <p>Field grouping by type matches the schema:
 * <ul>
 *   <li>COL_01..COL_10 — VARCHAR</li>
 *   <li>COL_11..COL_20 — INTEGER</li>
 *   <li>COL_21..COL_30 — DECIMAL</li>
 *   <li>COL_31..COL_35 — DATE</li>
 *   <li>COL_36..COL_40 — TIMESTAMP</li>
 *   <li>COL_41..COL_45 — BOOLEAN</li>
 *   <li>COL_46..COL_50 — CLOB</li>
 * </ul>
 */
@Getter
@Setter
public class WideColumns {
    private String keyId;
    // VARCHAR
    private String col01; private String col02; private String col03; private String col04; private String col05;
    private String col06; private String col07; private String col08; private String col09; private String col10;
    // INTEGER
    private Integer col11; private Integer col12; private Integer col13; private Integer col14; private Integer col15;
    private Integer col16; private Integer col17; private Integer col18; private Integer col19; private Integer col20;
    // DECIMAL
    private BigDecimal col21; private BigDecimal col22; private BigDecimal col23; private BigDecimal col24; private BigDecimal col25;
    private BigDecimal col26; private BigDecimal col27; private BigDecimal col28; private BigDecimal col29; private BigDecimal col30;
    // DATE
    private Date col31; private Date col32; private Date col33; private Date col34; private Date col35;
    // TIMESTAMP
    private Timestamp col36; private Timestamp col37; private Timestamp col38; private Timestamp col39; private Timestamp col40;
    // BOOLEAN
    private Boolean col41; private Boolean col42; private Boolean col43; private Boolean col44; private Boolean col45;
    // CLOB (as String)
    private String col46; private String col47; private String col48; private String col49; private String col50;
}
