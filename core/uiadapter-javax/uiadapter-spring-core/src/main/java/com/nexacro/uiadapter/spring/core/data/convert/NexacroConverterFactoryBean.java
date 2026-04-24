package com.nexacro.uiadapter.spring.core.data.convert;

import java.util.Set;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>Spring 환경에서 {@code FactoryBean}으로 등록할 수 있는 Nexacro Converter Factory 빈 클래스입니다.
 * <br>
 * Spring DI(Dependency Injection)를 통해 NexacroConverter 인스턴스 세트를 주입받아 Factory에 등록하는 역할을 수행합니다.
 *
 * @author Park SeongMin
 * @since 08.09.2015
 * @version 1.0
 */
public class NexacroConverterFactoryBean implements FactoryBean<NexacroConverterFactory>, InitializingBean {

    /** 등록할 NexacroConverter들의 집합 */
    private Set<NexacroConverter> converters;

    /** 실제로 반환될 팩토리 객체(싱글톤) */
    private NexacroConverterFactory converterFactory;

    /**
     * Converter 세트를 주입받는 Setter 입니다.
     * <br>
     * Spring xml 혹은 @Configuration 등에서 Converter bean 집합을 주입할 때 사용됩니다.
     *
     * @param converters 등록 대상 Converter Set
     */
    public void setConverters(Set<NexacroConverter> converters) {
        this.converters = converters;
    }

    /**
     * Bean 초기화 시점에서 호출되는 메서드입니다.  
     * Factory 단일 인스턴스 획득 후, 등록받은 converter 집합을 Factory에 등록합니다.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        this.converterFactory = NexacroConverterFactory.getInstance();
        NexacroConverterFactory.register(converters);
    }

    /**
     * {@code FactoryBean} 인터페이스 구현 - 실제 반환 프로덕트 Bean을 리턴합니다.
     *
     * @return NexacroConverterFactory 싱글톤 인스턴스
     */
    @Override
    public NexacroConverterFactory getObject() throws Exception {
        return converterFactory;
    }

    /**
     * {@code FactoryBean} 인터페이스 구현 - 반환 대상 타입을 제공합니다.
     *
     * @return 반환 객체의 타입 (NexacroConverterFactory)
     */
    @Override
    public Class<NexacroConverterFactory> getObjectType() {
        return NexacroConverterFactory.class;
    }
}