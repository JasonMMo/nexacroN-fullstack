package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.business.domain.file.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
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
 * Ported from <boot-jdk17-jakarta-legacy>/<FileController.java>@e49a17791d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 * Resolve by FILE_ID (path-traversal safe) instead of raw filename params;
 * streams via javax.servlet.http.HttpServletResponse + java.util.zip; returns 404 if nothing to bundle.
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
     * Ported from boot-jdk17-jakarta-legacy/.../FileController.multiDownloadFiles@e49a17791d on 2026-04-24.
     * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
     * HTTP verb is GET per contract (query params {@code fileIds}, {@code subFolder}).
     * JDK 8: MultiDownloadEntry.file()/originalName() record accessors replaced with getFile()/getOriginalName().
     */
    @GetMapping("/multiDownloadFiles.do")
    public void multiDownload(
            @RequestParam(value = "subFolder", required = false) String subFolder,
            @RequestParam(value = "fileIds", required = false) List<String> fileIds,
            HttpServletResponse response) throws IOException {
        List<FileService.MultiDownloadEntry> entries =
                fileIds == null ? Collections.<FileService.MultiDownloadEntry>emptyList() : fileService.multiDownload(fileIds);
        if (entries.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "no files");
            return;
        }
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"download.zip\"");
        ZipOutputStream zout = new ZipOutputStream(response.getOutputStream());
        try {
            zout.setLevel(8);
            byte[] buf = new byte[8192];
            for (FileService.MultiDownloadEntry e : entries) {
                File f = e.getFile();
                if (f == null || !f.exists()) continue;
                zout.putNextEntry(new ZipEntry(e.getOriginalName()));
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
                try {
                    int n;
                    while ((n = in.read(buf)) > 0) zout.write(buf, 0, n);
                } finally {
                    in.close();
                }
                zout.closeEntry();
            }
        } finally {
            zout.close();
        }
    }
}
