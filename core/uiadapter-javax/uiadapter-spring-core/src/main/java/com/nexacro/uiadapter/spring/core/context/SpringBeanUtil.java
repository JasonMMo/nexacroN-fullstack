package com.nexacro.uiadapter.spring.core.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * 	bean객체가 아닌, new로 생성한 Instance 객체에서 Spring Bean을 가져와야 할때 사용.
 */
public class SpringBeanUtil{

	/**
	 * Retrieves a Spring bean by name using the current request.
	 * <p>This method uses the current request to find the appropriate WebApplicationContext
	 * and then retrieves the bean with the specified name.
	 *
	 * <p>Usage example:
	 * <pre>
	 * LoginServiceImpl loginServiceImpl = (LoginServiceImpl)SpringBeanUtil.getBean("loginService");
	 * </pre>
	 *
	 * @param beanId the name of the bean to retrieve
	 * @return the bean instance, or null if not found
	 * @throws Exception if an error occurs during bean retrieval
	 */
	public static Object getBean(final String beanId) throws Exception {
		return getBean(ServletContextUtil.getRequest(), beanId);
	}

	/**
	 * Retrieves a Spring bean by name using the specified request.
	 * <p>This method first tries to find the bean in the WebApplicationContext associated with
	 * the DispatcherServlet, and if not found, tries the WebApplicationContext associated with
	 * the ContextLoaderListener.
	 *
	 * @param request the HTTP servlet request
	 * @param beanId the name of the bean to retrieve
	 * @return the bean instance, or null if not found
	 * @throws Exception if an error occurs during bean retrieval
	 */
	public static Object getBean(final HttpServletRequest request, final String beanId) throws Exception {

		Object beanObject = null;
		ServletContext servletContext;
		HttpSession httpSession;
		WebApplicationContext webApplicationContext;

		// DispatcherServlet으로 로딩된 context를 가져 온다.DispatcherServlet은 여러개 있을 수 있기에 find~로 바뀜.
		webApplicationContext = RequestContextUtils.findWebApplicationContext(request);
		// 빈을 검색해서 해당 빈 오브젝트를 가져 온다.
		if (webApplicationContext.containsBean(beanId)) {
			beanObject = webApplicationContext.getBean(beanId);
			return beanObject;
		}

		httpSession = request.getSession();
		servletContext = httpSession.getServletContext();

		// ContextLoaderListener으로 로딩된 context를 가져 온다.
		webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (webApplicationContext != null && webApplicationContext.containsBean(beanId)) {
            beanObject = webApplicationContext.getBean(beanId);
            return beanObject;
        }
        return null;
	}
	/**
	 * <PRE>
	 * Name : getBean
	 * <PRE>
	 * @param classType
	 * @throws Exception
	 * @return      : Object
	 */
	public static <T> T getBean(final Class<T> classType) throws Exception {
		return getBean(ServletContextUtil.getRequest(), classType) ;
	}

	/**
	 * Retrieves a Spring bean by type using the specified request.
	 * <p>This method first tries to find the bean in the WebApplicationContext associated with
	 * the DispatcherServlet, and if not found, tries the WebApplicationContext associated with
	 * the ContextLoaderListener.
	 *
	 * @param <T> the type of the bean to retrieve
	 * @param request the HTTP servlet request
	 * @param classType the type of the bean to retrieve
	 * @return the bean instance, or null if not found
	 * @throws Exception if an error occurs during bean retrieval
	 */
	public static <T> T getBean(final HttpServletRequest request, final Class<T> classType) throws Exception {

		T beanObject = null;
		ServletContext servletContext;
		HttpSession httpSession;
		WebApplicationContext webApplicationContext;

		// DispatcherServlet으로 로딩된 context를 가져 온다.DispatcherServlet은 여러개 있을 수 있기에 find~로 바뀜.
		webApplicationContext = RequestContextUtils.findWebApplicationContext(request);
		// 빈을 검색해서 해당 빈 오브젝트를 가져 온다.
		if (webApplicationContext.containsBean(classType.getName())) {
			beanObject = webApplicationContext.getBean(classType);
			return beanObject;
		}

		httpSession = request.getSession();
		servletContext = httpSession.getServletContext();

		// ContextLoaderListener으로 로딩된 context를 가져 온다.
		webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (webApplicationContext != null && webApplicationContext.containsBean(classType.getName())) {
            beanObject = webApplicationContext.getBean(classType);
            return beanObject;
        }
        return null;
	}
}
