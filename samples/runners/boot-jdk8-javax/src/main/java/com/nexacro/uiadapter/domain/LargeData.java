package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

/**
 * LargeData POJO mirroring the TB_LARGE table (javax lane, paging-demo source).
 *
 * <p>No direct canonical pojo analog — the GitLab sample's
 * {@code LargeDataController} generates rows inline. Scope follows our
 * project convention: {@code private} fields with Lombok
 * {@link Getter}/{@link Setter}.
 *
 * <p>Extends {@link NexacroBase} so {@code _RowType_} is preserved
 * when the controller pages results into a chunked SSV stream.
 */
@Getter
@Setter
public class LargeData extends NexacroBase {
    private Integer largeId;
    private String  name;
    private Date    regDate;
    private String  story;
    private Integer status;
}
