package com.nexacro.uiadapter.spring.core.util;

import com.nexacro.uiadapter.spring.core.context.SpringAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Environment를 이용한 Properties 관리 클래스
 * EtcPropertiesBase를 대체하여 Spring의 Environment에서 설정값을 읽어옴
 * Usage:
 * 1. 직접 SpringEnvironmentPropertiesProvider 사용
 *   PropertiesProvider propertiesProvider = PropertiesProvider.getInstance();
 *   String bufferSize = propertiesProvider.getEtcProperty("nexacro.stream.buffer-size");
 *
 * 2. Etc 클래스의 통합 메서드 사용
 *   String charset = Etc.getProperty("nexacro.use-request-charset", "UTF-8");
 *
 * 3. 기존 패턴과 동일하게 사용 (자동 fallback)
 *   EtcPropertiesBase와 동일한 인터페이스로 사용 가능
 *
 * @author ai
 * @since 2025.05.29
 * @version 1.0
 */

public class PropertiesProvider {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesProvider.class);

    private static PropertiesProvider instance;
    private static final Object lock = new Object();

    // 캐시된 프로퍼티들
    private final Map<String, String> cachedProperties = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    private volatile boolean etcPropertyLoaded = false;

    // EtcPropertiesBase 호환성을 위한 리스트
    private final List<Map<String, String>> etcList = new ArrayList<>();

    private PropertiesProvider() {
        // private 생성자
        initialize();
    }

    /**
     * 싱글톤 인스턴스 반환
     */
    public static PropertiesProvider getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new PropertiesProvider();
                }
            }
        }
        return instance;
    }

    /**
     * Spring Environment 초기화
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        synchronized (lock) {
            if (!initialized) {
                try {
                    ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
                    if (applicationContext != null) {
                        Environment environment = applicationContext.getEnvironment();
                        loadPropertiesFromEnvironment(environment);
                        loadPropertiesFromEtcPropertyBean(applicationContext);
                        initialized = true;
                        logger.info("PropertiesProvider initialized successfully");
                    } else {
                        logger.warn("ApplicationContext not available, using fallback properties");
                    }
                } catch (Exception e) {
                    logger.error("Failed to initialize PropertiesProvider", e);
                }
            }
        }
    }

    /**
     * Environment에서 프로퍼티들 로드
     */
    private void loadPropertiesFromEnvironment(Environment environment) {
        // Nexacro 관련 프로퍼티들 로드
        loadNexacroProperties(environment);

        // UI Adapter 관련 프로퍼티들 로드
        loadUiAdapterProperties(environment);

        // Stream 관련 프로퍼티들 로드
        loadStreamProperties(environment);

        logger.debug("Loaded {} properties from Spring Environment", cachedProperties.size());
    }

    /**
     * Nexacro 관련 프로퍼티 로드
     */
    private void loadNexacroProperties(Environment environment) {
        String[] nexacroKeys = {
                "nexacro.client-column-case",
                "nexacro.db-column-case",
                "nexacro.use-request-charset",
                "nexacro.use-request-contenttype",
                "nexacro.trim-paramdataset",
                "nexacro.trim-paramvariable",
                "nexacro.replace-all-empty-variable"
        };

        for (String key : nexacroKeys) {
            String value = environment.getProperty(key);
            if (value != null) {
                cacheProperty(key, value);
                addToEtcList(key, value);
            }
        }
    }

    /**
     * UI Adapter 관련 프로퍼티 로드 (기존 호환성)
     */
    private void loadUiAdapterProperties(Environment environment) {
        String[] uiAdapterKeys = {
                "uiAdapter.useRequestCharset",
                "uiAdapter.useRequestContentType",
                "uiAdapter.useRequestCompressType",
                "uiAdapter.trimParamDataSet",
                "uiAdapter.trimParamVariable",
                "uiAdapter.replaceAllEmptyVariable"
        };

        for (String key : uiAdapterKeys) {
            String value = environment.getProperty(key);
            if (value != null) {
                cacheProperty(key, value);
                addToEtcList(key, value);
            }
        }
    }

    /**
     * Stream 관련 프로퍼티 로드
     */
    public void loadStreamProperties(Environment environment) {
        String[] streamKeys = {
                "nexacro.stream.buffer-size",
                "nexacro.stream.use-nio",
                "nexacro.stream.adaptive-buffer",
                "nexacro.stream.enable-security-headers",
                "nexacro.stream.max-file-size",
                "nexacro.stream.allowed-extensions"
        };

        for (String key : streamKeys) {
            String value = environment.getProperty(key);
            if (value != null) {
                cacheProperty(key, value);
                addToEtcList(key, value);
            }
        }
    }

    /**
     * dispatcher-servlet.xml 등에 정의된 EtcProperty bean에서 프로퍼티 로드
     */
    private void loadPropertiesFromEtcPropertyBean(ApplicationContext applicationContext) {
        if (etcPropertyLoaded) {
            return;
        }

        try {
            if (applicationContext.containsBean("EtcProperty")) {
                Object bean = applicationContext.getBean("EtcProperty");
                if (bean instanceof java.util.Properties) {
                    java.util.Properties props = (java.util.Properties) bean;
                    for (String key : props.stringPropertyNames()) {
                        String value = props.getProperty(key);
                        // Environment에서 로드된 값이 없을 경우에만 EtcProperty에서 가져온 값 사용
                        if (value != null && !cachedProperties.containsKey(key)) {
                            cacheProperty(key, value);
                            addToEtcList(key, value);
                        }
                    }
                    etcPropertyLoaded = true;
                    logger.debug("Loaded properties from EtcProperty bean");
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to load properties from EtcProperty bean", e);
        }
    }

    /**
     * 프로퍼티 캐시에 저장
     */
    private void cacheProperty(String key, String value) {
        cachedProperties.put(key, value);
        logger.debug("Cached property: {} = {}", key, value);
    }

    /**
     * EtcPropertiesBase 호환성을 위한 리스트에 추가
     */
    private void addToEtcList(String key, String value) {
        Map<String, String> propMap = new HashMap<>();
        propMap.put(key, value);
        etcList.add(propMap);
    }

    /**
     * 프로퍼티 값 조회 (EtcPropertiesBase.getEtcProperty() 호환)
     */
    public String getEtcProperty(String propertyKey) {
        // 초기화되지 않았다면 초기화 시도
        if (!initialized) {
            initialize();
        }

        // EtcProperty 지연 로딩 처리 (dispatcher-servlet.xml 빈 로딩 시점 차이 해결)
        if (!etcPropertyLoaded) {
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                loadPropertiesFromEtcPropertyBean(applicationContext);
            }
        }

        // 캐시에서 먼저 확인
        String cachedValue = cachedProperties.get(propertyKey);
        if (cachedValue != null) {
            return cachedValue;
        }

        // 캐시에 없다면 실시간으로 Environment에서 조회
        try {
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                Environment environment = applicationContext.getEnvironment();
                String value = environment.getProperty(propertyKey);

                if (value == null) {
                    if (applicationContext.containsBean("EtcProperty")) {
                        Object bean = applicationContext.getBean("EtcProperty");
                        if (bean instanceof java.util.Properties) {
                            value = ((java.util.Properties) bean).getProperty(propertyKey);
                        }
                    }
                }

                if (value != null) {
                    // 조회된 값을 캐시에 저장
                    cacheProperty(propertyKey, value);
                    addToEtcList(propertyKey, value);
                    return value;
                }
            }
        } catch (Exception e) {
            logger.debug("Failed to get property from Environment: {}", propertyKey, e);
        }
        // 기본값 반환
        return "";
    }

    /**
     * 프로퍼티 값 조회 (기본값 지정)
     */
    public String getEtcProperty(String propertyKey, String defaultValue) {
        String value = getEtcProperty(propertyKey);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    /**
     * 프로퍼티 맵 조회 (EtcPropertiesBase.getEtcPropertyMap() 호환)
     */
    public Map<String, String> getEtcPropertyMap(String propertyKey) {
        if (!initialized) {
            initialize();
        }

        if (!etcPropertyLoaded) {
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                loadPropertiesFromEtcPropertyBean(applicationContext);
            }
        }

        return etcList.stream()
                .filter(m -> m.containsKey(propertyKey))
                .findFirst()
                .orElse(null);
    }

    /**
     * 프로퍼티 존재 여부 확인 (EtcPropertiesBase.hasProperties() 호환)
     */
    public boolean hasProperties() {
        if (!initialized) {
            initialize();
        }

        if (!etcPropertyLoaded) {
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                loadPropertiesFromEtcPropertyBean(applicationContext);
            }
        }

        return !etcList.isEmpty() || !cachedProperties.isEmpty();
    }

    /**
     * 프로퍼티 동적 추가
     */
    public void addEtcProperty(String key, String value) {
        cacheProperty(key, value);
        addToEtcList(key, value);
        logger.debug("Added dynamic property: {} = {}", key, value);
    }

    /**
     * 프로퍼티 맵 동적 추가
     */
    public void addEtcProperty(Map<String, String> propMap) {
        if (propMap != null) {
            propMap.forEach((key, value) -> {
                cacheProperty(key, value);
                logger.debug("Added property from map: {} = {}", key, value);
            });
            etcList.add(new HashMap<>(propMap));
        }
    }

    /**
     * 캐시 갱신
     */
    public void refresh() {
        synchronized (lock) {
            cachedProperties.clear();
            etcList.clear();
            initialized = false;
            etcPropertyLoaded = false;
            initialize();
            logger.info("PropertiesProvider refreshed");
        }
    }

    /**
     * EtcPropertiesBase 호환 리스트 반환
     */
    public List<Map<String, String>> getEtcList() {
        if (!initialized) {
            initialize();
        }
        if (!etcPropertyLoaded) {
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                loadPropertiesFromEtcPropertyBean(applicationContext);
            }
        }
        return new ArrayList<>(etcList);
    }

    /**
     * 모든 캐시된 프로퍼티 반환
     */
    public Map<String, String> getAllCachedProperties() {
        if (!initialized) {
            initialize();
        }
        if (!etcPropertyLoaded) {
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                loadPropertiesFromEtcPropertyBean(applicationContext);
            }
        }
        return new HashMap<>(cachedProperties);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropertiesProvider{");
        sb.append("initialized=").append(initialized);
        sb.append(", cachedProperties=").append(cachedProperties.size());
        sb.append(", etcList=").append(etcList.size());
        sb.append("}");
        return sb.toString();
    }

}
