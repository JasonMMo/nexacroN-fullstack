package com.nexacro.uiadapter.spring.dao.ibatis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibatis.sqlmap.client.SqlMapClient;
import com.nexacro.uiadapter.spring.core.util.ReflectionUtil;
import com.nexacro.uiadapter.spring.dao.Dbms;
import com.nexacro.uiadapter.spring.dao.DbmsProvider;

/**
 * Spring 혹은 EgovFramework의 ibatis 처리 시 메타 데이터를 획득하기 위한 class이다.
 * reflection을 이용하여 메타데이터를 획득한다. 
 * 
 * @author Park SeongMin
 * @since 10.12.2015
 * @version 1.0
 *
 */
public class NexacroIbatisMetaDataProvider {
	
	/*
		AOP를 사용할 경우 설정
	<bean id="ibatisMetaDataProvider" class="com.nexacro.spring.dao.ibatis.NexacroIbatisMetaDataProvider" >
		<property name="dbmsProvider" ref="dbmsProvider" />
	</bean>
 
	<aop:aspectj-autoproxy proxy-target-class="true"/>
    <aop:config >
        <aop:pointcut  id="ibatisSelectPointCut" expression="execution(* nexacro..service.dao.ibatis..DAO*.list*(..))" />
        <aop:aspect ref="ibatisMetaDataProvider" >
        	<aop:around method="getQueryMetaData" pointcut-ref="ibatisSelectPointCut"/>
        </aop:aspect>
    </aop:config>
	
	*/
	
	private static final String SPRING_INTERFACE_NAME        = "org.springframework.orm.ibatis.SqlMapClientCallback";
	private static final String EGOVFRAMEWORK_INTERFACE_NAME = "egovframework.rte.psl.orm.ibatis.SqlMapClientCallback";
	
	private static final Logger logger = LoggerFactory.getLogger(NexacroIbatisMetaDataProvider.class);
	
	private DbmsProvider dbmsProvider;

	public DbmsProvider getDbmsProvider() {
		return dbmsProvider;
	}

	public void setDbmsProvider(DbmsProvider dbmsProvider) {
		this.dbmsProvider = dbmsProvider;
	}

	/**
	 * 쿼리를 실행하고 조회 된 결과가 0건일 경우 메타데이터 정보를 획득한다.
	 * @param pjp ProceedingJoinPoint
	 * @return Object JoinPoint의 대상 객체
	 */
	public Object getQueryMetaData(ProceedingJoinPoint pjp) throws Throwable{
		
		Object target = pjp.getTarget();
		StaticPart staticPart = pjp.getStaticPart();
		
		Object result = pjp.proceed();
		
		if(result instanceof List) {
			List listResult = (List) result;
			if(listResult.isEmpty()) {
				return doGetQueryMetaData(pjp.getTarget(), pjp.getArgs());
			}
		}
		
		return result;
		
	}
	
	// EgovAbstractDAO를 상속받아 처리 할 경우 superclass 에 정의 된 list 형태는 AOP가 적용되지 않는다. 
	// framework 별 sample 구성 시 추상 클래스를 제공하도록 하자. spring의 경우 aop를 바로 적용하도록 하자.
	public Object doGetQueryMetaData(Object daoObject, Object[] arguments) {
	
		if(arguments == null || arguments.length < 2) {
			return new ArrayList();
		}
		
		Class<?> daoClass = daoObject.getClass();
		String statementName = (String) arguments[0];
		Object parameterObject = arguments[1];
		
		// reflection..
		Object sqlMapClientTemplate ;
		Object sqlMapClientCallback ;
		Method executeMethod ;
		
		try {
			
			// TODO 전부 Null 처리 해야 한다.
			
			// find sqlMapClientTemplate in Dao
			Method getSqlMapClientTemplateMethod = ReflectionUtil.getMethod(daoClass, "getSqlMapClientTemplate");
			if(getSqlMapClientTemplateMethod == null) {
				throw new UnsupportedOperationException("getSqlMapClientTemplate method is null.");
			}
			sqlMapClientTemplate = getSqlMapClientTemplateMethod.invoke(daoObject, (Object) null);
			
			// find dataSource in sqlMapClientTemplate
			Method getDataSourceMethod = ReflectionUtil.getMethod(sqlMapClientTemplate.getClass(), "getDataSource");
			if(getDataSourceMethod == null) {
				throw new UnsupportedOperationException("getDataSource method is null.");
			}
			DataSource dataSource = (DataSource) getDataSourceMethod.invoke(sqlMapClientTemplate, (Object) null);
			
			// get dbms
			Dbms dbms = dbmsProvider.getDbms(dataSource);
			
			// find sqlMapClient in dao
			Method getSqlMapClientMethod = ReflectionUtil.getMethod(daoClass, "getSqlMapClient");
			if(getSqlMapClientMethod == null) {
				throw new UnsupportedOperationException("getSqlMapClient method is null.");
			}
			SqlMapClient sqlMapClient = (SqlMapClient) getSqlMapClientMethod.invoke(daoObject, (Object) null);
			
			Class<?> findedSqlMapClientCallbackInterface = findSqlMapClientCallbackInterface();
			sqlMapClientCallback = createProxiedSqlMapClientCallback(dbms, sqlMapClient, statementName, parameterObject, findedSqlMapClientCallbackInterface);
			
			// find execute method in SqlMapClientTemplate 
			executeMethod = ReflectionUtil.getMethod(sqlMapClientTemplate.getClass(), "execute", findedSqlMapClientCallbackInterface);
			
		} catch(Throwable e) {
			logger.error("unsupported getting metadata. e={}", e.getMessage());
			return new ArrayList();
		}
		
		// execute..
		Object queryMetaData = null;
		try {
            if (executeMethod != null) {
                queryMetaData = executeMethod.invoke(sqlMapClientTemplate, sqlMapClientCallback);
            }
        } catch(Throwable e) {
			logger.error("an error has occurred while querying the metadata. e={}", e.getMessage());
			return new ArrayList();
		}
		
		return queryMetaData;
	}
	
	private Object createProxiedSqlMapClientCallback(Dbms dbms, SqlMapClient sqlMapClient, String statementName, Object parameterObject, Class<?> findedSqlMapClientCallbackInterface) {
		ClassLoader classLoader = this.getClass().getClassLoader();
		InvocationHandler sqlMapClientCallbackImpl = new NexacroIbatisMetaDataGatherer(dbms, sqlMapClient, statementName, parameterObject);
		return Proxy.newProxyInstance(classLoader, new Class[]{findedSqlMapClientCallbackInterface}, sqlMapClientCallbackImpl);
	}
	
	private Class<?> findSqlMapClientCallbackInterface() {

		Class<?> sqlMapClient = null;
		try {
			sqlMapClient = Class.forName(SPRING_INTERFACE_NAME);
		} catch (ClassNotFoundException e) {
		}

		if (sqlMapClient == null) {
			try {
				sqlMapClient = Class.forName(EGOVFRAMEWORK_INTERFACE_NAME);
			} catch (ClassNotFoundException e) {
			}

			if(sqlMapClient == null) {
				throw new UnsupportedOperationException("does not exist SqlMapClientCallback interface. unsupported getting metadata");
			}
			
		}
		
		return sqlMapClient;
	}

}
