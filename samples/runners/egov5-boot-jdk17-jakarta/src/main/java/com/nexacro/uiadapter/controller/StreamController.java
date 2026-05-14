package com.nexacro.uiadapter.controller;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.jakarta.core.data.NexacroStreamResult;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Video / image streaming controller.
 *
 * <p>Mirrors the canonical Nexacro N reference at
 * {@code https://gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta/-/blob/master/src/main/java/example/nexacro/uiadapter/web/StreamController.java}.
 *
 * <p>Adaptations for Spring Boot embedded tomcat:
 * <ul>
 *   <li>{@code VIDEO_PATH} default points to the bundled
 *       {@code src/main/resources/static/videos/} (canonical hard-codes the same).
 *       Override at runtime via {@code media.storage.base-path} property to read from
 *       a writable filesystem location.</li>
 *   <li>The view-resolving endpoints {@code /video.do} and {@code /movie/{fileName}/**}
 *       return Spring view names ({@code "video"} / {@code "streamView"}); they require
 *       a registered template engine (JSP / Thymeleaf). The streaming workhorse
 *       {@code /streamingVideo.do} works without any template engine.</li>
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
     * Streams a video file with optional NIO / memory-mapping / range-based "part"
     * delivery, depending on {@code streamType} request parameter.
     *
     * @param request  request carrying {@code fileName} and optional {@code streamType}
     * @param response servlet response (404 set when file is missing)
     * @return {@link NexacroStreamResult} configured with stream mode flags
     */
    @RequestMapping("/streamingVideo.do")
    public NexacroStreamResult streamingVideo(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String savePath = getVideoDirectory();
        String filename = request.getParameter("fileName");
        String streamType = request.getParameter("streamType");

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

        NexacroStreamResult fileResult = new NexacroStreamResult(file);
        fileResult.setOriginalName(filename);
        fileResult.setPart(true);

        if (streamType == null || streamType.equals("")) {
            fileResult.setUseMemoryMapping(false);
            fileResult.setUseNio(false);
        } else if (streamType.equals("memory")) {
            fileResult.setUseMemoryMapping(true);
            fileResult.setUseNio(false);
        } else if (streamType.equals("nio")) {
            fileResult.setUseMemoryMapping(false);
            fileResult.setUseNio(true);
        }
        fileResult.setUseAdaptiveBuffer(true); // default : 64k

        return fileResult;
    }

    private String getVideoDirectory() {
        if (StringUtils.hasText(baseStoragePath)) {
            // 절대 경로가 설정된 경우
            return baseStoragePath;
        } else {
            // ServletContext 기준 상대 경로
            return VIDEO_PATH;
        }
    }
}
