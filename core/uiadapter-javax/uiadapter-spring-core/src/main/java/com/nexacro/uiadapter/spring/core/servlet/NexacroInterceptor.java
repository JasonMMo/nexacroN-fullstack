package com.nexacro.uiadapter.spring.core.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.nexacro.java.xapi.data.Debugger;
import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.uiadapter.spring.core.NexacroConstants;
import com.nexacro.uiadapter.spring.core.context.NexacroContext;
import com.nexacro.uiadapter.spring.core.context.NexacroContextHolder;

/**
 * Interceptor for handling Nexacro platform requests in Spring MVC.
 * <p>This interceptor processes incoming requests from Nexacro platform clients,
 * extracting and parsing the platform data for use in controllers.
 * 
 * @author Park SeongMin
 * @since 08.11.2015
 * @version 1.0
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public class NexacroInterceptor implements HandlerInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(NexacroInterceptor.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger(NexacroConstants.PERFORMANCE_LOGGER);

    /**
     * Pre-processes the request before handler execution.
     * <p>This method parses the Nexacro request and stores it in the NexacroContext.
     * 
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @param handler the chosen handler to execute
     * @return always returns true to proceed with handler execution
     * @throws Exception in case of errors
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        parseNexacroRequest(request, response, handler);
        return true;
    }

    /**
     * Post-processes the request after successful handler execution.
     * <p>This implementation delegates to the parent class.
     * 
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @param handler the handler that was executed
     * @param modelAndView the ModelAndView that the handler returned
     * @throws Exception in case of errors
     */
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
    	HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * Processes the request after handler execution is complete.
     * <p>This implementation delegates to the parent class.
     * 
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @param handler the handler that was executed
     * @param ex any exception that was thrown during handler execution
     * @throws Exception in case of errors
     */
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    	HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    /**
     * Parses the Nexacro request and creates a NexacroContext.
     * <p>This method extracts platform data from the request and logs performance metrics.
     * <p>Added null check condition on 2023.11.21 to support parseNexacroRequest() functionality in Filter.
     * 
     * @param request the current HTTP request
     * @param response the current HTTP response
     * @param handler the handler that will be executed
     * @throws Exception in case of errors during parsing
     */
    private void parseNexacroRequest(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        StopWatch sw = new StopWatch(getClass().getSimpleName());
        try {
            sw.start("parse request");
            // 2023.11.21 Filter에서 parseNexacroRequest()기능 추가할 경우를 위해 null 체크 조건 추가. 
            if(NexacroContextHolder.getNexacroContext() == null) {
	            NexacroContext context = NexacroContextHolder.getNexacroContext(request, response);
	            PlatformData platformData = context.getPlatformData();
	            if(logger.isDebugEnabled()) {
	                logger.debug("got request=[{}]", new Debugger().detail(platformData));
	            }
            }
        } finally {
            sw.stop();
            if(performanceLogger.isTraceEnabled()) {
                String stopWatchLog = sw.prettyPrint()
                        .replace('\r', '_').replace('\n', '_'); // Replace CRLF
                performanceLogger.trace("Performance summary:\n{}", stopWatchLog);
            }
        }

    }

}
