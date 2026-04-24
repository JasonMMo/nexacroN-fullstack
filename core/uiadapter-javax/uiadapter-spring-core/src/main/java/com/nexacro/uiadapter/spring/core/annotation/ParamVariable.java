package com.nexacro.uiadapter.spring.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Nexacro의 <code>Variable</code>을 기본 데이터 타입(primitive type)으로 변환하기 위해 사용하는 어노테이션입니다.
 *
 * <p>Spring Controller의 메소드 파라미터에 적용할 수 있습니다.  
 *  doService 메소드에서 변수 변환 사용 예시는 아래와 같습니다:
 * <blockquote>
 * 예) public void doService(@ParamVariable(name="varUserName") String userName)
 * </blockquote>
 *
 * <p>
 * 이 어노테이션은 Nexacro에서 전달되는 Variable 값을 명명된 이름에 따라 추출하여,  
 * 컨트롤러 메소드 파라미터에 바로 대입될 수 있도록 지원합니다.
 * 
 * @author Park SeongMin
 * @since 2015.07.28
 * @version 1.0
 * @see ParamDataSet
 */
@Target({ java.lang.annotation.ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamVariable {

	/**
	 * 변환 대상 <code>Variable</code>의 식별자(이름)를 지정합니다.
	 * @return 변수 이름
	 */
	String name();

	/**
	 * 파라미터 필수 여부를 지정합니다.
	 * <p>기본값은 {@code true}이며, 요청에 해당 값이 없을 경우 예외가 발생합니다.
	 * <p>필수가 아닌 경우 {@code false}로 지정하면 값이 없을 때 {@code null}이 전달됩니다.
	 */
	boolean required() default true;
}