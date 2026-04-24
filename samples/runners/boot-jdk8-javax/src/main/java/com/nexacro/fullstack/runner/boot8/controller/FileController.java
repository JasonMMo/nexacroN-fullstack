package com.nexacro.fullstack.runner.boot8.controller;

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
@RequestMapping("/file")
public class FileController extends NexacroController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/upload.do", consumes = "multipart/form-data")
    public NexacroEnvelope upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", required = false, defaultValue = "anonymous") String uploadedBy)
            throws IOException {
        Map<String, Object> meta = fileService.upload(file, uploadedBy);
        NexacroEnvelope out = NexacroResponseBuilder.ok();
        out.getParameters().add(new NexacroEnvelope.Parameter("fileId", meta.get("FILE_ID"), "string"));
        return out;
    }

    @PostMapping("/list.do")
    public NexacroEnvelope list() {
        return NexacroResponseBuilder.ok(fileService.list());
    }

    @GetMapping("/download.do")
    public ResponseEntity<Resource> download(@RequestParam("fileId") String fileId) throws IOException {
        FileService.DownloadInfo info = fileService.download(fileId);
        File f = info.getFile();
        Map<String, Object> meta = info.getMeta();
        String originalName = String.valueOf(meta.get("ORIGINAL_NAME"));
        String contentType  = meta.get("CONTENT_TYPE") != null
                ? String.valueOf(meta.get("CONTENT_TYPE"))
                : "application/octet-stream";
        Resource body = new FileSystemResource(f);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + originalName + "\"")
                .contentLength(f.length())
                .body(body);
    }
}
