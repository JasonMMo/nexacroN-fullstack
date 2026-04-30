package com.nexacro.uiadapter.service.impl;

import com.nexacro.uiadapter.domain.FileMeta;
import com.nexacro.uiadapter.mapper.FileMapper;
import com.nexacro.uiadapter.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileMapper fileMapper;

    @Value("${nexacro.file.storage-dir:./uploads}")
    private String storageDir;

    @Override
    public String upload(MultipartFile file, String uploadedBy) throws IOException {
        String fileId = UUID.randomUUID().toString();
        String originalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");

        Path dir = Paths.get(storageDir);
        Files.createDirectories(dir);
        Path target = dir.resolve(fileId + "-" + originalName);
        Files.copy(file.getInputStream(), target);

        FileMeta meta = new FileMeta();
        meta.setFileId(fileId);
        meta.setOriginalName(originalName);
        meta.setStoredPath(target.toString());
        meta.setContentType(file.getContentType());
        meta.setSizeBytes(file.getSize());
        meta.setUploadedBy(uploadedBy);
        fileMapper.insert(meta);
        return fileId;
    }

    @Override
    public FileMeta findById(String fileId) {
        return fileMapper.selectById(fileId);
    }

    @Override
    public List<FileMeta> list() {
        return fileMapper.selectList();
    }

    @Override
    public List<FileMeta> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return fileMapper.selectByIds(ids);
    }
}
