package com.nexacro.uiadapter.service;

import com.nexacro.uiadapter.domain.FileMeta;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** File service contract — upload/download/list/multi-download. */
public interface FileService {

    /** Persist the upload to disk + insert FILE_META; returns generated file id. */
    String upload(MultipartFile file, String uploadedBy) throws IOException;

    /** Locate metadata + on-disk file by id. Returns {@code null} if missing. */
    FileMeta findById(String fileId);

    /** All non-deleted file metadata rows (newest first). */
    List<FileMeta> list();

    /** Resolve multiple files for zip-bundle download. */
    List<FileMeta> findByIds(List<String> ids);
}
