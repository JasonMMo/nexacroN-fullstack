package com.nexacro.uiadapter.spring.core.data.support;

import java.lang.reflect.Method;

/**
 * <p>bean의 멤버 필드(프로퍼티)에 대한 정보를 보관하는 클래스입니다.
 * <br>
 * - 프로퍼티명, 타입, static 여부, 원래 프로퍼티명, writeMethod 등 리플렉션 및 데이터 바인딩에 필요한 정보를 저장합니다.
 *
 * @author Park SeongMin
 * @since 08.10.2015
 * @version 1.0
 */
public class NexacroBeanProperty {

    /** 프로퍼티명 */
    private final String propertyName;

    /** 프로퍼티 타입 */
    private final Class<?> propertyType;

    /** static 필드 여부 */
    private boolean isStatic;

    /** 원본 프로퍼티명(필요 시) */
    private String originalPropertyName;

    // 성능 향상을 위해 PropertyDescriptor 대신 writeMethod만 보관
    // PropertyDescriptor의 getWriteMethod()는 synchronized이므로 writeMethod 참조만 따로 관리
    /** setter Method */
    private Method writeMethod;

    /**
     * 생성자
     * @param propertyName 프로퍼티명
     * @param propertyType 프로퍼티 타입
     */
    public NexacroBeanProperty(String propertyName, Class<?> propertyType) {
        this.propertyName = propertyName;
        this.propertyType = propertyType;
    }

    /**
     * 프로퍼티명을 반환합니다.
     * @return 프로퍼티명
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 프로퍼티 타입을 반환합니다.
     * @return 프로퍼티 타입
     */
    public Class<?> getPropertyType() {
        return propertyType;
    }

    /**
     * static 필드로 표시합니다.
     * (패키지 전용)
     */
    void setStatic() {
        this.isStatic = true;
    }

    /**
     * static 필드 여부를 반환합니다.
     * @return static 여부
     */
    public boolean isStatic() {
        return isStatic;
    }

    /**
     * 원본 프로퍼티명을 반환합니다.
     * (패키지 전용)
     * @return 원본 프로퍼티명
     */
    String getOriginalPropertyName() {
        return originalPropertyName;
    }

    /**
     * 원본 프로퍼티명을 지정합니다.
     * (패키지 전용)
     * @param originalPropertyName 원본 프로퍼티명
     */
    void setOriginalPropertyName(String originalPropertyName) {
        this.originalPropertyName = originalPropertyName;
    }

    /**
     * setter Method 정보를 반환합니다.
     * (패키지 전용)
     * @return writeMethod(Method)
     */
    Method getWriteMethod() {
        return writeMethod;
    }

    /**
     * setter Method 정보를 지정합니다.
     * (패키지 전용)
     * @param writeMethod 메서드 객체
     */
    void setWriteMethod(Method writeMethod) {
        this.writeMethod = writeMethod;
    }

}