package com.nexacro.uiadapter.spring.core.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>Spring ApplicationContext에 대한 접근을 제공하는 유틸리티 클래스입니다.
 *
 * <p>ApplicationContextAware를 구현하여, 모든 Spring 빈이 로드된 후에  
 * ApplicationContext 레퍼런스를 획득할 수 있도록 도와줍니다.
 *
 * <p>이 클래스를 통해 어디서든 ApplicationContext를 static하게 참조할 수 있으며,
 * 특별히 Bean으로 등록하지 않은 영역(예: 일반 유틸리티 클래스)에서도  
 * Spring Bean을 동적으로 주입받거나 사용할 수 있습니다.
 * 
 * @author Park SeongMin
 * @since 2015.10.15
 * @version 1.0
 * @see org.springframework.context.ApplicationContextAware
 * @see org.springframework.context.ApplicationContext
 */
public class ApplicationContextProvider implements ApplicationContextAware {

    /** 로거 인스턴스 */
    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextProvider.class);

    /**
     * Spring의 ApplicationContext 인스턴스.
     * <p>모든 빈이 로드된 후, Spring 컨테이너가 setApplicationContext를 통해 값을 주입합니다.
     */
    private static ApplicationContext applicationContext;

    /**
     * ApplicationContext를 주입받는 메소드 (Spring에 의해 호출됨).
     * <p>ApplicationContext를 static 필드에 저장하며, 
     * Singleton 객체 SpringAppContext에도 설정합니다.
     *
     * @param applicationContext 주입받는 ApplicationContext
     * @throws BeansException 처리 중 예외
     */
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        ApplicationContextProvider.applicationContext = applicationContext;
        logger.info(" > > ApplicationContext is instanced in setApplicationContext() of ApplicationContextProvider class.");
        final SpringAppContext springAppContext = SpringAppContext.getInstance(); 
        springAppContext.setApplicationContext(applicationContext);
    }

    /**
     * 정적으로 ApplicationContext를 반환합니다.
     * 
     * @return 저장된 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}