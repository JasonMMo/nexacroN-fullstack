package com.nexacro.fullstack.business.domain.file;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    public static class DownloadInfo {
        private final File file;
        private final Map<String, Object> meta;

        public DownloadInfo(File file, Map<String, Object> meta) {
            this.file = file;
            this.meta = meta;
        }

        public File getFile() { return file; }
        public Map<String, Object> getMeta() { return meta; }
    }

    @Value("${nexacro.file.storage-dir:./uploads}")
    private String storageDir;

    private final FileDao dao;

    public FileService(FileDao dao) { this.dao = dao; }

    public Map<String, Object> upload(MultipartFile file, String uploadedBy) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("file is required");
        }
        File dir = new File(storageDir);
        if (!dir.exists()) dir.mkdirs();
        String fileId = UUID.randomUUID().toString();
        String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
        String storedName = fileId + "-" + original;
        File target = new File(dir, storedName);
        file.transferTo(target);

        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("FILE_ID", fileId);
        meta.put("ORIGINAL_NAME", original);
        meta.put("STORED_PATH", target.getAbsolutePath());
        meta.put("CONTENT_TYPE", file.getContentType());
        meta.put("SIZE_BYTES", file.getSize());
        meta.put("UPLOADED_BY", uploadedBy);
        dao.insert(meta);
        return meta;
    }

    public DownloadInfo download(String fileId) {
        Map<String, Object> meta = dao.findById(fileId);
        if (meta == null) throw new IllegalArgumentException("file not found: " + fileId);
        File f = new File(String.valueOf(meta.get("STORED_PATH")));
        if (!f.exists()) throw new IllegalArgumentException("stored file missing: " + fileId);
        return new DownloadInfo(f, meta);
    }

    /**
     * One entry of a multi-download bundle: the original filename + the resolved disk file.
     *
     * Ported from <boot-jdk17-jakarta-legacy>/<FileService.java>@e49a17791d on 2026-04-24.
     * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
     * JDK 8: record replaced with static inner class + constructor + getters.
     */
    public static class MultiDownloadEntry {
        private final String originalName;
        private final File file;

        public MultiDownloadEntry(String originalName, File file) {
            this.originalName = originalName;
            this.file = file;
        }

        public String getOriginalName() { return originalName; }
        public File getFile() { return file; }
    }

    /**
     * Resolve multiple files by FILE_ID for zip-bundling download.
     *
     * Ported from <boot-jdk17-jakarta-legacy>/<FileService.java>@e49a17791d on 2026-04-24.
     * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
     * JDK 8: List.of() replaced with Collections.emptyList(); record replaced with static inner class.
     */
    public List<MultiDownloadEntry> multiDownload(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<Map<String, Object>> metas = dao.findByIds(fileIds);
        if (metas == null || metas.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        List<MultiDownloadEntry> out = new ArrayList<MultiDownloadEntry>(metas.size());
        for (Map<String, Object> meta : metas) {
            String storedPath = String.valueOf(meta.get("STORED_PATH"));
            String originalName = String.valueOf(meta.get("ORIGINAL_NAME"));
            out.add(new MultiDownloadEntry(originalName, new File(storedPath)));
        }
        return out;
    }

    public NexacroDataset list() {
        NexacroDataset ds = new NexacroDataset();
        ds.setId("files");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        List<NexacroDataset.Column> cols = new ArrayList<NexacroDataset.Column>();
        cols.add(new NexacroDataset.Column("FILE_ID", "STRING", "40"));
        cols.add(new NexacroDataset.Column("ORIGINAL_NAME", "STRING", "255"));
        cols.add(new NexacroDataset.Column("STORED_PATH", "STRING", "500"));
        cols.add(new NexacroDataset.Column("CONTENT_TYPE", "STRING", "100"));
        cols.add(new NexacroDataset.Column("SIZE_BYTES", "BIGINT", "19"));
        cols.add(new NexacroDataset.Column("UPLOADED_BY", "STRING", "32"));
        cols.add(new NexacroDataset.Column("UPLOADED_AT", "DATETIME", "19"));
        ci.setColumns(cols);
        ds.setColumnInfo(ci);
        ds.setRows(dao.listAll());
        return ds;
    }
}
