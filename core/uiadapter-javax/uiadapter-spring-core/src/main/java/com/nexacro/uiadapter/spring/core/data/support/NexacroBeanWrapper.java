package com.nexacro.uiadapter.spring.core.data.support;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.util.ReflectionUtils;

import com.nexacro.uiadapter.spring.core.util.ReflectionFailException;
import com.nexacro.uiadapter.spring.core.util.ReflectionUtil;

/**
 * <p>Java Bean 객체를 감싸서(property 접근 및 조작을) 지원하는 래퍼 클래스입니다.<br>
 *	내부적으로 Java의 Introspection 를 통해 MemberField의 정보를 알아내고 값을 설정한다.
 *	Java의 PropertyDescriptor의 경우 Method 명칭은 MemberField의 명칭으로 get | set 으로 정의된다. 하지만 boolean의 경우 is가 생략되기 때문에 별도로 처리해야 한다.
 *	Member Field에 값 할당 시 Spring에서 데이터 변경에대한 Event 처리, 데이터 Type 처리 등의 처리로 속도가 현저히 떨어지기 때문에 값을 설정할 경우에는 reflection을 이용하여 바로 설정하도록 한다.
 *	Member Field 의 readMethod (getter)가 static일 경우 Spring에서 Method를 찾을수 없는 상태가 된다. Read method가 null 일 경우 static method를 찾아서 설정해야 한다.
 * <br>
 *	BeanWrapper의 경우 Method 명칭에 해당하는 property 명칭으로 값을 설정한다.
 *	하지만 field가 boolean일 경우 eclipseis에서 generation 되는 메서드의 명칭은 is가 생략되기 때문에
 *	해당부분만을 처리하며, 나머지는 spring으로 위임한다.
 * <br>
 * - 리플렉션 기반으로 Bean 속성의 읽기/쓰기, 속성 정보 조회 등 데이터 매핑 및 동적 바인딩에 활용됩니다.
 * - Bean의 속성명을 기준으로 NexacroBeanProperty 관리 및 값 할당 등 수행.

 * @author Park SeongMin
 * @since 08.04.2015
 * @version 1.0
 * @see BeanWrapper
 */
public class NexacroBeanWrapper {

    private final BeanWrapper beanWrapper;
    private CachedBeanMappings cachedMapping;

    /**
     * 입력 객체를 받아서 NexacroBeanWrapper를 생성합니다.
     * @param obj 래핑할 객체
     */
    private NexacroBeanWrapper(Object obj) {
        beanWrapper = new BeanWrapperImpl(obj);
    }

    /**
     * 입력받은 class를 통해 {@code NexacroBeanWrapper} 객체를 생성합니다.
     * 생성자는 주어진 클래스의 모든 필드 정보를 리플렉션으로 분석하여 propertyMap에 저장합니다.
     *
     * @param clazz 감쌀 Bean 클래스 타입
     */
    private NexacroBeanWrapper(Class<?> clazz) {
        beanWrapper = new BeanWrapperImpl(clazz);
    }
    
    /**
     * 이 Bean이 가지는 모든 NexacroBeanProperty 정보를 배열 형태로 반환합니다.
     *
     * <p>
     * NexacroBeanProperty는 각 Bean의 필드, 타입, Getter/Setter 메소드 정보를 담고 있는 객체입니다.
     * 이 메서드는 내부적으로 캐싱된 Bean 속성 정보를 활용하여, 반복 호출 시에도 빠르게 동작합니다.
     * </p>
     *
     * @return 이 Bean이 가지는 모든 NexacroBeanProperty 객체의 배열
     */
    public NexacroBeanProperty[] getProperties() {
        return getCachedBeanMappings().getProperties();
    }

    /**
     * 이 Bean이 가지는 모든 NexacroBeanProperty 정보를 배열 형태로 반환합니다.
     *
     * <p>
     * NexacroBeanProperty는 각 Bean의 필드, 타입, Getter/Setter 메소드 정보를 담고 있는 객체입니다.
     * 이 메서드는 내부적으로 캐싱된 Bean 속성 정보를 활용하여, 반복 호출 시에도 빠르게 동작합니다.
     * </p>
     *
     * @param propertyName 검색할 변수명
     * @return 이 Bean이 가지는 propertyName 속성의 NexacroBeanProperty 객체
     */
    public NexacroBeanProperty getProperty(String propertyName) {
        return getCachedBeanMappings().getProperty(propertyName);
    }

    /**
     * 지정한 NexacroBeanProperty에 해당하는 Bean의 필드(프로퍼티)에 값을 설정합니다.
     *
     * <p>
     * 이 메서드는 Spring의 BeanWrapper를 사용하지 않고, 리플렉션을 통해 직접 Setter 메서드를 호출하여
     * 속성 값을 할당합니다. 성능 향상을 목적으로 BeanWrapper를 우회하며, 대용량 데이터 처리(예: 10만 건) 시 적은 시간으로 처리할 수 있습니다.
     * </p>
     *
     * <p>
     * Setter 메서드가 존재하지 않거나, 접근 예외 및 실행 예외가 발생할 경우 NotWritablePropertyException이 발생합니다.
     * </p>
     *
     * @param property   값을 설정할 NexacroBeanProperty 객체(필수)
     * @param value      지정할 값
     * @throws NotWritablePropertyException Setter 호출 실패(접근권한/실행 오류 등) 시 발생
     */
    public void setPropertyValue(NexacroBeanProperty property, Object value) {
        if(property == null) {
            return;
        }
        
        // 데이터 설정 시 beanwrapper를 사용하지 않고 직접 처리 한다.
        // 데이터 변경에 대한 Event 처리, Data Type 처리 등에 따른 속도 저하..
        // 10만건 처리 시 5초 vs 1초 정도 차이.
        Method writeMethod = property.getWriteMethod();
        try {
            ReflectionUtil.makeAccessible(writeMethod);
            writeMethod.invoke(getInstance(), value);
        } catch (IllegalArgumentException e) {
            throw new NotWritablePropertyException(getInstance().getClass(), property.getPropertyName(), "Could not set object property", e);
        } catch (IllegalAccessException e) {
            throw new NotWritablePropertyException(getInstance().getClass(), property.getPropertyName(), "Could not set object property", e);
        } catch (InvocationTargetException e) {
            throw new NotWritablePropertyException(getInstance().getClass(), property.getPropertyName(), "Could not set object property", e.getTargetException());
        }
    }
    
    /**
     * 입력받은 명칭(propertyName)에 해당하는 멤버필드에 값(value)를 설정한다.
     *
     * @param propertyName   값을 설정할 NexacroBeanProperty 객체(필수)
     * @param value      지정할 값
     */
    public void setPropertyValue(String propertyName, Object value) {
        NexacroBeanProperty property = getCachedBeanMappings().getProperty(propertyName);
        if(property == null ) {
            throw new NotWritablePropertyException(getInstance().getClass(), propertyName);
        }
        setPropertyValue(property, value);
    }

    /**
     * 지정한 NexacroBeanProperty에 해당하는 Bean 프로퍼티의 값을 반환합니다.
     *
     * <p>
     * 내부적으로 원본 프로퍼티명이 존재하면 해당 이름을 사용하여 값을 조회하고, 그렇지 않을 경우 현재 프로퍼티명을 사용합니다.
     * BeanWrapper를 활용하여 안전하게 값을 가져옵니다.
     * property가 null인 경우에는 null을 반환합니다.
     * </p>
     *
     * @param property 값을 조회할 NexacroBeanProperty 객체
     * @return 해당 프로퍼티의 값, property가 null이면 null
     */
    public Object getPropertyValue(NexacroBeanProperty property) {
        if(property == null) {
            return null;
        }
        
        String propertyName = property.getPropertyName();
        if(property.getOriginalPropertyName() != null) {
            propertyName = property.getOriginalPropertyName();
        }
        
        return beanWrapper.getPropertyValue(propertyName);
    }

    /**
     * 입력받은 명칭(propertyName)에 해당하는 멤버필드에 값(value)를 반환한다.
     * @param propertyName
     * @return value
     */
    public Object getPropertyValue(String propertyName) {
        NexacroBeanProperty property = getCachedBeanMappings().getProperty(propertyName);
        if(property == null) {
            throw new NotWritablePropertyException(getInstance().getClass(), propertyName);
        }
        return getPropertyValue(property);
    }
    
    /**
     * 현재 설정 된 class의 object instance를 반환한다.
     * @return object instance
     */
    public Object getInstance() {
        return beanWrapper.getWrappedInstance();
    }

    /**
     * 현재 Bean에 대한 속성 매핑 정보(CachedBeanMappings)를 반환합니다.
     *
     * <p>
     * 속성 정보는 캐시에 저장되어, 최초 요청 시에만 생성되고 이후에는 캐시된 인스턴스를 재사용합니다.
     * 이 메서드는 Bean의 Property 정보 조회 등에 반복적으로 사용됩니다.
     * </p>
     *
     * @return 현재 Bean에 대응하는 CachedBeanMappings 객체
     */
    private CachedBeanMappings getCachedBeanMappings() {
        if(cachedMapping != null) {
            return cachedMapping;
        }
        
        cachedMapping = CachedBeanMappings.beanMappings(beanWrapper);
        return cachedMapping;
    }
    
    /**
     * 입력받은 Object를 통해 {@code NexacroBeanWrapper}를 생성한다.
     * @param obj 생성할 객체
     * @return wrapped class
     */
    public static NexacroBeanWrapper createBeanWrapper(Object obj) {
        return new NexacroBeanWrapper(obj);
    }
    
    /**
     * 입력받은 class를 통해 {@code NexacroBeanWrapper}를 생성한다.
     * @param clazz 생성할 class
     * @return wrapped class
     */
    public static NexacroBeanWrapper createBeanWrapper(Class<?> clazz) {
        return new NexacroBeanWrapper(clazz);
    }
    
    /**
     * <p>Beans의 Property 중 nexacro에서 처리가능한 Field에 대한 정보를 cache 하는 class이다
     * @author Park SeongMin
     *
     */
    private static class CachedBeanMappings {
        
        private static final Logger logger = LoggerFactory.getLogger(NexacroBeanWrapper.class);
        private static final String IS = "is";
        private static final Map<Class, CachedBeanMappings> classCache = Collections.synchronizedMap(new HashMap<Class, CachedBeanMappings>());
        
        private Map<String, NexacroBeanProperty> propertyCache;  
        
        /* supported all properties */
        private NexacroBeanProperty[] beanProperties;
        
        private CachedBeanMappings(BeanWrapper beanWrapper) {
            initBeanPropertyNames(beanWrapper);
        }
        
        static CachedBeanMappings beanMappings(BeanWrapper beanWrapper) {
            
            Class wrappedClass = beanWrapper.getWrappedClass();
            CachedBeanMappings mapping = classCache.get(wrappedClass);
            if(mapping != null) {
                return mapping;
            }
            mapping = new CachedBeanMappings(beanWrapper);
            classCache.put(wrappedClass, mapping);
            return mapping;
        }
        
        /**
         * java.beans.Introspector 의 property name 변환</p>
         * get/set 메서드의 이름 중 첫번째 두번째 모두 대문자 일 경우 대문자 반환
    	 * 아닐 경우 첫번째 글자만 소문자로 변환한다.
    	 * 
    	 * <pre>
			public static String decapitalize(String name) {
			if (name == null || name.length() == 0) {
			    return name;
			}
			if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
					Character.isUpperCase(name.charAt(0))){
			    return name;
			}
			char chars[] = name.toCharArray();
			chars[0] = Character.toLowerCase(chars[0]);
			return new String(chars);
			}
    	 * </pre>
         * @param beanWrapper
         */
        private void initBeanPropertyNames(BeanWrapper beanWrapper) {
            
            propertyCache = new HashMap<String, NexacroBeanProperty>();
            List<NexacroBeanProperty> tmpList = new ArrayList<NexacroBeanProperty>();
            
            Class wrappedClass = beanWrapper.getWrappedClass();
            
            // not ordered.. 
            PropertyDescriptor[] propertyDescriptors = beanWrapper.getPropertyDescriptors();
            
            for(PropertyDescriptor descriptor: propertyDescriptors) {
                
                if(!validateReadAndWriteMethod(wrappedClass, descriptor)) {
                    continue;
                }
                
                // ignore row type
                if("rowType".equalsIgnoreCase(descriptor.getName())) {
                    continue;
                }
                
                String name = descriptor.getName();
                Class<?> propertyType = descriptor.getPropertyType();
                boolean isConverted = false;
                String adjustName = name;
                if(propertyType == boolean.class) {
                    if(!name.startsWith(IS)) {
                        try {
                            // check exist field
                            if(wrappedClass.getField(IS + getBaseName(name)) != null) {
                                adjustName = IS + getBaseName(name);
                                isConverted = true;
                            }
                        } catch (SecurityException e) {
                            throw e;
                        } catch (NoSuchFieldException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                
                NexacroBeanProperty beanProperty = new NexacroBeanProperty(adjustName, propertyType);
                if(isConverted) {
                    beanProperty.setOriginalPropertyName(name);
                }
                
                if(isStaticProperty(descriptor)) {
                    beanProperty.setStatic();
                }
                
                beanProperty.setWriteMethod(descriptor.getWriteMethod());
                
                tmpList.add(beanProperty);
                propertyCache.put(adjustName, beanProperty);
            }
            beanProperties = new NexacroBeanProperty[tmpList.size()];
            beanProperties = tmpList.toArray(beanProperties);
        }
        
        /**
         * Bean의 Property 중 nexacro platform에서 처리 가능한 필드 정보만을 반환한다.
         * @return NexacroBeanProperty 객체 배열
         */
        public NexacroBeanProperty[] getProperties() {
            return beanProperties;
        }

        /**
         * Bean의 Property 중 nexacro platform에서 처리 가능한 필드 정보만을 반환한다.
         * @param name Bean의 Property 이름
         * @return NexacroBeanProperty 객체
         */
        public NexacroBeanProperty getProperty(String name) {
            return propertyCache.get(name);
        }

        /**
         * 지정한 클래스와 PropertyDescriptor에 대해 읽기/쓰기 메서드의 유효성을 검증합니다.
         *
         * <p>
         * Getter(readMethod)와 Setter(writeMethod)가 정상적으로 정의되어 있는지 확인하며,
         * boolean 타입 또는 정적(static) 필드의 경우 네이밍 이슈나 접근성 문제로 인해
         * 메서드가 누락될 수 있으므로 이를 보완하여 추가 검증을 수행합니다.
         * </p>
         *
         * @param clazz      검증할 대상 클래스
         * @param descriptor 속성 정보를 담고 있는 PropertyDescriptor 객체
         * @return 읽기/쓰기 메서드가 모두 정상적으로 존재하면 true, 그렇지 않으면 false
         */
        private boolean validateReadAndWriteMethod(Class<?> clazz, PropertyDescriptor descriptor) {
            
            String name = descriptor.getName();
            Method readMethod = descriptor.getReadMethod();
            Method writeMethod = descriptor.getWriteMethod();
            
            if(name == null) {
                return false;
            }
            
            if(!NexacroConverterHelper.isConvertibleType(descriptor.getPropertyType())) {
                logger.debug("{} type of {} is ignored.", descriptor.getPropertyType(), name);
                // unsupported type
                return false;
            }
            
            if(readMethod == null && writeMethod != null) {
                // find static method.. introspection is unsupported static getter.
                String findPropertyName = "get"+getBaseName(name);
                Method findedMethod = ReflectionUtils.findMethod(clazz, findPropertyName);
                if(findedMethod != null) {
                    if(ReflectionUtil.isStaticMethod(findedMethod)) {
                        setStaticReadMethodIntoDescriptor(descriptor, findedMethod);
                    }
                }
                
                readMethod = descriptor.getReadMethod();
                if(readMethod == null) {
                	 if(logger.isDebugEnabled()) {
                         logger.debug("skipped property {} of bean class[{}]:" +
                             " Bean Property {} is not readable or has an invalid getter or setter." +
                             " Does the return type of the getter match the parameter type of the setter"
                             , name, clazz, name);
                         
                     }
                	 return false;
                }
            } else return readMethod != null && writeMethod != null;
            
            return true;
        }

        /**
         * 주어진 PropertyDescriptor에 정적(static) getter 메서드를 설정합니다.
         *
         * <p>
         * Bean의 읽기 메서드(readMethod)가 null이며, 해당 프로퍼티의 getter가 정적(static)으로 선언된 경우,
         * PropertyDescriptor에 직접 해당 static 메서드를 세팅하여 읽기 처리가 정상적으로 동작하도록 보완합니다.
         * 이는 Spring BeanWrapper가 static getter를 기본적으로 인식하지 못하는 문제를 해결하기 위함입니다.
         * </p>
         *
         * @param descriptor    PropertyDescriptor 객체
         * @param staticMethod  설정할 static readMethod (getter)
         */
        private void setStaticReadMethodIntoDescriptor(PropertyDescriptor descriptor, Method staticMethod) {
            
            // for spring GenericTypeAwarePropertyDescriptor
            Field field = null;
            try {
                field = ReflectionUtil.getField(descriptor.getClass(), "readMethod");
            } catch(ReflectionFailException e) {
                // nothing..
            }
            
            if(field != null) {
                ReflectionUtil.makeAccessible(field);
                try {
                    field.set(descriptor, staticMethod);
                } catch (IllegalArgumentException e) {
                    logError(descriptor.getName(), staticMethod, e);
                } catch (IllegalAccessException e) {
                    logError(descriptor.getName(), staticMethod, e);
                }
            } else {
                try {
                    descriptor.setReadMethod(staticMethod);
                } catch (IntrospectionException e) {
                    logError(descriptor.getName(), staticMethod, e);
                }
            }
        }

        /**
         * 지정한 프로퍼티명 및 static 메서드와 관련된 예외 발생 시 에러 로그를 기록합니다.
         *
         * <p>
         * PropertyDescriptor 처리 등 리플렉션 기반 작업 처리 중 staticMethod 설정 단계에서 오류가 발생할 경우,
         * 프로퍼티명과 staticMethod(메서드 정보), 예외 메시지를 함께 로그로 남깁니다.
         * 문제 발생 원인 추적 및 디버깅 목적에 활용됩니다.
         * </p>
         *
         * @param propertyName   예외가 발생한 프로퍼티명
         * @param staticMethod   문제가 발생한 static 메서드 객체(또는 관련 정보)
         * @param e              발생한 예외 객체
         */
        private void logError(String propertyName, Object staticMethod, Exception e) {
            String message ;
            if (logger.isErrorEnabled()) {
                message = "Argument {} is checked static error. '{}' method setting failed.";
                logger.error(message, propertyName, staticMethod, e);
            }
        }

        /**
         * 주어진 PropertyDescriptor가 static 프로퍼티(getter/setter 메서드가 static)인지 여부를 판별합니다.
         *
         * <p>
         * PropertyDescriptor 내의 readMethod(getter)나 writeMethod(setter) 중 하나라도 static이면 true를 반환합니다.
         * 일반적으로 static 프로퍼티는 인스턴스가 아닌 클래스 단위로 접근되는 속성으로,
         * Spring BeanWrapper 또는 리플렉션 처리 시 별도의 핸들링이 필요할 수 있습니다.
         * </p>
         *
         * @param descriptor 검사할 PropertyDescriptor 객체
         * @return 프로퍼티의 read/write Method 중 하나라도 static이면 true, 아니면 false
         */
        private boolean isStaticProperty(PropertyDescriptor descriptor) {
            
            Method readMethod = descriptor.getReadMethod();
            Method writeMethod = descriptor.getWriteMethod();

            return ReflectionUtil.isStaticMethod(readMethod) && ReflectionUtil.isStaticMethod(writeMethod);
        }

        /**
         * 주어진 프로퍼티명에서 prefix(예: "is", "get", "set")를 제거한 기본 속성명을 반환합니다.
         *
         * <p>
         * Java Beans 규약에 따라 메서드 명칭에서 "is", "get", "set" 등 접두사가 존재할 경우,
         * 이를 제외한 실제 프로퍼티의 이름 부분만 추출하여 반환합니다.<br>
         * 예: "getName" → "name", "isActive" → "active"
         * </p>
         *
         * @param name 프로퍼티명 또는 메서드명
         * @return prefix가 제거된 실제 기본 프로퍼티명(첫 글자는 소문자 처리됨)
         */
        private String getBaseName(String name) {
            if (name == null || name.isEmpty()) {
                return name;
            }
            return name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
        }
    }
}