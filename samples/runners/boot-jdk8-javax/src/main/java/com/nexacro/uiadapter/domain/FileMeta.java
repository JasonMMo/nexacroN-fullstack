package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/**
 * FileMeta POJO mirroring the FILE_META table (javax lane).
 *
 * <p>No direct canonical analog — {@code pojo/} in the GitLab sample
 * has no FileMeta. Scope follows our project convention: every field
 * {@code private} with Lombok {@link Getter}/{@link Setter}.
 *
 * <p>Extends {@link NexacroBase} so {@code _RowType_} is preserved on
 * any future bulk DataSet exchange (delete-bulk, etc).
 */
@Getter
@Setter
public class FileMeta extends NexacroBase {
    private String    fileId;
    private String    originalName;
    private String    storedPath;
    private String    contentType;
    private Long      sizeBytes;
    private String    uploadedBy;
    private Timestamp uploadedAt;
    private Boolean   deleted;
}
