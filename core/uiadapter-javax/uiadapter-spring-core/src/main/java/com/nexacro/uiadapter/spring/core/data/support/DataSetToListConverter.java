package com.nexacro.uiadapter.spring.core.data.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.uiadapter.spring.core.data.DataSetRowTypeAccessor;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertDefinition;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertException;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverter;
import com.nexacro.uiadapter.spring.core.util.Etc;

/**
 * <p><code>DataSet</code>에서 List 형태의 데이터로 변환을 수행하는 클래스입니다. 
 * <br>
 * 주로 Nexacro의 DataSet 데이터를 Java List<Bean> 또는 List<Map<String, Object>> 형태로 변환할 때 사용합니다.
 * <br>
 * 제네릭 타입 정보(Bean, Map 등)에 따라 각각 변환 방식이 달라집니다.
 *
 * @author Park SeongMin
 * @since 07.28.2015
 * @version 1.0
 */
public class DataSetToListConverter extends AbstractDataSetConverter implements NexacroConverter<DataSet, List> {

    /**
     * 변환 지원 여부를 확인합니다.
     * <br>
     * source가 DataSet, target이 List인 경우에만 true를 반환합니다.
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
        
        // 변환 지원: DataSet → List
        if(DataSet.class.equals(source) && List.class.equals(target)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * DataSet을 List로 변환합니다.
     * <br>
     * - definition의 genericType에 따라 Bean, Map 변환 방식을 결정합니다.
     * - Bean: List<VO/DTO> 변환, Map: List<Map<String, Object>> 변환
     * <br>
     * DataSet 또는 genericType이 잘못된 경우 예외가 발생합니다.
     *
     * @param ds 변환 대상 DataSet
     * @param definition 변환 정의 정보(제네릭타입 등)
     * @return 변환 결과 List
     * @throws NexacroConvertException 변환 실패 시
     */
    @Override
    public List convert(DataSet ds, ConvertDefinition definition) throws NexacroConvertException {

        if(definition == null) {
            throw new NexacroConvertException(ConvertDefinition.class.getSimpleName()+" must not be null.");
        }
        
        // 제네릭 타입(Class<Bean> 또는 Map.class) 획득
        Class genericType = definition.getGenericType();
        
        // DataSet이 null이면 빈 리스트 반환
        if(ds == null) {
            return new ArrayList();
        }
        
        // 제네릭 타입 미설정 시 예외 처리
        if(genericType == null) {
            throw new NexacroConvertException("List<> generic type must be declared.");
        }
        
        // genericType에 따라 분기: Map → map리스트, Bean → bean리스트
        List dataList ;
        if(Map.class.equals(genericType)) {
            dataList = convertDataSetToListMap(ds, definition);
        } else {
            dataList = convertDataSetToListBean(ds, definition);
        }
        
        return dataList;
    }
    
    /**
     * DataSet을 List<Map<String, Object>> 형태로 변환합니다.
     *
     * @param ds DataSet 객체
     * @param definition 변환 정의 정보
     * @return Map 리스트 결과값
     */
    private List<Map<String, Object>> convertDataSetToListMap(DataSet ds, ConvertDefinition definition) {
        
        String[] columnNames = getDataSetColumnNames(ds);
        
        // 기본 데이터 row 처리
        List<Map<String, Object>> dataListMap = new ArrayList<Map<String, Object>>();
        int rowCount = ds.getRowCount();
        for(int rowIndex=0; rowIndex<rowCount; rowIndex++) {
            addRowIntoListMap(dataListMap, ds, rowIndex, columnNames);
        }
        
        // 삭제된 row 처리
        int removedRowCount = ds.getRemovedRowCount();
        for(int removedIndex=0; removedIndex<removedRowCount; removedIndex++) {
            addRemovedRowIntoListMap(dataListMap, ds, removedIndex, columnNames);
        }
    
        return dataListMap;
    }
    
    /**
     * DataSet의 한 row를 Map으로 만들어 리스트에 추가합니다.
     *
     * @param dataListMap 대상 리스트
     * @param ds DataSet
     * @param rowIndex 행 인덱스
     * @param columnNames 컬럼명 배열
     */
    private void addRowIntoListMap(List<Map<String, Object>> dataListMap, DataSet ds, int rowIndex, String[] columnNames) {
        // 한 row를 Map으로 변환하여 리스트에 추가
        Map<String, Object> dataRow = new HashMap<String, Object>();
        addRowIntoMap(dataRow, ds, rowIndex, columnNames);
        dataListMap.add(dataRow);
    }

    /**
     * 삭제된 row 정보를 Map으로 만들어 리스트에 추가합니다.
     * 삭제된 행임을 나타내는 rowType 컬럼이 추가됩니다.
     *
     * @param dataList 대상 리스트
     * @param ds DataSet
     * @param removedIndex 삭제된 row 인덱스
     * @param columnNames 컬럼명 배열
     */
    private void addRemovedRowIntoListMap(List<Map<String, Object>> dataList, DataSet ds, int removedIndex, String[] columnNames) {
        // 삭제된 row를 Map으로 변환
        Map<String, Object> dataRow = new HashMap<String, Object>();
        for(int columnIndex=0; columnIndex<columnNames.length; columnIndex++) {
            Object object = ds.getRemovedData(removedIndex, columnIndex);
            // 리스너(후처리) 적용
            object = fireDataSetConvertedValue(ds, object, removedIndex, columnIndex, false, true);
            // property를 설정할 경우, 컬럼명 케이스 변환
            String columnName = Etc.convertColumnCaseFromTo(
                columnNames[columnIndex], 
                etcProperty.getEtcProperty("nexacro.client-column-case"), 
                etcProperty.getEtcProperty("nexacro.db-column-case"));
            dataRow.put(columnName, object);
        }
        // 삭제된 행 type 명시
        dataRow.put(DataSetRowTypeAccessor.NAME, DataSet.ROW_TYPE_DELETED);
        dataList.add(dataRow);
    }
    
    /**
     * DataSet을 Bean 리스트(List<Bean>)로 변환합니다.
     * 
     * @param ds DataSet
     * @param definition 변환 정의 정보(Bean 타입 포함)
     * @return 변환된 Bean 리스트
     * @throws NexacroConvertException 지원하지 않는 타입 혹은 변환 실패 시
     */
    private List<?> convertDataSetToListBean(DataSet ds, ConvertDefinition definition) throws NexacroConvertException {
        
        Class genericType = definition.getGenericType();
        
        // bean 지원 여부 확인
        if(!NexacroConverterHelper.isSupportedBean(genericType)) {
            throw new NexacroConvertException("unsupported source type. type="+genericType);
        }
        
        List dataList = new ArrayList();
        boolean isRemovedData = false;
        
        // 기본 row 처리(Bean 생성 및 값 주입)
        int rowCount = ds.getRowCount();
        for(int rowIndex=0; rowIndex<rowCount; rowIndex++) {
            addRowIntoListBean(dataList, genericType, ds, rowIndex, isRemovedData);
        }
        
        // 삭제된 row 처리
        int removedRowCount = ds.getRemovedRowCount();
        for(int removedIndex=0; removedIndex<removedRowCount; removedIndex++) {
            addRemovedRowIntoListBean(dataList, genericType, ds, removedIndex);
        }
        
        return dataList;
    }
    
    /**
     * DataSet의 한 row를 Bean으로 변환하여 리스트에 추가합니다.
     *
     * @param dataList 대상 리스트
     * @param beanType 변환할 Bean 타입
     * @param ds DataSet
     * @param rowIndex 행 인덱스
     * @param isRemovedData 삭제여부
     * @throws NexacroConvertException 변환 실패 시
     */
    private void addRowIntoListBean(List dataList, Class beanType, DataSet ds, int rowIndex, boolean isRemovedData) throws NexacroConvertException {
        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(beanType);
        addRowAndOrgRowIntoBean(beanWrapper, ds, rowIndex, isRemovedData);
        Object bean = beanWrapper.getInstance();
        dataList.add(bean);
    }
    
    /**
     * 삭제된 row를 Bean 인스턴스로 변환하여 리스트에 추가합니다.
     * 
     * @param dataList 대상 리스트
     * @param beanType 변환할 Bean 타입
     * @param ds DataSet
     * @param removedIndex 삭제된 row 인덱스
     * @throws NexacroConvertException 변환 실패 시
     */
    private void addRemovedRowIntoListBean(List dataList, Class beanType, DataSet ds, int removedIndex)
            throws NexacroConvertException {

        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(beanType);
        Object bean = beanWrapper.getInstance();
        boolean isSavedData = false;
        boolean isRemovedData = true;
        addRowIntoBean(beanWrapper, ds, removedIndex, isSavedData, isRemovedData);

        dataList.add(bean);
    }
    
}