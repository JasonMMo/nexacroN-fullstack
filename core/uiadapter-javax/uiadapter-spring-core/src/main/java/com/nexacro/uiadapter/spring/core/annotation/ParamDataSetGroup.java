package com.nexacro.uiadapter.spring.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>여러 개의 <code>DataSet</code>을 그룹으로 묶어 List 또는 POJO 자료구조로 변환하여 트랜잭션 처리가 가능하도록 하는 어노테이션입니다.
 * 
 * <p>Spring Controller의 메소드 파라미터에 적용할 수 있습니다.  
 *   데이터셋 그룹 변환을 사용하는 예시는 아래와 같습니다:
 * <blockquote>
 * 예) saveMultiProcedure(@ParamDataSetGroup(name = "DATASET_GROUP", required = true) LinkedHashMap&lt;String, List&lt;Map&lt;String, Object&gt;&gt;&gt; saveListGroup)
 * </blockquote>
 * 
 * <p>
 * 이 어노테이션은 여러 DataSet 이름을 그룹으로 받아 LinkedHashMap에 각각 List<Map> 형태 등으로 변환하여 전달합니다.  
 * 일반적으로 여러 프로시저를 하나의 트랜잭션으로 묶어서 처리할 때 유용하게 사용할 수 있습니다.
 * 
 * @author Park SeongMin
 * @since 2015.07.28
 * @version 1.0
 * @see ParamVariable
 */
@Target({ java.lang.annotation.ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamDataSetGroup {
	/**
	 * DataSet 그룹의 식별자(이름)를 지정합니다.
	 * @return 데이터셋 그룹 이름
	 */
	String name();

	/**
	 * 파라미터 필수 여부를 지정합니다.
	 * <p>기본값은 {@code true}이며, 요청에 해당 값이 없으면 예외가 발생합니다.
	 * <p>필수가 아닌 경우 {@code false}로 지정하면 값이 없을 때 {@code null}이 전달됩니다.
	 */
	boolean required() default true;
}