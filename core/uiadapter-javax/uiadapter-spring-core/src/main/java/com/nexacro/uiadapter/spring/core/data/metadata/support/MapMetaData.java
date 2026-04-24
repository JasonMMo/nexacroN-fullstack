package com.nexacro.uiadapter.spring.core.data.metadata.support;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import com.nexacro.uiadapter.spring.core.data.metadata.NexacroMetaData;

/**
 * <p>{@code NexacroMetaData}의 구현체로, Map 형태의 메타데이터(키-값 쌍 형식 설정 정보)를 보관하는 클래스입니다.
 * <br>
 * DataSet 또는 유사 객체의 결과 타입이 Map일 때, 컬럼 구조 등의 메타정보를 이 클래스를 통해 저장·관리합니다.
 * 
 * @author Park SeongMin
 * @since 08.06.2015
 * @version 1.0
 */
public class MapMetaData extends NexacroMetaData {

    /** 메타데이터로 저장할 Map 객체(키: 컬럼명, 값: 각 컬럼의 값 또는 예시값) */
    private Map<String, Object> metaDataMap;

    /** (현재는 미사용) 컬럼명을 대문자로 반환할지 여부 플래그 */
    private final boolean isUpperCase = false;

    /**
     * Map 객체를 전달받아 메타데이터로 저장하는 생성자입니다.
     *
     * @param mapData 메타데이터로 사용할 Map 객체
     */
    public MapMetaData(Map<String, Object> mapData) {
        setMetaData(mapData);
    }

    /**
     * 메타데이터로 사용할 Map 객체를 저장합니다.
     * 
     * @param metaDataMap 저장할 메타데이터 Map
     */
    @Override
    public void setMetaData(Object metaDataMap) {
        this.metaDataMap = (Map) metaDataMap;
        // 2025.03.26~2025.04.29의 처리 이력(가비지 데이터 방지, add() 처리 관련) 참고:
        // 기존에 List<Map> 요청 후 재조회 시 불필요한 데이터가 추가되는 현상 방지 목적이었습니다.
        // 현재는 주석 처리(불필요 데이터 방지 로직은 add(), setNullMetaData() 등에서 관리).
    }

    /**
     * 2024.03.26<br>
     * metaDataMap의 값을 null로 초기화하여 빈 Map 형태로 만듭니다.
     * (각 컬럼의 값을 모두 null로)
     */
    public void setNullMetaData() {
        HashMap<String, Object> nullMap = new HashMap<String, Object>();
        this.metaDataMap.forEach((key, value)->{
            nullMap.put(key, null);
        } );
        metaDataMap = nullMap;
    }

    /**
     * 2024.05.22<br>
     * metaDataMap의 모든 컬럼명을 대문자로 변환하여 저장합니다.
     * (키 이름만 upperCase로 바꾼 새 Map에 복사)
     */
    public void setMetaDataKeyToUpperCase() {
        HashMap<String, Object> upperCaseMap = new HashMap<String, Object>(); 
        this.metaDataMap.forEach((key, value) -> upperCaseMap.put(StringUtils.upperCase(key), value));
        metaDataMap = upperCaseMap;
    }

    /**
     * 저장된 메타데이터(Map 형태)를 반환합니다.
     *
     * @return 저장된 metaDataMap
     */
    @Override
    public Object getMetaData() {
        return this.metaDataMap;
    }

    /**
     * 2024.05.22<br>
     * 메타데이터 Map을 컬럼명을 대문자로 변환하여 반환합니다.
     *
     * @param isUpperCase 대문자 변환 여부(강제 대문자 반환, true일 때 변환)
     * @return 컬럼명이 대문자인 metaDataMap
     */
//    public Map<String, Object> getMetaData(boolean isUpperCase) {
//        setMetaDataKeyToUpperCase();
//        return this.metaDataMap;
//    }

    /**
     * 현재 저장된 metaDataMap 객체의 내용을 문자열로 반환합니다.
     */
    @Override
    public String toString() {
        return "MapMetaData [metaDataMap=" + metaDataMap + "]";
    }
}