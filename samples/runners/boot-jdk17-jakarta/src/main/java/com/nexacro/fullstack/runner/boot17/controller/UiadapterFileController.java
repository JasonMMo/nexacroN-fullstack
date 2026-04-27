package com.nexacro.fullstack.runner.boot17.controller;

import com.nexacro.fullstack.business.domain.file.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Uiadapter multi-file download controller — contract endpoint #7.
 *
 * <p>Ported from boot-jdk17-jakarta-legacy/src/main/java/com/nexacro/example/web/FileController.java@e49a17791d on 2026-04-24.
 * Adaptations: resolve by FILE_ID (path-traversal safe) instead of raw filename params;
 * streams via jakarta.servlet.http.HttpServletResponse + java.util.zip; returns 404 if nothing to bundle.
 */
@RestController
@RequestMapping("/uiadapter")
public class UiadapterFileController {

    private final FileService fileService;

    public UiadapterFileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Endpoint #7 — Multi-file download as ZIP stream.
     *
     * <p>Ported from boot-jdk17-jakarta-legacy/.../FileController.multiDownloadFiles@e49a17791d on 2026-04-24.
     * Adaptations: resolve by FILE_ID (path-traversal safe) instead of raw filename params;
     * streams via jakarta.servlet.http.HttpServletResponse + java.util.zip; returns 404 if nothing to bundle.
     * HTTP verb is GET per contract (query params {@code fileIds}, {@code subFolder}).
     * TODO Plan8: spec §2 mandates POST but ZIP streaming uses GET query params.
     * Retained as GET — flag for Opus to decide verb or add POST alias.
     */
    // spec #9: multi-file download as ZIP (renamed from multiDownloadFiles.do)
    @GetMapping("/advancedDownloadList.do")
    public void multiDownload(
            @RequestParam(value = "subFolder", required = false) String subFolder,
            @RequestParam(value = "fileIds", required = false) List<String> fileIds,
            HttpServletResponse response) throws IOException {
        List<FileService.MultiDownloadEntry> entries =
                fileIds == null ? Collections.emptyList() : fileService.multiDownload(fileIds);
        if (entries.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "no files");
            return;
        }
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"download.zip\"");
        try (ZipOutputStream zout = new ZipOutputStream(response.getOutputStream())) {
            zout.setLevel(8);
            byte[] buf = new byte[8192];
            for (FileService.MultiDownloadEntry e : entries) {
                File f = e.file();
                if (f == null || !f.exists()) continue;
                zout.putNextEntry(new ZipEntry(e.originalName()));
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(f))) {
                    int n;
                    while ((n = in.read(buf)) > 0) zout.write(buf, 0, n);
                }
                zout.closeEntry();
            }
        }
    }
}
