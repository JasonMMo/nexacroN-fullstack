package com.nexacro.uiadapter.spring.core.data.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertDefinition;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertException;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverter;
import com.nexacro.uiadapter.spring.core.data.metadata.NexacroMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.MapMetaData;

/**
 * <p>List를 <code>DataSet</code>으로 변환하는 클래스입니다.
 * <br>
 * - Nexacro와의 연동 시 Java의 List 데이터를 DataSet 포맷으로 변환할 때 사용합니다.
 * - List의 요소가 Map 구조이든 Bean 구조이든 모두 지원합니다.
 *
 * @author Park SeongMin
 * @since 07.28.2015
 * @version 1.0
 */
public class ListToDataSetConverter extends AbstractDataSetConverter implements NexacroConverter<List, DataSet> {

    private static final int CHECK_INDEX = 0;

    /**
     * 변환 지원 여부를 확인합니다.
     * <br>
     * source가 List 계열, target이 DataSet일 때 true 반환.
     *
     * @param source 변환 소스 타입
     * @param target 변환 타겟 타입
     * @return 지원 여부
     */
    @Override
    public boolean canConvert(Class source, Class target) {
        
        if(source == null || target == null) {
            return false;
        }

        // List 하위 타입이면서, 타겟 객체가 DataSet인 경우만 지원
        if(List.class.isAssignableFrom(source) && DataSet.class.equals(target)) {
            return true;
        }
        
        return false;
    }

    /**
     * List를 DataSet으로 변환합니다.
     * <br>
     * - List가 비었거나 null인 경우 컬럼만 정의된 빈 DataSet 반환
     * - List에서 첫 번째 값의 타입에 따라 Bean 또는 Map 변환 방식 결정
     *
     * @param source 변환 소스(List)
     * @param definition 변환 정의(스키마 정보 등)
     * @return 변환 DataSet
     * @throws NexacroConvertException 변환 실패 시
     */
    @Override
    public DataSet convert(List source, ConvertDefinition definition) throws NexacroConvertException {
        
        if(definition == null) {
            throw new IllegalArgumentException(ConvertDefinition.class.getSimpleName()+" must not be null.");
        }
        if(source == null) {
            // List가 null일 경우 빈 DataSet 반환
            return createDataSet(definition);
        }

        // 사용할 수 있는(Null이 아닌) 첫 번째 데이터를 체크
        Object availableFirstData = checkAvailable(source);
        if(availableFirstData == null) {
            // 컬럼만 정의된 빈 DataSet 반환
            return createDataSet(definition);
        }
        
        // 첫 데이터가 Map이면 Map 기반 변환, 아니면 Bean 기반 변환
        DataSet ds;
        if(availableFirstData instanceof Map) {
            ds = convertListMapToDataSet(source, definition, (Map) availableFirstData);
        } else {
            ds = convertListBeanToDataSet(source, definition, availableFirstData);
        }

        return ds;
    }

    /**
     * List에서 변환 가능한 첫 번째 데이터를 얻습니다.
     * <br>
     * - NexacroMetaData일 경우 그 내부 메타데이터를 사용
     * - List가 비어있거나 모두 null이면 빈 Map 반환
     *
     * @param source 입력 List
     * @return 사용 가능한 데이터(Bean, Map) 또는 null, 모두 없을 경우 빈 Map
     */
    private Object checkAvailable(List source) {
        // ibatis 등에서 MetaData 타입의 빈 Data를 반환하는 경우 처리
        if(source instanceof NexacroMetaData) {
            NexacroMetaData metaData = (NexacroMetaData) source;
            return metaData.getMetaData();
        }

        if(source.isEmpty()) {
            return null;
        }
        // 첫 번째 null이 아닌 데이터를 반환
        for(Object obj: source) {
            if(obj != null) {
                return obj;
            }
        }

        // 모두 null이면 빈 Map 반환(컬럼 생성 목적)
        return new HashMap();
    }

    /**
     * List<Map> 형태의 데이터를 DataSet으로 변환합니다.
     * <br>
     * - 스키마 DataSet 또는 동적 컬럼 정의 지원
     * - MapMetaData의 경우 metaData도 갱신
     * - MapMetaData이고 데이터가 0건이면 row 추가하지 않음
     *
     * @param source List 데이터
     * @param definition 변환 정의
     * @param availableFirstData 컬럼 구조 파악을 위한 첫 Map 데이터
     * @return 변환된 DataSet
     * @throws NexacroConvertException 변환 실패 시
     */
    private DataSet convertListMapToDataSet(List source, ConvertDefinition definition, Map availableFirstData) throws NexacroConvertException {
        DataSet ds;

        if(definition.getSchemaDataSet() != null) {
            // 사전에 정의한 schema를 사용하는 경우
            ds = createDataSet(definition);
        } else {
            ds = createDataSet(definition);
            // source가 MapMetaData 타입이면 metaData를 함께 세팅
            if(source instanceof MapMetaData) {
                ((MapMetaData) source).setMetaData(availableFirstData);
            }
            // 동적 컬럼 등록
            addColumnIntoDataSet(ds, availableFirstData);
        }

        // MapMetaData 타입이고 데이터 없는 경우 row 미추가
        if(!(source instanceof MapMetaData)) {
            for (Object obj : source) {
                addRowIntoDataSet(ds, (Map) obj, definition.isDisallowChangeStructure());
            }
        }
        return ds;
    }

    /**
     * List<Bean> 형태의 데이터를 DataSet으로 변환합니다.
     * <br>
     * - Bean 타입 지원 여부 체크
     * - schemaDataSet 사용 여부에 따라 컬럼 정의 처리
     * - 모든 Bean 객체를 row로 등록
     *
     * @param source List 데이터
     * @param definition 변환 정의
     * @param availableFirstData 컬럼 구조 파악을 위한 첫 Bean 데이터
     * @return 변환된 DataSet
     * @throws NexacroConvertException 변환 실패, 미지원 타입 등
     */
    private DataSet convertListBeanToDataSet(List source, ConvertDefinition definition, Object availableFirstData) throws NexacroConvertException {
        // Bean 타입 지원 여부 확인
        if(!NexacroConverterHelper.isSupportedBean(availableFirstData.getClass())) {
            throw new NexacroConvertException("unsupported generic type. type="+availableFirstData.getClass());
        }

        DataSet ds;
        if(definition.getSchemaDataSet() != null) {
            // 사전 정의 스키마 사용
            ds = createDataSet(definition);

	        // map과 달리 bean은 이미 정의가 되어 있기 때문에 row를 추가할때 컬럼을 추가하지 않고, 미리 설정한다.
            if(!definition.isDisallowChangeStructure()) {
                addColumnIntoDataSet(ds, availableFirstData);
            }

        } else {
            ds = createDataSet(definition);
            // Bean 기반 컬럼 동적 등록
            addColumnIntoDataSet(ds, availableFirstData);
        }

        // 모든 요소를 DataSet row로 추가
        for(Object obj: source) {
            addRowIntoDataSet(ds, obj);
        }

        return ds;
    }
}