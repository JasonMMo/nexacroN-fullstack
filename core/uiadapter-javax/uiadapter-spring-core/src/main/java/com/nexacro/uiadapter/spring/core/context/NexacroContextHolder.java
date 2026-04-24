package com.nexacro.uiadapter.spring.core.context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.uiadapter.spring.core.NexacroConstants;

import java.util.Objects;

/**
 * <p>HTTP 요청에 대해 스레드별로 <code>NexacroContext</code> 인스턴스를 관리합니다.
 * <p>이 클래스는 Spring의 RequestContextHolder를 사용하여 NexacroContext 객체를 저장하고 검색하는 정적 메서드를 제공합니다.
 *
 * @author Park SeongMin
 * @since 08.11.2015
 * @version 1.0
 * @see NexacroContext
 * @see org.springframework.web.context.request.RequestContextHolder
 */
public abstract class NexacroContextHolder {

    /**
     * 주어진 요청과 응답에 대한 NexacroContext를 생성하고 초기화합니다.
     * <p>이 메서드는 새로운 NexacroContext를 생성하고, 요청을 파싱하고, 요청 속성에 저장한 후 반환합니다.
     *
     * @param request HTTP 서블릿 요청
     * @param response HTTP 서블릿 응답
     * @return 초기화된 NexacroContext
     * @throws PlatformException 요청 파싱 중 오류가 발생할 경우
     */
    public static NexacroContext getNexacroContext(HttpServletRequest request, HttpServletResponse response) throws PlatformException {
        //NexacroContext nexacroContext = new NexacroContext(request, response);
    	NexacroContext nexacroContext = new NexacroContext();

    	nexacroContext.parseRequest(request, response);
        Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).setAttribute(NexacroConstants.ATTRIBUTE.NEXACRO_REQUEST, NexacroConstants.ATTRIBUTE.NEXACRO_REQUEST, RequestAttributes.SCOPE_REQUEST);
        setNexacroContext(nexacroContext);
        return nexacroContext;
    }

    /**
     * 주어진 NexacroContext를 요청 속성에 저장합니다.
     *
     * @param context 저장할 NexacroContext
     */
    public static void setNexacroContext(NexacroContext context) {
        Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).setAttribute(NexacroConstants.ATTRIBUTE.NEXACRO_CACHE_DATA, context, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * 요청 속성에서 NexacroContext를 검색합니다.
     *
     * @return NexacroContext, 찾지 못하거나 속성이 NexacroContext가 아닌 경우 null 반환
     */
    public static NexacroContext getNexacroContext() {
        // 요청 속성에서 NEXACRO_CACHE_DATA 속성 값을 가져옴
        Object context = Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).getAttribute(NexacroConstants.ATTRIBUTE.NEXACRO_CACHE_DATA, RequestAttributes.SCOPE_REQUEST);
        if(context == null) {
            return null;
        }
        // 속성 값이 NexacroContext 인스턴스인지 확인
        if(context instanceof NexacroContext) {
            return (NexacroContext) context;
        }
        return null;
    }

}
