package com.nexacro.uiadapter.spring.core.context;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nexacro.uiadapter.spring.core.util.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.tx.HttpPlatformRequest;
import com.nexacro.java.xapi.tx.HttpPlatformResponse;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.java.xapi.util.StringUtils;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;
import com.nexacro.uiadapter.spring.core.util.Etc;
import com.nexacro.uiadapter.spring.core.util.EtcPropertiesBase;

/**
 * <p>HTTP 요청에서 데이터를 수신 및 저장하는 역할을 담당합니다.
 * <p>대용량 데이터 분할 전송을 위한 NexacroFirstRowHandler도 제공합니다.
 *
 * @author Park SeongMin
 * @since 07.28.2015
 * @version 1.0
 * @see NexacroContextHolder
 * @see com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler
 */
public class NexacroContext {
	/** Logger for this class */
	private final Logger logger = LoggerFactory.getLogger(getClass());

    /* 데이터 수신시 HTTP GET 데이터 등록 여부의 키 */
    //private static final String REGISTER_GET_PARAMETER = "http.getparameter.register";
    /* HTTP GET 데이터 등록시 Variable 형식으로 변환 여부의 키 */
    //private static final String GET_PARAMETER_AS_VARIABLE = "http.getparameter.asvariable";

    /** The HTTP servlet request */
    private transient HttpServletRequest request;
    /** The HTTP servlet response */
    private transient HttpServletResponse response;

    /** The platform request for handling Nexacro data */
    private transient HttpPlatformRequest platformRequest;
    /** The platform response for handling Nexacro data */
    private transient HttpPlatformResponse platformResponse;
    /** 대용량/분할 데이터 처리용 Row Handler (지연 응답 지원) */
    private transient NexacroFirstRowHandler firstRowHandler;

    private boolean bZipSupported;
    /**
	 * 2023.07.26 추가
     * 환경설정 관리 객체
     * - 컬럼 표기 형식(camel, kebab, snake, upper 등) 자동 변환 지원
     *
     * 2026.02.23 useRequestCompressType 추가.
	 */
    PropertiesProvider propertiesProvider = PropertiesProvider.getInstance();

	//EtcPropertiesBase etcProperty;

	// spring f/w 환경에서는 1.0.1 버전 사용해야 함.
    //@Nullable
    //@Value("#{EtcProperty['uiAdapter.useRequestContentType'] ?:null}")
    @Value("${uiAdapter.useRequestContentType:}")
    private String useRequestContentType;
    @Value("${uiAdapter.useRequestCompressType:}")
    private String useRequestCompressType;

	/**
     * etcProperty에서 Content-Type 관련 설정값 주입
     * (useRequestContentType 값이 없을 경우 재설정)
	 */
	public void setEtcProperty() {
		if(useRequestContentType==null || useRequestContentType.isEmpty()) {
            useRequestContentType = propertiesProvider.getEtcProperty("nexacro.use-request-contenttype");
        }
        if(useRequestCompressType==null || useRequestCompressType.isEmpty()) {
            useRequestCompressType = propertiesProvider.getEtcProperty("nexacro.use-request-compresstype");
            if(useRequestCompressType==null || useRequestCompressType.isEmpty()) {
                useRequestCompressType = propertiesProvider.getEtcProperty("uiAdapter.useRequestCompressType");
            }
        }
	}

    /**
     * 기본 생성자
     * - NexacroContext 객체가 생성될 때 즉시 환경설정 Bean(etcProperty)을 찾아서 필드에 세팅
     * - etcProperty가 정상적으로 존재하고 프로퍼티가 있을 경우 setEtcProperty()로 Content-Type 설정값도 적용
     * (※ DI 미적용 시 직접 Bean을 가져옴. Spring Bean Pool 밖에서 new NexacroContext() 가능)
     */
    public NexacroContext() {
        setEtcProperty();
        /*
    	Object bean = Etc.getBean("etcProperty");
    	if(bean != null) {
            // 받아온 빈을 타입 캐스팅하여 멤버에 저장
    		etcProperty = (EtcPropertiesBase) bean;
            // 환경 프로퍼티가 하나라도 들어있다면 Content-Type 옵션을 한 번 더 반영
    		if(etcProperty.hasProperties()) {
    			setEtcProperty();
    		}
    	}
    	*/
    }

    /**
     * HTTP 요청 파싱 및 platform 객체 초기화
     * - 클라이언트로부터 받은 HttpServletRequest/Response를 설정
     * - Request의 InputStream을 기반으로, 읽어온 데이터 유형(SSV, Binary, JSON 등)에 맞추어 HttpPlatformRequest 생성/세팅
     * - 사용자 요청 헤더 및 ContentType 기반으로 실제 응답/처리 타입 결정
     * - 수신 데이터가 정상인 경우 receiveData() 호출
     * - platformRequest/platformResponse를 최종 바인딩
     *
     * @param request  HttpServletRequest (서블릿의 요청 객체)
     * @param response HttpServletResponse (서블릿의 응답 객체)
     * @throws PlatformException InputStream 파싱 또는 처리 오류
     */
    public void parseRequest(final HttpServletRequest request, final HttpServletResponse response) throws PlatformException {

        this.request = request;
        this.response = response;

        InputStream inputStream ;
        try {
            // 요청 입력 스트림을 획득 (예외 시 PlatformException 래핑)
            inputStream = this.request.getInputStream();
        } catch (IOException e) {
            throw new PlatformException("Could not get HTTP InputStream", e);
        }

        final String httpContentType = this.request.getContentType();
        final String userAgent = this.request.getHeader("User-Agent");
        final String contentType = findContentType(httpContentType, userAgent); // binary만 식별함.
        String charset = "";

        // SSV 자동처리를 위해 inputStream을 사용하도록 한다.
        final HttpPlatformRequest httpPlatformRequest = new HttpPlatformRequest(inputStream);
        // Binary 전송일 경우 Content-Type을 별도 지정
        if(PlatformType.HTTP_CONTENT_TYPE_BINARY.equals(httpContentType)) {
            httpPlatformRequest.setContentType(contentType);
        }

        // 2021.09.14 , PlatformType.CONTENT_TYPE_JSON 추가 반영
        // - 오류로 막음(2022.06.10)
        // request.header의 contentType값이 nexacro의 contentType과 다름.
        // else if(PlatformType.HTTP_CONTENT_TYPE_JSON.equals(httpContentType)){
        // 	httpPlatformRequest.setContentType(contentType);
        // }
        try {
            if(logger.isDebugEnabled()) {
                logger.debug("[★]  Before receiveData() httpPlatformRequest ContentType=[{}]",contentType);
            }
            // 실제 데이터 파싱(실패 시 PlatformException)
        	httpPlatformRequest.receiveData();

            // 2026.02.24 protocolType = zip 체크
            bZipSupported = httpPlatformRequest.containsProtocolType(PlatformType.PROTOCOL_TYPE_ZLIB);

            //log level이 debug인 경우 platform data content type 출력.
            if(logger.isDebugEnabled()) {
                logger.debug("[★]  After receiveData()   bZipSupported={}/{}", httpPlatformRequest.getProtocolType(0), bZipSupported);
            }

        } catch(PlatformException e) {
        	// ExceptionResolver에서 상세한 로그를 남긴다. 간략로그만을 남기도록 한다.
//        	final Logger logger = LoggerFactory.getLogger(getClass());
        	if(logger.isErrorEnabled()) {
                logger.error("receive platform data failed. e={}", e.getMessage());
        	}
        	throw e;
        }



        // 응답 플랫폼 객체 생성
        final HttpPlatformResponse httpPlatformResponse = getHttpPlatformResponse(response, httpPlatformRequest);

        this.platformRequest  = httpPlatformRequest;
        this.platformResponse = httpPlatformResponse;
    	if(logger.isDebugEnabled()) {
    		logger.debug("	< ★ >  httpPlatformRequest ContentType = [{}], useLocalType[{}], httpPlatformResponse ContentType=[{}]",httpPlatformRequest.getContentType(), useRequestContentType, httpPlatformResponse.getContentType());
    	}

    }

    /**
     * 응답 객체 생성 및 Content-Type/Charset 설정
     * - request에서 파악된 Content-Type 정보를 응답에도 동일하게 적용
     * - SSV 외의 케이스에서는 useRequestContentType 플래그 따라 별도 로직 분기
     * @param response            HTTP 응답 객체
     * @param httpPlatformRequest 처리된 플랫폼 요청 객체
     * @return HttpPlatformResponse : 실제 응답 객체
     */
    private HttpPlatformResponse getHttpPlatformResponse(HttpServletResponse response, HttpPlatformRequest httpPlatformRequest) {
        final HttpPlatformResponse httpPlatformResponse;

        // SSV 일 경우에만 contentType 존재.
        if(httpPlatformRequest.getContentType() == null) {
            //\\httpPlatformRequest.setContentType(contentType);
            httpPlatformResponse = new HttpPlatformResponse(response, httpPlatformRequest);
        }
        else {
        	// 2022.06.10 :: SSV 이외 처리를 설정.
        	// 나머지 contentType은 receiveData() 후 httpPlatformRequest에서 값 가져와야 함.
        	if(useRequestContentType != null) {
            	httpPlatformResponse = new HttpPlatformResponse(response, httpPlatformRequest.getContentType(), httpPlatformRequest.getCharset());
            } else {
            	httpPlatformResponse = new HttpPlatformResponse(response, httpPlatformRequest);
        	}
        }
        return httpPlatformResponse;
    }

    /**
     * PlatformRequest 반환
     * @return 초기화된 HttpPlatformRequest (분석 및 데이터 접근용)
     */
    public HttpPlatformRequest getPlatformRequest() {
        return platformRequest;
    }

    /**
     * PlatformResponse 반환
     * - 만약 platformResponse가 할당되어 있지 않을 시 새로 생성 후 반환
     * @return HttpPlatformResponse
     */
    public HttpPlatformResponse getPlatformResponse() {
        if(platformResponse != null) {
            return platformResponse;
        }
        platformResponse = new HttpPlatformResponse(response, platformRequest);
        return platformResponse;
    }

    /**
     * 분할전송(FirstRow) 핸들러 반환
     * (없으면 생성 후 반환)
     * @return NexacroFirstRowHandler
     */
    public NexacroFirstRowHandler getFirstRowHandler() {
        if(firstRowHandler != null) {
            return firstRowHandler;
        }

        firstRowHandler = new NexacroFirstRowHandler(response, platformRequest);
        return firstRowHandler;
    }

    /**
     * 첫 번째 Row가 이미 전송(fired)되었는지 여부 확인
     * @return true = 전송됨, false = 미전송
     */
    public boolean isFirstRowFired() {
    	boolean isFirstRowFire = false;

        if(firstRowHandler != null) {
        	isFirstRowFire = firstRowHandler.isFirstRowStarted();
        }
        return isFirstRowFire;
    }

    /**
     * 실제 받은 플랫폼 데이터 반환
     * @return PlatformData : 내부 request 객체에서 데이터 추출
     */
    public PlatformData getPlatformData() {
        return this.platformRequest == null ? null : this.platformRequest.getData();
    }

    /**
     * HTTP Content-Type과 User-Agent 정보를 분석하여 적절한 플랫폼 Content-Type을 결정합니다.
     * <p>
     * 이 메서드는 HTTP 요청의 Content-Type과 User-Agent 헤더를 검사하여
     * 데이터 통신에 사용할 적절한 플랫폼 Content-Type을 결정합니다.
     * <p>
     * 주요 처리 로직:
     * <ul>
     *   <li>Content-Type이 비어있으면 null 반환</li>
     *   <li>Content-Type에서 ';' 이전 부분만 추출</li>
     *   <li>XML, BINARY, HTML 등의 Content-Type 값에 따라 처리</li>
     *   <li>MiPlatform 클라이언트인 경우 MiPlatform 전용 Content-Type 반환</li>
     * </ul>
     *
     * @param httpContentType HTTP 요청의 Content-Type 헤더 값
     * @param userAgent HTTP 요청의 User-Agent 헤더 값
     * @return 적절한 플랫폼 Content-Type, httpContentType이 비어있으면 null 반환
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">HTTP/1.1 Content-Type</a>
     * @see com.nexacro.java.xapi.tx.PlatformType
     */
    String findContentType(final String httpContentType, final String userAgent) {
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html

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
                // } else if (contentType.equals(PlatformType.HTTP_CONTENT_TYPE_JSON)) {
                // 	platformContentType = PlatformType.CONTENT_TYPE_JSON;
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
        return platformContentType;
    }

    /**
     * request 패킷이 압축통신인지 체크 (nexacro 압축통신 여부와는 관계 없음)
     * @Date 2026.02.23
     * @return
     */
    public boolean isGzipSupported() {
        return this.bZipSupported;
    }

    /**
     * 주어진 User-Agent가 MiPlatform 클라이언트를 나타내는지 확인합니다.
     * <p>
     * User-Agent 문자열이 "MiPlatform"으로 시작하는지 검사하여
     * MiPlatform 클라이언트에서 보낸 요청인지 판별합니다.
     * <p>
     * 예시 MiPlatform User-Agent 형식: "MiPlatform 3.1;win32;1400x1050"
     *
     * @param userAgent HTTP 요청의 User-Agent 헤더 값
     * @return MiPlatform 클라이언트이면 true, 아니면 false
     */
    private boolean isMiPlatform(final String userAgent) {
    	boolean isMiPlatform ;

        // MiPlatform 3.1;win32;1400x1050
        if (userAgent == null) {
        	isMiPlatform = false;
        } else {
        	isMiPlatform = userAgent.startsWith("MiPlatform");
        }

        return isMiPlatform;
    }

}
