package com.nexacro.uiadapter.spring.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.Variable;

import com.nexacro.uiadapter.spring.core.context.SpringAppContext;

import software.amazon.smithy.utils.CaseUtils;

/**
 * nexacro platform에서 static하게 사용할 함수 모음.
 * 
 * @author Tech Service
 * @since 2022.05.18
 * 	- 수정 : 2022.07.27 : trimDataSet() 처리시에 rowType값이 변경됨. rowType값 변경을 막도록 stopStoreDataChanges() 처리함.
 * 	- 수정 : 2023.01.31 : regexTrim() 다양한 형태의 string 앞뒤 공백문자 trim.
 * 	- 수정 : 2023.07.26 : convertToCamelCase() ui의 컬럼 유형과 DB의 컬럼 유형이 다를 경우 컬럼 case를 변환하는 기능 추가.
 * 	- 수정 : 2025.12.22 : PropertiesProvider 추가, trimDataSet(DataSet ds, String trimType) 추가
 * @version 1.0
 */
public class Etc {
	
	private static final Logger logger = LoggerFactory.getLogger(Etc.class);

    /**
     * Spring Environment를 이용한 Properties Provider 반환
     */
    public static PropertiesProvider getSpringEnvironmentProvider() {
        return PropertiesProvider.getInstance();
    }

    /**
     * Spring Environment 또는 EtcPropertiesBase에서 프로퍼티 조회
     * Spring 환경이면 Environment를, 아니면 EtcPropertiesBase를 사용
     */
    public static String getProperty(String key) {
        try {
            // Spring 환경 확인
            ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
            if (applicationContext != null) {
                PropertiesProvider provider = getSpringEnvironmentProvider();
                return provider.getEtcProperty(key);
            }
        } catch (Exception e) {
            logger.debug("Spring Environment not available, falling back to EtcPropertiesBase", e);
        }

        // Fallback to EtcPropertiesBase
        Object bean = getBean("etcProperty");
        if (bean instanceof EtcPropertiesBase) {
            EtcPropertiesBase etcProperty = (EtcPropertiesBase) bean;
            return etcProperty.getEtcProperty(key);
        }

        return "";
    }

    /**
     * Spring Environment 또는 EtcPropertiesBase에서 프로퍼티 조회 (기본값 포함)
     */
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

	/*
	 * 	2023.07.26 bean이 아닌 class에서 bean객체를 얻어오기.
	 * 		- parameter : beanName 은 @Bean(name="etcProperty") 형식으로 등록한 bean name
	 */
    public static Object getBean(String beanName) {
		final SpringAppContext springAppContext = SpringAppContext.getInstance(); 
		ApplicationContext ctx = springAppContext.getApplicationContext(); 
        Object bean;
        if (ctx == null) {
            // 2025.12.22 ctx가 null일때 default etc bean 리턴.
            logger.debug("Init empty etc bean caused by an applicationContext is null. Returning default bean.");
            return new EtcPropertiesBase();
        }
        try{
        	bean = ctx.getBean(beanName);
        }catch(org.springframework.beans.factory.NoSuchBeanDefinitionException ex) {
        	bean = new EtcPropertiesBase();
        }
        
		return bean;
    }

	/*
	 * DataSet값 중 string형태의 값을 trim 처리한다.
	 */
	public static void trimDataSet(DataSet ds) {
		
		int rowIdx;
		int colIdx;
		ds.stopStoreDataChanges(true); // 수정 : 07.27.2022
		for(rowIdx=0; rowIdx<ds.getRowCount(); rowIdx++) {
			
			for(colIdx=0; colIdx<ds.getColumnCount(); colIdx++) {
				
				if(ds.getColumn(colIdx).getDataType()==2 && ds.getString(rowIdx, colIdx)!=null) { // String만 trim.
                    logger.debug("trimDataSet().[{}]->[{}]", ds.getString(rowIdx, colIdx), Etc.regexTrim(ds.getString(rowIdx, colIdx)));
					ds.set(rowIdx, colIdx, Etc.regexTrim(ds.getString(rowIdx, colIdx, "")));
				}
				
			}
		}
		ds.startStoreDataChanges(true); // 수정 : 07.27.2022
	}

    /*
     * 추가 : 2025.12.22 (jakarta버전과 맞춤)
     * DataSet값 중 string형태의 값을 right, left trim을 처리한다.
     * 	trimParamDataSet = rTrim
     * 	trimParamDataSet = lTrim
     */
    public static void trimDataSet(DataSet ds, String trimType) {

        if(trimType!=null && trimType.equals("all")) {
            trimDataSet(ds);
        } else {
            int rowIdx;
            int colIdx;
            ds.stopStoreDataChanges(true); // 수정 : 07.27.2022
            for(rowIdx=0; rowIdx<ds.getRowCount(); rowIdx++) {

                for(colIdx=0; colIdx<ds.getColumnCount(); colIdx++) {

                    if(ds.getColumn(colIdx).getDataType()==2 && ds.getString(rowIdx, colIdx)!=null) { // String만 trim.
                        if(trimType!=null && trimType.equalsIgnoreCase("rTrim")) {
                            logger.debug("trimDataSet().["+ds.getString(rowIdx, colIdx)+"]->["+Etc.rTrim(ds.getString(rowIdx, colIdx))+"]");
                            ds.set(rowIdx, colIdx, Etc.rTrim(ds.getString(rowIdx, colIdx, "")));
                        } else if(trimType!=null && trimType.equalsIgnoreCase("lTrim")) {
                            logger.debug("trimDataSet().["+ds.getString(rowIdx, colIdx)+"]->["+Etc.lTrim(ds.getString(rowIdx, colIdx))+"]");
                            ds.set(rowIdx, colIdx, Etc.lTrim(ds.getString(rowIdx, colIdx, "")));
                        } else {
                            logger.debug("trimDataSet().["+ds.getString(rowIdx, colIdx)+"]->["+Etc.regexTrim(ds.getString(rowIdx, colIdx))+"]");
                            ds.set(rowIdx, colIdx, Etc.regexTrim(ds.getString(rowIdx, colIdx, "")));
                        }
                    }

                }
            }
            ds.startStoreDataChanges(true); // 수정 : 07.27.2022
        }
    }

	/*
	 * variable 값을 trim 처리한다.
	 */
	public static void trimVariable(Variable variable) {

        logger.debug("trimVariable().[{}]->[{}]", variable.getString(), Etc.regexTrim(variable.getString()));
		variable.set(Etc.regexTrim(variable.getString()));
	}
	
	/*
	 * variable 값의 공백문자를 공백으로 치환한다. ("&#32;" --> " ") 
	 */
	public static void replaceAllEmptyVariable(Variable variable) {

        logger.debug("replaceAllEmptyVariable().[{}]->[{}]", variable.getString(), variable.getString().replaceAll("&amp;", "&").replaceAll("&#32;", " "));
		// variable.set(variable.getString().replaceAll("&#32;"," "));
		variable.set(Etc.regexTrim(variable.getString()).replaceAll("&amp;","&").replaceAll("&#32;"," "));
		
	}

	static final String W_SPACE_DEL = " |" + "\u00A0|" + "\u1680|" + "\u2000|" + "\u2001|" + "\u2002|"
									+ "\u2003|" + "\u2004|" + "\u2005|" + "\u2006|" + "\u2007|" + "\u2008|"
									+ "\u2009|" + "\u2029|" + "\u202F|" + "\u205F|" + "⠀|" + "\u3000|";

    /*
	 * 추가 		: 2023.01.31
	 * 참조url 	: https://code-boki.tistory.com/98
	 * 기능		: Regex와 replaceAll() 함수를 조합하여  string 앞뒤 공백문자 trim
	 */
	public static final String regexTrim(String inStr) {
    	String del_wSpaceRegex = "(^["+W_SPACE_DEL+"]+|["+W_SPACE_DEL+"]+$)";  // (^(?U)\\s+|["+W_SPACE_DEL+"]+$)

    	return inStr.replaceAll(del_wSpaceRegex,"");
	}

	/*
	 * 추가 		: 2024.10.07
	 * 참조url 	: https://www.baeldung.com/java-trim-alternatives
	 * 기능		: string 뒤 공백문자 trim
	 */
	public static final String rTrim(String inStr) {
		String del_wSpaceRegex = "(["+W_SPACE_DEL+"]+$)";  // (^(?U)\\s+|["+W_SPACE_DEL+"]+$)

		return inStr.replaceAll(del_wSpaceRegex,"");
	}

	/*
	 * 추가 		: 2024.10.07
	 * 참조url 	: https://www.baeldung.com/java-trim-alternatives
	 * 기능		: string 앞 공백문자 trim
	 */
	public static final String lTrim(String inStr) {
		String del_wSpaceRegex = "(^["+W_SPACE_DEL+"]s+)";  // (^(?U)\\s+|["+W_SPACE_DEL+"]+$)

		return inStr.replaceAll(del_wSpaceRegex,"");
	}

    /*
	 * 추가 		: 2023.08.24
	 * 용도		: ui의 컬럼 유형과 DB의 컬럼 유형이 다를 경우 DB 컬럼 case에 맞도록 변환해야 할 경우.
	 * 참조url 	: https://smithy.io/javadoc/1.17.0/software/amazon/smithy/utils/CaseUtils.html
	 * 			  https://github.com/smithy-lang/smithy/blob/main/smithy-utils/src/main/java/software/amazon/smithy/utils/CaseUtils.java
	 * 기능 		: client의 column case 형식에 따라 case converting 
	 * 지원 caes 	: camel(whoAreYou), snake(who_are_you), upper(WHO_ARE_YOU)
	 * 미지원		: pascal(WhoAreYou), kebab(who-are-you)
	 * @columnName	: 컬럼명 
	 * @from		: 변환 전 컬럼 case
	 * @to			: 변환 후 컬럼 case
	 * return		: 변환된 컬럼명
	 */
	public static final String convertColumnCaseFromTo(String columnName, String from, String to) {
		
		String resultStr = columnName;
		if(StringUtils.isEmpty(from) || StringUtils.isEmpty(to) ) {
			return resultStr;
		}

        switch (from) {

            case "camel":

                if (to.equals("snake")) {
                    resultStr = CaseUtils.toSnakeCase(columnName);
                } else if (to.equals("upper")) {
                    resultStr = columnName.toUpperCase();
                } else {
                    resultStr = columnName;
                }
                break;

            case "snake":

                if (to.equals("camel")) {
                    resultStr = CaseUtils.snakeToCamelCase(columnName);
                } else if (to.equals("upper")) {
                    resultStr = columnName.toUpperCase();
                } else {
                    resultStr = columnName;
                }
                break;

            case "upper":

                if (to.equals("camel")) {
                    resultStr = CaseUtils.snakeToCamelCase(columnName);
                } else if (to.equals("snake")) {
                    resultStr = columnName.toLowerCase();
                } else {
                    resultStr = columnName;
                }
                break;
        }

        logger.debug("convertColumnCaseFrom[{}]To[{}].[{}]->[{}]", from, to, columnName, resultStr);
		
		return resultStr;
	}
	
	public static final String convertColumnCaseFromDbToUi(String columnName, String fromArg, String to) {
		
		String resultStr = columnName;
		String from;
		if(StringUtils.isEmpty(fromArg) ) {
			from = "";
		} else {
			from = fromArg;
		}
		if(StringUtils.isEmpty(to) ) {
			return resultStr;
		}

        switch (from) {

            case "camel":

                if (to.equals("snake")) {
                    resultStr = CaseUtils.toSnakeCase(columnName);
                } else if (to.equals("upper")) {
                    resultStr = columnName.toUpperCase();
                } else {
                    resultStr = columnName;
                }
                break;

            case "snake":

                if (to.equals("camel")) {
                    resultStr = CaseUtils.snakeToCamelCase(columnName);
                } else if (to.equals("upper")) {
                    resultStr = columnName.toUpperCase();
                } else {
                    resultStr = columnName;
                }
                break;

            case "upper":

                if (to.equals("camel")) {
                    resultStr = CaseUtils.snakeToCamelCase(columnName);
                } else if (to.equals("snake")) {
                    resultStr = columnName.toLowerCase();
                } else {
                    resultStr = columnName;
                }
                break;

            default:

                if (to.equals("camel")) {
                    resultStr = CaseUtils.snakeToCamelCase(columnName);
                } else if (to.equals("snake")) {
                    resultStr = columnName.toLowerCase();
                } else if (to.equals("upper")) {
                    resultStr = columnName.toUpperCase();
                } else {
                    resultStr = columnName;
                }
                break;
        }

        logger.debug("convertColumnCaseFromDbToUi[{}]To[{}].[{}]->[{}]", from, to, columnName, resultStr);
		
		return resultStr;
	}
}
