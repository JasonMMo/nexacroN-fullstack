package com.nexacro.uiadapter.spring.core.data;

import com.nexacro.java.xapi.tx.HttpPartPlatformResponse;
import com.nexacro.java.xapi.tx.PlatformException;

/**
 * <p>
 * {@link NexacroFirstRowHandler}에 대한 다양한 접근 메서드를 제공하는 추상 클래스입니다.
 * </p>
 * <p>
 * 본 클래스는 Nexacro 플랫폼 환경에서 응답 데이터의 일부/첫 번째 row 송수신을 지원하는
 * {@link NexacroFirstRowHandler}의 주요 기능을 static 메서드로 노출하여,
 * 외부에서 handler 객체를 통해 필요한 정보(응답 객체, 변수명, 데이터셋명 등)를 손쉽게 획득할 수 있도록 설계되었습니다.
 * </p>
 *
 * <p>주요 사용 예시:
 * <pre>
 * HttpPartPlatformResponse response = NexacroFirstRowAccessor.getHttpPartPlatformResponse(handler);
 * NexacroFirstRowAccessor.end(handler);
 * String[] sendVars = NexacroFirstRowAccessor.getSendOutVariableNames(handler);
 * </pre>
 *
 * @see NexacroFirstRowHandler
 */
public abstract class NexacroFirstRowAccessor {

    /**
     * 지정된 handler로부터 {@link HttpPartPlatformResponse} 객체를 반환합니다.
     *
     * @param handler 응답 핸들러
     * @return 플랫폼 응답 객체
     */
    public static HttpPartPlatformResponse getHttpPartPlatformResponse(NexacroFirstRowHandler handler) {
        return handler.getHttpPartPlatformResponse();
    }

    /**
     * 지정된 handler의 응답 처리를 종료합니다.
     *
     * @param handler 응답 핸들러
     * @throws PlatformException 처리 도중 예외 발생 시
     */
    public static void end(NexacroFirstRowHandler handler) throws PlatformException {
        handler.end();
    }

    /**
     * 지정된 handler로부터 송신할 변수 이름 목록을 반환합니다.
     *
     * @param handler 응답 핸들러
     * @return 송신될 변수 이름 배열
     */
    public static String[] getSendOutVariableNames(NexacroFirstRowHandler handler) {
        return handler.getSendOutVariableNames();
    }

    /**
     * 지정된 handler로부터 송신할 변수의 개수를 반환합니다.
     *
     * @param handler 응답 핸들러
     * @return 송신 변수 개수
     */
    public static int getSendOutVariableCount(NexacroFirstRowHandler handler) {
        return handler.getSendOutVariableCount();
    }

    /**
     * 지정된 handler로부터 송신할 데이터셋 이름 목록을 반환합니다.
     *
     * @param handler 응답 핸들러
     * @return 송신될 데이터셋 이름 배열
     */
    public static String[] getSendOutDataSetNames(NexacroFirstRowHandler handler) {
        return handler.getSendOutDataSetNames();
    }

    /**
     * 지정된 handler로부터 송신할 데이터셋의 개수를 반환합니다.
     *
     * @param handler 응답 핸들러
     * @return 송신될 데이터셋 개수
     */
    public static int getSendOutDataSetCount(NexacroFirstRowHandler handler) {
        return handler.getSendOutDataSetCount();
    }

}