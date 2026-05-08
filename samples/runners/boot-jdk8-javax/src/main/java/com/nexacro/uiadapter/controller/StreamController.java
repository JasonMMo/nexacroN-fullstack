package com.nexacro.uiadapter.controller;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.spring.core.data.NexacroFileResult;

/**
 * Video / image streaming controller (javax lane).
 *
 * <p>Mirrors the canonical Nexacro N reference at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta/-/blob/master/src/main/java/example/nexacro/uiadapter/web/StreamController.java}.
 *
 * <p>Lane differences vs canonical (jakarta):
 * <ul>
 *   <li>{@code uiadapter-spring-core 1.4.19.2-SNAPSHOT} (javax lane) does not ship
 *       {@code NexacroStreamResult}. {@code /streamingVideo.do} therefore returns a
 *       plain {@link NexacroFileResult}; the {@code streamType} parameter
 *       (memory / nio / part) is accepted for API parity but currently has no effect
 *       in this lane. For full streaming features use the jakarta runner.</li>
 *   <li>{@code VIDEO_PATH} default points to the bundled
 *       {@code src/main/resources/static/videos/}. Override via
 *       {@code media.storage.base-path} property.</li>
 *   <li>View-resolving endpoints {@code /video.do} and {@code /movie/{fileName}/**}
 *       return Spring view names; they require a template engine to be registered.</li>
 * </ul>
 */
@Controller
public class StreamController {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    static String VIDEO_PATH = "./src/main/resources/static/videos/";

    @Value("${media.storage.base-path:}")
    private String baseStoragePath;

    @RequestMapping("/video.do")
    public String greeting() {
        return "video";
    }

    @RequestMapping("/movie/{fileName}/**")
    public ModelAndView video(@PathVariable("fileName") String fileName) {
        ModelAndView mv = new ModelAndView("streamView");
        mv.addObject("movieName", fileName);
        return mv;
    }

    /**
     * Streams a video file. {@code streamType} is accepted for API parity with the
     * jakarta lane but has no effect (see class javadoc).
     */
    @RequestMapping("/streamingVideo.do")
    public NexacroFileResult streamingVideo(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String savePath = getVideoDirectory();
        String filename = request.getParameter("fileName");
        String streamType = request.getParameter("streamType"); // accepted, no-op in javax lane

        // 파일명 인코딩
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = PlatformType.DEFAULT_CHAR_SET;
        }

        File file = new File(savePath + File.separator + filename);

        if (file.exists()) {
            log.debug("     ======================= 있음  ");
        } else {
            log.debug("     ======================= 없음  ");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        NexacroFileResult fileResult = new NexacroFileResult(file);
        fileResult.setOriginalName(filename);
        log.debug("streamType requested (no-op in javax lane): {}", streamType);

        return fileResult;
    }

    private String getVideoDirectory() {
        if (StringUtils.hasText(baseStoragePath)) {
            return baseStoragePath;
        } else {
            return VIDEO_PATH;
        }
    }
}
