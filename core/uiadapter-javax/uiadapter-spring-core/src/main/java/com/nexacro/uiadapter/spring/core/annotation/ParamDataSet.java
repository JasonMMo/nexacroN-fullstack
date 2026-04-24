package com.nexacro.uiadapter.spring.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Nexacro의 <code>DataSet</code>을 List 또는 POJO 자료구조로 변환하기 위해 사용하는 어노테이션입니다.
 * 
 * <p>Spring Controller의 메소드 파라미터에 적용할 수 있습니다.  
 *   doService 메소드에서 데이터셋 변환 예시는 아래와 같습니다:
 * <blockquote>
 * 예) public void doService(@ParamDataSet(name="dsUnit") List<Map> dsUnits)
 * </blockquote>
 * 
 * @author Park SeongMin
 * @since 2015.07.28
 * @version 1.0
 * @see ParamVariable
 */
@Target({ java.lang.annotation.ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamDataSet {
	/**
	 * 변환 대상 <code>DataSet</code>의 식별자(이름)를 지정합니다.
	 * @return 변환할 데이터셋 이름
	 */
	String name();

	/**
	 * 파라미터 필수 여부를 지정합니다.
	 * <p>기본값은 {@code true}이며, 요청에 해당 값이 없을 경우 예외가 발생합니다.
	 * <p>필수가 아닌 경우 {@code false}로 지정하면 값이 없을 때 {@code null}이 전달됩니다.
	 */
	boolean required() default true;
}