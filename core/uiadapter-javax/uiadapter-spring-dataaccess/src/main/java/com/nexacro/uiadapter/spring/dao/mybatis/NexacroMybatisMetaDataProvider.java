package com.nexacro.uiadapter.spring.dao.mybatis;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cache.NullCacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;

import com.nexacro.uiadapter.spring.core.data.metadata.NexacroMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.BeanMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.MapMetaData;
import com.nexacro.uiadapter.spring.core.data.metadata.support.UnsupportedMetaData;

/**
 * <p>Mybatis의 {@link Executor#query(MappedStatement, Object, RowBounds, ResultHandler)} plugin으로, 
 * 쿼리 실행 시 ({@code List} 형태의 select) 데이터가 0건일 경우 컬럼의 메타데이터 정보를 획득한다.  
 * 
 * @author Park SeongMin
 * @since 10.13.2015
 * @version 1.0
 */
@Intercepts({ @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class})})
public class NexacroMybatisMetaDataProvider implements Interceptor {

	private static final Logger logger = LoggerFactory.getLogger(NexacroMybatisMetaDataProvider.class);

    // 2025.03.24 추가 START - - -
	// procedure 호출결과가 0건일 경우 재조회를 하지않는 속성 추가.
	// ignoreProcedureZeroResult = true
 	/** 사용방식.
	1. config.xml에 속성값 지정
	  - 파일 예시 : sql-mapper-config.xml
		<plugin interceptor="com.nexacro.uiadapter.spring.dao.mybatis.NexacroMybatisMetaDataProvider" >
			<property name="ignoreProcedureZeroResult" value="true"/>
		</plugin>
	2. mapper.xml의 쿼리에 statementType="CALLABLE" 명시
	  - 파일 예시 : sample_mapper.xml
	    <select id="selectProcedure" statementType="CALLABLE">
	        {call read_TB_BOARD()}
	    </select>
	*/
	private String ignoreProcedureZeroResult;
	public void setIgnoreProcedureZeroResult(String property) {
		this.ignoreProcedureZeroResult = property;
	}
	public String getIgnoreProcedureZeroResult() {
		return this.ignoreProcedureZeroResult;
	}
	// 2025.03.24 추가 E N D - - -

	// 2025.04.24 추가 START - - -
	// resultType=vo 일 때, 호출 결과=0건일 경우, BeanMetaData 객체에 vo를 add한다.
	// return vo 1건.
	/** 사용방식.
	1. config.xml에 속성값 지정
	  - 파일 예시 : sql-mapper-config.xml
		<plugin interceptor="com.nexacro.uiadapter.spring.dao.mybatis.NexacroMybatisMetaDataProvider" >
			<property name="addBeanMetaData" value="true"/>
		</plugin>
	*/
	private String addBeanMetaData;
	public void setAddBeanMetaData(String property) {
		this.addBeanMetaData = property;
	}
	public String getAddBeanMetaData() {
		return this.addBeanMetaData;
	}
	// 2025.04.24 추가 E N D - - -


	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
		// 2025.03.12 plugin 속성 추가 - - -

        setIgnoreProcedureZeroResult(properties.getProperty("ignoreProcedureZeroResult"));
		setAddBeanMetaData(properties.getProperty("addBeanMetaData"));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		
		Object proceed = invocation.proceed();

		// 2025.03.24 추가
		// procedure 호출결과가 0건일 경우 재조회 skip
		if(getIgnoreProcedureZeroResult() != null) {
			Object[] args = invocation.getArgs();
			MappedStatement ms = (MappedStatement) args[0];

			StatementType statementType = ms.getStatementType();
			if(statementType.name().equalsIgnoreCase("CALLABLE")) {
				logger.debug("ignoreProcedureZeroResult is enabled.");
				return proceed;
			}
		}

		if(proceed instanceof List) {
			List list = (List) proceed;
			if(list.isEmpty()) {
				return getNexacroMetaData(invocation);
			}
			
			// 2024.05.09 프로시저 호출 후 1종 이상의 Multi Object를 리턴할때 일부 객체가 0건인 케이스 추가.
			else if(((List)proceed).get(0) instanceof List) {
				ArrayList multiList = (ArrayList) proceed;
				
				int listSize = multiList.size();
				boolean isContainDataAll = true;
				boolean[] isContainDataArr = new boolean[listSize];
				
				for(int idx=0; idx<listSize; idx++) {
					if(((ArrayList) multiList.get(idx)).isEmpty()) {
						isContainDataArr[idx] = false;
						isContainDataAll = false;
					} else {
						isContainDataArr[idx] = true;
					}
				}
				
				if(!isContainDataAll) {
					return getNexacroMetaDataMulti(invocation, proceed, isContainDataArr);
				}
			}
		}
		
		return proceed;
	}
	
	private Object getNexacroMetaData(Invocation invocation) {
		
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];

		List<ResultMap> resultMaps = ms.getResultMaps();
		for(ResultMap resultMap: resultMaps) {
			if(!requireExecuteQuery(resultMap)) {
                return generateMetaDataFromClass(resultMap.getType());
            }
		}
		
//		return doGetMetaData(executor, ms, param, rowBounds, resultHandler);
		
		// ResultSetHandler를 등록해 두고, 여기서 실행 하는 경우에만 상태값을 저장하여 처리하도록 하자.
		LookupResultSetMetaDataConfig config = new LookupResultSetMetaDataConfig(true, ms);
		LookupResultSetMetaDataHolder.setLookupResultSetMetaDataConfig(config);
		try {
			// used cache..
//			proceed = invocation.proceed();
			
			Executor executor = (Executor) invocation.getTarget();
			Object parameter = args[1];
			RowBounds rowBounds = (RowBounds) args[2];
			ResultHandler resultHandler = (ResultHandler) args[3];
			BoundSql boundSql = ms.getBoundSql(parameter);
			CacheKey cacheKey = new NullCacheKey(); // or create cache key. msId, parameter, 
			
			return executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
			
		} catch(Throwable e) {
			// ignore
			Logger logger = LoggerFactory.getLogger(getClass());
            logger.warn("failed to query the metadata information. statement={}", ms.getId(), e);
		} finally {
			LookupResultSetMetaDataHolder.resetLookupResultSetMetaDataConfig();
		}
		
		return new ArrayList();
		

	}
	
	/*
	 * 2024.04.02 프로시저 호출 후 1종 이상의 Multi Object를 리턴하는 케이스 추가.
	 * 
	 * @param Invocation 	호출 arguments
	 * @return Object		metadata가 세팅된 다종의 객체를 리턴.
	 */
	private Object getNexacroMetaDataMulti(Invocation invocation, Object proceed) {
		
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		
		// ResultSetHandler를 등록해 두고, 여기서 실행 하는 경우에만 상태값을 저장하여 처리하도록 하자.
		LookupResultSetMetaDataConfig config = new LookupResultSetMetaDataConfig(true, ms);
		LookupResultSetMetaDataHolder.setLookupResultSetMetaDataConfig(config);
		try {
			// used cache..
			// proceed = invocation.proceed();
			
			Executor executor = (Executor) invocation.getTarget();
			Object parameter = args[1];
			RowBounds rowBounds = (RowBounds) args[2];
			ResultHandler resultHandler = (ResultHandler) args[3];
			BoundSql boundSql = ms.getBoundSql(parameter);
			CacheKey cacheKey = new NullCacheKey(); // or create cache key. msId, parameter, 
			
			List<Object> resultList =  executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
			return resultList;
		} catch(Throwable e) {
			// ignore
			Logger logger = LoggerFactory.getLogger(getClass());
            logger.warn("failed to query the metadata information. statement={}", ms.getId(), e);
		} finally {
			LookupResultSetMetaDataHolder.resetLookupResultSetMetaDataConfig();
		}
		
		return new ArrayList();
		

	}
	
	/*
	 * 2024.05.09 프로시저 호출 후 1종 이상의 Multi Object를 리턴하는 케이스 중에 일부 Object가 0건인 케이스의 MetaData세팅.
	 * 
	 * @param Invocation 	호출 arguments
	 * @param proceed 		호출 arguments
	 * @param isContainDataArr 	객체의 리턴데이터가 있는지, 0건인지 유무
	 * @return Object		metadata가 세팅된 다종의 객체를 리턴.
	 */
	private Object getNexacroMetaDataMulti(Invocation invocation, Object proceed, boolean[] isContainDataArr) {
		
		Object[] args = invocation.getArgs();
		MappedStatement ms = (MappedStatement) args[0];
		
		// ResultSetHandler를 등록해 두고, 여기서 실행 하는 경우에만 상태값을 저장하여 처리하도록 하자.
		LookupResultSetMetaDataConfig config = new LookupResultSetMetaDataConfig(true, ms);
		LookupResultSetMetaDataHolder.setLookupResultSetMetaDataConfig(config);
		try {
			// used cache..
			// proceed = invocation.proceed();
			
			Executor executor = (Executor) invocation.getTarget();
			Object parameter = args[1];
			RowBounds rowBounds = (RowBounds) args[2];
			ResultHandler resultHandler = (ResultHandler) args[3];
			BoundSql boundSql = ms.getBoundSql(parameter);
			CacheKey cacheKey = new NullCacheKey(); // or create cache key. msId, parameter, 
			
			List<Object> resultList =  executor.query(ms, parameter, rowBounds, resultHandler, cacheKey, boundSql);
			
			for(int idx=0; idx<isContainDataArr.length; idx++) {
				if(isContainDataArr[idx]) {
					resultList.set(idx, ((ArrayList)proceed).get(idx));
				}
			}
			
			return resultList;
		} catch(Throwable e) {
			// ignore
			Logger logger = LoggerFactory.getLogger(getClass());
            logger.warn("failed to query the metadata information. statement={}", ms.getId(), e);
		} finally {
			LookupResultSetMetaDataHolder.resetLookupResultSetMetaDataConfig();
		}
		
		return new ArrayList();
		

	}
	
	// No Use.
	private List doGetMetaData(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) {
		
		// http://zgundam.tistory.com/34 참고..
		BoundSql        boundSql = ms.getBoundSql(parameter);
		Executor wrapper = executor;
		Configuration configuration = ms.getConfiguration();
		StatementHandler handler = configuration.newStatementHandler(wrapper, ms, parameter, rowBounds, resultHandler, boundSql);
		// log 처리 하지 않음
//		Statement stmt = prepareStatement(handler, ms.getStatementLog());
		Statement stmt ;
		try {
			
			Transaction transaction = executor.getTransaction();
			Connection connection ;
			try {
				connection = transaction.getConnection();
			} catch (SQLException e) {
				logger.warn("getting connection failed for MetaData.", e);
				return new ArrayList();
			}
			
			stmt = handler.prepare(connection);
			handler.parameterize(stmt);
		} catch(SQLException e) {
			logger.warn("create statement and parameterize failed.", e);
			return new ArrayList();
		}
		
		// handler 별 호출을 처리 할 까?
		try {
			handler.query(stmt, resultHandler);
		} catch (SQLException e) {
            logger.error("failed to query the metadata information. statement={}", ms.getId(), e);
			
		}
		
		
		return new ArrayList();
	}
	
	private boolean requireExecuteQuery(ResultMap resultMap) {
        
        // Map이 아니라면 실행하지 않는다. XML, primitive 등 은 처리하지 않도록 한다.
        Class resultClass = resultMap.getType();
        
        Boolean autoMapping = resultMap.getAutoMapping();

        return Map.class.isAssignableFrom(resultClass);

    }
    
    private NexacroMetaData generateMetaDataFromClass(Class clazz) {
        
        if(!Map.class.isAssignableFrom(clazz)) {
            if(ClassUtils.isPrimitiveOrWrapper(clazz)) {
                return new UnsupportedMetaData(null);
            }

			// 2025.04.24 조건 추가
			if(getAddBeanMetaData() == null) {
				return new BeanMetaData(clazz);
			} else {
				// addBeanMetaData 값이 있으면 , BeanMetaData를 생성하면서 list에 vo객체를 1건 추가한다.
				return new BeanMetaData(clazz, this.addBeanMetaData);
			}
        }
        
        return null;
    }
    
    /*
     * 2024.04.02
     * resultMap객체의 element로 column 정보를 NexacroMetaData에 세팅하여 리턴
     * 
     * @param	resultMap
     * @return	NexacroMetaData
     */
    private NexacroMetaData generateMetaDataFromMap(ResultMap resultMap) {
        Map<String, Object> columnMap = new HashMap<>();

        for (ResultMapping resultMapping : resultMap.getResultMappings()) {
        	String column = resultMapping.getColumn();
        	if (column != null) {
        		columnMap.put(column.toUpperCase(Locale.ENGLISH),"");
        	} 
        }
        return new MapMetaData(columnMap);
    }
    
}
