package com.nexacro.uiadapter.domain;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

/** FileMeta POJO mirroring the FILE_META table. */
@Getter
@Setter
public class FileMeta {
    private String    fileId;
    private String    originalName;
    private String    storedPath;
    private String    contentType;
    private Long      sizeBytes;
    private String    uploadedBy;
    private Timestamp uploadedAt;
    private Boolean   deleted;
}
