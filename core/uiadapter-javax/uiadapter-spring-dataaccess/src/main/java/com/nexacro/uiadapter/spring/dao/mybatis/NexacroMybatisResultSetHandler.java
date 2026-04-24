package com.nexacro.uiadapter.spring.dao.mybatis;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.ibatis.executor.resultset.ResultSetHandler;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import com.nexacro.java.xapi.data.DataTypes;
import com.nexacro.java.xapi.data.datatype.DataType;
import com.nexacro.uiadapter.spring.core.context.SpringAppContext;
import com.nexacro.uiadapter.spring.core.data.metadata.support.MapMetaData;
import com.nexacro.uiadapter.spring.core.data.support.NexacroConverterHelper;
import com.nexacro.uiadapter.spring.dao.DbColumn;
import com.nexacro.uiadapter.spring.dao.DbMetaDataGathererUtil;
import com.nexacro.uiadapter.spring.dao.DbVendorsProvider;
import com.nexacro.uiadapter.spring.dao.Dbms;
import com.nexacro.uiadapter.spring.dao.DbmsProvider;

/**
 * <p>Mybatis의 {@link ResultSetHandler#handleResultSets(Statement)} plugin으로  {@link NexacroMybatisMetaDataProvider}에서 메타데이터 요청시에만 실행되며, 
 * 쿼리를 수행하고 <code>ResultSet</code>으로 부터 {@link DbmsProvider}를 이용하여 메타데이터 정보를 획득한다.
 * 
 * @author Park SeongMin
 * @since 10.13.2015
 * @version 1.0
 */

@Intercepts({ @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})})
public class NexacroMybatisResultSetHandler implements Interceptor {

	private static final Logger logger = LoggerFactory.getLogger(NexacroMybatisMetaDataProvider.class);
	private static DbmsProvider dbmsProvider;

    // 2025.04.16 추가 START - - -
	// Map 호출결과가 0건일 경우 mybatis camelcase 무시해야 하는 경우.
	// ignoreMybatisColumnCase = true
	/*
	1. config.xml에 속성값 지정
	  - 파일 예시 : sql-mapper-config.xml
		<plugin interceptor="com.nexacro.uiadapter.spring.dao.mybatis.NexacroMybatisResultSetHandler" >
			<property name="ignoreMybatisColumnCase" value="true"/>
		</plugin>
	*/
	private String ignoreMybatisColumnCase;
	public void setIgnoreMybatisColumnCase(String property) {
		this.ignoreMybatisColumnCase = property;
	}
	public String getIgnoreMybatisColumnCase() {
		return this.ignoreMybatisColumnCase;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// 2025.03.12 plugin 속성 추가 - - -
        setIgnoreMybatisColumnCase(properties.getProperty("ignoreMybatisColumnCase"));
	}
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		
		LookupResultSetMetaDataConfig config = LookupResultSetMetaDataHolder.getLookupResultSetMetaDataConfig();
		
		if(config != null && config.isSearchMetaData() && !config.getMappedStatement().getStatementType().name().equals("CALLABLE")) {
			
			// search metadata
			Object[] args = invocation.getArgs();
			Statement statement = (Statement) args[0];
			
			return getMetaDataFromResultSet(config, statement);
		
		} else if(config != null && config.isSearchMetaData() && config.getMappedStatement().getStatementType().name().equals("CALLABLE")) {
			// search metadata is setted in NexacroMybatisMetaDataProvider
			Object[] args = invocation.getArgs();
			Statement statement = (Statement) args[0];
			
			// procedure 호출 결과 0건 조회시 컬럼 정보 구성.
			return getMetaDataFromProcedureResultSet(config, statement);

		} else {
			return invocation.proceed();
		}
		
	}

	private Object getMetaDataFromResultSet(LookupResultSetMetaDataConfig config, Statement statement) {
		
		initDbmsProvider();
		MapMetaData generateMapMetaData = null;
		
		MappedStatement mappedStatement = config.getMappedStatement();
		try {
			ResultSet rs = statement.getResultSet();
			
			ResultSetMetaData metaData = rs.getMetaData();

			Connection connection = statement.getConnection();
			Dbms dbms = dbmsProvider.getDbms(connection);

			// get column information from ResultSetMetaData
			List<DbColumn> dbColumns = dbms.getDbColumns(metaData);

			List<ResultMap> resultMaps = mappedStatement.getResultMaps();
			Class<?> resultType = null;
			boolean isMap ;
			
			if(validResultMaps(resultMaps)) {
				// mapping MetaData and Ibatis ResultMap
				ResultMap resultMap = resultMaps.get(0);
				
				resultType = resultMaps.get(0).getType();

				// auto mappings..
				// AutoMappingBehavior.NONE.equals(mappedStatement.getConfiguration().getAutoMappingBehavior())
				// NONE - 자동매핑을 사용하지 않는다. 오직 수동으로 매핑한 프로퍼티만을 설정할것이다.
				// PARTIAL - 조인 내부에 정의한 내포된 결과매핑을 제외하고 자동매핑한다.
				// FULL - 모든것을 자동매핑한다.
				
				// DefaultResultSetHandler Code.. getRowValue(). AutoMapping
//				if (!shouldApplyAutomaticMappings(resultMap, !AutoMappingBehavior.NONE.equals(mappedStatement.getConfiguration().getAutoMappingBehavior()))) {
//				if (!shouldApplyAutomaticMappings(resultMap, !AutoMappingBehavior.NONE.equals(mappedStatement.getConfiguration().getAutoMappingBehavior()))) {
					mappingDbColumnAndResultMappings(dbColumns, resultMap);
//				}
				
			}

			/*
			 *  2022.02.16
			 *  MyBatis에서는 map으로 리턴할 때 camalcase가 지원되지 않음.
			 *  조회결과가 0건이고 컬럼이 많을 경우 별도로 컬럼을 정의하지 않더라고 
			 *  mapper.configuration.MapUnderscoreToCamelCase 값을 참조하여 true이면
			 *  resultType을 HashMap으로 CamelCase형태의 컬럼이 나오도록 기능추가함..
			 *  기존코드 : generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumns(dbColumns);
			 */
			boolean isCamelCase = mappedStatement.getConfiguration().isMapUnderscoreToCamelCase();
			isMap = DbMetaDataGathererUtil.hasInterface(resultType, Map.class);

			//if (isCamelCase && isMap)
			// 2025.04.16 map의 경우 mybatis setting 정보 무시하는 조건 추가
			if (isCamelCase && isMap && !"true".equalsIgnoreCase(getIgnoreMybatisColumnCase()))
			{
				generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumnsToCamelCase(dbColumns);
			} else {
			
				generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumns(dbColumns);			
			}

		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
            if(logger.isErrorEnabled()) {
                logger.error("failed to query the metadata information.", e);
            }
		}
        		
        return generateMapMetaData;
	}

	/***********************************************
	 * 2024.05.07 멀티 객체 리턴 조건 추가....
	 * 추가 사유 :  procedure 호출시 0건 처리 로직 수정.
	 * 			ResultSet에 필요한 정보가 없음.(
	 * 2024.05.16 rs = null 케이스 추가
	 ***********************************************/
	private Object getMetaDataFromProcedureResultSet(LookupResultSetMetaDataConfig config, Statement statement) {
		
		initDbmsProvider();
		MapMetaData generateMapMetaData = null;
		List<List<Map<String, Object>>> resultMultiList = new <List<Map<String, Object>>>ArrayList();
		
		MappedStatement mappedStatement = config.getMappedStatement();
		Collection<ResultMap> resultMaps = mappedStatement.getConfiguration().getResultMaps();
		try {

			Connection connection = statement.getConnection();	
			Dbms dbms = dbmsProvider.getDbms(connection);

			do {
				ResultSet rs = statement.getResultSet();
				
				// rs = null 케이스 추가
				if(rs!=null) {
					ResultSetMetaData metaData = rs.getMetaData();
					resultMultiList.add(genMapMetaData(resultMaps, dbms.getDbColumns(metaData), mappedStatement));
				}
				
			} while(statement.getMoreResults());
			
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
            if(logger.isErrorEnabled()) {
                logger.error("failed to query the metadata information.", e);
            }
		}
        		
        return resultMultiList;
	}

	/*
	 * 2024.05.07 :: 프로시저 멀티 reslutset 0건 일때, 컬럼정보 생성 테스트
	 */
	private MapMetaData genMapMetaData(Collection<ResultMap> resultMaps, List<DbColumn> dbColumns, MappedStatement mappedStatement) {
		MapMetaData generateMapMetaData = null;
		Class<?> resultType ;
		boolean isMap ;

		try {
			ResultMap resultMap = resultMaps.iterator().next();
			resultType = resultMap.getType();
			
			/*
			 *  2022.02.16
			 *  MyBatis에서는 map으로 리턴할 때 camalcase가 지원되지 않음.
			 *  조회결과가 0건이고 컬럼이 많을 경우 별도로 컬럼을 정의하지 않더라고 
			 *  mapper.configuration.MapUnderscoreToCamelCase 값을 참조하여 true이면
			 *  resultType을 HashMap으로 CamelCase형태의 컬럼이 나오도록 기능추가함..
			 *  기존코드 : generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumns(dbColumns);
			 */
			boolean isCamelCase = mappedStatement.getConfiguration().isMapUnderscoreToCamelCase();
			isMap = DbMetaDataGathererUtil.hasInterface(resultType, Map.class);
			
			if (isCamelCase && isMap) 
			{
				generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumnsToCamelCase(dbColumns);
			} else {
			
				generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumns(dbColumns);			
			}
		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
            if(logger.isErrorEnabled()) {
                logger.error("failed to query the metadata information.", e);
            }
		}
		return generateMapMetaData;
	}
	
	private Object getMetaDataFromProcedureResultSetOrg(LookupResultSetMetaDataConfig config, Statement statement) {
		
		initDbmsProvider();
		MapMetaData generateMapMetaData = null;
		
		MappedStatement mappedStatement = config.getMappedStatement();
		Collection<ResultMap> resultMaps = mappedStatement.getConfiguration().getResultMaps();
		
		try {
			Connection connection = statement.getConnection();
			Dbms dbms = dbmsProvider.getDbms(connection);
	
	
			Class<?> resultType ;
			boolean isMap ;

			ResultSet rs = statement.getResultSet();
			
			ResultSetMetaData metaData = rs.getMetaData();
			
			//if(validResultMaps(resultMaps)) {
				// mapping MetaData and Ibatis ResultMap
				ResultMap resultMap = resultMaps.iterator().next();
				List<ResultMapping> resultMappings = resultMap.getResultMappings();
				
				// get column information from resultMappings
				List<DbColumn> dbColumns = dbms.getDbColumns(resultMappings);
				
				resultType = resultMap.getType();

				// auto mappings..
				// AutoMappingBehavior.NONE.equals(mappedStatement.getConfiguration().getAutoMappingBehavior())
				// NONE - 자동매핑을 사용하지 않는다. 오직 수동으로 매핑한 프로퍼티만을 설정할것이다.
				// PARTIAL - 조인 내부에 정의한 내포된 결과매핑을 제외하고 자동매핑한다.
				// FULL - 모든것을 자동매핑한다.
				
				// DefaultResultSetHandler Code.. getRowValue(). AutoMapping
//				if (!shouldApplyAutomaticMappings(resultMap, !AutoMappingBehavior.NONE.equals(mappedStatement.getConfiguration().getAutoMappingBehavior()))) {
//				if (!shouldApplyAutomaticMappings(resultMap, !AutoMappingBehavior.NONE.equals(mappedStatement.getConfiguration().getAutoMappingBehavior()))) {
					mappingDbColumnAndResultMappings(dbColumns, resultMap);
//				}				
			//}
			
			/*
			 *  2022.02.16
			 *  MyBatis에서는 map으로 리턴할 때 camalcase가 지원되지 않음.
			 *  조회결과가 0건이고 컬럼이 많을 경우 별도로 컬럼을 정의하지 않더라고 
			 *  mapper.configuration.MapUnderscoreToCamelCase 값을 참조하여 true이면
			 *  resultType을 HashMap으로 CamelCase형태의 컬럼이 나오도록 기능추가함..
			 *  기존코드 : generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumns(dbColumns);
			 */
			boolean isCamelCase = mappedStatement.getConfiguration().isMapUnderscoreToCamelCase();
			isMap = DbMetaDataGathererUtil.hasInterface(resultType, Map.class);
			
			if (isCamelCase && isMap) 
			{
				generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumnsToCamelCase(dbColumns);
			} else {
			
				generateMapMetaData = DbMetaDataGathererUtil.generateMetaDataFromDbColumns(dbColumns);			
			}

		} catch (Exception e) {
			Logger logger = LoggerFactory.getLogger(this.getClass());
            if(logger.isErrorEnabled()) {
                logger.error("failed to query the metadata information.", e);
            }
		}
        		
        return generateMapMetaData;
	}

	private void mappingDbColumnAndResultMappings(List<DbColumn> dbColumns, ResultMap resultMap) throws SecurityException {
		
		Class<?> resultType = resultMap.getType();
		/*
		 *  2022.02.16	
		 *  MyBatis에서는 map으로 리턴할 때 camalcase가 지원되지 않음.
		 *  조회결과가 0건이고 컬럼이 많을 경우 별도로 컬럼을 정의하지 않더라고 
		 *  resultType을 HashMap으로 CamelCase형태의 컬럼이 나오도록 기능추가함..
		 *  NexacroMybatisResultSetHandler.getMetaDataFromResultSet()에서 호출됨.
		 *  기존코드 if(resultType != Map.class) {
		 */
		if(!DbMetaDataGathererUtil.hasInterface(resultType, Map.class) ) {
			// resultSetMetaData columns
			return;
		}
		
		// Map 만을 대상으로 하기 때문에 PropertyMappings만을 사용한다. (getConstructorResultMappings는 사용하지 않는다.)
		final List<ResultMapping> propertyMappings = resultMap.getPropertyResultMappings();
		// propertyMappings length가 0인 경우 auto mapping 으로 처리 한다.
		if(propertyMappings.isEmpty()) {
			return;
		}
		
		
		int size = dbColumns.size(); 
        for(int i=size-1; i>=0; i--) { 
        	DbColumn dbColumn = dbColumns.get(i);
	        String name = dbColumn.getName();
	        boolean existColumn = false;
			
			// Map의 Automapping이 아니다. 일반 Mapping이다.
//			Boolean autoMapping = resultMap.getAutoMapping();
//			if(autoMapping != null && Boolean.TRUE.equals(autoMapping)) {
//				existColumn = true;
//			}
			
			for (ResultMapping propertyMapping: propertyMappings) {
			
				String columnName = propertyMapping.getColumn();
				String propertyName = propertyMapping.getProperty();
				
				// 대소문자를 구별하지 않는 DB에 따라 ignorecase 처리
				if(name.equalsIgnoreCase(columnName) || name.equalsIgnoreCase(propertyName)) {
                    if(propertyName != null) {
                        dbColumn.setName(propertyName);
                    }
                    
                    // javaType이 선언되지 않았을 경우 기본적으로 Object 형식으로 반환된다. 
                    // NexacroConverterHelper.getDataType 의 Object는 undefined 이기 때문에 ResultSetMetaData의 타입으로 처리가 된다.
                    Class javaType = propertyMapping.getJavaType();
                    if(javaType != null) {
                        DataType dataType = NexacroConverterHelper.getDataType(javaType);
                        if(dataType.getType() != DataTypes.UNDEFINED) {
                            dbColumn.setDataType(dataType);
                        }
                    }
                    
                    // existColumn = true;
                    break;
                } 
				
			}// end resultMappings
				
			// resultMap에 정의되지 않은 컬럼은 DB에서 전달되는 이름으로 처리 한다. (삭제 하지 않는다.)
//			if(!existColumn) {
//				dbColumns.remove(i);
//			}
			
	    } // end dbColumns
		
		
	}
	
	private boolean validResultMaps(List<ResultMap> resultMaps) {
        return resultMaps != null && resultMaps.size() >= 1;
    }
	
	private boolean shouldApplyAutomaticMappings(ResultMap resultMap, boolean def) {
		return resultMap.getAutoMapping() != null ? resultMap.getAutoMapping(): def;
	}

	private void initDbmsProvider() {
		if (dbmsProvider != null) {
			return;
		}

		ApplicationContext applicationContext = SpringAppContext.getInstance().getApplicationContext();
		if (applicationContext != null) {
			dbmsProvider = (DbmsProvider) applicationContext.getBean("dbmsProvider");
		}

		if (dbmsProvider == null) {
			dbmsProvider = new DbVendorsProvider();
		}
	}
	
	
	protected Class<?> resolveInterface(Class<?> type) {
		
		// only list..
		
	    Class<?> classToCreate;
	    if (type == List.class || type == Collection.class || type == Iterable.class) {
	      classToCreate = ArrayList.class;
	    } else if (type == Map.class) {
	      classToCreate = HashMap.class;
	    } else if (type == SortedSet.class) { // issue #510 Collections Support
	      classToCreate = TreeSet.class;
	    } else if (type == Set.class) {
	      classToCreate = HashSet.class;
	    } else {
	      classToCreate = type;
	    }
	    return classToCreate;
	  }
	
}
