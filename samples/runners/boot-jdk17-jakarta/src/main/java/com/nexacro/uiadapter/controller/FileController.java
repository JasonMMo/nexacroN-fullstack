package com.nexacro.uiadapter.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.nexacro.uiadapter.jakarta.core.NexacroException;
import com.nexacro.uiadapter.jakarta.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.jakarta.core.data.NexacroFileResult;
import com.nexacro.uiadapter.jakarta.core.data.NexacroMultiFileResult;
import com.nexacro.uiadapter.jakarta.core.data.NexacroResult;
import com.nexacro.uiadapter.jakarta.core.util.CharsetUtil;
import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataTypes;
import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.data.datatype.PlatformDataType;
import com.nexacro.java.xapi.tx.DataDeserializer;
import com.nexacro.java.xapi.tx.DataSerializerFactory;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.java.xapi.tx.PlatformType;

/**
 * File controller — canonical Nexacro N upload/download endpoints.
 *
 * <p>Mirrors the GitLab reference (
 * {@code gitlab.com/nexacron/spring-boot/jakarta/uiadapter-jakarta} →
 * {@code example.nexacro.uiadapter.web.FileController}). Spring Boot embedded
 * tomcat lacks a real {@code WEB-INF/upload}, so the upload root is taken from
 * {@code nexacro.file.storage-dir} (default: {@code ./uploads}).
 *
 * <p>Exposed endpoints:
 * <ul>
 *   <li>{@code /advancedUploadFiles.do}    — multipart upload, returns {@code ds_output}</li>
 *   <li>{@code /advancedDownloadFile.do}   — single-file download (NexacroFileResult)</li>
 *   <li>{@code /multiDownloadFiles.do}     — multi-file download (NexacroMultiFileResult)</li>
 *   <li>{@code /advancedDownloadFiles.do}  — multi-file ZIP stream (void/manual)</li>
 *   <li>{@code /advancedDownloadList.do}   — list files in folder (NexacroResult/dsList)</li>
 *   <li>{@code /advancedDeleteFiles.do}    — delete files by DataSet({@code input})</li>
 * </ul>
 */
@Controller
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String SP = File.separator;

    /** Upload root — defaults to {@code ./uploads} relative to the running app. */
    @Value("${nexacro.file.storage-dir:./uploads}")
    private String uploadRoot;

    /**
     * 업로드 파일 — multipart payload + nested {@code inputDatasets} SSV parameter.
     */
    @RequestMapping(value = "/advancedUploadFiles.do")
    public NexacroResult uploadFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (!(request instanceof MultipartHttpServletRequest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Request is not a MultipartHttpServletRequest");
            }
            return new NexacroResult();
        }

        logger.debug("-------------------- nexacro platform uploadFiles ---------------------------");
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = PlatformType.DEFAULT_CHAR_SET;
        }
        String charsetOfRequest = CharsetUtil.getCharsetOfRequest(request, characterEncoding);
        String queryString = request.getQueryString();
        Map<String, String> queryMap = getQueryMap(queryString, charsetOfRequest);
        String filefolder = queryMap.get("filefolder");

        DataSet resultDs = createDataSet4UploadResult();

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        uploadParameters(multipartRequest);
        uploadMultipartFiles(multipartRequest, resultDs, filefolder);

        NexacroResult nexacroResult = new NexacroResult();
        nexacroResult.addDataSet(resultDs);

        return nexacroResult;
    }

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
        String queryString = request.getQueryString();
        Map<String, String> queryMap = getQueryMap(queryString, charsetOfRequest);

        String filefolder = queryMap.get("filefolder");

        String fileName = queryMap.get("file");
        if (fileName == null) {
            throw new NexacroException("No input fileName specified.");
        }

        // already URL-decoded once by container; canonical decodes a second time.
        fileName = URLDecoder.decode(fileName, charsetOfRequest);
        fileName = removedPathTraversal(fileName);

        String filePath;
        if (filefolder == null) {
            filePath = getFilePath();
        } else {
            filePath = getFilePath() + SP + filefolder;
        }
        String realFileName = filePath + SP + fileName;

        logger.debug("     FILE PATH :{}", filePath);
        logger.debug("     FILE NAME :{}", fileName);

        File file = new File(realFileName);

        return new NexacroFileResult(file);
    }

    /**
     * 다운로드 파일 — multi-file as Nexacro multi-file result.
     */
    @RequestMapping(value = "/multiDownloadFiles.do")
    public NexacroFileResult multiDownloadFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {

        logger.debug("-------------------- nexacro multiDownloadFiles ---------------------------");
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = PlatformType.DEFAULT_CHAR_SET;
        }
        String charsetOfRequest = CharsetUtil.getCharsetOfRequest(request, characterEncoding);
        String queryString = request.getQueryString();
        Map<String, String> queryMap = getQueryMap(queryString, charsetOfRequest);

        String filePath;

        String filefolder = queryMap.get("filefolder");
        if (filefolder == null) {
            filePath = getFilePath();
            filefolder = "download";
        } else {
            filePath = getFilePath() + SP + filefolder;
        }

        String filenamelist = request.getParameter("filenamelist");
        if (filenamelist == null) {
            throw new NexacroException("No input fileName specified.");
        }

        filenamelist = URLDecoder.decode(filenamelist, charsetOfRequest);
        filenamelist = removedPathTraversal(filenamelist);

        return new NexacroMultiFileResult(filefolder, filePath, filenamelist);
    }

    /**
     * 다운로드 파일 — multi-file ZIP stream (manual {@code void} response writer).
     */
    @RequestMapping(value = "/advancedDownloadFiles.do")
    public void downloadFiles(HttpServletRequest request, HttpServletResponse response) throws Exception {

        logger.debug("-------------------- nexacro platform downloadFile ---------------------------");
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = PlatformType.DEFAULT_CHAR_SET;
        }
        String charsetOfRequest = CharsetUtil.getCharsetOfRequest(request, characterEncoding);
        String queryString = request.getQueryString();
        Map<String, String> queryMap = getQueryMap(queryString, charsetOfRequest);

        String filefolder = queryMap.get("filefolder");
        String filenamelist = request.getParameter("filenamelist");
        if (filenamelist == null) {
            throw new NexacroException("No input fileName specified.");
        }

        filenamelist = URLDecoder.decode(filenamelist, charsetOfRequest);
        filenamelist = removedPathTraversal(filenamelist);

        String[] fileNameArr = filenamelist.split(",");
        String filePath;
        if (filefolder == null) {
            filePath = getFilePath();
            filefolder = "download";
        } else {
            filePath = getFilePath() + SP + filefolder;
        }
        String realFileName;

        ServletOutputStream out_stream = null;
        BufferedInputStream in_stream = null;
        ZipOutputStream zout_stream = null;

        try {
            response.flushBuffer();
            response.setContentType("application/octet;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename = \"" + filefolder + ".zip" + "\"");

            out_stream = response.getOutputStream();
            zout_stream = new ZipOutputStream(out_stream);
            zout_stream.setLevel(8);

            for (String fileName : fileNameArr) {
                fileName = URLDecoder.decode(fileName, charsetOfRequest);
                fileName = removedPathTraversal(fileName);

                realFileName = filePath + SP + fileName;

                File fis = new File(realFileName);
                in_stream = new BufferedInputStream(new FileInputStream(fis));

                ZipEntry zentry = new ZipEntry(fileName);
                zentry.setTime(fis.lastModified());
                zout_stream.putNextEntry(zentry);

                byte[] buffer = new byte[1024];
                int n;
                while ((n = in_stream.read(buffer, 0, 1024)) != -1) {
                    zout_stream.write(buffer, 0, n);
                }

                zout_stream.closeEntry();
            }
        } catch (Exception e) {
            logger.error("downloadFiles failed", e);
        } finally {
            if (zout_stream != null) {
                try {
                    zout_stream.close();
                } catch (Exception e) {
                    logger.warn("zout_stream close failed", e);
                }
            }
            if (out_stream != null) {
                try {
                    out_stream.close();
                } catch (Exception e) {
                    logger.warn("out_stream close failed", e);
                }
            }
            if (in_stream != null) {
                try {
                    in_stream.close();
                } catch (Exception e) {
                    logger.warn("in_stream close failed", e);
                }
            }
        }
    }

    /**
     * 다운로드 파일 목록 — list files in folder, return as DataSet({@code dsList}).
     */
    @RequestMapping(value = "/advancedDownloadList.do")
    public NexacroResult downloadList(HttpServletRequest request,
            @ParamDataSet(name = "ds_search", required = false) Map<String, String> searchInfo) throws Exception {

        logger.debug("-------------------- nexacro platform downloadFile List ---------------------------");
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding == null) {
            characterEncoding = PlatformType.DEFAULT_CHAR_SET;
        }
        String charsetOfRequest = CharsetUtil.getCharsetOfRequest(request, characterEncoding);
        String queryString = request.getQueryString();
        Map<String, String> queryMap = getQueryMap(queryString, charsetOfRequest);

        String filefolder = queryMap.get("filefolder");
        String filePath;
        if (filefolder == null) {
            filePath = getFilePath();
            filefolder = "download";
        } else {
            filePath = getFilePath() + SP + filefolder;
        }

        String url = request.getRequestURL() + uploadRoot + filefolder;
        logger.debug("-------url------ :{}", url);

        DataSet dsList = getDownloadList(filePath);

        NexacroResult result = new NexacroResult();
        result.addDataSet(dsList);

        return result;
    }

    /**
     * 삭제 파일 — accepts DataSet({@code input}) with {@code file} column.
     */
    @RequestMapping(value = "/advancedDeleteFiles.do")
    public NexacroResult deleteFiles(@ParamDataSet(name = "input") DataSet dsInput) throws Exception {
        logger.debug("-------------------- nexacro platform deleteFiles ---------------------------");

        if (dsInput == null) {
            throw new NexacroException("No input DataSet('input') specified.");
        }

        NexacroResult result = new NexacroResult();
        String filePath = getFilePath();
        StringBuilder errorMessage = new StringBuilder();
        int rowCount = dsInput.getRowCount();

        logger.debug("    filePath :{}", filePath);
        logger.debug("    rowCount :{}", rowCount);

        for (int i = 0; i < rowCount; i++) {

            String fileRealNm = dsInput.getString(i, "file");
            if (fileRealNm == null || fileRealNm.length() == 0) {
                continue;
            }

            String fileName = removedPathTraversal(fileRealNm);

            logger.debug("    fileName :{}", fileName);

            if (errorMessage.length() > 0) {
                errorMessage.append("\r\n");
            }

            try {
                File f = new File(filePath + File.separator, fileName);
                if (f.exists()) {
                    if (f.delete()) {
                        errorMessage.append("'").append(fileName).append("' Delete Success");
                    } else {
                        errorMessage.append("'").append(fileName).append("' Delete failed");
                    }
                } else {
                    errorMessage.append("'").append(fileName).append("' File not available");
                }
            } catch (Exception e) {
                errorMessage.append("'").append(fileName).append("' ").append(e);
                NexacroException nexacroException = new NexacroException();
                nexacroException.setErrorCode(-1);
                nexacroException.setErrorMsg(errorMessage.toString());
            }

            logger.debug("    errorMessage :{}", errorMessage);
        }

        result.addVariable("ErrorCode", 0);
        result.addVariable("ErrorMsg", errorMessage.toString());

        return result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers (mirror canonical reference)
    // ─────────────────────────────────────────────────────────────────────────

    private void uploadParameters(MultipartHttpServletRequest multipartRequest) throws NexacroException {
        Enumeration<String> parameterNames = multipartRequest.getParameterNames();

        while (parameterNames.hasMoreElements()) {

            String parameterName = parameterNames.nextElement();
            if (parameterName == null || parameterName.length() == 0) {
                continue;
            }

            String value = multipartRequest.getParameter(parameterName);

            if ("inputDatasets".equals(parameterName)) {

                PlatformData platformData = new PlatformData();
                StringReader reader = new StringReader(value);
                DataDeserializer deserializer = DataSerializerFactory.getDeserializer(PlatformType.CONTENT_TYPE_SSV);
                try {
                    platformData = deserializer.readData(reader, null, PlatformType.DEFAULT_CHAR_SET);
                } catch (PlatformException e) {
                    logger.debug("xml data not deserialize. data={}", value);
                    continue;
                }

                DataSet dsInput = platformData.getDataSet("ds_input");
                if (dsInput != null && logger.isDebugEnabled()) {
                    logger.debug("dsInput data=\n{}", dsInput.saveXml());
                }

                // 이후 처리는 각 업무로직에 맞게 사용할 것.

            } else {
                String filePath = getFilePath();
                String fileName = removedPathTraversal(value);
                File f = new File(filePath + SP, fileName);
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    /**
     * Resolve the canonical "WEB-INF/upload" equivalent for Spring Boot embedded tomcat.
     * Uses {@code nexacro.file.storage-dir} (default {@code ./uploads}). Ensures the
     * directory exists.
     */
    private String getFilePath() {
        File dir = new File(uploadRoot);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (logger.isDebugEnabled()) {
                logger.debug("uploadRoot create={} path={}", created, dir.getAbsolutePath());
            }
        }
        String absolute = dir.getAbsolutePath();
        logger.debug("--------------| getFilePath = {} |-----------------------", absolute);
        return absolute;
    }

    private void uploadMultipartFiles(MultipartHttpServletRequest multipartRequest, DataSet resultDs, String sFilefolder) throws IOException {

        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        String filePath;
        if (sFilefolder == null) {
            filePath = getFilePath();
        } else {
            filePath = getFilePath() + SP + sFilefolder;
        }

        Set<String> keySet = fileMap.keySet();
        for (String name : keySet) {

            MultipartFile multipartFile = fileMap.get(name);

            String originalFilename = multipartFile.getOriginalFilename();

            // IE 에서 파일업로드 시 DataSet 파라매터의 Content-Type 이 설정되지 않아 여기로 옴. 무시.
            if (originalFilename == null || originalFilename.length() == 0) {
                continue;
            }

            File destination = new File(filePath);

            if (!destination.exists()) {
                boolean mkdirs = destination.mkdirs();
                destination.setWritable(true);

                logger.debug("-------------- create directory ----------------------{}", mkdirs);
            }

            File targetFile = new File(filePath + SP + originalFilename);

            try (InputStream inputStream = multipartFile.getInputStream();
                 FileOutputStream out = new FileOutputStream(targetFile)) {
                FileCopyUtils.copy(inputStream, out);
            }

            int row = resultDs.newRow();
            resultDs.set(row, "fileid", originalFilename);
            resultDs.set(row, "filename", originalFilename);
            resultDs.set(row, "filesize", targetFile.length());

            if (logger.isDebugEnabled()) {
                logger.debug("uploaded file write success. file={}", originalFilename);
            }
        }
    }

    private DataSet getDownloadList(String sFilePth) throws IOException {

        DataSet dsList = new DataSet("dsList");
        dsList.addColumn("FILE_PATH", DataTypes.STRING, 255);
        dsList.addColumn("FILE_NAME", DataTypes.STRING, 255);
        dsList.addColumn("FILE_URL", DataTypes.STRING, 255);
        dsList.addColumn("FILE_SIZE", DataTypes.STRING, 255);

        File dir = new File(sFilePth);
        if (!dir.exists() || !dir.isDirectory()) {
            return dsList;
        }

        String[] fileNames = dir.list();
        if (fileNames == null) {
            return dsList;
        }
        for (String filename : fileNames) {

            File targetFile = new File(sFilePth + SP + filename);

            int row = dsList.newRow();

            dsList.set(row, "FILE_PATH", targetFile.getPath());
            dsList.set(row, "FILE_NAME", filename);
            dsList.set(row, "FILE_URL", filename);
            long megabyte = targetFile.length() / 1024;
            dsList.set(row, "FILE_SIZE", megabyte + " kb");

            if (logger.isDebugEnabled()) {
                logger.debug("file info in download Folder. file={}", targetFile.getName());
            }
        }
        return dsList;
    }

    private String removedPathTraversal(String fileName) {
        if (fileName == null) {
            return null;
        }

        fileName = fileName.replace("/", "");
        fileName = fileName.replace("\\", "");
        // fileName = fileName.replace(".", "");
        fileName = fileName.replace("&", "");
        return fileName;
    }

    private DataSet createDataSet4UploadResult() {

        DataSet ds = new DataSet("ds_output");
        ds.addColumn("fileid", PlatformDataType.STRING);
        ds.addColumn("fileimg", PlatformDataType.STRING);
        ds.addColumn("filename", PlatformDataType.STRING);
        ds.addColumn("filesize", PlatformDataType.INT);
        ds.addColumn("tranfilesize", PlatformDataType.INT);
        ds.addColumn("prog", PlatformDataType.INT);

        return ds;
    }

    private Map<String, String> getQueryMap(String queryString, String charset) throws UnsupportedEncodingException {

        Map<String, String> map = new HashMap<>();
        if (queryString == null || queryString.length() == 0) {
            return map;
        }

        String decodeQs = URLDecoder.decode(queryString, charset);
        int questionIndex = decodeQs.indexOf("?");
        String parameterString = decodeQs.substring(questionIndex + 1);
        String[] parameterPairs = parameterString.split("&");

        for (int i = 0; i < parameterPairs.length; i++) {
            String[] keyAndValue = parameterPairs[i].split("=");
            if (keyAndValue.length > 1) {
                map.put(keyAndValue[0], keyAndValue[1]);
            }
        }

        return map;
    }
}
