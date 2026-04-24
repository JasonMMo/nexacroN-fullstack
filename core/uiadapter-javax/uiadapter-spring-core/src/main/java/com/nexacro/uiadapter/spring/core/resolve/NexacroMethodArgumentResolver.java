package com.nexacro.uiadapter.spring.core.resolve;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nexacro.uiadapter.spring.core.util.PropertiesProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataSetList;
import com.nexacro.java.xapi.data.PlatformData;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.java.xapi.data.VariableList;
import com.nexacro.java.xapi.tx.HttpPlatformRequest;
import com.nexacro.java.xapi.tx.HttpPlatformResponse;
import com.nexacro.java.xapi.tx.PlatformException;
import com.nexacro.uiadapter.spring.core.NexacroConstants;
import com.nexacro.uiadapter.spring.core.UiadapterConstants;
import com.nexacro.uiadapter.spring.core.annotation.ParamDataSet;
import com.nexacro.uiadapter.spring.core.annotation.ParamDataSetGroup;
import com.nexacro.uiadapter.spring.core.annotation.ParamVariable;
import com.nexacro.uiadapter.spring.core.context.NexacroContext;
import com.nexacro.uiadapter.spring.core.context.NexacroContextHolder;
import com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertDefinition;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertException;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertListener;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverter;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverterFactory;
import com.nexacro.uiadapter.spring.core.util.Etc;
import com.nexacro.uiadapter.spring.core.util.EtcPropertiesBase;


/**
 * 
 * <p>요청에 대한 메서드 파라매터(<code>PlatformData</code>) 데이터 변환을 위한 {@link HandlerMethodArgumentResolver}이다.
 *
 * <p>정의된 형식은 다음과 같다.
 * <blockquote>
 *    <table border="thin">
 *        <tr class="TableSubHeadingColor">
 *            <th>class</th>
 *            <th>description</th>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>List&lt;?&gt; list</td>
 *            <td>DataSet을 List로 형식으로 변환한다. @ParamDataSet과 사용한다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>Primitive Types</td>
 *            <td>Variable을 Primitive 형식으로 변환한다. @ParamVariable과 사용한다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>PlatformData</td>
 *            <td>nexacro platform의 데이터 통신의 기본 단위이다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>DataSetList</td>
 *            <td>DataSet들의 저장하는 DataSetList이다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>VariableList</td>
 *            <td>Variable들을 저장하는 VariableList이다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>DataSet</td>
 *            <td>2차원 형태의 데이터 구조인 DataSet이다. @ParamDataSet과 사용한다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>Variable</td>
 *            <td>식별자와 값으로 구성 된 데이터 구조인 Variable이다. @ParamVariable과 사용한다.</td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>HttpPlatformRequest</td>
 *            <td>HTTP 요청으로 데이터를 수신 받는다. </td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>HttpPlatformResponse</td>
 *            <td>HTTP 응답으로 데이터를 송신한다. </td>
 *        </tr>
 *        <tr class="TableRowColor">
 *            <td>NexacroFirstRowHandler</td>
 *            <td>데이터 분한 전송을 하기 위한 Handler이다.</td>
 *        </tr>
 *    </table>
 * </blockquote>
 * 
 * @author Park SeongMin
 * @since 07.27.2015
 * @version 1.0
 * @see NexacroHandlerMethodReturnValueHandler
 * @see NexacroFirstRowHandler
 * @see PlatformData
 */
public class NexacroMethodArgumentResolver implements HandlerMethodArgumentResolver {

    private static final Logger logger = LoggerFactory.getLogger(NexacroMethodArgumentResolver.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger(NexacroConstants.PERFORMANCE_LOGGER);

    // convert에서 사용가능한 event listener를 등록할 수 있어야 한다.
    private static final Map<Class<?>, Object> PRIMITIVETYPEDEFAULTVALUEMAP = new HashMap<Class<?>, Object>(8);
    
    
    
    private List<NexacroConvertListener> convertListeners;
    
    static {
        PRIMITIVETYPEDEFAULTVALUEMAP.put(byte.class,    0);
        PRIMITIVETYPEDEFAULTVALUEMAP.put(short.class,   0);
        PRIMITIVETYPEDEFAULTVALUEMAP.put(int.class,     0);
        PRIMITIVETYPEDEFAULTVALUEMAP.put(long.class,    0L);
        PRIMITIVETYPEDEFAULTVALUEMAP.put(float.class,   0.0f);
        PRIMITIVETYPEDEFAULTVALUEMAP.put(double.class,  0.0d);
        PRIMITIVETYPEDEFAULTVALUEMAP.put(char.class,    '\u0000');
        PRIMITIVETYPEDEFAULTVALUEMAP.put(boolean.class, false);
    }
    
    /* 
     * 2022.05.18 추가
     * [spring f/w 환경 지원]
     * <dispatcher-servlet.xml>
     * #util:properties 설정에서 값 취득.
     * # @ParamDataSet, @ParamVariable 으로 들어오는 string 값 trim 적용.  \n \t 도 제거됨
     * 
     * <주의> spring f/w 환경에서는 uiadapter 1.0.1 버전 사용해야 함.
     * - xml 설정
     * 2023.07.26 수정  boot용 환경변수 취득 방식 변경.
     * <주의> spring boot 에서 uiadapter 1.3 버전부터 적용
     * 	application.yml 설정 방식
	 *	nexacro:
     *    client-column-case: 
     *    db-column-case: 
	 *	  who-are-you: 
	 *	  use-request-charset: 
	 *	  use-request-contenttype: 
	 *	  trim-paramdataset:
	 *	  trim-paramvariable:
     *
     * 2024.05.24 수정  dataset을 group으로 전송시 파싱.
     *
     * (2025.12.30 :: spel방식은 더이상 사용하지 않음)
     * 	<util:properties id="EtcProperty">
     * 		<prop key="uiAdapter.trimParamDataSet">all</prop>
     * 		<prop key="uiAdapter.trimParamVariable">all</prop>
     *
     * 2026.02.23 spel방식도 다시 적용.
     */
    PropertiesProvider propertiesProvider = PropertiesProvider.getInstance();

    //private EtcPropertiesBase etcProperty;

    //@Value("#{EtcProperty['uiAdapter.trimParamDataSet'] ?:null}")
    @Value("${uiAdapter.trimParamDataSet:}")
    private String trimParamDataSet;
    //@Nullable
    //@Value("#{EtcProperty['uiAdapter.trimParamVariable'] ?:null}")
    @Value("${uiAdapter.trimParamVariable:}")
    private String trimParamVariable;
    
    //@Value("#{EtcProperty['uiAdapter.replaceAllEmptyVariable'] ?:null}")
    @Value("${uiAdapter.replaceAllEmptyVariable:}")
    private String replaceAllEmptyVariable;


	public void setEtcProperty() {
		if(trimParamDataSet==null || trimParamDataSet.isEmpty())
			trimParamDataSet = propertiesProvider.getEtcProperty("nexacro.trim-paramdataset");
		if(trimParamVariable==null || trimParamVariable.isEmpty())
			trimParamVariable = propertiesProvider.getEtcProperty("nexacro.trim-paramvariable");
		if(replaceAllEmptyVariable==null || replaceAllEmptyVariable.isEmpty())
			replaceAllEmptyVariable = propertiesProvider.getEtcProperty("nexacro.replace-all-empty-variable");
        /*
    	Object bean = Etc.getBean("etcProperty");
    	if(bean instanceof EtcPropertiesBase) {
    		etcProperty = (EtcPropertiesBase) bean;
    		if(etcProperty.hasProperties()) {
    			setEtcProperty();
    		}
    	}
        if(logger.isDebugEnabled()) {
            logger.debug("NexacroMethodArgumnetResolver() {}", this);
        }
        */
	}

    public NexacroMethodArgumentResolver() {
        setEtcProperty();
    }
    
    public void setConvertListeners(List<NexacroConvertListener> convertListeners) {
        this.convertListeners = convertListeners;
    }
    
    @Override
    public boolean supportsParameter(MethodParameter param) {
        if(isDefaultParameter(param)) {
            return true;
        } else return isExtendedParameter(param);
    }
    
    private boolean isDefaultParameter(MethodParameter param) {
        Class paramClass = param.getParameterType();
        if(PlatformData.class.equals(paramClass)) {
            return true;
        } else if(DataSetList.class.equals(paramClass)) {
            return true;
        } else if(VariableList.class.equals(paramClass)) {
            return true;
        } else if(HttpPlatformRequest.class.equals(paramClass)) {
            return true;
        } else if(HttpPlatformResponse.class.equals(paramClass)) {
            return true;
        } else if(NexacroFirstRowHandler.class.equals(paramClass)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isExtendedParameter(MethodParameter param) {
        if(param.getParameterAnnotation(ParamDataSet.class) != null) {
            return true;
        } else if(param.getParameterAnnotation(ParamDataSetGroup.class) != null) {
            return true;
        } else if(param.getParameterAnnotation(ParamVariable.class) != null) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public Object resolveArgument(MethodParameter param, ModelAndViewContainer arg1, NativeWebRequest nativeWebRequest,
            WebDataBinderFactory arg3) throws Exception {
        
        NexacroContext nexacroCachedData = prepareResolveArguments(nativeWebRequest);
        
        StopWatch sw = new StopWatch(getClass().getSimpleName());
        sw.start("resolve argument (" +param.getParameterName()+ ")");
        
        Object obj ;
        try {
            
            if(isDefaultParameter(param)) {
                obj = extractDefaultParameter(param, nexacroCachedData);
            } else {
                obj = extractExtendedParameter(param, nexacroCachedData);
            }
        } finally {
            sw.stop();
            if(performanceLogger.isTraceEnabled()) {
                performanceLogger.trace(sw.prettyPrint());
            }
        }
        
        obj = postResolveArguments(param, obj);
        
        return obj;
    }
    
    /**
     * <pre>
     * 데이터 변환을 수행하기 전 Request로 부터 Platform 데이터로의 변환을 수행한다.
     * </pre>
     *
     * @param nativeWebRequest webRequest
     * @return NexacroCachedData
     * @throws PlatformException Nexacro 요청에 대한 Exception
     */
    private NexacroContext prepareResolveArguments(NativeWebRequest nativeWebRequest) throws PlatformException {
        NexacroContext cache = parsePlatformRequestOrGetAttribute(nativeWebRequest);
        return cache;
    }
    
    private Object postResolveArguments(MethodParameter param, Object resolvedObject)  {
        
        if(resolvedObject != null) {
            return resolvedObject;
        }
        
        // primitive type의 경우 해당 데이터 타입으로 전달해야 한다.
        // null로 전달할 경우 IllegalArgumentException이 발생한다.
        Class<?> parameterType = param.getParameterType();    
        if(PRIMITIVETYPEDEFAULTVALUEMAP.containsKey(parameterType)) {
            return PRIMITIVETYPEDEFAULTVALUEMAP.get(parameterType);
        }
        
        return null;
    }
    
    private Object extractDefaultParameter(MethodParameter param, NexacroContext nexacroCachedData) {
        
        Class<?> parameterType = param.getParameterType();
        
        if(PlatformData.class.equals(parameterType)) {
            return nexacroCachedData.getPlatformData();
        } else if(DataSetList.class.equals(parameterType)) {
            return nexacroCachedData.getPlatformData().getDataSetList();
        } else if(VariableList.class.equals(parameterType)) {
            return nexacroCachedData.getPlatformData().getVariableList();
        } else if(HttpPlatformRequest.class.equals(parameterType)) {
            return nexacroCachedData.getPlatformRequest();
        } else if(HttpPlatformResponse.class.equals(parameterType)) {
            return nexacroCachedData.getPlatformResponse();
        } else if(NexacroFirstRowHandler.class.equals(parameterType)) {
            return nexacroCachedData.getFirstRowHandler();
        } else {
            return null;
        }
    }
    
    private Object extractExtendedParameter(MethodParameter param, NexacroContext nexacroCachedData) throws NexacroConvertException {
        
        if(param.getParameterAnnotation(ParamDataSet.class) != null) {
            return extractDataSetParameter(param, nexacroCachedData);
        // 2024.05.21 group tr 처리를 위한 분기 추가.
        } else if(param.getParameterAnnotation(ParamDataSetGroup.class) != null) {
            return extractDataSetGroupParameter(param, nexacroCachedData);
        } else if(param.getParameterAnnotation(ParamVariable.class) != null) {
            return extractVariableParameter(param, nexacroCachedData);
        } else {
            return null;
        }
    }
    
    private Object extractDataSetParameter(MethodParameter param, NexacroContext nexacroCachedData) throws NexacroConvertException {
        
    	// TODO paging http://terasolunaorg.github.io/guideline/1.0.x/en/ArchitectureInDetail/Pagination.html
    	
        Class<?> parameterType = param.getParameterType();
        ParamDataSet paramDataSet = param.getParameterAnnotation(ParamDataSet.class);

        String dsName = null;
        if (paramDataSet != null) {
            dsName = paramDataSet.name();
        }
        DataSet dataSet = nexacroCachedData.getPlatformData().getDataSet(dsName);
        if(dataSet == null) {
        	if(logger.isDebugEnabled()) {
                logger.debug("@ParamDataSet {} argument is null.", dsName);
            }
        	if (paramDataSet.required()) {
				handleMissingValue(paramDataSet.name(), param);
			}
            return null;
        } 
        else if(trimParamDataSet != null && !trimParamDataSet.isEmpty()) {
        	Etc.trimDataSet(dataSet);
        }
       
        // return dataset
        if(DataSet.class.equals(parameterType)) {
            return dataSet;
        }
        
//        // support only list parameter
//        if(!List.class.equals(parameterType)) {
//            throw new IllegalArgumentException(ParamDataSet.class.getSimpleName()+" annotation support List<?> and DataSet parameter.");
//        }

        Class convertedGenericType = findGenericType(param);
        if(convertedGenericType == null) {
            // Generic이 null 일 경우에 Map으로 할지? 혹은 바로 오류를 내보낼지 처리 하도록 하자.
            throw new IllegalArgumentException(ParamDataSet.class.getSimpleName()+" annotation must be List<?>.");
        }
        
        ConvertDefinition definition = new ConvertDefinition();
        definition.setName(dsName);
        definition.setGenericType(convertedGenericType);

        Class<?> fromType = DataSet.class;

        NexacroConverter<DataSet, Object> converter = NexacroConverterFactory.getConverter(fromType, parameterType);
        if(converter == null) {
            throw new IllegalArgumentException("invalid @ParamDataSet. supported type={DataSet, Object<?>}");
        }
        
        try {
            addConvertListeners(converter);
            return converter.convert(dataSet, definition);
        } finally {
            removeConvertListeners(converter);
        }
        
    }
    
    /**
     * {@code @date} 2024.05.21 추가
     * {@code @desc} 1개 이상의 procedure를 transactional하게 처리할 경우 group으로 묶는다.
     * 		 이때 @ParamDataSetGroup을 사용하여 group으로  묶인 dataset을 추출한다.  
			ex) xfdl
					"GROUP_TR=dsList0,dsList1,dsList2"
				java
					saveMultiProcedure(@ParamDataSetGroup(name = UiadapterConstants.DATASET_GROUP, required = true) LinkedHashMap<String, List<Map<String, Object>>> saveGroupMap
     * @param param group에서 사용하는 parameter
     * @param nexacroCachedData nexacro Cache Data
     * @return LinkedHashMap<String, List<Map<String, Object>>>
     * @throws NexacroConvertException Nexacro Convert 중 발생하는 Exception
     */
    private Object extractDataSetGroupParameter(MethodParameter param, NexacroContext nexacroCachedData) throws NexacroConvertException {
        
    	// TODO paging http://terasolunaorg.github.io/guideline/1.0.x/en/ArchitectureInDetail/Pagination.html
    	
    	LinkedHashMap<String, Object> saveGroupMap = new LinkedHashMap<String, Object>();
    	
    	// 2024.05.27 그룹의 경우 LinkedHashMap<String, List<Map<String, Object>>> 임.
    	// MethodParameter로 넘어오는 param의 값을 map group 의 처리로직 추가.
    	// DataSetGroupParameter의 경우 NestedParameterType을 사용함.

        final Class<?> parameterClassType = getClassType(param);

        ParamDataSetGroup paramDataSetGroup = param.getParameterAnnotation(ParamDataSetGroup.class);
        
        PlatformData platformData = nexacroCachedData.getPlatformData();
        Variable variable = platformData.getVariable(UiadapterConstants.DATASET_GROUP);
        
        if(variable!=null) {
        	String[] dsNameArr = variable.getString().split(",");

            for (String dsName : dsNameArr) {

                DataSet dataSet = platformData.getDataSet(dsName);
                if (dataSet == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("@ParamDataSet {} argument is null.", dsName);
                    }
                    if (paramDataSetGroup.required()) {
                        handleMissingValue(paramDataSetGroup.name(), param);
                    }
                    return null;
                } else if (trimParamDataSet != null && !trimParamDataSet.equals("")) {
                    Etc.trimDataSet(dataSet);
                }

// return dataset
                if (DataSet.class.equals(parameterClassType)) {
                    return dataSet;
                }

                // 2024.05.25 :: DATASET_GROUP, LinkedHashMap<String, List<Map<String, Object>>> 형태의 경우 findNestedGenericType()를 사용.
                Class convertedGenericType = findNestedGenericTypeMap(param);
                if (convertedGenericType == null) {
                    // Generic이 null 일 경우에 Map으로 할지? 혹은 바로 오류를 내보낼지 처리 하도록 하자.
                    throw new IllegalArgumentException(ParamDataSetGroup.class.getSimpleName() + " annotation must be Map<String, List<?>>.");
                }

                ConvertDefinition definition = new ConvertDefinition();
                definition.setName(dsName);
                definition.setGenericType(convertedGenericType);

                Class<?> fromType = DataSet.class;
                Class<?> toType = parameterClassType;

                NexacroConverter<DataSet, Object> converter = NexacroConverterFactory.getConverter(fromType, toType);
                if (converter == null) {
                    throw new IllegalArgumentException("invalid @ParamDataSetGroup. supported type={DataSet, Object<?>}");
                }

                try {
                    addConvertListeners(converter);
                    saveGroupMap.put(dsName, converter.convert(dataSet, definition));
                } finally {
                    removeConvertListeners(converter);
                }

            }
        }
        return saveGroupMap;
    }

    private Class<?> getClassType(MethodParameter param) {
        Class<?> parameterGroupType = param.getNestedParameterType();
        Class<?> parameterType = param.getNestedParameterType();
        if(!List.class.equals(parameterGroupType)) {

            Type genericParameterType = param.getGenericParameterType();
            if (genericParameterType instanceof ParameterizedType) {

                Type[] types = ((ParameterizedType) genericParameterType).getActualTypeArguments();
                if(types[1] instanceof ParameterizedType) {
                    parameterType = (Class) ((ParameterizedType) types[1]).getRawType();
                }
            }
        }
        return parameterType;
    }

    private Object extractVariableParameter(MethodParameter param, NexacroContext nexacroCachedData) throws NexacroConvertException {
        
        Class<?> parameterType = param.getParameterType();
        // support only list parameter
        
        ParamVariable paramVariable = param.getParameterAnnotation(ParamVariable.class);
        String varName = null;
        if (paramVariable != null) {
            varName = paramVariable.name();
        }
        Variable variable = nexacroCachedData.getPlatformData().getVariable(varName);
        if(variable == null) {
            if(logger.isDebugEnabled()) {
                logger.debug("@ParamVariable '{}' argument is null.", varName);
            }
            if (paramVariable.required()) {
				handleMissingValue(paramVariable.name(), param);
			}
            return null;
        }

        
        if(trimParamVariable != null && !trimParamVariable.isEmpty()) {
        	Etc.trimVariable(variable);
        }
        
        if(replaceAllEmptyVariable != null && replaceAllEmptyVariable.isEmpty()) {
        	Etc.replaceAllEmptyVariable(variable);
        }

        // reutrn variable
        if(Variable.class.equals(parameterType)) {
            return variable;
        }
        
        Class<?> fromType = Variable.class;
        NexacroConverter<Variable, Object> converter = NexacroConverterFactory.getConverter(fromType, parameterType);
        if(converter == null) {
            throw new IllegalArgumentException("invalid @ParamVariable. supported type={Variable, Object, String, int, boolean, long, float, double, BigDecimal, Date, byte[]} ");
        }
        
        ConvertDefinition definition = new ConvertDefinition();
        definition.setName(varName);
        definition.setGenericType(parameterType);
        
        try {
            addConvertListeners(converter);
            return converter.convert(variable, definition);
        } finally {
            removeConvertListeners(converter);
        }
        
    }
    
    private Class findGenericType(MethodParameter param) {

    	Class<?> parameterType = param.getParameterType();

    	if (!List.class.equals(parameterType)) {
    		return parameterType;
    	} else {
    		Type genericParameterType = param.getGenericParameterType();
    		if (genericParameterType instanceof ParameterizedType) {
    			Type[] types = ((ParameterizedType) genericParameterType).getActualTypeArguments();
    			if(types[0] instanceof ParameterizedType) {
    				// List<Map<String, Object>>
    				return (Class) ((ParameterizedType) types[0]).getRawType();
    			} else {
    				// List<Bean>
    				// List<Map>
    				return (Class) types[0];
    			}
    		}
    	}

    	return null;
    }
    
    /*
     * 2024.05.27
     * @param LinkedHashMap<List<Map<String, Object>>>
     * dataset을 group으로 묶을 경우 중첩된 파라미터의 값을 추출하여 타입을 리턴한다.
     */
    private Class findNestedGenericTypeMap(MethodParameter param) {

    	Class<?> parameterType = param.getParameterType();

    	if (!List.class.equals(parameterType)) {
    		Type genericParameterType = param.getGenericParameterType();
    		if (genericParameterType instanceof ParameterizedType) {
    			Type[] types = ((ParameterizedType) genericParameterType).getActualTypeArguments();
    			if(types[1] instanceof ParameterizedType) {
    				
    				Type[] nestedTypes =((ParameterizedType) types[1]).getActualTypeArguments();
    				// LinkedHashMap<List<Map<String, Object>>>
    				if(nestedTypes[0] instanceof ParameterizedType ) {
    					return (Class) ((ParameterizedType) nestedTypes[0]).getRawType();

    				// List<Map<String, Object>>
    				} else {
	    				return (Class) ((ParameterizedType) types[0]).getRawType();
    				}
    			} else {
    				// List<Bean>
    				// List<Map>
    				return (Class) types[0];
    			}
    		}

    		//    		return parameterType;
    	} else {
    		Type genericParameterType = param.getGenericParameterType();
    		if (genericParameterType instanceof ParameterizedType) {
    			Type[] types = ((ParameterizedType) genericParameterType).getActualTypeArguments();
    			if(types[0] instanceof ParameterizedType) {
    				
    				Type[] nestedTypes =((ParameterizedType) types[0]).getActualTypeArguments();
    				// List<List<Map<String, Object>>>
    				if(nestedTypes[0] instanceof ParameterizedType ) {
    					return (Class) ((ParameterizedType) nestedTypes[0]).getRawType();

    				// List<Map<String, Object>>
    				} else {
	    				return (Class) ((ParameterizedType) types[0]).getRawType();
    				}
    			} else {
    				// List<Bean>
    				// List<Map>
    				return (Class) types[0];
    			}
    		}
    	}

    	return null;
    }
    /*
     * 2024.05.25
     * @param List<List<Map<String, Object>>>
     * dataset을 group으로 묶을 경우 중첩된 파라미터의 값을 추출하여 타입을 리턴한다.
     */
    private Class findNestedGenericType(MethodParameter param) {

    	Class<?> parameterType = param.getParameterType();

    	if (!List.class.equals(parameterType)) {
    		return parameterType;
    	} else {
    		Type genericParameterType = param.getGenericParameterType();
    		if (genericParameterType instanceof ParameterizedType) {
    			Type[] types = ((ParameterizedType) genericParameterType).getActualTypeArguments();
    			if(types[0] instanceof ParameterizedType) {
    				
    				Type[] nestedTypes =((ParameterizedType) types[0]).getActualTypeArguments();
    				// List<List<Map<String, Object>>>
    				if(nestedTypes[0] instanceof ParameterizedType ) {
    					return (Class) ((ParameterizedType) nestedTypes[0]).getRawType();

    				// List<Map<String, Object>>
    				} else {
	    				return (Class) ((ParameterizedType) types[0]).getRawType();
    				}
    			} else {
    				// List<Bean>
    				// List<Map>
    				return (Class) types[0];
    			}
    		}
    	}

    	return null;
    }

 
    
	protected void handleMissingValue(String name, MethodParameter parameter) throws NexacroConvertException {
		throw new MissingNexacroParameterException(name, parameter.getParameterType().getSimpleName());
	}
    
    private Object getAttribute(NativeWebRequest nativeWebRequest, String attrName) {
        return nativeWebRequest.getAttribute(attrName, RequestAttributes.SCOPE_REQUEST);
    }

    private void setAttribute(NativeWebRequest nativeWebRequest, String attrName, Object obj) {
        nativeWebRequest.setAttribute(attrName, obj, RequestAttributes.SCOPE_REQUEST);
    }

    /**
     * NexacroPlatform 형식의 Cache 데이터를 반환 한다.
     *
     * @throws PlatformException Nexacro 요청에 대한 Exception
     */
    private NexacroContext parsePlatformRequestOrGetAttribute(NativeWebRequest nativeWebRequest) throws PlatformException {
        
        // when already parsed. ex - methodArgumentResolver or interceptor 
        NexacroContext cache = NexacroContextHolder.getNexacroContext();
        if(cache != null) {
            return  cache;
        }
        
        HttpServletRequest servletRequest = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        HttpServletResponse servletResponse = (HttpServletResponse) nativeWebRequest.getNativeResponse();
        
        cache = NexacroContextHolder.getNexacroContext(servletRequest, servletResponse);
        return cache;
    }
 
    private void addConvertListeners(NexacroConverter converter) {
        if(this.convertListeners == null) {
            return;
        }
        for(NexacroConvertListener listener: this.convertListeners) {
            converter.addListener(listener);
        }
    }
    
    private void removeConvertListeners(NexacroConverter converter) {
        if(this.convertListeners == null) {
            return;
        }
        for(NexacroConvertListener listener: this.convertListeners) {
            converter.removeListener(listener);
        }
    }
    
}
