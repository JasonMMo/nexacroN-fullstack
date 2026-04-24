package com.nexacro.uiadapter.spring.core.data.convert;

import com.nexacro.java.xapi.data.Variable;

/**
 * <p>Variable 데이터의 변환 시 처리되는 이벤트 객체입니다.
 * <p>
 * {@link ConvertEvent}를 상속하여 Variable에 대한 변환 작업이 이루어질 때 활용됩니다.
 * 이벤트 리스너를 통해 변환 중인 값을 참조하거나 가공할 수 있습니다.
 *
 * @author Park SeongMin
 * @since 08.09.2015
 * @version 1.0
 */
public class ConvertVariableEvent extends ConvertEvent {

    /** 직렬화 버전 UID */
    private static final long serialVersionUID = 7527494468054562758L;
    
    /**
     * Variable 변환 이벤트 객체를 생성합니다.
     *
     * @param source      이벤트가 발생한 Variable 객체
     * @param targetValue 현재 변환 중인 값
     */
    public ConvertVariableEvent(Variable source, Object targetValue) {
        super(source, targetValue);
    }

}