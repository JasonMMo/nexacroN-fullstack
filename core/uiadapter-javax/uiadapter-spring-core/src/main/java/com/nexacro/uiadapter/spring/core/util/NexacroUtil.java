package com.nexacro.uiadapter.spring.core.util;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.datatype.PlatformDataType;
import com.nexacro.uiadapter.spring.core.NexacroConstants;

import java.util.Objects;

/**
 *
 * @author Park SeongMin
 * @since 2015. 7. 28.
 * @version 1.0
 */
public abstract class NexacroUtil {
    
    public static final String ATTR_PLATFORM_REQUEST = "PlatformRequest";

	// 2023.07.26 client의 column case 형식에 따라 case converting 추가
    // nexacro.column-case: camel(whoAreYou), kebab(who-are-you), snake(who_are_you),upper(WHO_ARE_YOU)
    final EtcPropertiesBase etcProperty;
    
	// spring f/w 환경에서는 1.0.1 버전 사용해야 함.
	//@Value("#{EtcProperty['uiAdapter.getHeader.WhoAreYou'] ?:''}")
    public static String whoAreYou;
	
	public void setEtcProperty() {
		if(whoAreYou==null || whoAreYou.isEmpty())
			whoAreYou = etcProperty.getEtcProperty("nexacro.who-are-you");
	}
    
    public NexacroUtil() {
		etcProperty = (EtcPropertiesBase) Etc.getBean("etcProperty");
		setEtcProperty();
    }
    
    public static DataSet createFirstRowStatusDataSet(int errorCode, String errorMsg) {
        
        DataSet ds = new DataSet(NexacroConstants.ERROR_FIRST_ROW.ERROR_DATASET);
        ds.addColumn(NexacroConstants.ERROR_FIRST_ROW.ERROR_CODE, PlatformDataType.INT);
        ds.addColumn(NexacroConstants.ERROR_FIRST_ROW.ERROR_MSG, PlatformDataType.STRING);
        
        int newRow = ds.newRow();
        ds.set(newRow, NexacroConstants.ERROR_FIRST_ROW.ERROR_CODE, errorCode);
        ds.set(newRow, NexacroConstants.ERROR_FIRST_ROW.ERROR_MSG, errorMsg);
        
        return ds;
    }
    
    public static boolean isNexacroRequest(HttpServletRequest request) {
        
    	// 2022.06.24 추가 :: 파일 다운로드 error시 nexacro reqeust 체크용 (request header의 [WhoAreYou] key값으로 식별)
    	String requestFrom = HttpUtil.getHeaderValue( request, "WhoAreYou");
		if(whoAreYou!=null && whoAreYou.equals(requestFrom)) {
            return true;
        }
			
        // runtime 버전의 경우에만 userAgent 값이 설정 된다.
        Object nexacroRequest = Objects.requireNonNull(RequestContextHolder.getRequestAttributes()).getAttribute(NexacroConstants.ATTRIBUTE.NEXACRO_REQUEST, RequestAttributes.SCOPE_REQUEST);
        if(NexacroConstants.ATTRIBUTE.NEXACRO_REQUEST.equals(nexacroRequest)) {
            return true;
        }
        
        String userAgent = request.getHeader(HttpUtil.HEADER_USER_AGENT);
        
        if(userAgent != null) {
            userAgent = userAgent.trim().toLowerCase();
            
            if(userAgent.startsWith(HEADER_NEXACRO.USER_AGENT_KEY.toLowerCase())) {
                return true;
            } else if (userAgent.startsWith(HEADER_XPLATFORM.USER_AGENT_KEY.toLowerCase())) {
                return true;
            } else if (userAgent.startsWith(HEADER_MIPLATFORM.USER_AGENT_KEY.toLowerCase())) {
                return true;
            }
        }
        
        return false;
    }

    public static class HEADER_NEXACRO {
        public final static String USER_AGENT_KEY = "nexacro";
        public final static String USER_AGENT_SAMPLE = "nexacroplatform14-Win32/2014 (compatible; Mozilla/4.0; MSIE 7.0)";
    }
    
    public static class HEADER_XPLATFORM {
        public final static String USER_AGENT_KEY = "XPLATFORM";
        public final static String USER_AGENT_SAMPLE = "XPLATFORM-Win32/2009 (compatible; Mozilla/4.0; MSIE 7.0)";
    }
    
    public static class HEADER_MIPLATFORM {
        public final static String USER_AGENT_KEY = "MiPlatform";
        public final static String USER_AGENT_SAMPLE = "MiPlatform 3.1;win32;1280x800";
    }
}
