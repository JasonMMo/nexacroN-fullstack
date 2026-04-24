package com.nexacro.uiadapter.spring.dao.dbms;

import com.nexacro.uiadapter.spring.dao.AbstractDbms;
import com.nexacro.uiadapter.spring.dao.DbColumn;

/**
 * <p>Mariadb에서 사용되는 데이터 타입과 <code>DataSet</code>의 데이터 타입간의 매핑 정보를 제공한다.
 * 
 * @author Park SeongMin
 * @since 08.12.2015
 * @version 1.0
 */
public class Mariadb extends AbstractDbms {

	// MariaDB Mappings From SQL/JDBC Data Types to Java Data Types
	// http://underpop.online.fr/m/mysql/manual/mysql-connectors-apis-connector-j-reference-type-conversions.html
    @Override
    public void handleColumnDataType(final DbColumn column) {

        /* *******************************************************************
         * Data Types 맵핑 셈플
         * MariaDB Data Types을 Java Data Type 으로 전환하는 셈플로서 사이트에 맞게 맵핑한다.
         * *******************************************************************/
        /*
        if ("MEDIUMINT".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.INT);
        } else if ("SMALLINT".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.INT);
        } else if ("TINYINT".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.INT);
        } else if ("BIGINT".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.LONG);
        } else if ("REAL".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.FLOAT);
        } else if ("FLOAT".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.FLOAT);
        } else if ("DOUBLE".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.DOUBLE);
        } else if ("DECIMAL".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.BIG_DECIMAL);
        } else if ("NUMERIC".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.BIG_DECIMAL);
        } else if ("DATETIME".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.DATE_TIME);
        } else if ("DATE".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.DATE);
        } else if ("YEAR".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.DATE);
        } else if ("TIME".equals(column.getVendorsTypeName())) {
            column.setDataType(PlatformDataType.TIME);
 	    } else if ("LONGVARCHAR".equals(column.getVendorsTypeName())) {
	        column.setDataType(PlatformDataType.STRING);
 	    } else if ("BINARY".equals(column.getVendorsTypeName())) {
	        column.setDataType(PlatformDataType.BLOB);
 	    } else if ("VARBINARY".equals(column.getVendorsTypeName())) {
	        column.setDataType(PlatformDataType.BLOB);
 	    } else if ("LONGVARBINARY".equals(column.getVendorsTypeName())) {
	        column.setDataType(PlatformDataType.BLOB);
 	    } else if ("BIT".equals(column.getVendorsTypeName())) {
	        column.setDataType(PlatformDataType.BOOLEAN);
	    }
	    */
        // 위에서 설정하지 않은 data type의 경우 jdk에서 처리한다.
    }
}
