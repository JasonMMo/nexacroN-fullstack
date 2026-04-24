package com.nexacro.uiadapter.spring.core.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.nexacro.uiadapter.spring.core.context.ApplicationContextProvider;

/**
 * Spring MVC 환경에서
 * 애플리케이션 컨텍스트, HTTP 요청/응답 객체, 세션 속성 등에 접근하기 위한 유틸리티 클래스
 * <p>사용 예시:
 * <pre>
 * // 타입으로 빈 가져오기
 * UserService userService = ServletContextUtil.getBean(UserService.class);
 * 
 * // 이름과 타입으로 빈 가져오기
 * UserService userService = ServletContextUtil.getBean("userService", UserService.class);
 * 
 * // 현재 요청 객체 가져오기
 * HttpServletRequest request = ServletContextUtil.getRequest();
 * 
 * // 세션에 속성 저장하기
 * ServletContextUtil.setAttrToSession("user", userObject);
 * 
 * // 세션에서 속성 가져오기
 * User user = (User) ServletContextUtil.getAttrFromSession("user");
 * </pre>
 *
 * @author UI Adapter 개발팀
 * @since 1.0
 * @version 1.0
 * @see org.springframework.web.context.request.RequestContextHolder
 * @see org.springframework.context.ApplicationContext
 */
public class ServletContextUtil {

	    /**
	     * WebApplicationContext 인스턴스를 가져옵니다.
	     * <p>ContextLoader를 사용하여 현재 웹 애플리케이션 컨텍스트를 반환합니다.
	     * 
	     * @return 현재 웹 애플리케이션 컨텍스트
	     */
	    public static WebApplicationContext getWebApplicationContext() {
	        return ContextLoader.getCurrentWebApplicationContext();
	    }

	/**
	 * 이름과 타입으로 빈을 가져옵니다.
	 * <p>이 메소드는 ApplicationContextProvider를 사용하여 ApplicationContext를 가져온 다음
	 * 지정된 이름과 타입의 빈을 반환합니다.
	 *
	 * @param <T> 가져올 빈의 타입
	 * @param qualifier 가져올 빈의 이름
	 * @param classType 가져올 빈의 클래스 타입
	 * @return 빈 인스턴스
	 */
	public static <T> T getBean(String qualifier, Class<T> classType) { 
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		return applicationContext.getBean(qualifier, classType);
	}
	/**
	 * 타입으로 빈을 가져옵니다.
	 * <p>이 메소드는 ApplicationContextProvider를 사용하여 ApplicationContext를 가져온 다음
	 * 지정된 타입의 빈을 반환합니다. 형변환이 필요하지 않습니다.
	 * 
	 * <p>사용 예시:
	 * <pre>
	 * MyService myService = ServletContextUtil.getBean(MyService.class);
	 * </pre>
	 *
	 * @param <T> 가져올 빈의 타입
	 * @param classType 가져올 빈의 클래스 타입
	 * @return 빈 인스턴스
	 */
	public static <T> T getBean(Class<T> classType) { 
		ApplicationContext applicationContext = ApplicationContextProvider.getApplicationContext();
		return applicationContext.getBean(classType);
	}

	/**
     * 이름으로 빈을 가져옵니다.
     * <p>이 메소드는 ContextLoader를 사용하여 현재 WebApplicationContext를 가져온 다음
     * 지정된 이름의 빈을 반환합니다. 형변환이 필요합니다.
     * 
     * <p>사용 예시:
     * <pre>
     * MyService myService = (MyService) ServletContextUtil.getBean("myService");
     * </pre>
     *
     * @param beanName 가져올 빈의 이름
     * @return 빈 인스턴스
     */
	public static Object getBean(String beanName) {
		WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
		return context.getBean(beanName);
	}

    /**
     * 현재 HttpServletRequest 객체를 반환합니다.
     * <p>이 메소드는 RequestContextHolder를 사용하여 현재 요청 속성을 가져온 다음
     * HttpServletRequest 객체를 반환합니다.
     *
     * @return 현재 HttpServletRequest 객체
     */
	public static HttpServletRequest getRequest() {
		ServletRequestAttributes attr = 
			(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		return attr.getRequest();
	}

    /**
     * 현재 HttpServletResponse 객체를 반환합니다.
     * <p>이 메소드는 RequestContextHolder를 사용하여 현재 요청 속성을 가져온 다음
     * HttpServletResponse 객체를 반환합니다.
     *
     * @return 현재 HttpServletResponse 객체
     */
	public static HttpServletResponse getResponse() {
		ServletRequestAttributes attr =
			(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		return attr.getResponse();
	}

    /**
     * 현재 HttpSession 객체를 반환합니다.
     * <p>이 메소드는 getRequest 메소드를 사용하여 현재 요청을 가져온 다음
     * HttpSession 객체를 반환합니다.
     *
     * @param isNew true인 경우 세션이 존재하지 않으면 새 세션을 생성합니다
     * @return 현재 HttpSession 객체
     */
	public static HttpSession getSession(boolean isNew) {
		return ServletContextUtil.getRequest().getSession(isNew);
	}

    /**
     * 요청 스코프에서 속성을 가져옵니다.
     * <p>이 메소드는 RequestContextHolder를 사용하여 현재 요청 속성을 가져온 다음
     * 요청 스코프에서 지정된 키의 속성을 반환합니다.
     *
     * @param key 가져올 속성의 키
     * @return 속성 값, 찾을 수 없는 경우 null
     */
	public static Object getAttrFromRequest(String key) {
		ServletRequestAttributes attr =
			(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		return attr.getAttribute(key, ServletRequestAttributes.SCOPE_REQUEST);
	}

    /**
     * 요청 스코프에 속성을 저장합니다.
     * <p>이 메소드는 RequestContextHolder를 사용하여 현재 요청 속성을 가져온 다음
     * 요청 스코프에 지정된 키로 속성을 저장합니다.
     *
     * @param key 저장할 속성의 키
     * @param obj 저장할 속성 값
     */
	public static void setAttrToRequest(String key, Object obj) {
		ServletRequestAttributes attr =
			(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		attr.setAttribute(key, obj, ServletRequestAttributes.SCOPE_REQUEST);
	}

    /**
     * 세션 스코프에서 속성을 가져옵니다.
     * <p>이 메소드는 RequestContextHolder를 사용하여 현재 요청 속성을 가져온 다음
     * 세션 스코프에서 지정된 키의 속성을 반환합니다.
     *
     * @param key 가져올 속성의 키
     * @return 속성 값, 찾을 수 없는 경우 null
     */
	public static Object getAttrFromSession(String key) {
		ServletRequestAttributes attr =
			(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		return attr.getAttribute(key, ServletRequestAttributes.SCOPE_SESSION);
	}

    /**
     * 세션 스코프에 속성을 저장합니다.
     * <p>이 메소드는 RequestContextHolder를 사용하여 현재 요청 속성을 가져온 다음
     * 세션 스코프에 지정된 키로 속성을 저장합니다.
     *
     * @param key 저장할 속성의 키
     * @param obj 저장할 속성 값
     */
	public static void setAttrToSession(String key, Object obj) {
		ServletRequestAttributes attr =
			(ServletRequestAttributes)RequestContextHolder.currentRequestAttributes();
		attr.setAttribute(key, obj, ServletRequestAttributes.SCOPE_SESSION);
	}
}
