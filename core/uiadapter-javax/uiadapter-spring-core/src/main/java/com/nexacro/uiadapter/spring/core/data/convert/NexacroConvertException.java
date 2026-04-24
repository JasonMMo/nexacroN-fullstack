package com.nexacro.uiadapter.spring.core.data.convert;

import com.nexacro.uiadapter.spring.core.NexacroException;

/**
 * <p>{@code NexacroConverter}에서 데이터 변환 중 발생하는 예외를 나타내는 클래스입니다.
 * <br>
 * 넥사크로(Nexacro) 플랫폼 데이터 타입과 자바 객체간의 변환 처리 시 에러 발생 시 본 예외가 던져집니다.
 *
 * @author Park SeongMin
 * @since 2015. 7. 28.
 * @version 1.0
 * @see NexacroConverter
 */
public class NexacroConvertException extends NexacroException {

    /** 직렬화 버전 UID */
    private static final long serialVersionUID = 2572392591528637297L;

    /**
     * 기본 생성자.<br>
     * 별도의 메시지 없이 예외를 생성합니다.
     */
    public NexacroConvertException() {
    }

    /**
     * 상세 메시지를 포함하는 생성자입니다.
     *
     * @param message 예외에 대한 상세 설명 메시지
     */
    public NexacroConvertException(String message) {
        super(message);
    }

    /**
     * 상세 메시지와 원인 예외를 포함하는 생성자입니다.
     *
     * @param message 예외에 대한 상세 설명 메시지
     * @param cause   원인 예외(내부적으로 발생한 Throwable)
     */
    public NexacroConvertException(String message, Throwable cause) {
        super(message, cause);
    }

}