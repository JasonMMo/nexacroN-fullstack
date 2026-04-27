package com.nexacro.fullstack.runner.boot17.controller;

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
 * Uiadapter video-streaming controller (contract endpoint #9).
 *
 * <p>Ported from boot-jdk17-jakarta-legacy/src/main/java/example/nexacro/uiadapter/web/StreamController.java@e49a17791d on 2026-04-24.
 * Adaptations: legacy {@code NexacroStreamResult} return type replaced by Spring
 * {@link ResponseEntity}&lt;{@link Resource}&gt;; Spring/Tomcat serves HTTP Range requests natively
 * for {@link FileSystemResource}, so the {@code streamType} (nio/legacy) distinction collapses
 * into a single implementation; file lookup delegates to {@link VideoService}.
 */
@RestController
@RequestMapping("/uiadapter")
public class UiadapterVideoController {

    private final VideoService videoService;

    public UiadapterVideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    /**
     * Endpoint #9 — Stream a video file by name.
     *
     * <p>Ported from boot-jdk17-jakarta-legacy/.../StreamController.streamingVideo@e49a17791d on 2026-04-24.
     * Adaptations: no {@code HttpServletRequest/Response} plumbing — Spring's
     * {@link ResponseEntity} + {@link FileSystemResource} supports Range automatically;
     * returns 404 when the configured media folder does not contain the requested file.
     * Path-traversal guard lives in {@link VideoService#resolve(String)}.
     *
     * @param fileName   target video file (resolved under {@code media.storage.base-path})
     * @param streamType retained for legacy compatibility ("nio" / "legacy"); not used
     */
    // spec #11: video streaming. TODO Plan8: spec §2 mandates POST but video streaming requires
    // GET for HTTP Range (partial content). Retained as GET — flag for Opus to decide.
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
                .contentType(contentType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(f.length())
                .body(body);
    }

    private static MediaType guessContentType(String fileName) {
        String lower = fileName == null ? "" : fileName.toLowerCase();
        if (lower.endsWith(".mp4"))  return MediaType.valueOf("video/mp4");
        if (lower.endsWith(".webm")) return MediaType.valueOf("video/webm");
        if (lower.endsWith(".ogv"))  return MediaType.valueOf("video/ogg");
        if (lower.endsWith(".mov"))  return MediaType.valueOf("video/quicktime");
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
