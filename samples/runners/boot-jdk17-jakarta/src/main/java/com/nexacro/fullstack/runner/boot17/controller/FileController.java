package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.business.domain.file.FileService;
import com.nexacro.fullstack.business.uiadapter.NexacroController;
import com.nexacro.fullstack.business.uiadapter.NexacroResponseBuilder;
import com.nexacro.fullstack.business.xapi.NexacroEnvelope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
public class FileController extends NexacroController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    // spec #7: multi-file upload
    @PostMapping(value = "/uiadapter/advancedUploadFiles.do", consumes = "multipart/form-data")
    public NexacroEnvelope upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false, defaultValue = "anonymous") String uploadedBy)
            throws IOException {
        String fileId = fileService.upload(file, uploadedBy);
        NexacroEnvelope out = NexacroResponseBuilder.ok();
        out.getParameters().add(new NexacroEnvelope.Parameter("fileId", fileId, "string"));
        return out;
    }

    // spec #8: single file download.
    // TODO Plan8: spec §2 mandates POST but file download requires GET for Range/Content-Disposition.
    // Retained as GET — flag for Opus to decide verb or add POST alias.
    @GetMapping("/uiadapter/advancedDownloadFile.do")
    public ResponseEntity<Resource> download(@RequestParam("fileId") String fileId) throws IOException {
        FileService.DownloadInfo info = fileService.download(fileId);
        File f = info.file();
        Map<String, Object> meta = info.meta();
        String originalName = String.valueOf(meta.get("ORIGINAL_NAME"));
        String contentType  = String.valueOf(meta.getOrDefault("CONTENT_TYPE", "application/octet-stream"));
        Resource body = new FileSystemResource(f);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalName + "\"")
                .contentLength(f.length())
                .body(body);
    }

    // /file/list.do removed — file-list endpoint is not in spec §2.
    // TODO Plan8: /file/list.do (file listing) is not in spec §2 — flag for Opus
}
