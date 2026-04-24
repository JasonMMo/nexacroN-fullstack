package com.nexacro.uiadapter.spring.dao.dbms;

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
public class EnterpriseDB extends AbstractDbms {

	// Postgre Mappings From SQL/JDBC Data Types to Java Data Types
	// https://www.instaclustr.com/postgresql-data-types-mappings-to-sql-jdbc-and-java-data-types/
	// https://www.enterprisedb.com/docs/jdbc_connector/latest/11_reference_jdbc_data_types/
	
    @Override
    public void handleColumnDataType(final DbColumn column) {

    	if (column == null) {
            return;
        }
        
    	// 데이타베이스 고유의 column type 명칭
        final String vendorsTypeName = column.getVendorsTypeName();
        
        if("BPCHAR".equals(vendorsTypeName) 
        		|| "VARCHAR".equals(vendorsTypeName) 
        		|| "BYTEA".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.STRING);
        } else if ("BOOL".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.BOOLEAN);
        } else if ("INT4".equals(vendorsTypeName) 
        		|| "INT2".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.INT);
        } else if ("INT8".equals(vendorsTypeName) ) {
            column.setDataType(PlatformDataType.LONG);
        } else if ("FLOAT8".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.DOUBLE);
        } else if ("FLOAT4".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.FLOAT);
        } else if ("NUMERIC".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.BIG_DECIMAL);
        } else if ("DATE".equals(vendorsTypeName)) {
            column.setDataType(PlatformDataType.DATE);
        } else if (column.getVendorsTypeName().startsWith("TIME")) { 
            column.setDataType(PlatformDataType.DATE_TIME);
            column.setSize(6);
            column.setDecimalDigit(null);
        }
        // 그 외 EnterpriseDB Postgre 타입의 경우 자동 변환은 수행하지 않는다.
    }
}
