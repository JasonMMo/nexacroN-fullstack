package com.nexacro.fullstack.business.domain.file;

import com.nexacro.fullstack.business.xapi.NexacroDataset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    private final FileDao fileDao;

    @Value("${nexacro.file.storage-dir:./uploads}")
    private String storageDir;

    public FileService(FileDao fileDao) {
        this.fileDao = fileDao;
    }

    /**
     * Stores the uploaded file on disk, inserts metadata into FILE_META,
     * and returns the generated FILE_ID (UUID).
     */
    public String upload(MultipartFile file, String uploadedBy) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");

        Path dir = Paths.get(storageDir);
        Files.createDirectories(dir);

        Path target = dir.resolve(fileId + "-" + originalName);
        Files.copy(file.getInputStream(), target);

        Map<String, Object> row = new LinkedHashMap<>();
        row.put("FILE_ID",       fileId);
        row.put("ORIGINAL_NAME", originalName);
        row.put("STORED_PATH",   target.toString());
        row.put("CONTENT_TYPE",  file.getContentType());
        row.put("SIZE_BYTES",    file.getSize());
        row.put("UPLOADED_BY",   uploadedBy);
        fileDao.insert(row);

        return fileId;
    }

    /**
     * Locates the stored file and its metadata row by FILE_ID.
     * Throws IllegalArgumentException when not found or marked deleted.
     */
    public DownloadInfo download(String fileId) {
        Map<String, Object> meta = fileDao.findById(fileId);
        if (meta == null) {
            throw new IllegalArgumentException("file not found: " + fileId);
        }
        File file = new File(meta.get("STORED_PATH").toString());
        return new DownloadInfo(file, meta);
    }

    /** Returns a NexacroDataset (id="files") with all non-deleted file metadata rows. */
    public NexacroDataset list() {
        List<Map<String, Object>> rows = fileDao.listAll();
        NexacroDataset ds = new NexacroDataset();
        ds.setId("files");
        NexacroDataset.ColumnInfo ci = new NexacroDataset.ColumnInfo();
        ci.setColumns(List.of(
            new NexacroDataset.Column("FILE_ID",       "string",   "40"),
            new NexacroDataset.Column("ORIGINAL_NAME", "string",   "255"),
            new NexacroDataset.Column("CONTENT_TYPE",  "string",   "100"),
            new NexacroDataset.Column("SIZE_BYTES",    "int",      "20"),
            new NexacroDataset.Column("UPLOADED_BY",   "string",   "32"),
            new NexacroDataset.Column("UPLOADED_AT",   "datetime", "20")
        ));
        ds.setColumnInfo(ci);
        ds.setRows(rows == null ? List.of() : rows);
        return ds;
    }

    /** Pairs a resolved {@link File} with its FILE_META row. */
    public record DownloadInfo(File file, Map<String, Object> meta) {}
}
