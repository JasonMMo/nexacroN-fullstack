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
