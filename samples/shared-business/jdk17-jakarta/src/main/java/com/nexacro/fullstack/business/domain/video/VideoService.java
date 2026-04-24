package com.nexacro.fullstack.business.domain.video;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

/**
 * Service for video-streaming endpoint (#9).
 *
 * <p>Ported from boot-jdk17-jakarta-legacy/src/main/java/example/nexacro/uiadapter/web/StreamController.java@e49a17791d on 2026-04-24.
 * Adaptations: extracted into standalone service; NexacroStreamResult (legacy xapi) replaced by
 * returning plain {@link File} — the controller handles HTTP range/NIO directly.
 */
@Service
public class VideoService {

    @Value("${media.storage.base-path:./videos}")
    private String videoDir;

    /**
     * Resolve a video file by name.
     * Returns the File object (may not exist — caller must check), or {@code null}
     * when {@code fileName} would escape the configured media base directory.
     *
     * Ported from boot-jdk17-jakarta-legacy/.../StreamController.streamingVideo@e49a17791d on 2026-04-24.
     * Adaptations: no HttpServletRequest/Response — returns File; caller builds HTTP response.
     * Path-traversal guard: canonical-path check ensures the resolved file lives under {@code videoDir}.
     */
    public File resolve(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        try {
            File base = new File(videoDir).getCanonicalFile();
            File target = new File(base, fileName).getCanonicalFile();
            if (!target.getPath().startsWith(base.getPath() + File.separator)
                    && !target.getPath().equals(base.getPath())) {
                return null;
            }
            return target;
        } catch (java.io.IOException e) {
            return null;
        }
    }
}
