package com.nexacro.uiadapter.spring.core.data.convert;

import com.nexacro.java.xapi.data.DataSet;

/**
 * <p>데이터 변환에 대한 정보를 저장하는 클래스입니다.
 * <p>DataSet 또는 Variable의 명칭 정보와, List 형식의 데이터 변환 시 Generic Type(타입 정보)을 포함할 수 있습니다.
 * 변환 과정에서 예외 무시 여부, 컬럼 구조 변경 허용 여부, Schema DataSet 지정 등 다양한 옵션을 제공합니다.
 *
 * <ul>
 *     <li>name: 변환 대상 DataSet/Variable의 명칭</li>
 *     <li>genericType: List 등 제네릭 컬렉션 데이터 변환 시의 타입 정보</li>
 *     <li>isIgnoreException: 변환 중 예외 발생 시 무시 여부</li>
 *     <li>schemaDataSet: 변환 시 참조할 DataSet 스키마 정보</li>
 *     <li>disallowChangeStructure: 컬럼/구조 변경 허용 여부</li>
 * </ul>
 *
 * @author Park SeongMin
 * @since 07.28.2015
 * @version 1.0
 */
public class ConvertDefinition {

    /** 변환 대상의 이름(DataSet 혹은 Variable) */
    private String name;

    /** List 변환 등, 제네릭 데이터 변환 시 타입 정보 */
    private Class genericType; // for generic

    /** 변환 수행 시 예외 무시 여부 */
    private boolean isIgnoreException = false;
    
    /** 변환 시 참조할 DataSet 스키마 정보 */
    private DataSet schemaDataSet;

    /** 변환 시 컬럼/구조 변경 허용 여부 (false: 허용, true: 비허용) */
    private boolean disallowChangeStructure;

    /** 기본 생성자 */
    public ConvertDefinition() {
        super();
    }

    /**
     * 이름 정보를 지정하는 생성자입니다.
     * 
     * @param name 변환 대상의 이름
     */
    public ConvertDefinition(String name) {
        setName(name);
    }

    /**
     * 변환 대상 이름을 반환합니다.
     * 
     * @return name(DataSet/Variable 명칭)
     */
    public String getName() {
        return name;
    }

    /**
     * 변환 대상 이름을 설정합니다.
     * 
     * @param name DataSet/Variable 명칭(필수)
     */
    public void setName(String name) {
        if(name == null || name.isEmpty()) {
            throw new IllegalArgumentException("converted name must be not null");
        }
        this.name = name;
    }

    /**
     * 변환 시 예외 무시 여부를 반환합니다.
     * 
     * @return 예외 무시 여부
     */
    public boolean isIgnoreException() {
        return isIgnoreException;
    }

    /**
     * 변환 시 예외 무시 여부를 설정합니다.
     * 
     * @param isIgnoreException true: 예외 무시, false: 예외 발생 시 throw
     */
    public void setIgnoreException(boolean isIgnoreException) {
        this.isIgnoreException = isIgnoreException;
    }

    /**
     * 제네릭 타입 정보를 반환합니다.
     * 
     * @return genericType(Class)
     */
    public Class getGenericType() {
        return genericType;
    }

    /**
     * 제네릭 타입 정보를 설정합니다.
     * 
     * @param genericType 변환 대상 타입 정보(List 변환 등)
     */
    public void setGenericType(Class genericType) {
        this.genericType = genericType;
    }

    /**
     * 구조 변경(컬럼 추가 등) 허용 여부를 반환합니다.
     * 
     * @return true: 구조 변경 불가, false: 구조 변경 허용
     */
    public boolean isDisallowChangeStructure() {
        return disallowChangeStructure;
    }

    /**
     * 구조 변경(컬럼 추가 등) 허용 여부를 설정합니다.
     * 
     * @param disallowChangeStructure true: 구조 변경 불가, false: 구조 변경 허용
     */
    public void setDisallowChangeStructure(boolean disallowChangeStructure) {
        this.disallowChangeStructure = disallowChangeStructure;
    }

    /**
     * 변환 시 참조할 스키마 DataSet을 반환합니다.
     * 
     * @return schemaDataSet(DataSet 정보)
     */
    public DataSet getSchemaDataSet() {
        return schemaDataSet;
    }

    /**
     * 변환 시 참조할 스키마 DataSet을 지정합니다.
     * 
     * @param schemaDataSet 스키마 DataSet 정보
     */
    public void setSchemaDataSet(DataSet schemaDataSet) {
        this.schemaDataSet = schemaDataSet;
    }

}