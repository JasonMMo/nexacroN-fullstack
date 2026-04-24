package com.nexacro.uiadapter.spring.core.data.convert;

import java.util.EventObject;

/**
 * <p>데이터 변환 과정에서 발생하는 이벤트 객체입니다.
 * <p>
 * {@link NexacroConverter#convert(Object, ConvertDefinition)}에서 데이터 변환 시, 
 * <code>DataSet</code>의 행값 또는 <code>Variable</code>의 값을 변환할 때 이 이벤트가 발생합니다.
 * 변환 중 값에 대한 변경·참조가 필요할 경우 활용할 수 있습니다.
 * <br>
 * 주로 변환 리스너에서 값의 검증, 가공, 변경 등이 필요할 때 사용됩니다.
 *
 * @author Park SeongMin
 * @since 08.09.2015
 * @version 1.0
 * @see NexacroConverter
 * @see NexacroConvertListener
 */
public class ConvertEvent extends EventObject {

    /** 직렬화 버전 UID */
    private static final long serialVersionUID = -8987092428557969722L;

    /** 변환되는 값 */
    private Object value;

    /**
     * 지정된 소스와 변환 값으로 새 ConvertEvent를 생성합니다.
     *
     * @param source 이벤트가 최초 발생한 원본 객체(일반적으로 DataSet/Variable)
     * @param value  현재 변환 중인 값
     */
    public ConvertEvent(Object source, Object value) {
        super(source);
        this.value = value;
    }

    /**
     * 현재 변환 중인 값을 반환합니다.
     * <br>
     * 변환 과정에서 현재 처리되고 있는 값을 얻을 수 있습니다.
     * 
     * @return 변환 중인 값
     */
    public Object getValue() {
        return value;
    }

    /**
     * 변환 중인 값을 설정합니다.
     * <br>
     * 리스너 등에서 이 메서드를 통해 변환 값을 가공·수정할 수 있습니다.
     * 
     * @param value 새로 설정할 변환 값
     */
    public void setValue(Object value) {
        this.value = value;
    }

}