# uiadapter-spring-core  for nexacroN #

version 정보

```
 - jdk : 1.8.0
 - xapi : 2.0.4-SNAPSHOT
```


# 1.4.19.2
2026.03.12

    [#core:1.4.19.2: logger log level 수정]
        - 내용 : logger.isDebugEnabled -> logger.isInfoEnabled() 변경
        - 수정 class : NexacroView.java
    

# 1.4.19.1
2026.02.24

    [#core:1.4.19.1: *-common.xml의 SpEL(util:properties) 설정 지원 및 압축통신(CompressType) 설정 추가]
    - 이슈 : dispatcher-servlet.xml에 <util:properties id="EtcProperty">로 설정된 프로퍼티를 PropertiesProvider에서 로딩하지 못하는 문제 해결.
    - 원인 : PropertiesProvider가 dispatcher-servlet.xml의 빈보다 먼저 초기화되어 EtcProperty 빈을 읽지 못함.
    - 가이드 : <util:properties id="EtcProperty"> 테그를  'context-common.xml' 파일로 옮겨서 dispatcher-servlet.xml 설정에 앞서 로딩되도록 조정.
    - 수정 class : PropertiesProvider.java, PropertyConfig.java, NexacroContext.java, NexacroMethodArgumentResolver.java
    - 상세 내용 :
        - PropertiesProvider에 EtcProperty 빈 지연 로딩(Lazy Loading) 기능 추가.
          etcPropertyLoaded 플래그를 통해 getEtcProperty() 호출 시점에 EtcProperty 빈이 준비되었으면 로드하도록 개선.
        - PropertiesProvider.loadPropertiesFromEtcPropertyBean() 추가.
          ApplicationContext에서 EtcProperty(java.util.Properties) 빈을 조회하여 캐시에 저장.
        - PropertyConfig에서 EtcProperty 빈 로딩 기능 추가.
          etcPropertiesBase() 빈 초기화 시 ApplicationContext에 EtcProperty 빈이 존재하면 해당 프로퍼티를 함께 로드.
        - PropertiesProvider의 uiAdapter 프로퍼티 키 목록에 uiAdapter.useRequestCompressType 추가.
        - NexacroContext에 useRequestCompressType 필드 및 압축통신(zlib) 지원 여부 체크 기능 추가.
          isGzipSupported() 메서드 추가, receiveData() 후 protocolType으로 zlib 여부 판별.
        - NexacroMethodArgumentResolver 주석 정리 및 spel 방식 재적용 안내 추가.

# 1.4.19
2026.02.23

    [#core:1.4.19: Property 설정 방식 개선 및 NexacroView 응답 설정 수정]
    - 이슈 : properties 파일을 통한 설정 정보 로드 방식 개선 및 NexacroView의 응답 컨텐츠 타입/캐릭터셋 설정 유연화.
    - 수정 class : NexacroView.java, PropertiesProvider.java
    - 추가 class : PropertyConfig.java
    - 상세 내용 :
        - PropertyConfig를 통해 application.properties, nexacro.properties, xeni.properties를 자동 로드하도록 개선.
        - NexacroView에서 uiAdapter.useRequestContentType 또는 nexacro.use-request-contenttype 설정 시 요청의 ContentType을 사용하도록 수정.
        - NexacroView에서 uiAdapter.useRequestCharset 또는 nexacro.use-request-charset 설정 시 요청의 Charset을 사용하도록 수정.
        - pom.xml 버전을 1.4.19-SNAPSHOT으로 업데이트.

# 1.4.18.1
2025.12.30

    [#core:1.4.18.1: propertiesProvider로 EtcProperty 대체]
    - 이슈 : Etc.getProperty() 에러 패치. 
    - 수정 class : NexacroMethodArgumentResolver.java, NexacroContext.java, NexacroView.java

# 1.4.18
2025.12.22

    [#core:1.4.18: EtcProperty 사용 배재]
    - 이슈 : Etc.getProperty() 에러 제거를 위해 Etc.getBean()에서 null 처리 및 PropertiesProvider 사용 추가. 
    - 수정 class : NexacroView.java, Etc.java
    - 추가 class : PropertiesProvider.java


# 1.4.17.1
2025.08.18

    [#core:1.4.17.1: EL에서 EtcProperty사용방식 수정에 따른 에러 픽스(원복)]
    - 수정 class : NexacroView.java, NexacroFileView.java, NexacroMethodArgumentResolver.java, NexacroContext.java

# 1.4.17
2025.05.23

    [#core:1.4.17: xapi : 2.0.0-SNAPSHOT 반영]
    - NexacroFileView 최적화

# 1.4.16
2025.04.29

    [#core:1.4.16:resultType=map 요청결과 0건일때 add()추가를 하지 않도록 막음.]
    - 이슈 : nexacro에서 재조회 후 Mybatis에서 selectOne 호출시 selectList 후 list.get(0)를 리턴함.
            add()를 하지 않고 resurltType=map의 단건 처리를 Map으로 받을수 없음.
    - 사유 : add()를 할 경우, 기존 <List<Map>> 요청후 재조회시 01건의 가비지데이터가 추가됨.
```java
            public <T> T selectOne(String statement, Object parameter) {
                // Popular vote was to return null on 0 results and throw exception on too many.
                List<T> list = this.<T>selectList(statement, parameter);
                if (list.size() == 1) {
                    return list.get(0);
                } else if (list.size() > 1) {
                    throw new TooManyResultsException("Expected one result (or null) to be returned by selectOne(), but found: " + list.size());
                } else {
                    return null;
                }
            }
```
    - 처리결과 : selectOne() 호출 결과가 null 인것은 제약사항으로 남음.
    - 처리내용 : MapMetaData의 add() 막음.
    - 처리함수 : MapMetaData의.setMetaData() 수정

# 1.4.15
2025.04.28

    - 이슈 : VERACODE 보안취약점 : CRLF 주입 패치- 
    - 상세 취약점 : 
        - HTTP 헤더에서 CRLF 시퀀스의 부적절한 중화 ('HTTP 요청 / 응답 분할 ')(CWE ID 113)
        - 로그에 대한 부적절한 출력 중화 (CWE ID 117)
        - 전송된 데이터에 민감한 정보 삽입 (CWE ID 201)
    - 취약점 패치 : CRLF제거. 파일명, 컨텐츠유형 설정 정제.
    - 대상 Class : NexacroView.java, NexacroFileView.java, NexacroInterceptor.java
    - 처리함수 : renderMergedOutputModel() 수정, sanitizeHeaderValue() 추가
    - 처리내용 : SLF4j's {} 치환 검증기능 활용.

# 1.4.14
2025.04.24

    - 추가기능 : resultType=vo이고 단건 조회결과가 0건일 때, BeanMetaData를 생성하면서 list에 vo객체를 1건 추가한다.
    - 처리 class : BeanMetaData.java
    - 추가함수 : BeanMetaData(Class<?> genericType, String addMetaData) 추가
    - 처리 class : NexacroMybatisMetaDataProvider.java
    - 수정함수 : generateMetaDataFromClass() 수정

# 1.4.13
2025.04.07

    - 이슈 : 단건조회결과 0건에 따른 재조회 결과로 null 데이터 1row가 들어온다.
    - 처리내용 : 재조회 결과 MapMetaData 는 row 추가 제외.
    - 처리함수 : ListToDataSetConverter.convertListMapToDataSet() 수정

# 1.4.12
2025.03.26

    - 이슈 : 단건 조회 (selectOne()) 후 리턴을 Map으로 받을 경우, MetaData를 못받는다.
    - 원인 : itatis의 DefaultSqlSession.selectOne()에서 결과가 0건이면 null을 리턴한다.
    - 우회처리 : 0건 재조회 후, MapMetaData 세팅할 때 강제로 add()처리.
    - 처리함수 : MapMetaData.setMetaData() 수정, setNullMetaData() 추가

# 1.4.11
2025.03.24

	- 기능.1 : servlet에서 spring f/w의 bean 취득하는 기능 추가
	- 추가 class : com.nexacro.uiadapter.jakarta.core.context.ServletContextUtil.java
	- Usage : UserInfo userInfo = ServletContextUtil.getBean("userInfo", UserInfo.class);
	- 추가 class : com.nexacro.uiadapter.jakarta.core.context.SpringBeanUtil.java
	- Usage : UserInfo userInfo = (UserInfo)SpringBeanUtil.getBean(ServletContextUtil.getRequest(), "userInfo");

	- 기능.2 : ApplicationContextProvider 에 ApplicationContext 객체 추가
	- 수정 class : com.nexacro.uiadapter.jakarta.core.context.ApplicationContextProvider.java
	- 수정함수 : setApplicationContext()
	- 추가함수 : getApplicationContext()

# 1.4.8
2025.03.24

	- 이슈 :  입력문자열의 $ 문자가 삭제되는 오류 수정.
	- 수정 class : Etc.java 
	- 수정함수 : regexTrim()
    - 추가함수 : rTrim(), lTrim()

# 1.4.7
2024.12.27

    - 이슈 : 멀티파일 zip 다운로드 설정 변경
    - 수정 class : com.nexacro.uiadapter.spring.core.viewNexacroFileView.java
    - 수정함수 : renderMergedOutputModel()

# 1.4.6
2024.11.28

	- 이슈 :  db의 column case를 지정하지 않더라도 client의 column case를 nexacro.client-column-case로 변환.
	- 수정 class : Etc.java 
		- 추가 함수 명 :	convertColumnCaseFromDbToUi() 추가
	- 수정  class : AbstractDataSetConverter.java
		- 수정함수 : addRowIntoDataSet(), addRowIntoDataSet(), addColumnIntoDataSet(), addColumnByMap()
		- 수정내용 : 단방향(db->clisne) 변환 기능 추가 함수 적용. Etc.convertColumnCaseFromDbToUi()
# 1.4.5
2024.11.14

	- 수정 사항 : EtcPropertiesBase에 property가 있는지 체크하는 함수 추가
	- 수정 class : EtcPropertiesBase.java 
		- 추가 함수 명 :	hasProperties() 추가

	- 수정 사항 : NexacroView 에서 EtcPropertiesBase에 Property가 존재하는지 체크하는 로직 추가.
	- 수정 class : NexacroView.java , NexacroContext.java
		- 수정 함수 명 :	NexacroView() 추가
		- 수정 조건 : useRequestCharset, useRequestContentType 초기화 조건 변경. ("" -> null)
					 Etc.getBean("etcProperty") 초기화 수정

	- 추가 사항 : 데이터가 0건일 경우 db -> ui 단방향 column case 변환 기능 추가.
	- 수정 class : Etc.java
		- 추가 함수 : convertColumnCaseFromDbToUi()
#1.4.1
2024.11.07

	- version : 1.4.1
	- 수정 사항 : trimParamDataSet 사용하지 않는 조건에 null 추가
	- 수정 class : NexacroMethodArgumentResolver.java
    - 함수 명 : 	extractDataSetParameter()
    			extractDataSetGroupParameter()

2024.05.24

	- version : 1.4.0
	- 수정 사항 : 화면에서 procedure 호출시 dataset을 group으로 묶어서 보낼 경우 처리하는 기능 추가
	- 추가 class : ParamDataSetGroup.java 
				UiadapterConstants.java 
	- 수정 class : NexacroMethodArgumentResolver.java
    - 함수 명 : 	extractDataSetGroupParameter()
    			findNestedGenericType()
	
2024.05.16

	- version : 0.4.0
	- 0.0.4 버전을 1.4.0 으로 버전 픽스.
	- 수정 사항 : bs에서 procedure in/out 조건이 0건인 케이스 추가.
	
2024.05.07

	- version : 0.0.4
	- DataType getDataType(final String targetClassName) 기능 추가.
	- 수정 class : NexacroConverterHelper.java

2023.08.02

	- ui의 컬럼 유형과 DB의 컬럼 유형이 다를 경우 컬럼 case를 변환하는 기능 추가.
	- 수정 class : Etc.java
				EtcPropertiesBase.java
				AbstractDataSetConverter.java
				DataSetToListConverter.java
				DataSetToObjectConverter.java
	
2021.11.02

    - 파일 다운로드시 다중파일 다운로드 처리기능 추가
        - 추가 class : com.nexacro.uiadapter.spring.core.view.NexacroMultiFileResult
             - 생성자(String sZipFileName, String sFilePath, String sFileNameList) 
             - sZipFileName : 다운로드될 압축파일명
             - sFilePath : 다운로드할 파일의 경로
             - sFileNameList : 다운로드할 파일들(aaa,bbb,ccc)
     
	- 여러개의 파일을 하나의 압축(zip)파일로 다운로드 처리.
    - 수정 class : com.nexacro.uiadapter.spring.core.view.NexacroFileView
        - 함수명 : renderMergedOutputModel()
        - 수정내용 :  NexacroMultiFileResult 처리 로직 추가.
    


2021.10.06

    - sendData할 때 getDefaultContentType()을 적용하도록 조건 추가.
    - class 명 : com.nexacro.uiadapter.spring.core.view.NexacroView
    - 함수 명 : sendResponse()
    - 추가 내용
        // getDefaultContentType()을 적용하도록 조건 추가 Start.
        platformResponse.setContentType(getDefaultContentType());
        platformResponse.setCharset(getDefaultCharset());
        // getDefaultContentType()을 적용하도록 조건 추가 E n d.