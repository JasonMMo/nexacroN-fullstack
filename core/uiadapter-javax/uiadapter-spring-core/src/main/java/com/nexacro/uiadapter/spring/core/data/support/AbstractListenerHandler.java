package com.nexacro.uiadapter.spring.core.data.support;

import java.time.LocalDateTime;

import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertDataSetEvent;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertVariableEvent;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertListener;
import com.nexacro.uiadapter.spring.core.util.DateUtils;

/**
 * NexacroConverterListener를 처리하기 위한 추상클래스입니다.
 * <br>
 * 이벤트 리스너 관리 및 데이터 변환 이벤트 후처리, 데이터 타입 변환 등 
 * Nexacro 데이터 컨버터에서 공통적으로 필요한 리스너 관련 편의 기능을 제공합니다.
 *
 * @author Park SeongMin
 * @since 08.09.2015
 * @version 1.0
 */
public abstract class AbstractListenerHandler {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /** 이벤트 리스너를 관리하는 리스트 */
    private EventListenerList listenerList;
    
    /**
     * <code>NexacroConvertListener</code>를 등록합니다.
     * <br>
     * 리스너 리스트가 아직 초기화되지 않은 경우 즉시 생성합니다.
     * 디버그 로깅 지원.
     *
     * @param listener 추가할 NexacroConvertListener 구현체
     * @see #removeListener(NexacroConvertListener)
     */
    public void addListener(NexacroConvertListener listener) {
        if (listenerList == null) {
            listenerList = new EventListenerList();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("addListener={}", listener);
        }
        
        listenerList.add(NexacroConvertListener.class, listener);
    }
    
    /**
     * <code>NexacroConvertListener</code>를 제거합니다.
     * <br>
     * 리스너가 등록되어 있지 않거나 리스트가 비어 있으면 아무 작업도 수행하지 않습니다.
     * 디버그 로깅 지원.
     *
     * @param listener 제거할 NexacroConvertListener 구현체
     * @see #addListener(NexacroConvertListener)
     */
    public void removeListener(NexacroConvertListener listener) {
        if (listenerList == null || listenerList.getListenerCount() == 0) {
            int listenerCount = (listenerList == null) ? -1 : listenerList.getListenerCount();

            if (logger.isDebugEnabled()) {
                logger.debug("removeListener: listenerCount={}, listener={}", listenerCount, listener);
            }

            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("removeListener={}", listener);
        }

        listenerList.remove(NexacroConvertListener.class, listener);
    }

    /**
     * DataTypeFactory에서 처리하지 못하는 일부 데이터 타입을 변환합니다.
     * <br>
     * - LocalDateTime → java.util.Date
     * - (확장 가능: 필요 시 BigInteger→Long 등)
     * <br>
     * 데이터 변환 전 타입 일관성을 보장하기 위해 사용되며, 
     * 변환 시 로깅이 수행됩니다.
     *
     * @param targetValue 변환 대상 객체
     * @return 변환된 객체(타입이 변환된 경우), 변환 불필요 시 원본 반환
     */
    Object castExtraDataType(Object targetValue) {
    	// mySql의 NUM, RNUM -> java.math.BigInteger
    	// mySql의 DATETIE 	-> java.time.LocalDateTime
        if(targetValue instanceof LocalDateTime) {
            logger.debug("\t\t[castExtraDataType(math.LocalDateTime->util.Date)] {}", targetValue);
            return DateUtils.toDate((LocalDateTime)targetValue);
        } else {
            return targetValue;
        }
    }

    /**
     * DataSet 값 변환이 발생한 후 리스너들을 호출하여 후처리를 수행합니다.
     * <br>
     * 등록된 NexacroConvertListener 목록 전체에 이벤트를 게시하며,
     * 각 리스너는 ConvertDataSetEvent 이벤트에서 값을 후처리 할 수 있습니다.
     * <br>
     * 모든 리스너의 처리 결과 후, 이벤트 객체 내부의 현재 값을 반환합니다.
     *
     * @param ds DataSet 객체
     * @param targetValue 변경/변환 대상 값
     * @param rowIndex 행 인덱스
     * @param columnIndex 컬럼 인덱스
     * @param isSavedData 저장된 데이터 여부
     * @param isRemovedData 삭제된 데이터 여부
     * @return (리스너 후처리된) 최종 값
     */
    public Object fireDataSetConvertedValue(
            DataSet ds, 
            Object targetValue, 
            int rowIndex, 
            int columnIndex, 
            boolean isSavedData, 
            boolean isRemovedData
    ) {
        if (listenerList == null || listenerList.getListenerCount() == 0) {
            return targetValue;
        }
        ConvertDataSetEvent event = new ConvertDataSetEvent(ds, targetValue, rowIndex, columnIndex, isSavedData, isRemovedData);
        Object[] listeners = listenerList.getListenerList();
        
        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == NexacroConvertListener.class) {
                ((NexacroConvertListener) listeners[i+1]).convertedValue(event);
            }
        }
        return event.getValue();
    }

    /**
     * Variable 값 변환 후 리스너를 호출하여 후처리를 수행합니다.
     * <br>
     * DataSet 처리와 구조는 동일하며, Variable용 ConvertVariableEvent를 생성합니다.
     * 
     * @param var Variable 객체
     * @param targetValue 변환 대상 값
     * @return (리스너 후처리된) 최종 값
     */
    public Object fireVariableConvertedValue(Variable var, Object targetValue) {
        if (listenerList == null || listenerList.getListenerCount() == 0) {
            return targetValue;
        }
        
        ConvertVariableEvent event = new ConvertVariableEvent(var, targetValue);
        Object[] listeners = listenerList.getListenerList();

        for (int i = 0; i < listeners.length; i += 2) {
            if (listeners[i] == NexacroConvertListener.class) {
                ((NexacroConvertListener) listeners[i+1]).convertedValue(event);
            }
        }
        
        return event.getValue();
    }

}