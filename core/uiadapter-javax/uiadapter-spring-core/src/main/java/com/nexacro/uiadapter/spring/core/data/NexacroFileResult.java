package com.nexacro.uiadapter.spring.core.data;

import java.io.File;

import com.nexacro.uiadapter.spring.core.resolve.NexacroHandlerMethodReturnValueHandler;
import com.nexacro.uiadapter.spring.core.view.NexacroFileView;

/**
 * <pre>
 * nexacro platform에서 파일 데이터를 전송하기 위한 정보를 보관하는 클래스입니다.
 * </pre>
 * <p>
 * 이 클래스는 nexacro 클라이언트로 파일을 전송할 때 파일 객체와 MIME 타입, 문자셋, 원본 파일명 등의
 * 부가 정보를 함께 관리할 수 있도록 도와줍니다.
 * </p>
 *
 * <p>주요 사용 예시:
 * <pre>
 * NexacroFileResult result = new NexacroFileResult(new File("sample.txt"));
 * result.setContentType("text/plain");
 * result.setCharset("UTF-8");
 * result.setOriginalName("원본파일명.txt");
 * </pre>
 *
 * @see NexacroFileView
 * @see NexacroHandlerMethodReturnValueHandler
 */
public class NexacroFileResult {

    /** 전송할 파일 객체 */
    private File file;

    /** 응답 Content-Type (예: "application/pdf") */
    private String contentType;

    /** 파일 응답 시 사용할 문자셋 (예: "UTF-8") */
    private String charset;

    /** 실제 파일의 원본 파일명 */
    private String originalName;

    /**
     * 파일 객체를 지정하며 NexacroFileResult를 생성합니다.
     * 
     * @param file 전송할 파일(필수, null이면 예외 발생)
     * @throws IllegalArgumentException 파일 객체가 null일 경우 발생
     */
    public NexacroFileResult(File file) {
        if(file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        this.file = file;
    }
    
    /**
     * 파일 객체를 반환합니다.
     *
     * @return 전송할 파일 객체
     */
    public File getFile() {
        return file;
    }

    /**
     * 파일 객체를 설정합니다.
     *
     * @param file 전송할 파일 객체
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Content-Type(MIME 타입)을 반환합니다.
     *
     * @return 응답 Content-Type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Content-Type(MIME 타입)을 설정합니다.
     *
     * @param contentType 응답 Content-Type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * 문자셋(charset)을 반환합니다.
     *
     * @return 응답 문자셋
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 문자셋(charset)을 설정합니다.
     *
     * @param charset 응답 문자셋
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * 원본 파일명을 반환합니다.
     *
     * @return 실제 파일의 원본 파일명
     */
    public String getOriginalName() {
        return originalName;
    }

    /**
     * 원본 파일명을 설정합니다.
     *
     * @param originalName 실제 파일의 원본 파일명
     */
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

}