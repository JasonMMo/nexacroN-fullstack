package com.nexacro.uiadapter.spring.core.data.metadata.support;

import com.nexacro.uiadapter.spring.core.data.metadata.NexacroMetaData;
import com.nexacro.uiadapter.spring.core.data.support.NexacroBeanWrapper;

/**
 * <p>{@code NexacroMetaData}의 구현체로서, Java Bean의 메타데이터(구조, 타입 등 설정 정보)를 보관하는 클래스입니다.
 * <br>
 * 주로 DataSet 등 넥사크로 연동 객체의 결과 타입 정보(Bean 클래스 정보)를 저장하고 활용합니다.
 *
 * @author Park SeongMin
 * @since 08.06.2015
 * @version 1.0
 */
public class BeanMetaData extends NexacroMetaData {

    /** 실제로 보관할 메타데이터 객체(Java Bean 인스턴스 또는 BeanWrapper) */
    private Object metaDataObject;
    
    /**
     * Bean 객체 자체를 전달받아 메타데이터로 저장하는 생성자입니다.
     *
     * @param obj 메타데이터로 사용할 Bean 객체
     */
    public BeanMetaData(Object obj) {
        setMetaData(obj);
    }
    
    /**
     * Bean 타입(Class)을 전달받아 해당 타입의 빈 인스턴스를 생성해 메타데이터로 저장하는 생성자입니다.
     *
     * @param genericType Bean 클래스 타입
     */
    public BeanMetaData(Class<?> genericType) {
        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(genericType);
        Object instance = beanWrapper.getInstance();
        setMetaData(instance);
    }

    /**
     * 2025.04.24<br>
     * resultType=vo 이고 조회 결과가 0건일 때, BeanMetaData를 생성하면서 리스트에 vo 객체를 1건 추가하는 주요 용도의 생성자입니다.
     *
     * @param genericType 생성할 Bean 타입(Class)
     * @param addMetaData MetaData 추가여부(값이 null이 아니면 list에 vo 객체 1건 추가)
     */
    public BeanMetaData(Class<?> genericType, String addMetaData) {
        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(genericType);
        Object instance = beanWrapper.getInstance();
        setMetaData(instance);
        if (addMetaData != null) {
            add(instance);
        }
    }

    /**
     * 메타데이터 객체를 설정합니다.<br>
     * 전달받은 obj가 NexacroBeanWrapper가 아니면, BeanWrapper로 래핑 후 실제 인스턴스를 저장합니다.
     *
     * @param obj 메타데이터(Bean 인스턴스 또는 BeanWrapper)
     */
    @Override
    public void setMetaData(Object obj) {
        if(!(obj instanceof NexacroBeanWrapper)) {
            NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(obj);
            this.metaDataObject = beanWrapper.getInstance();
        } else {
            this.metaDataObject = obj;
        }
    }

    /**
     * 저장되어 있는 메타데이터 객체를 반환합니다.
     *
     * @return 메타데이터 객체(Bean)
     */
    @Override
    public Object getMetaData() {
        return this.metaDataObject;
    }

    /**
     * 현재 메타데이터에 저장된 Bean 클래스 정보를 문자열로 반환합니다.
     */
    @Override
    public String toString() {
        return "BeanMetaData [class=" + this.metaDataObject.getClass() + "]";
    }

}