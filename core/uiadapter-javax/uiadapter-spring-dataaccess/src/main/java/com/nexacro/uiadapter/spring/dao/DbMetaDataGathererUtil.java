package com.nexacro.uiadapter.spring.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.ClassUtils;

import com.nexacro.java.xapi.data.datatype.DataType;
import com.nexacro.uiadapter.spring.core.data.metadata.NexacroMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.BeanMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.MapMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.UnsupportedMetaData;
import com.nexacro.uiadapter.spring.core.data.support.NexacroConverterHelper;
import com.nexacro.uiadapter.spring.dao.mybatis.NexacroMybatisMetaDataProvider;

public abstract class DbMetaDataGathererUtil {
	private static final Logger logger = LoggerFactory.getLogger(NexacroMybatisMetaDataProvider.class);
	
	public static NexacroMetaData generateMetaDataFromClass(Class clazz) {
        
        if(!Map.class.isAssignableFrom(clazz)) {
            if(ClassUtils.isPrimitiveOrWrapper(clazz)) {
                return new UnsupportedMetaData(null);
            }
            
            return new BeanMetaData(clazz);
        }
        
        return null;
    }
    
	public static MapMetaData generateMetaDataFromDbColumns(List<DbColumn> dbColumns) {
        Map<String, Object> mapData = new HashMap<String, Object>();
        for(DbColumn column: dbColumns) {
            String name = column.getName();
            DataType dataType = column.getDataType();
//            Object defaultValue = NexacroConverterHelper.getDefaultValue(dataType);
            // MetaData 생성 시 Map안의 데이터는 타입을 확인할 수 있도록 데이터타입의 기본값을 설정하도록 한다.
            Object defaultValue = NexacroConverterHelper.getDefaultMetaDataValue(dataType);
            
            mapData.put(name, defaultValue); 
        }
        
        return new MapMetaData(mapData);
    }
    
	/*
	 *  2022.02.16	
	 *  MyBatis에서는 map으로 리턴할 때 camalcase가 지원되지 않음.
	 *  조회결과가 0건이고 컬럼이 많을 경우 별도로 컬럼을 정의하지 않더라고 
	 *  resultType을 HashMap으로 CamelCase형태의 컬럼이 나오도록 기능추가함..
	 *  NexacroMybatisResultSetHandler.getMetaDataFromResultSet()에서 호출됨.
	 */
	public static MapMetaData generateMetaDataFromDbColumnsToCamelCase(List<DbColumn> dbColumns) {
        Map<String, Object> mapData = new HashMap<String, Object>();
        for(DbColumn column: dbColumns) {
            String name = JdbcUtils.convertUnderscoreNameToPropertyName(column.getName());
            DataType dataType = column.getDataType();
            // Object defaultValue = NexacroConverterHelper.getDefaultValue(dataType);
            // MetaData 생성 시 Map안의 데이터는 타입을 확인할 수 있도록 데이터타입의 기본값을 설정하도록 한다.
            Object defaultValue = NexacroConverterHelper.getDefaultMetaDataValue(dataType);
            
            mapData.put(name, defaultValue); 
        }
        
        return new MapMetaData(mapData);
    }
	/*
	 * object 가 interf의 subClass인지 체크.
	 */
	public static <T, U> Boolean hasInterface(Class<T> object, Class<U> interf){

		for(Class cls : ClassUtils.getAllInterfacesForClass(object))
		{
            logger.info("[ {} ] {}", cls.getCanonicalName(), interf.getCanonicalName());
	    	if( interf.equals(cls))
	    		return true;
		}

	    return false;
	}	
}
