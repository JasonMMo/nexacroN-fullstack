# uiadapter-spring-dataaccess.jar #

version 정보

```
 - jdk : 1.8.0
 - xapi : 2.0.4-SNAPSHOT
```

# 1.4.19
2026.03.12

    [#dataaccess:1.4.19: xapi : 2.0.4-SNAPSHOT 반영]

# 1.4.14
2025.05.23

    [#dataaccess:1.4.14: xapi : 2.0.0-SNAPSHOT 반영]

# 1.4.13
2025.04.29

    [#dataaccess:1.4.13:Mybatis properties 에 대한 접근자 public 수정]
    - 요구기능 : java 설정으로 사용할 수 있도록 접근자를 public 변경 요청.
    - 수정 class : NexacroMybatisMetaDataProvider.java
    - 수정 함수 : setAddBeanMetaData(), getAddBeanMetaData(), setIgnoreProcedureZeroResult(), getIgnoreProcedureZeroResult()
    - 수정 class : NexacroMybatisResultSetHandler.java
    - 수정 함수 : setIgnoreMybatisColumnCase(), getIgnoreMybatisColumnCase()

# 1.4.12
2025.04.24

    - 요구기능 : resultType=vo이고 조회결과가 0건일 때, BeanMetaData를 생성하면서 list에 vo객체를 1건 추가.
    - 수정 class : NexacroMybatisMetaDataProvider.java
    - 추가 함수 : setAddBeanMetaData(), getAddBeanMetaData()
    - 수정 함수 : setProperties(), generateMetaDataFromClass()

# 1.4.11
2025.03.24

	- 이슈 1 : uiadapter-jakarta-core 버전을 1.0.11-SNAPSHOT으로 올림.
	- 수정 xml : pom.xml

	- 이슈 2 : procedure 호출결과가 0건일 경우 재조회를 하지않는 조건 추가. 
	- 조치 : nexacro.ignore-procedure-zero-result 이 true일 경우, procedure 호출결과가 0건이더라도 재조회하지 않도록하고 조건 추가.
	- 수정 class : com.nexacro.uiadapter.spring.dao.mybatis.NexacroMybatisMetaDataProvider.java
		- 수정 함수 : setProperties(), intercept()에 조건 추가
	- 사용법 : 
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

# 1.4.5
2024.11.28

	- 이슈 : Postgre 환경에서 devpro의 컬럼은 upper case이고 postgre의 컬럼은 snake case 인 조건에서 데이터가 0건일 경우 column case를 맞춰주기 위한 재정의.
	- 수정 class : Postgre.java
	- 수정 사항 : getDbColumns() 재정의.

# 1.4.0 
2024.05.16

	- version : 0.4.0
	- 0.0.4 버전을 1.4.0 으로 버전 픽스.
	- 수정 사항 : bs에서 procedure in/out 조건이 0건인 케이스 추가.

# 0.0.4 
2024.04~05

	- procedure 호출 후 0건 재조회 기능 추가 및 보강 
		- 수정 : NexacroMybatisResultSetHandler
				NexacroMybatisMetaDataProvider
# 1.0.1
2022.02.16

    - MyBatis에서 resultType= map의 subclass로 리턴할 때 0건일 경우, camalcase설정을 반영하는 기능 추가.
        - 수정 class : com.nexacro.uiadapter.spring.dao.mybatis.NexacroMybatisResultSetHandler
            - 수정 함수 : getMetaDataFromResultSet() , mappingDbColumnAndResultMappings()
        - 수정 class : com.nexacro.uiadapter.spring.dao.mybatis.DbMetaDataGathererUtil
            - 추가 함수 : generateMetaDataFromDbColumnsToCamelCase()
                                hasInterface()
2021.11.10

    - 지원 DBMS - Postgre 추가
        - 추가 class : com.nexacro.uiadapter.spring.dao.dbms.Postgre
            - 컬럼 Mapping 참조 : https://www.instaclustr.com/postgresql-data-types-mappings-to-sql-jdbc-and-java-data-types/
            - xapi datatype 맵핑 : file:///E:/Download/17.1/nexacro17_xapi_JAVA_20210427(1.1.0)_1/docs/api/korean/index.html

