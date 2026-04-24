package com.nexacro.uiadapter.spring.core.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.java.xapi.util.StringUtils;

/**
 * 
 * HTTP Util class
 * 
 * @author Park SeongMin
 * @since 07.27.2015
 * @version 1.0
 */
public abstract class HttpUtil {
	private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT_CHARSET = "Accept-Charset";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

    public static String getHeaderValue(HttpServletRequest request, String targetHeaderName) {
        if (targetHeaderName == null) {
            return null;
        }

        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String headerName = enumeration.nextElement();
            if (targetHeaderName.equalsIgnoreCase(headerName)) {
                return request.getHeader(headerName);
            }
        }

        return null;
    }

    public static String getRemoteAddr(HttpServletRequest request) {

        /*
         * http://lesstif.com/pages/viewpage.action?pageId=20775886
         * 
         * WAS 는 보통 2차 방화벽 안에 있고 Web Server 를 통해 client 에서 호출되거나 cluster로 구성되어
         * load balancer 에서 호출되는데 이럴 경우에서 getRemoteAddr() 을 호출하면 웹서버나 load
         * balancer의 IP 가 나옴
         * WebLogic 의 web server 연계 모듈인 weblogic connector 는
         * 위 헤더를 사용하지 않고 Proxy-Client-IP 나 WL-Proxy-Client-IP 를 사용하므로
         * weblogic 에서 도는 application 작성시 수정이 필요함
         */

        String clientIp = request.getHeader("X-Forwarded-For");
        if (!isEmpty(clientIp)) {
            return getFirstIp(clientIp);
        }

        clientIp = request.getHeader("Proxy-Client-IP");
        if (!isEmpty(clientIp)) {
            return getFirstIp(clientIp);
        }

        clientIp = request.getHeader("WL-Proxy-Client-IP");
        if (!isEmpty(clientIp)) {
            return getFirstIp(clientIp);
        }

        clientIp = request.getHeader("HTTP_CLIENT_IP");
        if (!isEmpty(clientIp)) {
            return getFirstIp(clientIp);
        }

        clientIp = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (!isEmpty(clientIp)) {
            return getFirstIp(clientIp);
        }

        return request.getRemoteAddr();
    }

    private static boolean isEmpty(String str) {
        if (str == null || str.isEmpty() || "unknown".equalsIgnoreCase(str)) {
            return true;
        }

        return false;
    }

    private static String getFirstIp(String ip) {
        if (ip == null) {
            return null;
        }

        String[] split = ip.split(",");
        return split[0];
    }
    
    /* HTTP의 ContentType으로부터 송수신 형식을 검색한다. */
    public static String findContentType(final String httpContentType, final String userAgent) {
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    	logger.debug("	findContentType [httpContentType > {} , userAgent > {}]", httpContentType, userAgent);
        if (StringUtils.isEmpty(httpContentType)) {
            return null;
        }

        String platformContentType ;
        String contentType         ;
        
        final int index = httpContentType.indexOf(';');
        
        if(index == -1) {
        	contentType = httpContentType;
        }
        else {
        	contentType = httpContentType.substring(0, index);
        }

        switch (contentType) {
            case PlatformType.HTTP_CONTENT_TYPE_XML:
                if (isMiPlatform(userAgent)) {
                    platformContentType = PlatformType.CONTENT_TYPE_MI_XML;
                } else {
                    platformContentType = PlatformType.CONTENT_TYPE_XML;
                }
                break;
            case PlatformType.HTTP_CONTENT_TYPE_BINARY:
                if (isMiPlatform(userAgent)) {
                    platformContentType = PlatformType.CONTENT_TYPE_MI_BINARY;
                } else {
                    platformContentType = PlatformType.CONTENT_TYPE_BINARY;
                }
                break;
            case PlatformType.HTTP_CONTENT_TYPE_HTML:
                platformContentType = PlatformType.CONTENT_TYPE_HTML;

                // 2021.09.14 , PlatformType.CONTENT_TYPE_JSON 추가 반영
                break;
            case PlatformType.HTTP_CONTENT_TYPE_JSON:
                platformContentType = PlatformType.CONTENT_TYPE_JSON;
                break;
            // default XML로 처리.
            default:
                if (isMiPlatform(userAgent)) {
                    platformContentType = PlatformType.CONTENT_TYPE_MI_XML;
                } else {
                    platformContentType = PlatformType.CONTENT_TYPE_XML;
                }
                break;
        }

    	logger.debug("	return [httpContentType > {} ]", httpContentType);
        return platformContentType;
    }
    
    /* MiPlatform 여부 */
    private static boolean isMiPlatform(final String userAgent) {
    	boolean isMiPlatform;
    	
        // MiPlatform 3.1;win32;1400x1050
        if (userAgent == null) {
        	isMiPlatform = false;
        } else {
        	isMiPlatform = userAgent.startsWith("MiPlatform");
        }
        
        return isMiPlatform;
    }
    
}
