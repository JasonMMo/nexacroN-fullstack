package com.nexacro.fullstack.runner.boot8.controller;

import com.nexacro.fullstack.business.domain.video.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * Uiadapter video-streaming controller — contract endpoint #11.
 *
 * Ported from <boot-jdk17-jakarta-legacy>/<StreamController.java>@e49a17791d on 2026-04-24.
 * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
 * No HttpServletRequest/Response plumbing — Spring's ResponseEntity + FileSystemResource supports
 * Range automatically; returns 404 when the configured media folder does not contain the requested file.
 * Path-traversal guard lives in {@link VideoService#resolve(String)}.
 */
@RestController
@RequestMapping("/uiadapter")
public class UiadapterVideoController {

    private final VideoService videoService;

    public UiadapterVideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Endpoint #11 — Stream a video file by name.
     *
     * Ported from boot-jdk17-jakarta-legacy/.../StreamController.streamingVideo@e49a17791d on 2026-04-24.
     * Adaptations: javax.servlet imports, JDK 8 syntax (no var/records/text-blocks), Spring Boot 2.7 compatibility.
     *
     * @param fileName   target video file (resolved under {@code media.storage.base-path})
     * @param streamType retained for legacy compatibility ("nio" / "legacy"); not used
     */
    // spec #11: video streaming (renamed from streamingVideo.do)
    @GetMapping("/sampleVideoStream.do")
    public ResponseEntity<Resource> streamingVideo(
            @RequestParam("fileName") String fileName,
            @RequestParam(value = "streamType", required = false, defaultValue = "nio") String streamType) {
        File f = videoService.resolve(fileName);
        if (f == null || !f.exists() || !f.isFile()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        Resource body = new FileSystemResource(f);
        MediaType contentType = guessContentType(fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.getName() + "\"")
                .contentLength(f.length())
                .contentType(contentType)
                .body(body);
    }

    private MediaType guessContentType(String fileName) {
        if (fileName == null) return MediaType.APPLICATION_OCTET_STREAM;
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".mp4"))  return MediaType.parseMediaType("video/mp4");
        if (lower.endsWith(".webm")) return MediaType.parseMediaType("video/webm");
        if (lower.endsWith(".ogg"))  return MediaType.parseMediaType("video/ogg");
        if (lower.endsWith(".avi"))  return MediaType.parseMediaType("video/x-msvideo");
        if (lower.endsWith(".mov"))  return MediaType.parseMediaType("video/quicktime");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
