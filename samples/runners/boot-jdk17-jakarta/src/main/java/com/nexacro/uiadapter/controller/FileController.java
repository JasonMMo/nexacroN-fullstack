package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.domain.FileMeta;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamVariable;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File upload / download / list endpoints.
 *
 * <p>Uploads use {@code @RequestParam} multipart binding (the uiadapter
 * argument resolver tolerates non-{@code @ParamX} parameters); downloads are
 * raw byte streams written through {@link HttpServletResponse} and return
 * {@code void} (the uiadapter resolver allows this when the method writes the
 * response itself).
 */
@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /** Single-file upload — returns generated {@code fileId} variable. */
    @RequestMapping("/advancedUploadFiles.do")
    public NexacroResult upload(
            @RequestParam("file") MultipartFile file,
            @ParamVariable(name = "uploadedBy", required = false) String uploadedBy) throws IOException {
        String uploader = (uploadedBy == null || uploadedBy.isBlank()) ? "anonymous" : uploadedBy;
        String fileId = fileService.upload(file, uploader);
        NexacroResult result = new NexacroResult();
        result.addVariable("fileId", fileId);
        return result;
    }

    /** Single-file download by id — streams bytes directly. */
    @RequestMapping("/advancedDownloadFile.do")
    public void download(
            @RequestParam("fileId") String fileId,
            HttpServletResponse response) throws IOException {
        FileMeta meta = fileService.findById(fileId);
        if (meta == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "file not found");
            return;
        }
        File file = new File(meta.getStoredPath());
        if (!file.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "file missing on disk");
            return;
        }
        String contentType = meta.getContentType() != null
                ? meta.getContentType() : "application/octet-stream";
        response.setContentType(contentType);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + meta.getOriginalName() + "\"");
        response.setContentLengthLong(file.length());
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                response.getOutputStream().write(buf, 0, n);
            }
        }
    }

    /**
     * Multi-file download as a ZIP stream — comma-separated {@code fileIds}
     * variable selects the bundle members.
     */
    @RequestMapping("/advancedDownloadList.do")
    public void downloadList(
            @ParamVariable(name = "fileIds", required = false) String fileIdsCsv,
            HttpServletResponse response) throws IOException {
        List<String> ids = (fileIdsCsv == null || fileIdsCsv.isBlank())
                ? List.of()
                : List.of(fileIdsCsv.split("\\s*,\\s*"));
        List<FileMeta> metas = fileService.findByIds(ids);
        if (metas.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "no files");
            return;
        }
        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"download.zip\"");
        try (ZipOutputStream zout = new ZipOutputStream(response.getOutputStream())) {
            zout.setLevel(8);
            byte[] buf = new byte[8192];
            for (FileMeta m : metas) {
                File f = new File(m.getStoredPath());
                if (!f.exists()) continue;
                zout.putNextEntry(new ZipEntry(m.getOriginalName()));
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(f))) {
                    int n;
                    while ((n = in.read(buf)) > 0) zout.write(buf, 0, n);
                }
                zout.closeEntry();
            }
        }
    }
}
