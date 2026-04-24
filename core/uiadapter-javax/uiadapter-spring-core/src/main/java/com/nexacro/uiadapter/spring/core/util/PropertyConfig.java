package com.nexacro.uiadapter.spring.core.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySources({
    @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:nexacro.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:xeni.properties", ignoreResourceNotFound = true)
})
public class PropertyConfig {

    /**
     * 처리할 프로퍼티 파일명 목록
     * @PropertySources에 정의된 파일명과 일치해야 함
     */
    private static final List<String> ALLOWED_PROPERTY_FILES = Arrays.asList(
        "application.properties",
        "nexacro.properties",
        "xeni.properties"
    );

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public EtcPropertiesBase etcPropertiesBase() {
        EtcPropertiesBase etcProperties = new EtcPropertiesBase();
    
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment configEnv = (ConfigurableEnvironment) environment;
            configEnv.getPropertySources().forEach(source -> {
                // 허용된 프로퍼티 파일에서 로드된 PropertySource만 처리
                if (source instanceof EnumerablePropertySource && isAllowedPropertySource(source.getName())) {
                    EnumerablePropertySource<?> enumerableSource = (EnumerablePropertySource<?>) source;
                    for (String propName : enumerableSource.getPropertyNames()) {
                        Object propValue = enumerableSource.getProperty(propName);
                        if (propValue != null) {
                            etcProperties.addEtcProperty(propName, propValue.toString());
                        }
                    }
                }
            });
        }
    
        // dispatcher-servlet.xml 등에 정의된 EtcProperty 빈에서 프로퍼티 로드
        try {
            if (applicationContext != null && applicationContext.containsBean("EtcProperty")) {
                Object bean = applicationContext.getBean("EtcProperty");
                if (bean instanceof java.util.Properties) {
                    java.util.Properties props = (java.util.Properties) bean;
                    for (String propName : props.stringPropertyNames()) {
                        String propValue = props.getProperty(propName);
                        if (propValue != null) {
                            etcProperties.addEtcProperty(propName, propValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 로드 실패 시 무시
        }

        return etcProperties;
    }

    /**
     * 주어진 PropertySource 이름이 허용된 프로퍼티 파일인지 확인
     *
     * @param sourceName PropertySource 이름
     * @return 허용된 파일이면 true, 아니면 false
     */
    private boolean isAllowedPropertySource(String sourceName) {
        if (sourceName == null) {
            return false;
        }
        
        for (String allowedFile : ALLOWED_PROPERTY_FILES) {
            if (sourceName.contains(allowedFile)) {
                return true;
            }
        }
        return false;
    }
}