package com.nexacro.uiadapter.spring.core.data.support;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nexacro.java.xapi.data.ColumnHeader;
import com.nexacro.java.xapi.data.DataSet;
import com.nexacro.java.xapi.data.DataTypes;
import com.nexacro.java.xapi.data.Variable;
import com.nexacro.java.xapi.data.datatype.DataType;
import com.nexacro.java.xapi.data.datatype.DataTypeFactory;
import com.nexacro.java.xapi.data.datatype.PlatformDataType;
import com.nexacro.uiadapter.spring.core.data.convert.NexacroConverter.ConvertiblePair;
import com.nexacro.uiadapter.spring.core.util.ReflectionUtil;

/**
 * <p>DataSet혹은 Variable의 데이터 변환을 위한 helper class
 *
 * @author Park SeongMin
 * @since 2015. 7. 28.
 * @version 1.0
 */
public abstract class NexacroConverterHelper {

	private NexacroConverterHelper() {
		throw new IllegalStateException("NexacroConverterHelper class");
	}
    private static final Set<ConvertiblePair> LISTTODATASETCONVERTIBLESETS = Collections.singleton(new ConvertiblePair(List.class, DataSet.class));
    private static final Set<ConvertiblePair> DATASETTOLISTCONVERTIBLESETS = Collections.singleton(new ConvertiblePair(DataSet.class, List.class));
    
    private static final Set<ConvertiblePair> OBJECTTOVARIABLECONVERTIBLESETS = new HashSet<ConvertiblePair>();
    private static final Set<ConvertiblePair> VARIABLETOOBJECTCONVERTIBLESETS = new HashSet<ConvertiblePair>();
    
    private static final Map<Class<?>, Class<?>> PRIMITIVETYPEWRAPPERMAP = new HashMap<Class<?>, Class<?>>(8);
    private static final Map<Class<?>, Class<?>> NONPRIMITIVETYPEMAP = new HashMap<Class<?>, Class<?>>(4);
    
    
    static {
        // byte[], int, long, float, double, boolean, Object, String, BigDecimal, Date / byte, char, short는 지원하지 않는다.
        PRIMITIVETYPEWRAPPERMAP.put(byte[].class, Byte[].class);
        // 아래 항목 지원 시 데이터 유실이 발생할 수 있다.
        /*
        primitiveWrapperTypeMap.put(byte.class, Byte.class);
        primitiveWrapperTypeMap.put(char.class, Character.class);
        primitiveWrapperTypeMap.put(short.class, Short.class);
        */
        PRIMITIVETYPEWRAPPERMAP.put(int.class, Integer.class);
        PRIMITIVETYPEWRAPPERMAP.put(long.class, Long.class);
        PRIMITIVETYPEWRAPPERMAP.put(float.class, Float.class);
        PRIMITIVETYPEWRAPPERMAP.put(double.class, Double.class);
        PRIMITIVETYPEWRAPPERMAP.put(boolean.class, Boolean.class);
        
        NONPRIMITIVETYPEMAP.put(Object.class, Object.class);
        NONPRIMITIVETYPEMAP.put(String.class, String.class);
        NONPRIMITIVETYPEMAP.put(BigDecimal.class, BigDecimal.class);
        NONPRIMITIVETYPEMAP.put(Date.class, Date.class);
        NONPRIMITIVETYPEMAP.put(java.sql.Date.class, java.sql.Date.class); // used java.util.map in ibatis
        NONPRIMITIVETYPEMAP.put(java.sql.Timestamp.class, java.sql.Timestamp.class); // used java.util.map in ibatis
        NONPRIMITIVETYPEMAP.put(java.sql.Time.class, java.sql.Time.class); // used java.util.map in ibatis
        
        final Set<Class<?>> keySet = PRIMITIVETYPEWRAPPERMAP.keySet();
        for(final Class<?> clazz: keySet) {
            OBJECTTOVARIABLECONVERTIBLESETS.add(new ConvertiblePair(clazz, Variable.class));
            OBJECTTOVARIABLECONVERTIBLESETS.add(new ConvertiblePair(PRIMITIVETYPEWRAPPERMAP.get(clazz), Variable.class));
        }
        for(final Class<?> clazz: keySet) {
            VARIABLETOOBJECTCONVERTIBLESETS.add(new ConvertiblePair(Variable.class, clazz));
            VARIABLETOOBJECTCONVERTIBLESETS.add(new ConvertiblePair(Variable.class, PRIMITIVETYPEWRAPPERMAP.get(clazz)));
        }
        
        final Set<Class<?>> nonKeySet = NONPRIMITIVETYPEMAP.keySet();
        for(final Class<?> clazz: nonKeySet) {
            OBJECTTOVARIABLECONVERTIBLESETS.add(new ConvertiblePair(clazz, Variable.class));
        }
        for(final Class<?> clazz: nonKeySet) {
            VARIABLETOOBJECTCONVERTIBLESETS.add(new ConvertiblePair(Variable.class, clazz));
        }
    }
    
    static Set<ConvertiblePair> getObjectToVariableConvertibleTypes() {
        return OBJECTTOVARIABLECONVERTIBLESETS;
    }
    
    static Set<ConvertiblePair> getVariableToObjectConvertibleTypes() {
        return VARIABLETOOBJECTCONVERTIBLESETS;
    }
    
    static Set<ConvertiblePair> getListToDataSetConvertibleTypes() {
        return LISTTODATASETCONVERTIBLESETS;
    }
    
    static Set<ConvertiblePair> getDataSetToListConvertibleTypes() {
        return DATASETTOLISTCONVERTIBLESETS;
    }
    
    public static Object getDefaultValue(final DataType dataType) {
        
    	final int type = dataType.getType();
        
        if(type == DataTypes.STRING) {
            return DataTypes.DEFAULT_VALUE_STRING;
        } else if(type == DataTypes.INT) {
            return DataTypes.DEFAULT_VALUE_INT;
        } else if(type == DataTypes.LONG) {
            return DataTypes.DEFAULT_VALUE_LONG;
        } else if(type == DataTypes.FLOAT) {
            return DataTypes.DEFAULT_VALUE_FLOAT;
        } else if(type == DataTypes.DOUBLE) {
            return DataTypes.DEFAULT_VALUE_DOUBLE;
        } else if(type == DataTypes.BOOLEAN) {
            return DataTypes.DEFAULT_VALUE_BOOLEAN;
        } else if(type == DataTypes.DATE) {
            return DataTypes.DEFAULT_VALUE_DATE;
        } else if(type == DataTypes.DATE_TIME) {
            return DataTypes.DEFAULT_VALUE_DATE_TIME;
        } else if(type == DataTypes.TIME) {
            return DataTypes.DEFAULT_VALUE_TIME;
        } else if(type == DataTypes.BIG_DECIMAL) {
            return DataTypes.DEFAULT_VALUE_BIG_DECIMAL;
        } else if(type == DataTypes.BLOB) {
            return DataTypes.DEFAULT_VALUE_BLOB;
        }
        return DataTypes.DEFAULT_VALUE_OBJECT;
    }
    
    public static Object getDefaultMetaDataValue(final DataType dataType) {
        
    	final int type = dataType.getType();
        
        if(type == DataTypes.STRING) {
            return "";
        } else if(type == DataTypes.INT) {
            return DataTypes.DEFAULT_VALUE_INT;
        } else if(type == DataTypes.LONG) {
            return DataTypes.DEFAULT_VALUE_LONG;
        } else if(type == DataTypes.FLOAT) {
            return DataTypes.DEFAULT_VALUE_FLOAT;
        } else if(type == DataTypes.DOUBLE) {
            return DataTypes.DEFAULT_VALUE_DOUBLE;
        } else if(type == DataTypes.BOOLEAN) {
            return DataTypes.DEFAULT_VALUE_BOOLEAN;
        } else if(type == DataTypes.DATE) {
            return new Date();
        } else if(type == DataTypes.DATE_TIME) {
            return new Date();
        } else if(type == DataTypes.TIME) {
            return new Date();
        } else if(type == DataTypes.BIG_DECIMAL) {
            return new BigDecimal(0);
        } else if(type == DataTypes.BLOB) {
            return new byte[0];
        }
        
        return DataTypes.DEFAULT_VALUE_OBJECT;
    }
    
    public static DataType getDataType(final Class targetClass) {
        
        // DataTypeFactory의 byte[] 문제.
        if(String.class.equals(targetClass)) {
            return PlatformDataType.STRING;
        } else if(int.class.equals(targetClass) || Integer.class.equals(targetClass)) {
            return PlatformDataType.INT;
        } else if(long.class.equals(targetClass) || Long.class.equals(targetClass)) {
            return PlatformDataType.LONG;
        } else if(float.class.equals(targetClass) || Float.class.equals(targetClass)) {
            return PlatformDataType.FLOAT;
        } else if(double.class.equals(targetClass) || Double.class.equals(targetClass)) {
            return PlatformDataType.DOUBLE;
        } else if(boolean.class.equals(targetClass) || Boolean.class.equals(targetClass)) {
            return PlatformDataType.BOOLEAN;
        } else if(Date.class.equals(targetClass)) {
            return PlatformDataType.DATE_TIME;
        } else if(BigDecimal.class.equals(targetClass)) {
            return PlatformDataType.BIG_DECIMAL;
        } else if(Object.class.equals(targetClass)) {
            return PlatformDataType.UNDEFINED;
        } else if(targetClass.isArray() && (byte[].class.equals(targetClass) || Byte[].class.equals(targetClass))) {
            return PlatformDataType.BLOB;
        }
        
        return PlatformDataType.UNDEFINED;
    }
    
    public static DataType getDataType(final String targetClassName) {
        
        // DataTypeFactory의 byte[] 문제.
        if("String".equals(targetClassName)) {
            return PlatformDataType.STRING;
        } else if("int".equals(targetClassName) || "Integer".equals(targetClassName)) {
            return PlatformDataType.INT;
        } else if("long".equals(targetClassName) || "Long".equals(targetClassName)) {
            return PlatformDataType.LONG;
        } else if("float".equals(targetClassName) || "Float".equals(targetClassName)) {
            return PlatformDataType.FLOAT;
        } else if("double".equals(targetClassName) || "Double".equals(targetClassName)) {
            return PlatformDataType.DOUBLE;
        } else if("boolean".equals(targetClassName) || "Boolean".equals(targetClassName)) {
            return PlatformDataType.BOOLEAN;
        } else if("Date".equals(targetClassName)) {
            return PlatformDataType.DATE_TIME;
        } else if("BigDecimal".equals(targetClassName)) {
            return PlatformDataType.BIG_DECIMAL;
        } else if("Object".equals(targetClassName)) {
            return PlatformDataType.UNDEFINED;
        } else if(("byte[]".equals(targetClassName) || "Byte[]".equals(targetClassName))) {
            return PlatformDataType.BLOB;
        }
        
        return PlatformDataType.UNDEFINED;
    }
    
    static DataType getDataTypeOfValue(final Object value) {
        if(value == null) {
            return PlatformDataType.UNDEFINED;
        }
        DataType dataTypeOfValue = DataTypeFactory.getDataTypeOfValue(value);
        if(dataTypeOfValue.getType() == PlatformDataType.UNDEFINED.getType()) {
            if(value instanceof Byte[]) {
                dataTypeOfValue = PlatformDataType.BLOB;
            } else {
            	// getDataTypeOfValue()에서 커버하지 못하는 data type을 추가 변환 :: 2023.09.20
            	dataTypeOfValue = getExtraDataTypeOfValue(value) ;
            }
        } else if(dataTypeOfValue.getType() == PlatformDataType.DATE.getType()) {
            // data는 DATE_TIME으로 변경.
            dataTypeOfValue = PlatformDataType.DATE_TIME;
        }
        return dataTypeOfValue;
    }

    /*
     * TODO :: getDataTypeOfValue()에서 커버하지 못하는 data type을 추가 변환 :: 2023.09.20
     * 			mySql의 NUM, RNUM -> 	java.math.BigInteger	-> Long
     * 			mySql의 DATETIE 	 -> java.time.LocalDateTime	-> java.util.Date
     */    
    static DataType getExtraDataTypeOfValue(final Object value) {

    	if(java.math.BigInteger.class.equals(value.getClass())) {
    		return PlatformDataType.LONG;
    	} else if(java.time.LocalDateTime.class.equals(value.getClass())) {
    		return PlatformDataType.DATE_TIME;
    	} else {
    		return PlatformDataType.UNDEFINED;
    	}
    		
    }

    
    static Variable toVariable(final String name, final Object value) {
    	final Variable var = new Variable(name);
        if(value == null) {
            return var;
        }
        // 최초 type 설정이 안되어 있을 경우 설정된 값에 따라 Type이 설정 된다.
        if(value instanceof Byte[]) {
            var.set(toPrimitive((Byte[]) value));
        } else {
            var.set(value);
        }
        return var;
    }
    
    static Object toObject(final Variable variable, final Class<?> targetClass) {
        
        // 직접 변환할까..
        if(String.class.equals(targetClass)) {
            return variable.getString();
        } else if(int.class.equals(targetClass) || Integer.class.equals(targetClass)) {
            return variable.getInt();
        } else if(long.class.equals(targetClass) || Long.class.equals(targetClass)) {
            return variable.getLong();
        } else if(float.class.equals(targetClass) || Float.class.equals(targetClass)) {
            return variable.getFloat();
        } else if(double.class.equals(targetClass) || Double.class.equals(targetClass)) {
            return variable.getDouble();
        } else if(boolean.class.equals(targetClass) || Boolean.class.equals(targetClass)) {
            return variable.getBoolean();
        } else if(Date.class.equals(targetClass)) {
            return variable.getDateTime();
        } else if(BigDecimal.class.equals(targetClass)) {
            return variable.getBigDecimal();
        } else if(Object.class.equals(targetClass)) {
            return variable.getObject();
        } else if(targetClass.isArray()) {
            if(byte[].class.equals(targetClass)) {
                return variable.getBlob();    
            } else if(Byte[].class.equals(targetClass)) {
            	final byte[] blob = variable.getBlob();
                return toObject(blob);
            }
        }
        
        return variable.getObject();
    }
    
    static Object toObject(final Object obj) {
        if(obj == null) {
            return null; //Variable/DataSet 데이터 처리 시 데이터 없는 경우 null 처리하여 넥사크로화면에서 null 데이터로 인식하기 위해 null 반환.
        }
        if(obj instanceof Byte[]) {
            return toPrimitive((Byte[])obj);
        }
        return obj;
    }
    
    static Object toObjectFromDataSetValue(final DataSet ds, final int rowIndex, final int colIndex, final Class<?> targetClass, final boolean isSavedData, final boolean isRemovedData) {

        if(Object.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedData(rowIndex, colIndex);
            } else {
                return ds.getObject(rowIndex, colIndex);
            }
        } else if(String.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedStringData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedStringData(rowIndex, colIndex);
            } else {
                return ds.getString(rowIndex, colIndex);
            }
        } else if(int.class.equals(targetClass) || Integer.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedIntData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedIntData(rowIndex, colIndex);
            } else {
                return ds.getInt(rowIndex, colIndex);
            }
        } else if(long.class.equals(targetClass) || Long.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedLongData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedLongData(rowIndex, colIndex);
            } else {
                return ds.getLong(rowIndex, colIndex);
            }
        } else if(float.class.equals(targetClass) || Float.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedFloatData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedFloatData(rowIndex, colIndex);
            } else {
                return ds.getFloat(rowIndex, colIndex);
            }
        } else if(double.class.equals(targetClass) || Double.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedDoubleData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedDoubleData(rowIndex, colIndex);
            } else {
                return ds.getDouble(rowIndex, colIndex);
            }
        } else if(boolean.class.equals(targetClass) || Boolean.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedBooleanData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedBooleanData(rowIndex, colIndex);
            } else {
                return ds.getBoolean(rowIndex, colIndex);
            }
        } else if(Date.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedDateTimeData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedDateTimeData(rowIndex, colIndex);
            } else {
                return ds.getDateTime(rowIndex, colIndex);
            }
        } else if(BigDecimal.class.equals(targetClass)) {
            if(isSavedData) {
                return ds.getSavedBigDecimalData(rowIndex, colIndex);
            } else if(isRemovedData) {
                return ds.getRemovedBigDecimalData(rowIndex, colIndex);
            } else {
                return ds.getBigDecimal(rowIndex, colIndex);
            }
        } else if(targetClass.isArray()) {
            if(byte[].class.equals(targetClass)) {
                if(isSavedData) {
                    return ds.getSavedBlobData(rowIndex, colIndex);
                } else if(isRemovedData) {
                    return ds.getRemovedBlobData(rowIndex, colIndex);
                } else {
                    return ds.getBlob(rowIndex, colIndex);
                }
            } else if(Byte[].class.equals(targetClass)) {
                byte[] blob;
                if(isSavedData) {
                    blob = ds.getSavedBlobData(rowIndex, colIndex);
                    return toObject(blob);
                } else if(isRemovedData) {
                    blob = ds.getRemovedBlobData(rowIndex, colIndex);
                    return toObject(blob);
                } else {
                    blob = ds.getBlob(rowIndex, colIndex);
                    return toObject(blob);
                }
            }
        }
        
        // return object
        if(isSavedData) {
            return ds.getSavedData(rowIndex, colIndex);
        } else if(isRemovedData) {
            return ds.getRemovedData(rowIndex, colIndex);
        } else {
            return ds.getObject(rowIndex, colIndex);
        }
    }
    
    static boolean isSupportedBean(final Class clazz) {
        if(!clazz.isInterface() && !clazz.isPrimitive() && !clazz.isEnum() && !clazz.isArray()) {
            return true;
        }
        return false;
    }
    
    /*
     * 2023.07.26 사용되지 않음을 확인.
     */
    static Map<String, Field> getAdjustConvertibleFields(final Class clazz, final DataSet ds) {
        
    	final Map<String, Field> accessibleFields = getAccessibleFields(clazz);
    	final Map<String, Field> adjustConvertibleFields = new HashMap<String, Field>();
        // 획득한 field와 dataset field를 검사하여 실제로 사용될 field 들을 추려둔다.
    	final int columnCount = ds.getColumnCount();
        for(int i=0; i<columnCount; i++) {
        	final ColumnHeader column = ds.getColumn(i);
        	final String columnName = column.getName();
            
            // 대소문자를 구별한다!
        	final Field field = accessibleFields.get(columnName);
            if(field != null) {
                adjustConvertibleFields.put(columnName, field);
            }
        }        
        
        return adjustConvertibleFields;
        
    }
    
    /*
     * 2023.07.26 사용되지 않음을 확인.
     */
    static Map<String, Field> getAccessibleFields(final Class clazz) {
        
        final Map<String, Field> accessibleFields = new HashMap<String, Field>();
        
        Class<?> searchType = clazz;
        while (!Object.class.equals(searchType) && searchType != null) {
        	final Field[] fields = searchType.getDeclaredFields();
            for (final Field field : fields) {
            	final Class<?> type = field.getType();
                if(isConvertibleType(type)) {
                    ReflectionUtil.makeAccessible(field);
                    accessibleFields.put(field.getName(), field);
                }
            }
            searchType = searchType.getSuperclass();
        }
        
        return accessibleFields;
    }
    
    static boolean isConvertibleType(final Class<?> type) {
        if(PRIMITIVETYPEWRAPPERMAP.get(type) != null) {
            return true;
        }
        else if(NONPRIMITIVETYPEMAP.get(type) != null) {
            return true;
        }
        else if(PRIMITIVETYPEWRAPPERMAP.containsValue(type)) {
            return true;
        }
        else if(NONPRIMITIVETYPEMAP.containsValue(type)) {
            return true;
        }
        else {
        	return false;
        }
    }

    /*
     * Done :: DataTypeFactory에서 커버하지 못하는 data type을 체크전에 변환 :: 2023.09.20
     * 			java.math.BigInteger	-> Long
     * 			java.time.LocalDateTime	-> java.util.Date
     */
    static boolean isExtraConvertibleType(final Class<?> type) {
    	// mySql의 NUM, RNUM -> java.math.BigInteger
    	// mySql의 DATETIE 	-> java.time.LocalDateTime
    	if(java.math.BigInteger.class.equals(type)) {
    		return true;
    	} else if(java.time.LocalDateTime.class.equals(type)) {
    		return true;
    	} else {
        	return false;
        }
    }
    /* commons.lang.ArrayUtils source*/
    private static Byte[] toObject(final byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new Byte[0];
        }
        final Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
        	result[i] = Byte.valueOf(array[i]);
        }
        return result;
    }
    
    private static byte[] toPrimitive(final Byte[] array) {
        if (array == null) {
            return null;  //Variable set 처리 시 데이터 없는 경우 null 처리하여 넥사크로화면에서 null 데이터로 인식하기 위해 null 반환.
        } else if (array.length == 0) {
            return new byte[0];
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }
}
