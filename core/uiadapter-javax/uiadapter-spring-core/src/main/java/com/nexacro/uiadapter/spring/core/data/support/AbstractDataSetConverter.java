package com.nexacro.uiadapter.spring.core.data.support;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.PropertyAccessException;

import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.datatype.DataType;
import com.nexacro.java.xapi.data.datatype.PlatformDataType;
import com.nexacro.uiadapter.spring.core.data.DataSetRowTypeAccessor;
import com.nexacro.uiadapter.spring.core.data.DataSetSavedDataAccessor;
import com.nexacro.uiadapter.spring.core.data.convert.ConvertDefinition;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConvertException;
import com.nexacro.uiadapter.spring.core.util.DateUtils;
import com.nexacro.uiadapter.spring.core.util.Etc;
import com.nexacro.uiadapter.spring.core.util.EtcPropertiesBase;
import com.nexacro.uiadapter.spring.core.util.ReflectionUtil;

/**
 * <p><code>DataSet</code> 데이터를 POJO 혹은 Map 형태의 데이터로 변환하기 위한 추상 클래스입니다.
 * <br>
 * 넥사크로의 DataSet과 바인딩되는 Java 객체의 변환 및 컬럼명 케이스 처리, 구조 변경 등 공통 변환 로직을 제공합니다.
 * <br>
 * 실질적인 변환 처리의 베이스 역할을 하며, 다양한 데이터 유형 변환 시 확장하여 사용할 수 있습니다.
 *
 * @author Park SeongMin
 * @since 08.11.2015
 * @version 1.0
 */
public class AbstractDataSetConverter extends AbstractListenerHandler {

    /** 변환 및 로깅을 위한 로거 객체 */
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 2023.07.26
     * client의 column명 case(표기법) 형식에 따라 케이스 컨버팅을 위한 설정 객체
     * camel, kebab, snake, upper 등 다양한 표기법 처리 지원
     */
    public EtcPropertiesBase etcProperty;

    /**
     * 생성자 - 공통 설정 Bean을 주입받아 초기화합니다.
     */
    public AbstractDataSetConverter() {    
        etcProperty = (EtcPropertiesBase) Etc.getBean("etcProperty");
    }

    /* ***********************************************************************************************
    **********************************  Object -> DataSet  *******************************************
    **************************************************************************************************/

    /**
     * Map 데이터를 DataSet의 행(row)으로 추가합니다.
     * <br>
     * 데이터가 null일 경우 행 추가 없이 종료하며,
     * 컬럼명 변환 및 미등록 컬럼은 구조 변경 또는 무시 조건(disallowChangeStructure)에 따라 처리됩니다.
     *
     * @param ds DataSet 객체
     * @param map 입력 데이터(Map)
     * @param disallowChangeStructure 컬럼 자동 추가 허용여부(false면 구조 변경 허용)
     * @throws NexacroConvertException 변환 오류 시 throw
     */
    protected void addRowIntoDataSet(DataSet ds, Map map, boolean disallowChangeStructure) throws NexacroConvertException {
        
        int newRow = ds.newRow();
        // null 데이터는 추가하지 않음
        if(map == null) {
            return;
        } 
        
        Iterator iterator = map.keySet().iterator();
        while(iterator.hasNext()) {
            Object key = iterator.next();
            Object value = map.get(key);
            
            if(!(key instanceof String)) {
                throw new NexacroConvertException("must be Map<String, Object> if you use List<Map>. target=" + ds.getName());
            }
            String columnName = (String) key;
            
            // 컬럼명 케이스 변환 처리
            columnName = Etc.convertColumnCaseFromDbToUi(
                columnName, 
                etcProperty.getEtcProperty("nexacro.db-column-case"), 
                etcProperty.getEtcProperty("nexacro.client-column-case")
            );

            // 무시 대상 컬럼명 처리
            if (ignoreSpecfiedColumnName(columnName)) {
                continue;
            }
            
            // 값 타입 변환(Byte[] 등)
            Object object = NexacroConverterHelper.toObject(value);
            
            int columnIndex = ds.indexOfColumn(columnName);
            if(columnIndex < 0) {
                if(disallowChangeStructure) {
                    // 구조 변경이 불가할 때는 컬럼 추가 무시
                    continue;
                } else {
                    // 구조 변경 허용 시 컬럼 추가
                    ds.setChangeStructureWithData(true);
                    // 컬럼 추가(원본 키명 기준)
                    if(!addColumnByMap(ds, (String) key, value)) {
                        continue;
                    }
                }
            }
            // LocalDateTime 타입 변환 처리
            if(object instanceof LocalDateTime) {
                object = DateUtils.toDate((LocalDateTime)object);
                logger.debug("\t[◆  addRowIntoDataSet(math.LocalDateTime->{})] {}", object.getClass().getName(), object);
            }
            // 이벤트(변환값 후처리 등)
            object = fireDataSetConvertedValue(ds, object, newRow, columnIndex, false, false);
            // 데이터 셋에 값 설정
            ds.set(newRow, columnName, object);
        }
    }
    
    /**
     * Bean 객체의 값을 DataSet의 행(row)으로 추가합니다.
     * <br>
     * Java Bean의 프로퍼티명을 컬럼명으로 변환하고, 값(Byte[] 등) 처리를 거쳐 행 데이터를 추가합니다.
     *
     * @param ds DataSet 객체
     * @param obj 입력 Bean 객체
     */
    protected void addRowIntoDataSet(DataSet ds, Object obj) {
        int newRow = ds.newRow();
        if(obj == null) { // null 데이터는 추가하지 않음
            return;
        } 
        
        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(obj);
        NexacroBeanProperty[] beanProperties = beanWrapper.getProperties();
        for(NexacroBeanProperty property: beanProperties) {
            // static(상수)에 해당하는 프로퍼티는 무시
            if(property.isStatic()) {
                continue;
            }
            String propertyName = property.getPropertyName();
            Object propertyValue = beanWrapper.getPropertyValue(property);
            // Byte[] 변환
            Object object = NexacroConverterHelper.toObject(propertyValue);
            // 컬럼명 케이스 변환
            propertyName = Etc.convertColumnCaseFromDbToUi(
                propertyName, 
                etcProperty.getEtcProperty("nexacro.db-column-case"), 
                etcProperty.getEtcProperty("nexacro.client-column-case")
            );
            // 컬럼 미존재 시 무시(Map과 달리 구조 변경이 이미 처리되었음)
            int columnIndex = ds.indexOfColumn(propertyName);
            if(columnIndex < 0) {
                continue;
            }
            // 이벤트 후처리
            object = fireDataSetConvertedValue(ds, object, newRow, columnIndex, false, false);
            // 데이터 셋에 값 설정
            ds.set(newRow, columnIndex, object);
        }
    }
    
    /**
     * Map의 key를 DataSet의 컬럼으로 추가합니다.
     * <br>
     * 값이 null이면 {@link PlatformDataType#UNDEFINED} 타입으로 지정합니다.
     *
     * @param ds DataSet 객체
     * @param map 컬럼 정보가 포함된 Map
     * @throws NexacroConvertException Map 구조가 잘못될 경우 throw
     */
    protected void addColumnIntoDataSet(DataSet ds, Map map) throws NexacroConvertException {
        ds.setChangeStructureWithData(true);
        Iterator iterator = map.keySet().iterator();
        while(iterator.hasNext()) {
            Object key = iterator.next();
            Object value = map.get(key);
            if(!(key instanceof String)) {
                throw new NexacroConvertException("must be Map<String, Object> if you use List<Map>. target="+ds.getName());
            }
            String columnName = (String) key;
            boolean isAdded = addColumnByMap(ds, columnName, value);
        }
    }
    
    /**
     * Bean의 멤버 필드 정보를 토대로 DataSet의 컬럼을 추가합니다.
     * <br>
     * 변환 가능 타입만 대상이며, static 프로퍼티와 상수 컬럼(constant column), 일반 컬럼을 구분해 처리합니다.
     *
     * @param ds DataSet 객체
     * @param availableFirstData 컬럼 정의용 Bean 데이터
     */
    protected void addColumnIntoDataSet(DataSet ds, Object availableFirstData) {
        ds.setChangeStructureWithData(true);
        NexacroBeanWrapper beanWrapper = NexacroBeanWrapper.createBeanWrapper(availableFirstData);
        NexacroBeanProperty[] beanProperties = beanWrapper.getProperties();
        for(NexacroBeanProperty property: beanProperties) {
            String propertyName = property.getPropertyName();
            Class<?> propertyType = property.getPropertyType();
            // 변환 불가 타입은 무시
            if(!NexacroConverterHelper.isConvertibleType(propertyType)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} type of {} is ignored because it can not be converted.", propertyType, propertyName);
                }
                continue;
            }
            DataType dataTypeOfValue = NexacroConverterHelper.getDataType(propertyType);
            // 컬럼명 케이스 변환
            propertyName = Etc.convertColumnCaseFromDbToUi(
                propertyName, 
                etcProperty.getEtcProperty("nexacro.db-column-case"), 
                etcProperty.getEtcProperty("nexacro.client-column-case")
            );
            if(property.isStatic()) {
                Object staticValue = beanWrapper.getPropertyValue(property);
                // Byte[] 변환
                staticValue = NexacroConverterHelper.toObject(staticValue);
                int columnIndex = ds.indexOfColumn(propertyName);
                // 이벤트 후처리
                staticValue = fireDataSetConvertedValue(ds, staticValue, -1, columnIndex, false, false);
                ds.addConstantColumn(propertyName, dataTypeOfValue, staticValue);
            } else {
                ds.addColumn(propertyName, dataTypeOfValue);
            }
        }
    }

    /**
     * Map을 기반으로 컬럼을 DataSet에 추가합니다.
     * <br>
     * 무시 컬럼 필터 및 케이스 변환, 데이터 타입 추출 등 세부 로직을 구현하는 부분입니다.
     *
     * @param ds DataSet 객체
     * @param columnName 컬럼 이름
     * @param value 샘플 데이터 값
     * @return 성공적으로 추가 시 true, 아니면 false
     */
    protected boolean addColumnByMap(DataSet ds, String columnName, Object value) {
        if (ignoreSpecfiedColumnName(columnName)) {
            return false;
        } 

        // 2023.08.24 columnCase 수정
    	// 2024.11.28 수정
    	columnName = Etc.convertColumnCaseFromDbToUi(columnName, etcProperty.getEtcProperty("nexacro.db-column-case"), etcProperty.getEtcProperty("nexacro.client-column-case"));

    	/* 값이 NULL인 경우 컬럼 타입을 UNDEFINED로 정의함 */
		if (value == null) {
			ds.addColumn(columnName, PlatformDataType.UNDEFINED);
			return false;
		}

		// Added :: isExtendConvertibleType()
		//			DataTypeFactory에서 커버하지 못하는 data type을 체크전에 변환 허용 :: 2023.09.20
		//			java.math.BigInteger	-> Long
		//			java.time.LocalDateTime	-> java.util.Date
		// check datatype before add column
		if ( !NexacroConverterHelper.isConvertibleType(value.getClass())
			&& !NexacroConverterHelper.isExtraConvertibleType(value.getClass())	) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} type of {} is ignored because it can not be converted.", value.getClass(), columnName);
            }
            return false;
		}
		DataType dataTypeOfValue = NexacroConverterHelper.getDataTypeOfValue(value);
		ds.addColumn(columnName, dataTypeOfValue);

        return true;
    }

    /**
     * DataSet의 모든 컬럼명을 String 배열로 반환합니다.
     * <br>
     * 컬럼명은 DataSet에 정의된 순서대로 리턴됩니다.
     * <br>
     * 컬럼이 없는 경우 빈 배열을 반환합니다.
     *
     * @param ds 대상 DataSet 객체
     * @return 컬럼명 배열(String[])
     */
    protected String[] getDataSetColumnNames(DataSet ds) {
        int columnCount = ds.getColumnCount();
        String[] columnNames = new String[columnCount];
        for(int i=0; i<columnNames.length; i++) {
            columnNames[i] = ds.getColumn(i).getName();
        }
        return columnNames;
    }
    
    
    /* ************************************************************************************************
    ********************************* (from)  DataSet -> (to) Object **********************************
    ***************************************************************************************************/
    /**
     * 입력받은 <code>DataSet</code>의 특정 행(rowIndex) 데이터를 Map에 저장합니다.
     * <br>
     * 일반 데이터와, 변경 전 데이터(saved data)가 있는 경우 모두 각각의 맵에 변환해 저장합니다.
     * <br>
     * property를 사용할 경우, 컬럼명 케이스 변환(nexacro.client-column-case → nexacro.db-column-case)을 적용합니다.
     * <br>
     * 행(row)의 타입 정보도 함께 Map에 저장합니다.
     *
     * @param dataMap        결과 데이터를 채워넣을 Map 객체
     * @param ds             변환 대상 DataSet 객체
     * @param rowIndex       추출할 행의 인덱스
     * @param columnNames    변환 대상 컬럼 명 목록 (순서 보장)
     */
    protected void addRowIntoMap(Map<String, Object> dataMap, DataSet ds, int rowIndex, String[] columnNames) {

        int rowType = -1; // 2022.11.23 xapi에 정의되지 않은 초기값.
        // DataSet에 실제 데이터가 있는 경우 각 컬럼값을 변환하여 dataMap에 저장
        if(ds.hasData()) {   // 2022.11.23 json type일 경우 값이 없을 경우 에러 대응.
            rowType = ds.getRowType(rowIndex);
            for(int columnIndex=0; columnIndex<columnNames.length; columnIndex++) {
                Object object = ds.getObject(rowIndex, columnIndex);

                // 이벤트 처리(값 변환, 후처리 등)
                object = fireDataSetConvertedValue(ds, object, rowIndex, columnIndex, false, false);

                // 컬럼명 case 변환 (클라이언트 → DB)
                String columnName = Etc.convertColumnCaseFromTo(
                    columnNames[columnIndex], 
                    etcProperty.getEtcProperty("nexacro.client-column-case"), 
                    etcProperty.getEtcProperty("nexacro.db-column-case")
                );

                dataMap.put(columnName, object);
                //dataMap.put(columnNames[columnIndex], object); // 원본 key로 넣던 코드(주석)
            }
        }
        // Saved 데이터(저장된 데이터)가 존재하는 경우 별도의 맵에 저장
        if(ds.hasSavedRow(rowIndex)) {
            Map<String, Object> savedDataRow = new HashMap<String, Object>();
            for(int columnIndex=0; columnIndex<columnNames.length; columnIndex++) {
                Object object = ds.getSavedData(rowIndex, columnIndex);

                // 이벤트 처리(값 변환, 후처리 등)
                object = fireDataSetConvertedValue(ds, object, rowIndex, columnIndex, true, false);

                // 컬럼명 case 변환 (클라이언트 → DB)
                String columnName = Etc.convertColumnCaseFromTo(
                    columnNames[columnIndex], 
                    etcProperty.getEtcProperty("nexacro.client-column-case"), 
                    etcProperty.getEtcProperty("nexacro.db-column-case")
                );

                savedDataRow.put(columnName, object);
                //savedDataRow.put(columnNames[columnIndex], object); // 원본 key로 넣던 코드(주석)
            }
            
            // Saved 데이터는 별도의 키(DataSetSavedDataAccessor.NAME)에 할당
            dataMap.put(DataSetSavedDataAccessor.NAME, savedDataRow);
        }
        
        // 행 타입(rowType)을 dataMap에 추가
        if(ds.hasData()) // 2022.11.23 json type일 경우 값이 없을 경우 에러 대응.
            dataMap.put(DataSetRowTypeAccessor.NAME, rowType);
    }
    
    /**
     * 입력받은 <code>DataSet</code>의 지정된 행(rowIndex)의 데이터를 bean에 저장합니다.
     * <br>
     * 일반 데이터와, 원본 데이터(saved data)가 모두 bean에 저장되며,
     * bean이 {@link DataSetSavedDataAccessor}를 구현한 경우 별도의 saved data bean에도 값을 할당합니다.
     * <br>
     * 또한, 행(row)의 타입 정보도 저장됩니다.
     *
     * @param beanWrapper   값을 세팅할 Bean Wrapper 객체
     * @param ds            변환 대상 DataSet 객체
     * @param rowIndex      추출할 행 인덱스
     * @param isRemovedData 삭제 데이터 여부 (true면 삭제된 데이터 처리)
     * @throws NexacroConvertException 변환이 실패한 경우 발생
     * @see DataSetRowTypeAccessor
     * @see DataSetSavedDataAccessor
     */
    protected void addRowAndOrgRowIntoBean(NexacroBeanWrapper beanWrapper, DataSet ds, int rowIndex, boolean isRemovedData) throws NexacroConvertException {
        
        boolean isSavedData = false;
        // 1. 현재 행의 일반 데이터를 bean에 저장
        addRowIntoBean(beanWrapper, ds, rowIndex, isSavedData, isRemovedData);
        
        // 2. 해당 행에 저장된(saved) 데이터가 존재하고,
        //    bean이 DataSetSavedDataAccessor 인터페이스를 구현한 경우 saved data 세팅
        if(ds.hasSavedRow(rowIndex)) {
            Object bean = beanWrapper.getInstance();
            Class<?> beanType = bean.getClass();
            if(ReflectionUtil.isImplemented(beanType, DataSetSavedDataAccessor.class)) {
                isSavedData = true;
                // saved data용 빈 생성 및 값 세팅
                NexacroBeanWrapper savedBeanWrapper = NexacroBeanWrapper.createBeanWrapper(beanType);
                addRowIntoBean(savedBeanWrapper, ds, rowIndex, isSavedData, isRemovedData);
                
                // 생성된 saved data 빈을 accessor를 통해 bean에 할당
                DataSetSavedDataAccessor accessor = (DataSetSavedDataAccessor) bean;
                accessor.setData(savedBeanWrapper.getInstance());
            }
        }
    }
    
    /**
     * 입력받은 <code>DataSet</code>의 특정 행(rowIndex) 데이터를 bean의 각 프로퍼티에 저장합니다.
     * <br>
     * property를 사용할 경우, 각 컬럼명을 case 변환하여 매핑하고,
     * 행 데이터의 타입(rowType) 정보(DataSetRowTypeAccessor 구현체일 경우)도 함께 bean에 저장합니다.
     *
     * @param beanWrapper   값을 세팅할 Bean Wrapper 객체
     * @param ds            변환 대상 DataSet 객체
     * @param rowIndex      추출할 행 인덱스
     * @param isSavedData   저장 데이터 여부 (true면 원본 데이터로 변환)
     * @param isRemovedData 삭제 데이터 여부 (true면 삭제된 데이터로 변환)
     * @throws NexacroConvertException 변환이 실패한 경우 발생
     */
    protected void addRowIntoBean(NexacroBeanWrapper beanWrapper,
            DataSet ds, int rowIndex, boolean isSavedData, boolean isRemovedData) throws NexacroConvertException {

        // 1. bean의 모든 프로퍼티에 대해 컬럼 매핑 및 변환 수행
        NexacroBeanProperty[] beanProperties = beanWrapper.getProperties();
        for(NexacroBeanProperty property : beanProperties) {
            
            String propertyName = property.getPropertyName();
            // 컬럼명 케이스 변환 (클라이언트 → DB 형식)
            propertyName = Etc.convertColumnCaseFromTo(
                propertyName, 
                etcProperty.getEtcProperty("nexacro.client-column-case"), 
                etcProperty.getEtcProperty("nexacro.db-column-case")
            );
           
            int columnIndex = ds.indexOfColumn(propertyName);
            // 컬럼이 없는 경우 해당 속성은 패스
            if(columnIndex == -1) {
                continue;
            }
            
            Class<?> propertyType = property.getPropertyType();

            // DataSet 값에서 Bean 프로퍼티 타입으로 변환
            Object convertDataSetValue = NexacroConverterHelper.toObjectFromDataSetValue(
                ds, rowIndex, columnIndex, propertyType, isSavedData, isRemovedData
            );
            
            // fire event
            convertDataSetValue = fireDataSetConvertedValue(ds, convertDataSetValue, rowIndex, columnIndex, isSavedData, isRemovedData);
            
            try {
                // 변환된 값을 Bean 프로퍼티에 실제 할당
                beanWrapper.setPropertyValue(property, convertDataSetValue);
            } catch (InvalidPropertyException e) {
                throw new NexacroConvertException(e.getMessage(), e);
            } catch (PropertyAccessException e) {
                throw new NexacroConvertException(e.getMessage(), e);
            } catch (BeansException e) {
                throw new NexacroConvertException(e.getMessage(), e);
            }
        }
        
        // 2. rowType 정보 세팅(인터페이스 구현Bean만)
        Object bean = beanWrapper.getInstance();
        Class beanType = bean.getClass();
        
        // Bean이 DataSetRowTypeAccessor 구현체인 경우 rowType 저장
        if(ReflectionUtil.isImplemented(beanType, DataSetRowTypeAccessor.class)) {
            
        	if(ds.hasData()) // 2022.11.23 값이 없는 json type일 경우 대응.
        	{
                int rowType ; 
                if(isRemovedData) {
                    rowType = DataSet.ROW_TYPE_DELETED;     // 삭제 데이터
                } else if(isSavedData){
                    rowType = DataSet.ROW_TYPE_NORMAL;      // Saved 데이터
                } else {
                    rowType = ds.getRowType(rowIndex);      // 실제 행의 타입
                }
                
                DataSetRowTypeAccessor accessor = (DataSetRowTypeAccessor) bean;
                accessor.setRowType(rowType);
            }
        }
    }
    
    /**
     * 무시해야 할 컬럼명인지 판단합니다.
     * <br>
     * 컬럼명이 null 이거나, 빈 문자열("")이거나, 특정 이름(ID, ROW_TYPE 등)에 해당하는 경우 true를 반환합니다.
     * <br>
     * 주로 DataSet에 데이터를 매핑할 때, 시스템에서 관리하는 필드나 
     * 변환 대상이 아닌 특수 컬럼은 처리에서 제외하기 위해 사용됩니다.
     *
     * @param columnName 검사할 컬럼명
     * @return 무시 대상이면 true, 아니면 false
     */
    protected boolean ignoreSpecfiedColumnName(String columnName) {

        // 컬럼명이 null 또는 빈 문자열인 경우 무시
        //if(columnName == null || "".equals(columnName)) {
        //    return true;
        //}

        if(DataSetRowTypeAccessor.NAME.equals(columnName) || DataSetSavedDataAccessor.NAME.equals(columnName)) {
        	// DataSetRowType, DataSetSavedData는 무시한다.
            return true;
        }
        // 추가 무시 컬럼이 있다면 여기에 이어서 조건 추가 가능
        return false;
    }
    
    /**
     * 데이터 변환 정의(ConvertDefinition)에 따라 DataSet 객체를 생성합니다.
     * <br>
     * - ConvertDefinition에 스키마 DataSet(schemaDataSet)이 지정되어 있으면 해당 DataSet을 반환합니다.
     * - 스키마 DataSet이 없는 경우에는, definition에서 제공하는 name을 이용해 새로운 DataSet 객체를 생성해서 반환합니다.
     * <br>
     * 즉, <b>스키마 기반 변환처리가 필요할 때는 사전 선언된 DataSet을, 그렇지 않을 때는 새로 생성</b>해서 변환 작업에 사용합니다.
     *
     * @param definition DataSet 생성에 필요한 정의 정보(스키마, 이름 등)
     * @return 생성 또는 참조된 DataSet 객체
     */
    protected DataSet createDataSet(ConvertDefinition definition) {
        // 1. 스키마 DataSet이 정의된 경우, 해당 인스턴스 바로 반환
        DataSet schemaDataSet = definition.getSchemaDataSet();
        if(schemaDataSet != null) {
            return schemaDataSet;
        } else {
            // 2. 스키마 DataSet이 없으면 definition의 name을 활용한 새로운 DataSet 생성
            return new DataSet(definition.getName());
        }
        
        /*
        // (대체 구현 예시) Optional 사용 시 가독성 ↑
        Optional<DataSet> schemaOpt = Optional.ofNullable(definition.getSchemaDataSet());
        return schemaOpt.orElse(new DataSet(definition.getName()));
        */
    }
    
}