package com.nexacro.uiadapter.spring.core.data.convert;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverter.ConvertiblePair;
import com.nexacro.uiadapter.spring.core.data.support.DataSetToListConverter;
import com.nexacro.uiadapter.spring.core.data.support.DataSetToObjectConverter;
import com.nexacro.uiadapter.spring.core.data.support.ListToDataSetConverter;
import com.nexacro.uiadapter.spring.core.data.support.ObjectToDataSetConverter;
import com.nexacro.uiadapter.spring.core.data.support.ObjectToVariableConverter;
import com.nexacro.uiadapter.spring.core.data.support.VariableToObjectConverter;

/**
 * <p>{@code NexacroConverter}를 등록하고 관리하는 팩토리(Factory) 클래스입니다.
 * <p>
 * 제공되는 {@code NexacroConverter} 인스턴스들은 싱글톤(singleton)으로 관리되며,
 * 변환 가능 타입 조합에 따라 적절한 Converter 인스턴스를 반환합니다.
 * 동적으로 Converter 등록/조회/캐시/실행 등 Converter 관련 공통 기능을 제공합니다.
 *
 * @author Park SeongMin
 * @since 07.28.2015
 * @version 1.0
 * @see NexacroConverter
 */
public class NexacroConverterFactory {

    /** 로깅을 위한 Logger 인스턴스 */
    private static final Logger logger = LoggerFactory.getLogger(NexacroConverterFactory.class);

    /** 등록된 Converter 집합(Set) */
    private static final Set<NexacroConverter> converterSets = new HashSet<NexacroConverter>();

    /** 변환 타입 쌍별 Converter 캐시 맵 (조회 속도 개선) */
    private static final Map<ConvertiblePair, NexacroConverter> convertibleCacheMap = new HashMap<ConvertiblePair, NexacroConverter>();

    /** 싱글톤 인스턴스 */
    private static final NexacroConverterFactory INSTANCE = new NexacroConverterFactory();

    /**
     * 생성자 (외부 생성 불가, 싱글톤)
     * <br>
     * 기본 Converter를 등록합니다.
     */
    private NexacroConverterFactory() {
        addDefaultConverter();
    }

    /**
     * 시스템 기본 Converter 들을 등록합니다.
     */
    private void addDefaultConverter() {
        NexacroConverterFactory.register(new DataSetToListConverter());
        NexacroConverterFactory.register(new ListToDataSetConverter());
        NexacroConverterFactory.register(new DataSetToObjectConverter());
        NexacroConverterFactory.register(new ObjectToDataSetConverter());
        NexacroConverterFactory.register(new VariableToObjectConverter());
        NexacroConverterFactory.register(new ObjectToVariableConverter());
    }

    /**
     * 싱글톤 인스턴스를 반환합니다.
     *
     * @return NexacroConverterFactory 단일 인스턴스
     */
    public static NexacroConverterFactory getInstance() {
        return INSTANCE;
    }

    /**
     * <p>소스(source) 타입에서 대상(target) 타입으로 변환 가능한 {@link NexacroConverter}를 반환합니다.
     * <br>
     * 등록된 Converter 중 변환 가능한 인스턴스가 없으면 {@code null}을 반환합니다.
     *
     * @param source 소스 타입 클래스
     * @param target 타겟 타입 클래스
     * @return 변환기(NexacroConverter) 또는 null
     * @throws IllegalArgumentException 파라미터가 null인 경우
     */
    public static NexacroConverter getConverter(Class source, Class target) {

        if(source == null || target == null) {
            throw new IllegalArgumentException("source and target class must not be null.");
        }

        NexacroConverter converter = findConvertibleCache(source, target);

        if(converter == null) {
            converter = findSupportedConverter(source, target);
        }

        // 변환 가능한 Converter가 없으면 null 반환 (NullConverter 사용하지 않음)
        return converter;
    }

    /**
     * 변환 타입 쌍에 대한 캐시에서 Converter를 조회합니다.
     *
     * @param source 소스 타입 클래스
     * @param target 타겟 타입 클래스
     * @return 캐시에 등록된 Converter 또는 null
     */
    private static NexacroConverter findConvertibleCache(Class source, Class target) {
        ConvertiblePair convertiblePair = new ConvertiblePair(source, target);
        NexacroConverter converter = convertibleCacheMap.get(convertiblePair);
        return converter;
    }

    /**
     * 등록된 Converter 집합에서 지원되는 Converter를 탐색합니다.
     * <br>
     * 탐색된 Converter는 캐시에 등록 후 반환합니다.
     *
     * @param source 소스 타입 클래스
     * @param target 타겟 타입 클래스
     * @return 탐색 결과 Converter 또는 null
     */
    private static NexacroConverter findSupportedConverter(Class source, Class target) {

        for(NexacroConverter converter: converterSets) {
            boolean canConvertible = converter.canConvert(source, target);
            if(canConvertible) {
                ConvertiblePair pair = new ConvertiblePair(source, target);
                convertibleCacheMap.put(pair, converter);
                logger.debug("{} to {} converter({}) registered.", source.getName(), target.getName(), converter.getClass().getName());
                return converter;
            }
        }

        return null;
    }

    /**
     * Converter 집합을 한 번에 등록합니다.
     *
     * @param converters 등록할 Converter 집합
     */
    public static synchronized void register(Set<NexacroConverter> converters) {
        if(converters == null) {
            return;
        }
        for(NexacroConverter converter: converters) {
            register(converter);
        }
    }

    /**
     * Converter를 단일 등록합니다.
     *
     * @param converter 등록할 Converter
     * @throws IllegalArgumentException 파라미터가 null인 경우
     */
    public static synchronized void register(NexacroConverter converter) {
        if(converter == null) {
            throw new IllegalArgumentException(NexacroConverter.class.getName()+" must not be null.");
        }

        if(converterSets.contains(converter)) {
            return;
        }

        if(logger.isDebugEnabled()) {
            logger.debug("{} registered.", converter.getClass());
        }
        converterSets.add(converter);

//        만약 Converter가 기본적으로 지원하는 ConvertiblePair 목록이 있다면 캐시에 추가하는 코드(주석처리됨)
//        Set<ConvertiblePair> defaultConvertibleTypes = converter.getDefaultConvertibleTypes();
//        for(ConvertiblePair pair: defaultConvertibleTypes) {
//            if(convertibleCacheMap.containsKey(pair)) {
//                continue;
//            }
//
//            convertibleCacheMap.put(pair, converter);
//            if(logger.isDebugEnabled()) {
//                logger.debug(pair + " registered.");
//            }
//        }
    }

    /**
     * 변환 쌍에 대한 고유 키 값을 생성합니다. (객체 버전)
     *
     * @param source 소스 객체
     * @param target 타겟 객체
     * @return "sourceClassName->targetClassName" 형식의 문자열
     */
    private static String getConvertibleKey(Object source, Object target) {
        return getConvertibleKey(source.getClass(), target.getClass());
    }

    /**
     * 변환 쌍에 대한 고유 키 값을 생성합니다. (클래스 버전)
     *
     * @param source 소스 클래스
     * @param target 타겟 클래스
     * @return "sourceClassName->targetClassName" 형식의 문자열
     */
    private static String getConvertibleKey(Class source, Class target) {
        return source.getName() + "->" + target.getName();
    }

    /**
     * 변환 불가 상황에서 사용되는 Null Object 패턴의 내부 Converter 구현입니다.
     * <br>
     * 실제 변환은 수행하지 않으며, convert 호출 시 예외를 발생시킵니다.
     */
    private static class NullConverter implements NexacroConverter {

        private final Class source;
        private final Class target;

        private NullConverter(Class source, Class target) {
            this.source = source;
            this.target = target;
        }

        public boolean canConvert(Class source, Class target) {
            return false;
        }

        public Object convert(Object source, ConvertDefinition definition) throws NexacroConvertException {
            throw new UnsupportedOperationException("Unsupported convert type. source="+source+", target="+target);
        }

        public void addListener(NexacroConvertListener listener) {
        }

        public void removeListener(NexacroConvertListener listener) {
        }

    }

}