package com.nexacro.uiadapter.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.jakarta.core.NexacroException;
import com.nexacro.uiadapter.jakarta.core.data.NexacroFileResult;
import com.nexacro.uiadapter.jakarta.core.util.CharsetUtil;

/**
 * Minimal file endpoint: serve a single resource via {@code NexacroFileResult}.
 */
@Controller
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static final String SP = File.separator;

    /** Upload root — defaults to {@code ./uploads} relative to the running app. */
    @Value("${nexacro.file.storage-dir:./uploads}")
    private String uploadRoot;

    /**
     * 다운로드 파일 — single-file. {@code file} from query string.
     */
    @RequestMapping(value = "/advancedDownloadFile.do")
    public NexacroFileResult downloadFile(HttpServletRequest request) throws Exception {

        logger.debug("-------------------- nexacro platform downloadFile ---------------------------");
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = PlatformType.DEFAULT_CHAR_SET;
        }
        String charsetOfRequest = CharsetUtil.getCharsetOfRequest(request, characterEncoding);
        Map<String, String> queryMap = getQueryMap(request.getQueryString(), charsetOfRequest);

        String filefolder = queryMap.get("filefolder");
        String fileName = queryMap.get("file");
        if (fileName == null) {
            throw new NexacroException("No input fileName specified.");
        }

        fileName = URLDecoder.decode(fileName, charsetOfRequest);
        fileName = removedPathTraversal(fileName);

        String filePath = (filefolder == null) ? getFilePath() : getFilePath() + SP + filefolder;
        File file = new File(filePath, fileName);

        logger.debug("     FILE PATH :{}", filePath);
        logger.debug("     FILE NAME :{}", fileName);

        return new NexacroFileResult(file);
    }

    /** Resolve the canonical upload root for embedded Tomcat. */
    private String getFilePath() {
        File dir = new File(uploadRoot);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (logger.isDebugEnabled()) {
                logger.debug("uploadRoot create={} path={}", created, dir.getAbsolutePath());
            }
        }
        return dir.getAbsolutePath();
    }

    private String removedPathTraversal(String fileName) {
        if (fileName == null) {
            return null;
        }
        fileName = fileName.replace("/", "");
        fileName = fileName.replace("\\", "");
        fileName = fileName.replace("&", "");
        return fileName;
    }

    private Map<String, String> getQueryMap(String queryString, String charset) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        if (queryString == null || queryString.isEmpty()) {
            return map;
        }

        String decodeQs = URLDecoder.decode(queryString, charset);
        int questionIndex = decodeQs.indexOf("?");
        String parameterString = (questionIndex >= 0) ? decodeQs.substring(questionIndex + 1) : decodeQs;
        String[] parameterPairs = parameterString.split("&");

        for (String pair : parameterPairs) {
            String[] keyAndValue = pair.split("=");
            if (keyAndValue.length > 1) {
                map.put(keyAndValue[0], keyAndValue[1]);
            }
        }

        return map;
    }
}
