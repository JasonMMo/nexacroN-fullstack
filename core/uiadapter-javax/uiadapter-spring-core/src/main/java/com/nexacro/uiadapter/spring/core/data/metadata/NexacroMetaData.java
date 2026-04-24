package com.nexacro.uiadapter.spring.core.data.metadata;

import java.util.ArrayList;

/**
 * <p>DataSet의 메타데이터 정보를 보관하는 추상 클래스입니다.
 * <br>
 * DataSet(혹은 유사 데이터)의 컬럼 구조, 타입 등 메타데이터를 담기 위해 사용됩니다.
 * <br>
 * 메타데이터 객체의 저장 및 조회 기능을 자식 클래스에서 구현하도록 강제합니다.
 *
 * @author Park SeongMin
 * @since 08.06.2015
 * @version 1.0
 */
public abstract class NexacroMetaData extends ArrayList {

    /**
     * 메타데이터 객체를 설정합니다.<br>
     * 
     * @param obj 메타데이터로 저장할 객체
     */
    public abstract void setMetaData(Object obj);
    
    /**
     * 저장된 메타데이터 객체를 반환합니다.
     * 
     * @return 저장된 메타데이터 객체
     */
    public abstract Object getMetaData();
    
}