package com.nexacro.uiadapter.spring.core.data;

import java.io.File;

import com.nexacro.uiadapter.spring.core.resolve.NexacroHandlerMethodReturnValueHandler;
import com.nexacro.uiadapter.spring.core.view.NexacroFileView;

/**
 * <pre>
 * nexacro platform으로 여러 개의 파일을 하나의 ZIP으로 압축하여 다운로드할 때
 * 관련 정보를 보관, 전달하기 위한 클래스입니다.
 * </pre>
 * 
 * <p>
 * 이 클래스는 Zip 파일명, 압축 파일이 위치한 경로, 압축에 포함된 각 파일들의 목록 등의
 * 정보를 함께 관리하여, 멀티 파일 다운로드 요청에 적합하게 사용됩니다.<br>
 * <b>NexacroFileResult</b>를 상속하여 파일 전송 공통 로직을 그대로 활용하면서,
 * 다중 파일(ZIP) 처리에 필요한 추가 필드를 제공합니다.
 * </p>
 *
 * @author Mo
 * @since 2021.02.11
 * @version 1.0
 * @see NexacroFileView
 * @see NexacroHandlerMethodReturnValueHandler
 */
public class NexacroMultiFileResult extends NexacroFileResult {

    /** 클라이언트에게 전달될 ZIP 파일명 */
    String zipFileName;

    /** 서버에 존재하는 ZIP 파일의 실제 경로 */
    String filePath;

    /** 압축 파일에 포함된 파일명 목록(구분자 구체적 규칙은 적용 상황마다 다름) */
    String fileNameList;

    /**
     * NexacroMultiFileResult 인스턴스를 생성합니다.
     *
     * @param sZipFileName  최종 다운로드될 zip 파일명
     * @param sFilePath     zip 파일이 위치한 서버의 경로
     * @param sFileNameList 압축에 포함되는 파일명 목록(문자열, 구분자 포함)
     */
    public NexacroMultiFileResult(String sZipFileName, String sFilePath, String sFileNameList) {
        super(new File(sFilePath));
        this.zipFileName 	= sZipFileName;
        this.filePath 		= sFilePath;
        this.fileNameList 	= sFileNameList;
    }
    
    /**
     * ZIP 파일명을 반환합니다.
     * 
     * @return 클라이언트 다운로드용 ZIP 파일명
     */
    public String getZipFileName() {
        return zipFileName;
    }

    /**
     * ZIP 파일명을 설정합니다.
     *
     * @param sZipFileName 클라이언트에 전달될 ZIP 파일명
     */
    public void setZipFileName(String sZipFileName) {
        this.zipFileName = sZipFileName;
    }

    /**
     * ZIP 파일의 서버 내 실제 경로를 반환합니다.
     * 
     * @return ZIP 파일의 경로(서버 디렉터리)
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * ZIP 파일의 서버 내 실제 경로를 설정합니다.
     * 
     * @param sFilePath ZIP 파일 디렉터리 경로
     */
    public void setFilePath(String sFilePath) {
        this.filePath = sFilePath;
    }

    /**
     * 압축 파일에 포함된 개별 파일명 리스트를 반환합니다.
     * 
     * @return 파일명 목록 문자열(구분자 포함)
     */
    public String getFileNameList() {
        return fileNameList;
    }

    /**
     * 압축 파일에 포함된 개별 파일명 리스트를 설정합니다.
     * 
     * @param sFileNameList 파일명 목록(구분자 포함)
     */
    public void setFileNameList(String sFileNameList) {
        this.fileNameList = sFileNameList;
    }

}