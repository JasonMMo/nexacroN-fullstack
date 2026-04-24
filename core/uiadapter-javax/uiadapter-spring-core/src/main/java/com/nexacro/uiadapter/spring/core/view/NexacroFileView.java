package com.nexacro.uiadapter.spring.core.view;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.spring.core.NexacroConstants;
import com.nexacro.uiadapter.spring.core.NexacroException;
import com.nexacro.uiadapter.spring.core.data.NexacroFileResult;
import com.nexacro.uiadapter.spring.core.data.NexacroMultiFileResult;
import com.nexacro.uiadapter.spring.core.util.CharsetUtil;

/**
 * <p>nexacro platform으로 파일 데이터를 송신하기 위한 {@link org.springframework.web.servlet.View}이다.
 * 
 * <p>파일데이터 전송 시 사용되는 MIME TYPE의 경우 {@link javax.activation.MimetypesFileTypeMap}을 이용하여 처리된다.
 * 
 * @author Park SeongMin
 * @since 07.27.2015
 * @version 1.0
 *
 */
public class NexacroFileView extends NexacroView {

	private static final String SP = File.separator;
	private final Logger logger = LoggerFactory.getLogger(NexacroFileView.class);

	public  NexacroFileView() {
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {

	    Object object = model.get(NexacroConstants.ATTRIBUTE.NEXACRO_FILE_DATA);

        if(!(object instanceof NexacroFileResult)) {

            sendResponse(request, response);

        } else if (object instanceof NexacroMultiFileResult) {

        	NexacroMultiFileResult multiFileResult = (NexacroMultiFileResult) object;

			// was의 uri encoding을 사용안함. (서버의 설정을 변경하여야 함. URIEncoding="UTF-8")
			// already decode..
			// tomcat의 기본 uriencoding 형식 + web.xml의 charsetfilter utf8 (runtime version 은  uriencoding 되지  않고 있음)
			String characterEncoding = request.getCharacterEncoding();
			if(characterEncoding == null) {
				characterEncoding = PlatformType.DEFAULT_CHAR_SET;
			}
			String charsetOfRequest = CharsetUtil.getCharsetOfRequest(request, characterEncoding);

			String[] fileNameArr = multiFileResult.getFileNameList().split(",");
			String filePath     = multiFileResult.getFilePath();
			String zipFileName	= multiFileResult.getZipFileName();

			String sanitizedZipFileName = sanitizeValue(zipFileName);
			if (sanitizedZipFileName == null || sanitizedZipFileName.trim().isEmpty()) {
				sanitizedZipFileName = "download"; // Provide a default name if empty after sanitization
			}

			ZipOutputStream		zOutStream	;

			try {
				response.setContentType("application/octet;charset=utf-8"); // application/octet
				response.setHeader("Content-Disposition", "attachment; filename=\"" + sanitizedZipFileName + ".zip\"");
				response.setHeader("Content-Description", "...");
				response.setHeader("Pragma", "no-cache");
				response.setHeader("Expires", "-1");

				try (OutputStream outStream = response.getOutputStream();
					 BufferedOutputStream bufferedOut = new BufferedOutputStream(outStream, 8192);
					 CheckedOutputStream cos = new CheckedOutputStream(bufferedOut, new Adler32());
					 ZipOutputStream zos = new ZipOutputStream(cos)) {

					zOutStream = zos; // Assign to outer variable if needed elsewhere (though try-with-resources is better)
					zOutStream.setLevel(Deflater.BEST_SPEED); // 0 < level < 9   DEFAULT_COMPRESSION

					byte[] buffer = new byte[8192];

					for( String fileName : fileNameArr){
						// was의 uri는 이미 decode되어 decoding을 사용안함. (서버의 설정을 변경하여야 함. URIEncoding="UTF-8")
						// tomcat의 기본 uriencoding 형식 + web.xml의 charsetfilter utf8 (runtime version 은  uriencoding 되지  않고 있음)
						String zipEntryName = URLDecoder.decode(fileName, charsetOfRequest);
						String sanitizedFileName = removedPathTraversal(zipEntryName); // Sanitize for path traversal

						if (zipEntryName == null || zipEntryName.trim().isEmpty()) {
							continue; // Skip empty or invalid names
						}

						File baseDir = new File(filePath); // Assume filePath is validated elsewhere to be a safe base directory
						File file = new File(baseDir, zipEntryName);

						if (!file.getCanonicalPath().startsWith(baseDir.getCanonicalPath())) {
							logger.warn("Path traversal attempt blocked for file: {} in path: {}", sanitizedFileName, sanitizeValue(filePath));
							continue; // Skip this file
						}
						// Original file existence checks
						if(!file.exists() || file.isDirectory() || !file.isFile() || !file.canRead()) {
							logger.warn("Skipping file in ZIP (not found, is directory, or cannot read): {}", sanitizeValue(file.getPath()));
							continue;
						}

						try (BufferedInputStream inStream = new BufferedInputStream(
                                Files.newInputStream(Paths.get(filePath + File.separator + zipEntryName)), 8192)) {

							ZipEntry zEntry = new ZipEntry(zipEntryName); // Use sanitized name for entry
							zEntry.setTime(file.lastModified());
							zOutStream.putNextEntry(zEntry);

							int length;
							while ((length = inStream.read(buffer)) != -1) // Simplified read loop
							{
								zOutStream.write(buffer, 0, length);
							}
							zOutStream.closeEntry();
						} // fis is automatically closed here
					}
					zOutStream.finish();
				} // zos and cos are automatically closed here
			}
			catch (Exception e)
			{
				logger.error("Error creating or streaming zip file for multi-download", e);
				// Consider sending a Nexacro error response if possible before the stream is committed
				if (!response.isCommitted()) {
					sendFailResponse(request, response, "Failed to generate multi-file download: " + e.getMessage());
				}
			}
		} else { // Single File Download Logic

			NexacroFileResult fileResult = (NexacroFileResult) object;

			String charset 		= fileResult.getCharset();
			String contentType 	= fileResult.getContentType();
			String originalName = fileResult.getOriginalName();

			File file = fileResult.getFile();
			if(file == null) {
				sendFailResponse(request, response, "send response failed. file is null.");
				return;
			}

			if (!file.exists() || !file.isFile() || !file.canRead()) {
				sendFailResponse(request, response, "Send response failed: File '" + file.getName() + "' does not exist, is not a file, or cannot be read.");
				return;
			}

			if(contentType == null) {
				try {
					Path path = file.toPath();
					contentType = Files.probeContentType(path);
				} catch (Exception e) {
					logger.warn("Could not determine content type for file: {}", file.getName(), e);
				}
			}

			charset = charset != null? charset: CharsetUtil.getCharsetOfRequest(request, StandardCharsets.UTF_8.name()); // "utf-8"
			contentType = contentType != null? contentType: "application/octet-stream";
			originalName = originalName != null? originalName: file.getName();

			String sanitizedContentType = contentType;
			String sanitizedCharset = charset;
			String sanitizedOriginalName = originalName;

			if (sanitizedOriginalName.trim().isEmpty()) {
				sanitizedOriginalName = "download"; // Default filename if empty
			}

			String enName = URLEncoder.encode(sanitizedOriginalName, String.valueOf(StandardCharsets.UTF_8));
			// MIME 형식 변환 규칙에 따라 파일명에 공백은 '+'로 전달됨.
			enName = enName.replaceAll("\\+", "%20"); // URLEncoding 후 '+' 로 변경된 공백을 '%20' 으로 다시 변경

			// ** VULNERABILITY FIX: Use sanitized values in headers **
			response.setContentType(this.sanitizeValue(sanitizedContentType) + "; charset=" + this.sanitizeValue(sanitizedCharset));
			response.setHeader("Content-Transfer-Encoding", "binary");
			response.setHeader("Content-Disposition", "attachment; filename=" + sanitizeValue(enName) + "; size=" + file.length()); // Add quotes around filename
			response.setHeader("Content-Description", "...");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0"); // -1

			try (OutputStream out = response.getOutputStream();
				 BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 8192)) {

				byte[] buffer = new byte[8192];
				int bytesRead;

				while ((bytesRead = bis.read(buffer)) != -1) {
					out.write(buffer, 0, bytesRead);
				}
				out.flush(); // Ensure all data is written to the client
			} catch (Exception e) {
				logger.error("Error streaming file '{}' to client", sanitizeValue(file.getName()), e);
				if (!response.isCommitted()) {
					sendFailResponse(request, response, "Failed to stream file: " + e.getMessage());
				}
			}
		}
	}

    private void sendFailResponse(HttpServletRequest request, HttpServletResponse response, String errorMsg) throws PlatformException {
		// Ensure response isn't already committed before trying to send Nexacro error
		if (response.isCommitted()) {
			logger.error("Cannot send Nexacro error response, stream already committed. Error was: {}", errorMsg);
			return;
		}
		PlatformData platformData = new PlatformData();
		// Sanitize error message before putting it in the response variable (optional but good practice)
		String sanitizedErrorMsg = sanitizeValue(errorMsg); // Re-use sanitize method, though this isn't for a header
		platformData.addVariable(Variable.createVariable(NexacroConstants.ERROR.ERROR_CODE, NexacroException.DEFAULT_ERROR_CODE));
		platformData.addVariable(Variable.createVariable(NexacroConstants.ERROR.ERROR_MSG, sanitizedErrorMsg));

		// Reset response state before sending Nexacro data
		response.reset(); // Clears headers and buffer
		// Set content type for Nexacro response (assuming default XML)
		response.setContentType(super.getDefaultContentType()); // Use parent's default
		response.setCharacterEncoding(super.getDefaultCharset()); // Use parent's default

		sendResponse(request, response, platformData);
    }

	/**
	 * 경로 이동 문자 제거
	 * @param fileName 파일명
	 * @return 경로 이동 문자 제거후 리턴
	 */
	private String removedPathTraversal(String fileName) {
		 if(fileName == null) {
			 return null;
		 }
		 fileName = fileName.replace("/", "");
		 fileName = fileName.replace("\\", "");
		 // Add check for ".." sequence which is common in traversal
		 if (fileName.contains("..")) {
			 // Log or handle this potential attack attempt
			 logger.warn("Potential path traversal sequence '..' detected and removed from filename: {}", fileName);
			 fileName = fileName.replace("..", ""); // Simple replacement, might still be insufficient
		 }
		 return fileName;
	 }

	/**
	 * CRLF 주입 방지를 위한 '\\' , '\'' , '\"', \b, \t, \n, \f, \r , \v, ;, ]문자 제거
	 *
	 * @param value The string to sanitize.
	 * @return The sanitized string, or null if the input was null.
	 */
	public String sanitizeValue(String value) {
		if (value == null) {
			return null;
		}
		// 입력값 추가 필터링
		return (Encode.forJava(value)).replaceAll("[\\v;]", "");
	}
}
