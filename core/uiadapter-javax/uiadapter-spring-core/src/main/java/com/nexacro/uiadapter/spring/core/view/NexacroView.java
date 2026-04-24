package com.nexacro.uiadapter.spring.core.view;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nexacro.java.xapi.tx.HttpPlatformRequest;
import com.nexacro.uiadapter.spring.core.util.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.view.AbstractView;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataSetList;
import com.nexacro.java.xapi.data.Debugger;
import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.java.xapi.data.VariableList;
import com.nexacro.java.xapi.tx.HttpPlatformResponse;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.java.xapi.tx.PlatformType;
import com.nexacro.uiadapter.spring.core.NexacroConstants;
import com.nexacro.uiadapter.spring.core.context.NexacroContext;
import com.nexacro.uiadapter.spring.core.context.NexacroContextHolder;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowAccessor;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;

import com.nexacro.uiadapter.spring.core.util.Etc;
import com.nexacro.uiadapter.spring.core.util.EtcPropertiesBase;
import com.nexacro.uiadapter.spring.core.util.NexacroUtil;

import static com.nexacro.java.xapi.tx.PlatformType.PROTOCOL_TYPE_ZLIB;

/**
 * <p>nexacro platform으로 데이터를 송신하기 위한 {@link org.springframework.web.servlet.View}이다.
 * 
 * <p>데이터 분한 전송이 이루어진 후 데이터를 전송하는 경우 기 전송된 데이터는 전송되지 않는다.
 * 또한 DataSet이 전송되었을 경우 Variable은 추가적으로 전송되지 않는다.
 * 
 * @author Park SeongMin
 * @since 07.27.2015
 * @version 1.0
 *
 */
public class NexacroView extends AbstractView {
    
	private static final Logger logger = LoggerFactory.getLogger(NexacroView.class);
	private static final Logger performanceLogger = LoggerFactory.getLogger(NexacroConstants.PERFORMANCE_LOGGER);
	
	private String defaultContentType;
	private String defaultCharset;

	// 2023.07.26 client의 column case 형식에 따라 case converting 추가
    // nexacro.column-case: camel(whoAreYou), kebab(who-are-you), snake(who_are_you),upper(WHO_ARE_YOU)
	// <주의> spring f/w 환경에서는 1.0.1 버전 사용해야 함.
    //private EtcPropertiesBase etcProperty;

	 /* 
     * 2022.06.10 추가
     * [spring f/w 환경 지원]
     * 	<dispatcher-servlet.xml>
     * 	 #util:properties 설정에서 값 취득.
     * (2025.12.30 :: spel방식은 더이상 사용하지 않음)
     * 	<util:properties id="EtcProperty">
     * 		uiAdapter.useRequestCharset           Response Charset을  Request의 Charset값으로 사용 
     * 		uiAdapter.useRequestContentType       Response ContentType을  Request의 ContentType값으로  사용 
     * 2023.08.24 추가
     * [spring boot 환경 지원]
     * application.yml
     * 	nexacro:
     *   client-column-case: 
     *   db-column-case: 
     *   who-are-you: nexacro
     *   use-request-charset: all
     *   use-request-contenttype: all
     *   trim-paramdataset: all
     *   trim-paramvariable: all
     *   replace-all-empty-variable: all
     *
     * 2026.02.23 useRequestCompressType 추가.
     *  - uiAdapter.useRequestCompressType
     *  - nexacro.use-request-compresstype
     */
     PropertiesProvider propertiesProvider = PropertiesProvider.getInstance();

    //@Nullable
    @Value("${uiAdapter.useRequestCharset:}")
    public String useRequestCharset;
    //@Nullable
    @Value("${uiAdapter.useRequestContentType:}")
    private String useRequestContentType;
    @Value("${uiAdapter.useRequestCompressType:}")
    private String useRequestCompressType;

	public void setEtcProperty() {
		if(useRequestCharset==null || useRequestCharset.isEmpty())
			useRequestCharset = propertiesProvider.getEtcProperty("nexacro.use-request-charset");
		if(useRequestContentType==null || useRequestContentType.isEmpty())
			useRequestContentType = propertiesProvider.getEtcProperty("nexacro.use-request-contenttype");
        if(useRequestCompressType==null || useRequestCompressType.isEmpty()) {
            useRequestCompressType = propertiesProvider.getEtcProperty("nexacro.use-request-compresstype");
            if(useRequestCompressType==null || useRequestCompressType.isEmpty()) {
                useRequestCompressType = propertiesProvider.getEtcProperty("uiAdapter.useRequestCompressType");
            }
        }
        if(logger.isDebugEnabled()) {
            logger.debug("	ㅇ NexacroView useRequestCompressType [{}]",useRequestCompressType);
        }
	}
    
    public NexacroView() {
        setEtcProperty();
//    	Object bean = Etc.getBean("etcProperty");
//    	if(bean instanceof EtcPropertiesBase) {
//    		etcProperty = (EtcPropertiesBase) bean;
//    		if(etcProperty.hasProperties()) {
//    			setEtcProperty();
//    		}
//    	}
    }
	
    public String getDefaultContentType() {
        if(defaultContentType == null) {
            return PlatformType.CONTENT_TYPE_XML;
        } else {
            return defaultContentType;
        }
    }
	
    /*
     * 2022.06.10 추가
     * Response ContentType을  Request의 ContentType값으로 사용하기 위한 함수 추가 
     */
    public String getDefaultContentType(NexacroContext cachedData) {
        if(useRequestContentType != null && !useRequestContentType.isEmpty()) {
        	if(cachedData != null) {
        		defaultContentType = cachedData.getPlatformRequest().getContentType();
        	}
        	return defaultContentType;
        } else if (defaultContentType == null){
            return PlatformType.CONTENT_TYPE_XML;
        } else {
            return defaultContentType;
        }
    }

    /**
     * <p>데이터 전송시 사용되는 기본 ContentType을 설정한다.
     * 
     * @param defaultContentType  기본 ContentType
     * @see PlatformType#CONTENT_TYPE_XML
     * @see PlatformType#CONTENT_TYPE_SSV
     * @see PlatformType#CONTENT_TYPE_JSON	// 2021.09.14 추가.
     * 		소스 변동사항 없어서 수정하지 않음.
     */
    public void setDefaultContentType(String defaultContentType) {
        this.defaultContentType = defaultContentType;
    }

    public String getDefaultCharset() {
        if(defaultContentType == null) {
            return PlatformType.DEFAULT_CHAR_SET;
        } else {
            return defaultCharset;
        }
    }

	
    /*
     * 2022.06.10 추가
     * Response Charset을  Request의 Charset값으로 설정하기 위한 함수 추가 
     */
    public String getDefaultCharset(NexacroContext cachedData) {

        if(useRequestCharset != null && !useRequestCharset.isEmpty()) {
        	if(cachedData != null) {
            	defaultCharset = cachedData.getPlatformRequest().getCharset();
        	}
        	return defaultCharset;
        } else if (defaultContentType == null){
            return PlatformType.DEFAULT_CHAR_SET;
        } else {
            return defaultCharset;
        }
    }

    /**
     * <p>데이터 전송 시 사용되는 기본 charset을 설정한다.
     * @param defaultCharset 기본 charset
     */
    public void setDefaultCharset(String defaultCharset) {
        this.defaultCharset = defaultCharset;
    }

    @Override
	protected void renderMergedOutputModel(Map<String, Object> model,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		
    	try {
            // 2026.02.24 한 번 더 체크.
            //if(useRequestCompressType==null || useRequestCompressType.isEmpty())
            //    useRequestCompressType = propertiesProvider.getEtcProperty("nexacro.use-request-compresstype");

            Object object = model.get(NexacroConstants.ATTRIBUTE.NEXACRO_PLATFORM_DATA);
		    if(!(object instanceof PlatformData)) {
		        sendResponse(request, response);
		        return;
		    }
			sendResponse(request, response, (PlatformData) object);
    	} catch(Throwable e) {
         	// ExceptionResolver가 처리되지 않기 때문에 로그를 남기도록 한다.
         	logger.error("an error has occurred during platform data transfer", e);
         	if(e instanceof Exception) {
         		throw (Exception) e;
         	} else {
         		throw new PlatformException("an error has occurred during platform data transfer", e);
         	}
    	}
	}
		
    protected void sendResponse(HttpServletRequest request, HttpServletResponse response) throws PlatformException{
        sendResponse(request, response, generatePlatformData());
	}

    protected void sendResponse(HttpServletRequest request, HttpServletResponse response, PlatformData platformData) throws PlatformException{
        
        NexacroContext cachedData = getCachedData(request, response);
        
        HttpPlatformResponse platformResponse ;
        
        StopWatch sw = new StopWatch(getClass().getSimpleName());
        sw.start("rendering platformdata");
        
        try {
            if(cachedData != null) {
                if(cachedData.isFirstRowFired()) {
                    NexacroFirstRowHandler firstRowHandler = cachedData.getFirstRowHandler();
                    sendFirstRowData(platformData, firstRowHandler, true);
                    
                    return;
                } else {
                    platformResponse = cachedData.getPlatformResponse();

                    // 2026.02.20 ContentType 설정방법 수정.
                    setContentType(platformResponse, cachedData);
                    setCharset(platformResponse, cachedData);
                    // 2026.02.24 CompressType 설정 추가.
                    setCompressType(platformResponse, cachedData);
                    // 2021.10.06 
                    // getDefaultContentType()을 적용하도록 조건 추가 Start.
                    //platformResponse.setContentType(getDefaultContentType(cachedData));
                    //platformResponse.setCharset(getDefaultCharset(cachedData));
                    // getDefaultContentType()을 적용하도록 조건 추가 E n d. 
                    platformResponse.setData(platformData);
                    platformResponse.sendData();
                }
            } else {
                // default 혹은 request parsing..
                platformResponse = new HttpPlatformResponse(response);
                platformResponse.setContentType(getDefaultContentType());
                platformResponse.setCharset(getDefaultCharset());
                platformResponse.setData(platformData);
                platformResponse.sendData();
            }
        } finally {
        	if(logger.isDebugEnabled()) {
        		logger.debug("	★  ★  ★  NexacroView finally response type = [{}], compress [{}]",cachedData.getPlatformResponse().getContentType(), cachedData.getPlatformRequest().containsProtocolType(PROTOCOL_TYPE_ZLIB));
        	}
            sw.stop();
            if(performanceLogger.isTraceEnabled()) {
                String stopWatchLog = sw.prettyPrint()
                        .replace('\r', '_').replace('\n', '_'); // Replace CRLF
                performanceLogger.trace("Performance summary:\n{}", stopWatchLog);
            }
        }
        
        if(logger.isWarnEnabled()) {
            //logger.warn("★  ★  ★   response platformdata=[{}]", new Debugger().detail(platformData));
        }
        
    }
    
    private void sendFirstRowData(PlatformData platformData, NexacroFirstRowHandler firstRowHandler, boolean isCallEndMethod)
            throws PlatformException {
        
    	setFirstRowStatusDataSet(platformData);
    	
        removeTransferData(firstRowHandler, platformData);
        
        if(logger.isWarnEnabled()) {
            logger.warn("response platformdata=[{}]", new Debugger().detail(platformData));
        }
        firstRowHandler.sendPlatformData(platformData);
        if(isCallEndMethod) {
            NexacroFirstRowAccessor.end(firstRowHandler);
        }
    }
    
    private void setFirstRowStatusDataSet(PlatformData platformData){
    	
    	Variable errorCodeVariable = platformData.getVariable(NexacroConstants.ERROR.ERROR_CODE);
    	if(errorCodeVariable != null && errorCodeVariable.getInt() < 0) {
    		// error status
    		Variable errorMsgVariable = platformData.getVariable(NexacroConstants.ERROR.ERROR_MSG);
    		platformData.addDataSet(NexacroUtil.createFirstRowStatusDataSet(errorCodeVariable.getInt(), errorMsgVariable != null? errorMsgVariable.getString(): null));
    	} else {
    		// success status
    		platformData.addDataSet(NexacroUtil.createFirstRowStatusDataSet(NexacroConstants.ERROR.DEFAULT_ERROR_CODE, null));
    	}
    	
    }


    /**
     * Removes transfer-related data from the given PlatformData based on the state
     * of the provided NexacroFirstRowHandler. This includes removing variables and
     * datasets that are already sent during the first row handling process.
     *
     * @param firstRowHandler the handler used to determine the transfer state of
     *                        variables and datasets
     * @param platformData    the PlatformData instance containing variables and
     *                        datasets to be processed and potentially removed
     */
    private void removeTransferData(NexacroFirstRowHandler firstRowHandler, PlatformData platformData) {
        
        VariableList variableList = platformData.getVariableList();
        
        if(NexacroFirstRowAccessor.getSendOutDataSetCount(firstRowHandler) > 0) {
            // dataset already sended.. 
            int size = variableList.size();
            for(int i=0; i<size; i++) {
                if (logger.isInfoEnabled()) {
                    logger.info("DataSet already sent. Ignoring variable={}", variableList.get(i).getName());
                }
            }
            platformData.setVariableList(new VariableList());
            
        } else {
            removeTransferVariables(firstRowHandler, variableList);
        }
        
        DataSetList dataSetList = platformData.getDataSetList();
        removeTransferDataSets(firstRowHandler, dataSetList);
        
    }

    /**
     * Removes variables from the provided VariableList that are already sent
     * out, based on the provided NexacroFirstRowHandler.
     *
     * @param firstRowHandler the handler that determines which variables have
     *                        been sent out during the first row handling process
     * @param variableList the list of variables from which the sent out
     *                     variables will be removed
     */
    private void removeTransferVariables(NexacroFirstRowHandler firstRowHandler, VariableList variableList) {
        
        String[] sendOutVariableNames = NexacroFirstRowAccessor.getSendOutVariableNames(firstRowHandler);
        Variable var ;
        int variableListSize = variableList.size();
        for(int variableListIndex = variableListSize-1 ; variableListIndex>=0 ; variableListIndex--) {
            var = variableList.get(variableListIndex);
            if(var == null) {
                continue;
            }
            boolean isSended = false;
            for(int sendedVariableIndex = 0; sendedVariableIndex<sendOutVariableNames.length; sendedVariableIndex++) {
                if(var.getName().equals(sendOutVariableNames[sendedVariableIndex])) {
                    isSended = true;
                    break;
                }
            }
            if(isSended) {
                variableList.remove(variableListIndex);
            }
        }
    }

    /**
     * Removes datasets from the provided DataSetList that are already sent out,
     * based on the provided NexacroFirstRowHandler.
     *
     * @param firstRowHandler the handler that determines which datasets have
     *                        been sent out during the first row handling process
     * @param dataSetList the list of datasets from which the sent out
     *                    datasets will be removed
     */
    private void removeTransferDataSets(NexacroFirstRowHandler firstRowHandler, DataSetList dataSetList) {
        
        String[] sendOutDataSetNames = NexacroFirstRowAccessor.getSendOutDataSetNames(firstRowHandler);
        DataSet dataSet ;
        int dataSetListSize = dataSetList.size();
        for(int datasetListIndex = dataSetListSize - 1 ; datasetListIndex >= 0; datasetListIndex--) {
            dataSet = dataSetList.get(datasetListIndex);
            if(dataSet == null) {
                continue;
            }
            boolean isSended = false;
            for(int sendedDataSetIndex = 0; sendedDataSetIndex<sendOutDataSetNames.length; sendedDataSetIndex++) {
                if(dataSet.getName().equals(sendOutDataSetNames[sendedDataSetIndex])) {
                    isSended = true;
                    break;
                }
            }
            if(isSended) {
                dataSetList.remove(datasetListIndex);
            }
        }
    }
    
    private PlatformData generatePlatformData() {
        PlatformData platformData = new PlatformData();
        platformData.addVariable(Variable.createVariable(NexacroConstants.ERROR.ERROR_CODE, NexacroConstants.ERROR.DEFAULT_ERROR_CODE));
        return platformData;
    }
    
    private NexacroContext getCachedData(HttpServletRequest request, HttpServletResponse response) {
        
        // get already parsed request
        NexacroContext nexacroContext = NexacroContextHolder.getNexacroContext();
        if(nexacroContext != null) {
            return nexacroContext;
        }
        
        return null;
    }

    private void setContentType(HttpPlatformResponse response, NexacroContext nexacroContext) {
        String[] propertyKeys = {
                "uiAdapter.useRequestContentType",
                "nexacro.use-request-contenttype"
        };

        for (String key : propertyKeys) {
            String value = propertiesProvider.getEtcProperty(key);
            if (value != null && !value.isEmpty()) {
                response.setContentType(nexacroContext.getPlatformRequest().getContentType());
                return;
            }
        }

        response.setContentType(defaultContentType);
    }

    //ToDo :: setCompressType 추가.
    // default : "", 압축 : PlatformType.PROTOCOL_TYPE_ZLIB
    private void setCompressType(HttpPlatformResponse response, NexacroContext nexacroContext) {
        String[] propertyKeys = {
                "uiAdapter.useRequestCompressType",
                "nexacro.use-request-compresstype"
        };
        //HttpPlatformRequest request = nexacroContext.getPlatformRequest();
        for (String key : propertyKeys) {
            String value = propertiesProvider.getEtcProperty(key);
            if (logger.isInfoEnabled()) {
                logger.info(" ★ ★ NexacroView setCompressType ={}/{}", value, nexacroContext.isGzipSupported() );
            }
            if ( (value != null && !value.isEmpty())
                && (nexacroContext.isGzipSupported())) {
                response.addProtocolType(PROTOCOL_TYPE_ZLIB);
                if (logger.isInfoEnabled()) {
                    logger.info("PlatformType.PROTOCOL_TYPE_ZLIB={}/{}", nexacroContext.isGzipSupported(), response.containsProtocolType(PROTOCOL_TYPE_ZLIB) );
                }
                return;
            }
        }
    }

    private void setCharset(HttpPlatformResponse response, NexacroContext nexacroContext) {
        String[] propertyKeys = {
            "uiAdapter.useRequestCharset",
            "nexacro.use-request-charset"
        };
        
        for (String key : propertyKeys) {
            String value = propertiesProvider.getEtcProperty(key);
            if (value != null && !value.isEmpty()) {
                response.setCharset(nexacroContext.getPlatformRequest().getCharset());
                return;
            }
        }
        
        response.setCharset(defaultCharset);
    }
}
