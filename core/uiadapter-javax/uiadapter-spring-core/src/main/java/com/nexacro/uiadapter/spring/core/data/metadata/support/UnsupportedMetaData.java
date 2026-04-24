package com.nexacro.uiadapter.spring.core.data.metadata.support;

import com.nexacro.uiadapter.spring.core.data.metadata.NexacroMetaData;

/**
 * <p>{@code NexacroMetaData}의 구현체로, 실제 메타데이터 정보가 존재하지 않는 경우를 표현하는 클래스입니다.
 * <br>
 * 넥사크로 연동 과정에서 메타데이터가 필요 없거나 지원되지 않는 구조(예: 단순 값, 파라미터 등)일 때 사용됩니다.
 *
 * @author Park SeongMin
 * @since 2015. 8. 6.
 * @version 1.0
 */
public class UnsupportedMetaData extends NexacroMetaData {

    /** 지원 불가 사유 및 안내 메시지 */
    final String message;
    
    /**
     * 지원하지 않는 메타데이터의 정보를 메시지로 지정하는 생성자입니다.
     *
     * @param message 메타데이터 미지원에 대한 설명 메시지
     */
    public UnsupportedMetaData(String message) {
        this.message = message;
    }
    
    /**
     * 메타데이터 설정은 지원하지 않으므로 아무 동작도 하지 않습니다.
     *
     * @param obj 무시됨(사용하지 않음)
     */
    @Override
    public void setMetaData(Object obj) {
        // 메타데이터 지원 불가 상황에서는 예외를 던질 수 있으나, 현재는 무시하도록 구현되어 있습니다.
        // throw new UnsupportedOperationException(this.message);
    }

    /**
     * 메타데이터가 없음을 알리는 메시지를 반환합니다.
     *
     * @return 메타데이터 미지원 메시지
     */
    @Override
    public Object getMetaData() {
        // 보통은 예외를 발생시킬 수 있으나, 현재는 메시지 반환 방식입니다.
        // throw new UnsupportedOperationException(this.message);
        return message;
    }

}