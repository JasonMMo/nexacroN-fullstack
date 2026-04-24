package com.nexacro.uiadapter.spring.dao;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.mapping.ResultMapping;

import com.nexacro.java.xapi.data.datatype.DataType;
import com.nexacro.java.xapi.data.datatype.DataTypeFactory;
import com.nexacro.java.xapi.data.datatype.PlatformDataType;

/**
 * Dbms를 구현한 추상클래스로서 ResultSetMetaData로부터 데이터셋으로 변환하기 위해 X-API의 DataType을 활용하여 기본형에 대해 데이터 변환을 수행한다. 
 *
 * @author Park SeongMin
 * @since 08.07.2015
 * @version 1.0
 */

public abstract class AbstractDbms implements Dbms {
    
    /**
     * DBMS type handle
     *
     * @param column column
     */
    public abstract void handleColumnDataType(DbColumn column);
    
    public List<DbColumn> getDbColumns(ResultSetMetaData resultSetMetaData) throws SQLException {
        
        List<DbColumn> columnList = new ArrayList<DbColumn>();
        
        int columnCount = resultSetMetaData.getColumnCount();
        // rs..
        for(int i=1; i<=columnCount; i++) {
            
            String columnName = resultSetMetaData.getColumnLabel(i);
            if (columnName == null || columnName.isEmpty()) {
                columnName = resultSetMetaData.getColumnName(i);
            }
            
            String vendorsTypeName = resultSetMetaData.getColumnTypeName(i);
            String typeJavaClassName = resultSetMetaData.getColumnClassName(i);
            
            DataType dataType = DataTypeFactory.getSqlDataType(vendorsTypeName);
            if(dataType==null) {
                // when dbms vendor's specific column type name
                int javaSqlTypeNumber = resultSetMetaData.getColumnType(i);
                dataType = DataTypeFactory.getSqlDataType(javaSqlTypeNumber);
            }

            // find platform datatype
            dataType = DataTypeFactory.getPlatformDataType(dataType);
            if(dataType == null) {
                dataType = PlatformDataType.UNDEFINED;
            }
            
            int precision = resultSetMetaData.getPrecision(i);
            int scale = resultSetMetaData.getScale(i);
            int columnSize = resultSetMetaData.getColumnDisplaySize(i);
            DbColumn column = new DbColumn(columnName, dataType, columnSize, vendorsTypeName);
            column.setTypeJavaClassName(typeJavaClassName);
            column.setPrecision(precision);
            column.setScale(scale);
            
            // handle column for dbms
            handleColumnDataType(column);
            
            columnList.add(column);
        }
        
        return columnList;
    }

	/****************************************************
	 * 2023.10.18 조건 추가....
	 * 추가 사유 : procedure 호출시 0건일 경우 컬럼 정보 처리 로직 추가.
	 * 제약 사항 : resultSetMetaData 대신 resultMappings 에서 값을 가져옴. 없는 정보가 있음.
	 * 없는 정보  :	vendorsTypeName	: "VARCHAR"
	 * 			precision		: 0
	 * 			scale			: 0
	 * 			columnSize		: 256
	 ****************************************************/    
    public List<DbColumn> getDbColumns(List<ResultMapping> resultMappings) throws SQLException {
        
        List<DbColumn> columnList = new ArrayList<DbColumn>();
        
        //\\int columnCount = resultSetMetaData.getColumnCount();
        int columnCount = resultMappings.size();
        // rs..
        for(int i=0; i<columnCount; i++) {

            String columnName = resultMappings.get(i).getColumn();

            String vendorsTypeName;
            if(resultMappings.get(i).getJdbcType()!=null) {
            	vendorsTypeName = resultMappings.get(i).getJdbcType().name();
            } else {
            	vendorsTypeName = "VARCHAR";
            }
            String typeJavaClassName = resultMappings.get(i).getJavaType().getName();

            DataType dataType = DataTypeFactory.getSqlDataType(vendorsTypeName);
            if(dataType==null) {
                // when dbms vendor's specific column type name
                int javaSqlTypeNumber = 12;	// VARCHAR
                dataType = DataTypeFactory.getSqlDataType(javaSqlTypeNumber);
            }

            // find platform datatype
            dataType = DataTypeFactory.getPlatformDataType(dataType);
            if(dataType == null) {
                dataType = PlatformDataType.UNDEFINED;
            }

            int precision = 0;
            int scale = 0;
            int columnSize = 256;
            DbColumn column = new DbColumn(columnName, dataType, columnSize, vendorsTypeName);
            column.setTypeJavaClassName(typeJavaClassName);
            column.setPrecision(precision);
            column.setScale(scale);

            // handle column for dbms
            handleColumnDataType(column);

            columnList.add(column);
        }
        
        return columnList;
    }

    protected DataType findPlatformDataType(int javaSqlTypeNumber, String vendorsTypeName) {
        
        DataType dataType = DataTypeFactory.getSqlDataType(vendorsTypeName);
        if(dataType==null) {
            // when dbms vendor's specific column type name
            dataType = DataTypeFactory.getSqlDataType(javaSqlTypeNumber);
        }

        dataType = DataTypeFactory.getPlatformDataType(dataType);
        if(dataType == null) {
            dataType = PlatformDataType.UNDEFINED;
        }
        
        return dataType;
    }
    
}
