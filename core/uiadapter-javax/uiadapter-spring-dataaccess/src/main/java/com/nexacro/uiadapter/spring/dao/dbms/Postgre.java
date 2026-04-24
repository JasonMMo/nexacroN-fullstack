package com.nexacro.uiadapter.spring.dao.dbms;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.nexacro.java.xapi.data.datatype.DataType;
import com.nexacro.java.xapi.data.datatype.DataTypeFactory;
import com.nexacro.java.xapi.data.datatype.PlatformDataType;

import com.nexacro.uiadapter.spring.dao.AbstractDbms;
import com.nexacro.uiadapter.spring.dao.DbColumn;

/**
 * <p>Postgre에서 사용되는 데이터 타입과 <code>DataSet</code>의 데이터 타입간의 매핑 정보를 제공한다.
 *
 * @author Tobesoft
 * @since 11.10.2021
 * @version 1.0
 */
public class Postgre extends AbstractDbms {

	// Postgre Mappings From SQL/JDBC Data Types to Java Data Types
	// https://www.instaclustr.com/postgresql-data-types-mappings-to-sql-jdbc-and-java-data-types/

	/**
	 * <pre>
	 * 	Desc	devpro의 컬럼은 upper case 이고 postgre의 컬럼은 snake case 인 조건에서 데이터가 0건일 경우 column case를 맞춰주기 위한 재정의
	 * 			- Etc.convertColumnCaseFromTo(String columnName, String from, String to); 사용
	 * 
	 * </pre>
	 * {@code @Date}	2024.11.28
     */
    public List<DbColumn> getDbColumns(ResultSetMetaData resultSetMetaData) throws SQLException {
        
        List<DbColumn> columnList = new ArrayList<DbColumn>();
        
        int columnCount = resultSetMetaData.getColumnCount();
        // rs..
        for(int i=1; i<=columnCount; i++) {
            
            String columnName = resultSetMetaData.getColumnLabel(i);
            if (columnName == null || columnName.isEmpty()) {
                columnName = resultSetMetaData.getColumnName(i);
            }
            // 2024.11.28 convert colume case 원복.
            //columnName = Etc.convertColumnCaseFromDbToUi(columnName, "snake", "upper");
            
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
    
    @Override
    public void handleColumnDataType(final DbColumn column) {

    	if (column == null) {
            return;
        }
        
        final String vendorsTypeName = column.getVendorsTypeName();
        
        if("char".equals(vendorsTypeName) 
        		|| "bpchar".equals(vendorsTypeName) 
        		|| "varchar".equals(vendorsTypeName) 
        		|| "text".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.STRING);
        } else if ("bool".equals(vendorsTypeName) 
        		|| "bit".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.BOOLEAN);
        } else if ("int4".equals(vendorsTypeName) 
        		|| "int2".equals(vendorsTypeName) 
        		|| "serial".equals(vendorsTypeName) 
        		|| "smallserial".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.INT);
        } else if ("int8".equals(vendorsTypeName) 
        		|| "bigserial".equals(vendorsTypeName) 
        		|| "oid".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.LONG);
        } else if ("float8".equals(vendorsTypeName) 
        		|| "money".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.DOUBLE);
        } else if ("float4".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.FLOAT);
        } else if ("numeric".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.BIG_DECIMAL);
        } else if ("date".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.DATE);
        } else if (column.getVendorsTypeName().startsWith("time")) { 
            column.setDataType(PlatformDataType.DATE_TIME);
            column.setSize(6);
            column.setDecimalDigit(null);
        } else if ("xml".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.STRING);
        } else if ("bytea".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.BLOB);
        }
        // 그 외 Postgre 타입의 경우 자동 변환은 수행하지 않는다.
    }
}
