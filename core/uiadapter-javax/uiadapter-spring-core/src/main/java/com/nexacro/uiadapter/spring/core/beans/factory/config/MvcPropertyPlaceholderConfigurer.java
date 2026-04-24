package com.nexacro.uiadapter.spring.core.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * <p>Spring 환경에서 프로퍼티(설정 값) 파일을 읽어와  
 * ApplicationContext에 placeholder(플레이스홀더: ${...}) 형태로 주입할 수 있도록 도와주는 빈 설정 지원 클래스입니다.
 *
 * <p>Spring의 XML 또는 Java Config에서 프로퍼티 파일의 값을 손쉽게 사용할 수 있도록 해주며,  
 * 별도의 확장 기능이나 커스텀 기능이 적용된 경우,  
 * 표준 PropertyPlaceholderConfigurer와 다르게 Controller 등 MVC에서의 활용도를 보완할 수 있습니다.
 *
 * <p>커스텀 ApplicationContext 등에서 외부 프로퍼티 파일을 읽어서  
 * 설정 값을 동적으로 삽입할 때 주로 사용합니다.
 *
 * @author Park SeongMin
 * @since 2015.07.28
 * @version 1.0
 */
public class MvcPropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // 실제 구현 내용이 여기에 들어갑니다.
    }

}