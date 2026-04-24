package com.nexacro.uiadapter.spring.core.data.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.spring.core.data.DataSetRowTypeAccessor;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertDefinition;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertException;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverter;
import com.nexacro.uiadapter.spring.core.util.Etc;

import com.nexacro.uiadapter.spring.core.util.ReflectionUtil;

/**
 * <p><code>DataSet</code>에서 Object(Bean 또는 Map) 형태의 데이터로 변환을 수행하는 클래스입니다.
 * <br>
 * 주로 Nexacro의 DataSet 데이터를 단일 Bean 또는 Map 형태의 객체로 변환할 때 사용합니다.
 * <br>
 * 제네릭 타입 정보에 따라 변환 대상 객체 타입이 결정됩니다.
 *
 * @author Park SeongMin
 * @since 08.17.2015
 * @version 1.0
 */
public class DataSetToObjectConverter extends AbstractDataSetConverter implements NexacroConverter<DataSet, Object> {

    /**
     * 변환 지원 여부를 확인합니다.
     * <br>
     * source가 DataSet이고 target이 List가 아닌 Bean(지원 타입) 또는 Map 계열일 때 true를 반환합니다.
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
        
        // Bean(POJO) 변환 지원: DataSet → Bean
        if(DataSet.class.equals(source) && !List.class.equals(target) && NexacroConverterHelper.isSupportedBean(target)) {
            return true;
        } 
        
        // Map 변환 지원: DataSet → Map (또는 그 하위 타입)
        if (DataSet.class.equals(source) && Map.class.isAssignableFrom(target)) {
        	return true;
        }
        
        return false;
    }
    
    /**
     * DataSet을 단일 Object(Bean 또는 Map)로 변환합니다.
     * <br>
     * - definition의 genericType 기준으로 Bean/Map 분기 처리합니다.
     * - DataSet이 null이거나 genericType이 없으면 예외 혹은 기본 인스턴스를 반환합니다.
     *
     * @param source 변환 소스(DataSet)
     * @param definition 변환 정의(타입, 제네릭타입 등)
     * @return 변환 Object(Bean 또는 Map)
     * @throws NexacroConvertException 변환 실패 시
     */
    @Override
    public Object convert(DataSet source, ConvertDefinition definition) throws NexacroConvertException {

        if(definition == null) {
            throw new NexacroConvertException(ConvertDefinition.class.getSimpleName()+" must not be null.");
        }
        
        // 변환 대상 타입(bean, map) 확인
        Class genericType = definition.getGenericType();
        
        // DataSet이 null일 경우 타입에 맞는 빈 객체 반환
        if(source == null) {
            return ReflectionUtil.instantiateClass(genericType);
        }
        
        if(genericType == null) {
            throw new NexacroConvertException("generic type must be declared.");
        }
        
        Object obj;
        // Map.class인 경우 Map 변환, 그 외에는 Bean 변환
        if(Map.class.equals(genericType)) {
            obj = convertDataSetToMap(source, definition);
        } else {
            obj = convertDataSetToBean(source, definition);
        }
        
        return obj;
    }

    /**
     * DataSet을 1개의 Bean 객체로 변환합니다.
     * <br>
     * DataSet의 첫 번째 row 값을 기준으로 Bean 객체의 property를 세팅합니다.
     * 삭제 데이터(row) 존재 여부도 내부적으로 처리됩니다.
     *
     * @param ds DataSet 객체
     * @param definition 변환 정의(Bean 타입 포함)
     * @return 변환 결과 Bean 객체
     * @throws NexacroConvertException 지원불가 타입 혹은 변환 실패 시
     */
    private Object convertDataSetToBean(DataSet ds, ConvertDefinition definition) throws NexacroConvertException {
        Class genericType = definition.getGenericType();
        
        // 지원 가능한 Bean 타입인지 확인
        if(!NexacroConverterHelper.isSupportedBean(genericType)) {
            throw new NexacroConvertException("unsupported generic type. type="+genericType);
        }
        
        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(genericType);
        
        // 삭제 데이터 존재 여부 체크
        boolean isRemovedData = (ds.getRemovedRowCount() > 0);
        
        // 첫 번째 row를 Bean 객체에 적용
        addRowAndOrgRowIntoBean(beanWrapper, ds, 0, isRemovedData);
        
        Object bean = beanWrapper.getInstance();
        
        return bean;
    }

    /**
     * DataSet을 1개의 Map 객체로 변환합니다.
     * <br>
     * DataSet의 첫 번째 row data와 삭제 row data 역시 1개의 Map에 병합/세팅합니다.
     *
     * @param ds DataSet 객체
     * @param definition 변환 정의 정보
     * @return 변환된 Map 객체
     */
    private Object convertDataSetToMap(DataSet ds, ConvertDefinition definition) {
        
        String[] columnNames = getDataSetColumnNames(ds);
        
        // 기본 row(data) 첫 건 변환
        Map<String, Object> dataRow = new HashMap<String, Object>();
        addRowIntoMap(dataRow, ds, 0, columnNames);
    
        // 삭제 row를 추가(병합) 처리
        int removedRowCount = ds.getRemovedRowCount();
        for(int removedIndex=0; removedIndex<removedRowCount; removedIndex++) {
            addRemovedRowIntoMap(dataRow, ds, removedIndex, columnNames);
        }
        
        return dataRow;
    }

    /**
     * 삭제 row 정보를 Map에 추가합니다.
     * <br>
     * DataSet에 삭제된 행이 있을 경우 column별로 데이터를 put하고,
     * 삭제된 데이터임을 rowType 정보로 명시합니다.
     *
     * @param dataRow 대상 Map
     * @param ds DataSet
     * @param removedIndex 삭제 row 인덱스
     * @param columnNames 컬럼명 배열
     */
    private void addRemovedRowIntoMap(Map<String, Object> dataRow, DataSet ds, int removedIndex, String[] columnNames) {
        
        for(int columnIndex=0; columnIndex<columnNames.length; columnIndex++) {
            Object object = ds.getRemovedData(removedIndex, columnIndex);
            // 값후처리 이벤트 적용
            object = fireDataSetConvertedValue(ds, object, removedIndex, columnIndex, false, true);
            // property를 설정할 경우, 컬럼명 케이스 변환
            String columnName = Etc.convertColumnCaseFromTo(
                columnNames[columnIndex], 
                etcProperty.getEtcProperty("nexacro.client-column-case"),
                etcProperty.getEtcProperty("nexacro.db-column-case"));
            dataRow.put(columnName, object);
        }
        // 삭제된 row임을 명시하는 key 세팅
        dataRow.put(DataSetRowTypeAccessor.NAME, DataSet.ROW_TYPE_DELETED);
    }

}