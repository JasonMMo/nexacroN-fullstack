package com.nexacro.uiadapter.controller;

import com.nexacro.uiadapter.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

/**
 * Video byte-range streaming endpoint.
 *
 * <p>Spring's {@link ResourceHttpRequestHandler} natively handles HTTP Range
 * requests for {@link FileSystemResource}, so we delegate to it. Method
 * returns {@code void} — the handler writes the response itself.
 */
@Controller
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @RequestMapping("/sampleVideoStream.do")
    public void stream(
            @RequestParam("fileName") String fileName,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        File f = videoService.resolve(fileName);
        if (f == null || !f.exists() || !f.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "video not found");
            return;
        }
        FileSystemResource body = new FileSystemResource(f);
        response.setContentType(guessContentType(fileName));
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes");
        // Hand off Range handling to Spring's ResourceHttpRequestHandler.
        ResourceHttpRequestHandler handler = new ResourceHttpRequestHandler() {
            @Override
            protected org.springframework.core.io.Resource getResource(HttpServletRequest req) throws IOException {
                return body;
            }
        };
        handler.setLocations(Collections.emptyList());
        try {
            handler.afterPropertiesSet();
        } catch (Exception ignored) {
            // location list is intentionally empty — the override above supplies the resource.
        }
        handler.handleRequest(request, response);
    }

    private static String guessContentType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".mp4"))  return "video/mp4";
        if (lower.endsWith(".webm")) return "video/webm";
        if (lower.endsWith(".ogv"))  return "video/ogg";
        if (lower.endsWith(".mov"))  return "video/quicktime";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
