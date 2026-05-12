# Nexacro N uiadapter Runner Cookbook

> **목적:** boot-jdk17-jakarta / boot-jdk8-javax 두 런너에 대해 검증 완료된 canonical 패턴을 한곳에 정리. 다음 작업(eGov/MVC/WebFlux 런너 또는 신규 엔드포인트 추가)에서 재탐색 없이 이 문서만 보고 빠르게 진행하기 위한 작업 노트.
>
> **검증 상태 (2026-05-12):**
> - boot lane: jdk17 + jdk8 두 런너 전 엔드포인트 빌드 + smoke 통과 (PR #1 ~ #18).
> - mvc lane: jdk17-jakarta + jdk8-javax XML-driven WAR 런너 Tomcat 10/9 배포 검증 (PR #19 ~ #23). 서비스 엔드포인트가 xapi 라이선스 게이트까지 도달 — boot 런너와 동일 동작 (license-less 환경 기준).

---

## 1. 두 lane의 좌표 차이 (cheat sheet)

| 항목 | jakarta lane (jdk17) | javax lane (jdk8) |
|---|---|---|
| Spring Boot | `3.3.5` (`boot3.version`) | `2.7.18` (`boot2.version`) |
| Servlet API | `jakarta.servlet:jakarta.servlet-api:6.0.0` (provided) | `javax.servlet:javax.servlet-api:4.0.1` (provided) |
| JSP API (runtime, 필수) | `jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1` | `javax.servlet.jsp:javax.servlet.jsp-api:2.3.3` |
| File Upload | `commons-fileupload2-jakarta:2.0.0-M1` (xeni-jakarta가 직접 참조) | (boot2 starter-web 번들) |
| MyBatis Spring Boot Starter | `3.0.3` | `2.3.2` |
| HSQLDB | `2.7.3` | `2.7.3` |
| **xapi** | `com.nexacro:nexacroN-xapi-jakarta:1.2.4-SNAPSHOT` | `com.nexacro:nexacroN-xapi:1.2.4-SNAPSHOT` |
| **xeni** | `com.nexacro:nexacroN-xeni-jakarta:1.5.21-SNAPSHOT` | `com.nexacro:nexacroN-xeni:1.5.21-SNAPSHOT` |
| **uiadapter-core** | `uiadapter-jakarta-core:1.0.27.1-SNAPSHOT` | `uiadapter-spring-core:1.4.19.2-SNAPSHOT` |
| **uiadapter-dataaccess** | `uiadapter-jakarta-dataaccess:1.0.14-SNAPSHOT` | `uiadapter-spring-dataaccess:1.4.19-SNAPSHOT` |
| **uiadapter-excel** | `uiadapter-jakarta-excel:1.5.4.3-SNAPSHOT` | `uiadapter-spring-excel:1.5.21-SNAPSHOT` |
| Nexus repo | `https://mangosteen.tobesoft.co.kr/nexus/repository/tobesoft-snapshots/` (anonymous, public) | 동일 |

### 패키지 import 차이 — 가장 자주 헷갈리는 표

| 용도 | jakarta import | javax import |
|---|---|---|
| 기본 어노테이션 | `com.nexacro.uiadapter.jakarta.core.annotation.{ParamDataSet, ParamVariable}` | `com.nexacro.uiadapter.spring.core.annotation.{ParamDataSet, ParamVariable}` |
| 결과 객체 | `com.nexacro.uiadapter.jakarta.core.data.{NexacroResult, NexacroFileResult, NexacroStreamResult}` | `com.nexacro.uiadapter.spring.core.data.{NexacroResult, NexacroFileResult, NexacroStreamResult, NexacroFirstRowHandler}` |
| 컨텍스트 (firstrow streaming용) | `com.nexacro.uiadapter.jakarta.core.context.{NexacroContext, NexacroContextHolder}` | `com.nexacro.uiadapter.spring.core.context.{NexacroContext, NexacroContextHolder}` |
| FirstRowHandler | `com.nexacro.uiadapter.jakarta.core.data.NexacroFirstRowHandler` | `com.nexacro.uiadapter.spring.core.data.NexacroFirstRowHandler` |
| MybatisRowHandler | `com.nexacro.uiadapter.jakarta.dao.mybatis.MybatisRowHandler` | `com.nexacro.uiadapter.spring.dao.mybatis.MybatisRowHandler` |
| Servlet API | `jakarta.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}` | `javax.servlet.http.{HttpServletRequest, HttpServletResponse, HttpSession}` |

> jakarta 는 `uiadapter.jakarta.{core,dao}`, javax 는 `uiadapter.spring.{core,dao}`. 패키지 prefix가 비대칭이라 코드 포팅 시 sed 한 줄로 안 된다 — 반드시 위 표 매핑 사용.

---

## 2. 프로젝트 4.2 canonical layout

런너 내부는 lane 무관하게 동일 패키지 트리를 가진다. skill (`nexacro-fullstack-starter`) Step 3-3가 강제하는 layout.

```
samples/runners/<RUNNER_KEY>/
├── pom.xml                     self-contained: parent 없음, deps 인라인
├── README.md
└── src/main/
    ├── java/com/nexacro/uiadapter/
    │   ├── Application.java                @SpringBootApplication
    │   ├── config/                          UiadapterWebMvcConfig, MyBatisConfig, WebConfig, RelayHttpConfig…
    │   ├── controller/                      @Controller + @RequestMapping("/foo.do")
    │   ├── service/                         interface only
    │   ├── service/impl/                    *ServiceImpl @Service
    │   ├── mapper/                          MyBatis @Mapper interfaces (top-level)
    │   └── domain/                          POJO/VO (NexacroBase 상속)
    └── resources/
        ├── application.yml                  context-path: /uiadapter, HSQLDB, mybatis path
        ├── schema.sql                       HSQLDB syntax, '^^' separator
        ├── data.sql                         HSQLDB syntax, '^^' separator
        ├── logback.xml
        └── mybatis/
            ├── sql-mapper-config.xml
            └── mappers/*-mapper.xml
```

**금지 패키지** (skill Step 3-3 검증으로 차단):
- `com.nexacro.fullstack.*`
- `com.nexacro.runner.*`

**Application.java** 는 `scanBasePackages` override 하지 않는다. `com.nexacro.uiadapter` 가 root 라 component-scan이 자동으로 모든 sub-package 를 잡는다.

---

## 3. Endpoint 패턴 모음 (검증 완료)

엔드포인트 구현 시 아래 7 패턴 중 하나를 골라 복붙 + 명칭만 변경. 모두 jdk17 + jdk8 양쪽에서 빌드 + smoke 통과.

### 3.1 동기 단건/리스트 (POJO bind)

```java
@RequestMapping("/select_datalist.do")
public NexacroResult selectDataList(
        @ParamDataSet(name = "ds_search", required = false) Board search) {
    NexacroResult result = new NexacroResult();
    result.addDataSet("output1", boardService.selectList(search));
    return result;
}
```

- `name` = xfdl `inDataset` (`Board.outData=` 또는 `transaction` arg) 가 보내는 dataset 이름과 **정확히 일치** 필요.
- 응답 dataset 이름 (`"output1"`) 도 xfdl `outData` spec과 일치해야 함. (PR #6 회귀)

### 3.2 동기 Map 모드 (camelCase 키 필수)

```java
@RequestMapping("/select_datalist_map.do")
public NexacroResult selectDatalistMap(
        @ParamDataSet(name = "dsSearch", required = false) Map<String, String> dsSearch) {
    NexacroResult result = new NexacroResult();
    result.addDataSet("output1", boardService.selectDatalistMap(dsSearch));
    return result;
}
```

- **Map 모드 mapper XML 에선 SELECT 컬럼 alias 를 camelCase 로** (PR #10 회귀). 예: `POST_ID AS postId, REG_DATE AS regDate`. 그렇지 않으면 xfdl 에서 컬럼이 lower-case raw로 들어와 화면 바인딩 깨짐.

### 3.3 CUD 일괄 (\_RowType\_ 기반 dispatch)

```java
@RequestMapping("/update_datalist.do")
public NexacroResult updateDatalist(
        @ParamDataSet(name = "input1") List<Board> input1) {
    int affected = boardService.updateDatalist(input1);
    NexacroResult result = new NexacroResult();
    result.addVariable("affectedRows", affected);
    return result;
}
```

- 각 행의 `_RowType_` (I/U/D) 으로 서비스에서 INSERT/UPDATE/DELETE dispatch. 도메인은 `NexacroBase` 상속해서 `rowType` 노출.

### 3.4 ★ 스트리밍 firstrow (대량 데이터)

```java
@RequestMapping("/select_datalist_firstrow.do")
public void selectDataListFirstRow(
        @ParamDataSet(name = "ds_search", required = false) Board search,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    NexacroContext ctx = NexacroContextHolder.getNexacroContext(request, response);
    NexacroFirstRowHandler firstRowHandler = ctx.getFirstRowHandler();
    boardService.selectListFirstRow(
            search == null ? new Board() : search,
            firstRowHandler, 100, "output1");
}
```

서비스:
```java
public void selectListFirstRow(Board search, NexacroFirstRowHandler frh,
                               int chunkSize, String dataSetName) {
    MybatisRowHandler rh = new MybatisRowHandler(frh, dataSetName, chunkSize);
    boardMapper.selectListFirstRow(search, rh);   // ResultHandler 시그니처
    rh.sendRemainData();                          // 마지막 partial chunk flush 필수
}
```

매퍼 인터페이스:
```java
void selectListFirstRow(Board search, ResultHandler<Board> handler);
```

매퍼 XML (DRY: `<sql>` fragment + `<include>`):
```xml
<sql id="selectListBody">
    SELECT POST_ID, TITLE, ... FROM TB_BOARD
    <where>
        <if test="regId != null and regId != ''">AND REG_ID = #{regId}</if>
        ...
    </where>
    ORDER BY POST_ID DESC
</sql>

<select id="selectList" parameterType="...Board" resultType="...Board">
    <include refid="selectListBody"/>
</select>

<select id="selectListFirstRow" parameterType="...Board" resultType="...Board">
    <include refid="selectListBody"/>
</select>
```

> **핵심 포인트**
> - MyBatis 는 동일 `id` 메서드 오버로딩 불가 → streaming 변종은 **반드시 새 이름** (`*FirstRow`).
> - `<sql id=>` + `<include>` 로 SQL body 한 곳에서 관리. 기존 endpoint 변경 시 자동 동기화.
> - `MybatisRowHandler` 생성자: `(NexacroFirstRowHandler, String dataSetName, int chunkSize)`. chunkSize 100 ~ 1000 권장.
> - controller 리턴타입 `void` — `firstRowHandler` 가 응답 body 를 직접 쓴다.

### 3.5 파일 업/다운로드

PR #11 에서 6개 패턴 검증 완료. 시그니처만 정리:

| 경로 | 시그니처 | 용도 |
|---|---|---|
| `/advancedUploadFiles.do` | `NexacroResult uploadFiles(HttpServletRequest, HttpServletResponse)` | 멀티파트 업로드 + 메타 dataset 반환 |
| `/advancedDownloadFile.do` | `NexacroFileResult downloadFile(HttpServletRequest)` | 단일 파일 다운로드 |
| `/multiDownloadFiles.do` | `NexacroFileResult multiDownloadFiles(req, res)` | 다중 파일 |
| `/advancedDownloadFiles.do` | `void downloadFiles(req, res)` | 직접 write |
| `/advancedDownloadList.do` | `NexacroResult downloadList(...)` | 다운로드 가능 목록 조회 |
| `/advancedDeleteFiles.do` | `NexacroResult deleteFiles(@ParamDataSet(name="input") DataSet dsInput)` | 다중 삭제 |

업로드 디렉터리는 `application.yml` 의 `nexacro.file.storage-dir` (기본 `./uploads`).

### 3.6 비디오/이미지 스트리밍

```java
@RequestMapping("/sampleVideoStream.do")
public void stream(HttpServletRequest req, HttpServletResponse res) throws Exception {
    // Range header 처리는 NexacroStreamResult 또는 직접 구현
}

@RequestMapping("/streamingVideo.do")
public NexacroStreamResult streamingVideo(req, res) throws Exception { ... }

@RequestMapping("/movie/{fileName}/**")
public ModelAndView video(@PathVariable("fileName") String fileName) { ... }
```

`WebConfig` 의 `addResourceHandlers` 가 `/packageN/**` 정적 리소스를 `classpath:/static/packageN/` 으로 매핑한다. xfdl 빌드 산출물 위치.

### 3.7 Excel export

```java
@RequestMapping("/exportBoardExcel.do")
public NexacroResult exportBoardExcel(
        @ParamDataSet(name = "dsBoard") List<Board> data,
        HttpServletResponse response) throws Exception {
    // uiadapter-{jakarta,spring}-excel API 사용
}
```

### 3.8 Login (stub)

```java
@RequestMapping("/login.do")
public NexacroResult login(@ParamDataSet(name="dsSearch") Map<String,String> creds,
                           HttpSession session) { ... }

@RequestMapping("/logout.do")
public NexacroResult logout(HttpSession session) {
    session.invalidate();
    return new NexacroResult();
}
```

실제 인증/세션 정책은 별도 구현 영역. 현재는 항상 200 반환하는 stub.

---

## 4. Domain: NexacroBase 상속 패턴

```java
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class TestDataType extends NexacroBase {

    private Integer id;                  // 로컬 PK (HSQLDB 업서트 dispatch)

    public String      stringValue;
    public Integer     intValue;
    public Boolean     booleanValue;
    public Long        longValue;
    public Float       floatValue;
    public Double      doubleValue;
    public BigDecimal  bigDecimalValue;

    public Date        dateValue;        // ★ java.util.Date (NOT java.sql.Date)
    public Date        timeValue;        // ★ java.util.Date (NOT java.sql.Timestamp)
    public Date        datetimeValue;    // ★ java.util.Date

    public byte[]      bytesValue;
}
```

규칙:
- **타입 필드는 `public`** (canonical `example.nexacro.uiadapter.pojo.ExampleDataType` 와 일치).
- 클래스 레벨 Lombok `@Getter`/`@Setter` 로 accessor 자동 생성.
- `@EqualsAndHashCode(callSuper = true)` — `NexacroBase.rowType` 가 equality 에 참여하도록.
- date/time/datetime 은 **모두 `java.util.Date`** (PR #16 회귀). `java.sql.*` 쓰면 uiadapter envelope ↔ Nexacro PlatformDataType 변환에서 문제 발생.

---

## 5. application.yml 필수 키

```yaml
server:
  port: 8080
  servlet:
    context-path: /uiadapter           # ★ xfdl svcurl/와 정렬

spring:
  datasource:
    url: jdbc:hsqldb:mem:nexacro;sql.syntax_mys=true
    driver-class-name: org.hsqldb.jdbc.JDBCDriver
    username: sa
    password: ""
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql
      separator: "^^"                   # ★ canonical 와 동일. 빠뜨리면 schema/data.sql 한 statement 로 인식되어 기동 실패
      continue-on-error: false
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

mybatis:
  config-location: classpath:mybatis/sql-mapper-config.xml
  mapper-locations: classpath:mybatis/mappers/*-mapper.xml
  type-aliases-package: com.nexacro.uiadapter.domain

nexacro:
  file:
    storage-dir: ./uploads
  relay:
    exim:
      url:                              # 비어 있으면 503 fallback
      connect-timeout: 5000
      read-timeout: 30000
      forward-headers: {}
```

> **404 회귀의 원인 1순위:** controller 매핑에 실수로 `/uiadapter/` prefix 를 넣으면 context-path 가 두 번 strip 되어 404. `@RequestMapping("/xxx.do")` 로만 적는다.

---

## 6. 검증된 엔드포인트 인벤토리 (jdk17 + jdk8 양쪽 동일)

| Controller | Endpoint |
|---|---|
| BoardController | `/select_data_single.do`, `/select_datalist.do`, `/select_datalist_map.do`, `/update_datalist.do`, `/update_datalist_map.do`, `/select_datalist_firstrow.do`, `/test.do` |
| DeptController | `/update_deptlist_map.do`, `/dept_tree.do` |
| LargeController | `/sampleLargeData.do` |
| WideController | `/sampleWideColumns.do` |
| TestDataController | `/sampleTestData.do`, `/check_testDataTypeList.do`, `/select_testDataTypeList.do`, `/select_testDataTypeList_map.do`, `/update_testDataTypeList.do`, `/update_testDataTypeList_map.do`, `/checkArgsAnotation.do` |
| FileController | `/advancedUploadFiles.do`, `/advancedDownloadFile.do`, `/multiDownloadFiles.do`, `/advancedDownloadFiles.do`, `/advancedDownloadList.do`, `/advancedDeleteFiles.do` |
| StreamController | `/video.do`, `/movie/{fileName}/**`, `/streamingVideo.do` |
| VideoController | `/sampleVideoStream.do` |
| ExcelExportController | `/exportBoardExcel.do` |
| LoginController | `/login.do`, `/logout.do` |
| RelayController | (exim relay — `nexacro.relay.exim.url` 설정 시 활성) |

전체 base path: `http://localhost:8080/uiadapter/<endpoint>`

---

## 7. 자주 밟는 함정 (PR 회귀 기록)

| 증상 | 원인 | PR | 대응 |
|---|---|---|---|
| `NoClassDefFoundError: jakarta/servlet/jsp/JspWriter` 기동 직후 폭사 | embedded tomcat 에 JSP container 없음. `xapi-jakarta HttpPlatformResponse.sendData()` 가 JSP API 참조 | #4 | `jakarta.servlet.jsp:jakarta.servlet.jsp-api` 를 **default scope** (NOT provided) 로 추가. javax lane 도 동일. |
| `/select_datalist.do` 404 | 매핑에 `/uiadapter/` prefix 박힘 (context-path 와 중복) | #2, #3 | `@RequestMapping("/xxx.do")` — prefix 빼기 |
| xfdl 화면이 응답을 못 받음 | 응답 dataset 이름이 xfdl `outData` spec 과 불일치 | #6 | controller `addDataSet("output1", ...)` 의 키와 xfdl `outData="dsList=output1"` 의 우변이 일치해야 함 |
| Map 모드 응답에서 컬럼이 raw lowercase 로 들어옴 | mapper XML SELECT 에 alias 없음 | #10 | `POST_ID AS postId, REG_DATE AS regDate` 처럼 camelCase alias 강제 |
| `mybatis-spring` 시작 시 `Invalid bound statement` | mapper 인터페이스 메서드 시그니처와 XML `id` mismatch (특히 streaming variant 추가 시) | (이번 firstrow 작업) | 메서드 새 이름 + XML 새 `<select id=>` |
| `dateValue` 가 null/이상값 | 도메인 필드를 `java.sql.Date` / `java.sql.Timestamp` 로 선언 | #16 | 전부 `java.util.Date` 로 통일 |
| `schema.sql` 실행 중 syntax error | `^^` separator 미지정 → 전체 파일 한 statement 로 파싱 | (초기) | `spring.sql.init.separator: "^^"` |
| `commons.fileupload2.jakarta` 못 찾음 | xeni-jakarta pom 이 transitive 선언 안 함 | #5 | runner pom 에 명시 dependency |
| skill 산출물에 `com.nexacro.{fullstack,runner}` 잔존 | upstream main 이 OLD self-implemented 패턴 | (Plan Phase E-3a) | skill Step 3-3 가드가 차단 — `/plugin update` 로 최신 버전 강제 |

---

## 8. 빌드 + 스모크 체크리스트

```bash
# jdk17 lane
cd samples/runners/boot-jdk17-jakarta
JAVA_HOME=/path/to/jdk-17 mvn -o clean package           # BUILD SUCCESS
mvn spring-boot:run &
sleep 30
curl -I http://localhost:8080/uiadapter/packageN/index.html               # 200
curl -sv -X POST http://localhost:8080/uiadapter/select_datalist.do \
  -H "Content-Type: application/x-www-form-urlencoded" --data 'method=x'  # 200 + nexacro envelope

# jdk8 lane — 동일 절차, JAVA_HOME 만 jdk-8 로
cd ../boot-jdk8-javax
JAVA_HOME=/path/to/jdk-8 mvn -o clean package
```

---

## 9. PR 머지 이력 (작업 순서 참고)

| PR | 제목 | 핵심 |
|---|---|---|
| #1 | refactor: make implemented runners self-contained (Part A) | parent pom 제거, shared-business 소스 인라인 |
| #2 | refactor(boot-jdk17-jakarta): canonical Nexacro N uiadapter pattern | 패키지 4.2 layout |
| #3 | refactor(boot-jdk8-javax): canonical pattern | javax lane 동일 |
| #4 | fix: add JSP API runtime dependency | embedded tomcat JSP 누락 fix |
| #5 | refactor: wire xapi/xeni/uiadapter modules canonically | 1st-party 모듈 실사용 |
| #6 | fix: align response dataset names with xfdl outData | 응답 dataset 명명 |
| #7 | fix: align jakarta+javax sample DB to GitLab canonical schema | 스키마 정렬 |
| #8 | feat(board): canonical Map-mode endpoints + 3 new | Map 모드 |
| #10 | fix(board-mapper): camelCase keys for map-mode SQL | alias |
| #11 | feat(file): canonical 6 upload/download endpoints | 파일 |
| #12 | feat(stream): canonical video/image streaming endpoints | 비디오 |
| #13 | feat(testdata + largedata): canonical endpoints | 타입 검증 |
| #14 | feat(domain): extend NexacroBase + canonical field scopes | 도메인 |
| #15 | feat(board): canonical `/select_datalist_firstrow.do` streaming | ★ firstrow |
| #16 | fix(TestDataType): use java.util.Date for dateValue | date 타입 |
| #17 | fix(nxui/frameLogin): remove 대량 컬럼 데이터 조회 menu row | xfdl 메뉴 정리 |

---

## 9-MVC. XML-driven MVC WAR 런너 (jdk17-jakarta / jdk8-javax)

`mvc-jdk17-jakarta` / `mvc-jdk8-javax` 는 boot 런너의 `Application.java` + `@Configuration` 클래스들을 **전통적인 Spring MVC XML** 으로 1:1 매핑한 WAR 런너다. 엔드포인트 시그니처와 도메인은 boot 런너와 동일 — 차이는 wiring 방식뿐.

> 참조 캐노니컬: <https://gitlab.com/nexacron/spring-framework/jakarta/nexacro-jakarta-example>

### 9-MVC.1 파일 레이아웃

```
samples/runners/mvc-{jdk17-jakarta,jdk8-javax}/
├── pom.xml                          packaging=war, parent 없음, self-contained
├── src/main/
│   ├── java/com/nexacro/uiadapter/  (boot 런너와 동일한 controller/service/mapper/domain)
│   ├── resources/
│   │   ├── spring/
│   │   │   ├── context-common.xml        property-placeholder + component-scan(@Controller 제외) + AntPathMatcher
│   │   │   ├── context-datasource.xml    (jakarta) HikariDataSource  /  (javax) DriverManagerDataSource
│   │   │   ├── context-transaction.xml   DataSourceTransactionManager + tx:advice + AOP pointcut
│   │   │   ├── context-mapper.xml        SqlSessionFactoryBean + MapperScannerConfigurer
│   │   │   ├── context-nexacro.xml       ApplicationContextProvider + Dbms map + DbVendorsProvider
│   │   │   ├── context-initialize.xml    ResourceDatabasePopulator (schema/data.sql, separator="^^")
│   │   │   └── context-relay.xml         RestTemplate (EXIM relay)
│   │   ├── schema.sql / data.sql         boot 런너와 동일 (^^ separator)
│   │   ├── etc.properties                (optional, jakarta 의 EtcProperty 빈이 읽음)
│   │   └── mybatis/                      boot 런너와 동일
│   └── webapp/WEB-INF/
│       ├── web.xml                       DispatcherServlet + ContextLoaderListener, multipart-config, *.do
│       └── config/springmvc/
│           └── dispatcher-servlet.xml    controllers, nexacro views, XENI bridge, util:properties EtcProperty
└── (배포 산출물) target/uiadapter.war
```

### 9-MVC.2 lane 별 좌표 차이

| 항목 | mvc-jdk17-jakarta | mvc-jdk8-javax |
|---|---|---|
| Servlet API | `jakarta.servlet:jakarta.servlet-api:6.0.0` | `javax.servlet:javax.servlet-api:4.0.1` |
| Spring | `6.1.14` (spring-webmvc/jdbc/context/tx/aop) | `5.3.39` |
| Container | Tomcat 10 / Jetty 11+ | Tomcat 9 / Jetty 9 |
| DataSource | `HikariDataSource` | `DriverManagerDataSource` (canonical 패턴 그대로) |
| Nexacro views | view + fileView + **streamView** | view + fileView (streamView 없음 — canonical 일치) |
| Dbms map | Hsql / Oracle / Mssql / Mysql / Tibero (5종) | Hsql only (canonical 일치) |
| **uiadapter-core** | `uiadapter-jakarta-core:1.0.27.1-SNAPSHOT` | `uiadapter-spring-core:1.4.19.2-SNAPSHOT` |
| AspectJ | `aspectjweaver` 필수 (`<aop:config>` pointcut) | 동일 |

### 9-MVC.3 dispatcher-servlet.xml 핵심 빈

```xml
<context:component-scan base-package="com.nexacro.uiadapter.controller">
    <context:include-filter type="annotation"
            expression="org.springframework.stereotype.Controller"/>
    <context:exclude-filter type="annotation"
            expression="org.springframework.stereotype.Service"/>
    <context:exclude-filter type="annotation"
            expression="org.springframework.stereotype.Repository"/>
</context:component-scan>

<!-- ★ jakarta lane 전용: NexacroMethodArgumentResolver 의 @Value SpEL 이 참조하는 빈 -->
<util:properties id="EtcProperty"
                 location="classpath:etc.properties"
                 ignore-resource-not-found="true"/>

<bean id="nexacroView"  class="...NexacroView">
    <property name="defaultContentType" value="PlatformXml"/>
    <property name="defaultCharset"     value="UTF-8"/>
</bean>
<bean id="nexacroFileView"   class="...NexacroFileView"/>
<bean id="nexacroStreamView" class="...NexacroStreamView"/>   <!-- jakarta only -->

<bean class="...NexacroRequestMappingHandlerAdapter" p:order="0">
    <property name="customArgumentResolvers">
        <list>
            <bean class="...NexacroMethodArgumentResolver"/>
        </list>
    </property>
    <property name="customReturnValueHandlers">
        <list>
            <bean class="...NexacroHandlerMethodReturnValueHandler">
                <property name="view"       ref="nexacroView"/>
                <property name="fileView"   ref="nexacroFileView"/>
                <property name="streamView" ref="nexacroStreamView"/>  <!-- jakarta only -->
            </bean>
        </list>
    </property>
</bean>

<bean id="exceptionResolver" class="...NexacroMappingExceptionResolver" p:order="1">
    <property name="view" ref="nexacroView"/>
    <property name="shouldLogStackTrace"  value="true"/>
    <property name="shouldSendStackTrace" value="true"/>
    <property name="defaultErrorMsg"      value="fail.common.msg"/>
</bean>

<!-- XENI servlet 브리지: /XExportImport.do -->
<bean class="org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter"/>
<bean id="xeniUrlMapping"
      class="org.springframework.web.servlet.handler.SimpleUrlHandlerMapping" p:order="0">
    <property name="mappings">
        <props>
            <prop key="/XExportImport.do">xeniWrappingController</prop>
        </props>
    </property>
</bean>
<bean id="xeniWrappingController"
      class="org.springframework.web.servlet.mvc.ServletWrappingController">
    <property name="servletClass">
        <value>com.nexacro.java.xeni.services.GridExportImportServlet</value>
    </property>
    <property name="servletName" value="XExportImport"/>
    <property name="initParameters">
        <props>
            <prop key="export-path">/excel</prop>
            <prop key="import-path">/excel</prop>
            <prop key="monitor-enabled">true</prop>
            <prop key="monitor-cycle-time">30</prop>
            <prop key="file-storage-time">10</prop>
        </props>
    </property>
</bean>

<bean id="multipartResolver"
      class="org.springframework.web.multipart.support.StandardServletMultipartResolver"/>

<mvc:annotation-driven/>
```

### 9-MVC.4 ★ XML 변환에서 가장 자주 밟는 함정 (PR #23 회귀)

| # | 증상 | 원인 | 대응 |
|---|---|---|---|
| 1 | 기동 시 `SQLSyntaxErrorException: unknown token ... DROP TABLE IF EXISTS FILE_META^^` | `ResourceDatabasePopulator` 가 default `;` separator 로 schema.sql 을 한 statement 로 파싱 | `context-initialize.xml` (또는 `context-datasource.xml`) 의 `dbInit` 빈에 `<property name="separator" value="^^"/>` |
| 2 | 컴포넌트 스캔 시 `EL1008E: Property or field 'EtcProperty' cannot be found on null` (**jakarta lane only**) | `uiadapter-jakarta-core` 의 `NexacroMethodArgumentResolver` 필드가 `@Value("#{EtcProperty['key'] ?:null}")` — SpEL bean indexer 가 `EtcProperty` 라는 빈을 요구. javax core 는 `${key:}` 플레이스홀더 사용이라 무관 | `dispatcher-servlet.xml` 에 `<util:properties id="EtcProperty" location="classpath:etc.properties" ignore-resource-not-found="true"/>` 추가. 파일이 없으면 모든 키가 null 로 resolve 되고 SpEL `?:null` fallback 으로 안전한 default. |
| 3 | `failOnMissingWebXml` true 인데 web.xml 없음 | maven-war-plugin 기본값 | jdk17 lane: `<failOnMissingWebXml>false</failOnMissingWebXml>` 또는 web.xml 작성. jdk8 lane: web.xml 작성 |
| 4 | `mvn` 빌드 중 `<aop:config>` 파싱 실패 | `aspectjweaver` 미선언 | pom 에 `org.aspectj:aspectjweaver` 추가 |
| 5 | 배포 후 controller 매핑이 잡히지 않음 | root context 에서 component-scan 이 `@Controller` 까지 포함, dispatcher context 에서 또 스캔 → 양쪽 등록 / 한쪽 빠짐 | root context 는 `@Controller` exclude, dispatcher context 만 `@Controller` include |
| 6 | 트랜잭션 미작동 | `<tx:advice>` 만 있고 `<aop:config>` pointcut 누락 | `context-transaction.xml` 에 `<aop:config>` + `<aop:pointcut>` + `<aop:advisor>` 셋 다 있어야 함 |

**boot vs XML 의 본질적 차이**: boot 런너는 `UiadapterWebMvcConfig.addArgumentResolvers()` 에서 `new NexacroMethodArgumentResolver()` 를 **프로그래매틱하게** 생성해서 등록한다. Spring autowiring 을 거치지 않으므로 `@Value` SpEL 이 평가되지 않아 EtcProperty 빈이 없어도 동작. XML config 에서는 `<bean class="...NexacroMethodArgumentResolver"/>` 가 Spring 컨테이너의 정상 생명주기를 따르므로 `@Value` 가 실제로 평가됨 — 그래서 EtcProperty 빈이 **반드시** 필요.

### 9-MVC.5 빌드 + 배포 + smoke

```powershell
# 1) WAR 빌드 (Windows, PowerShell)
$env:JAVA_HOME = 'C:\AppStudio\jdk\jdk-17'
$env:M2_HOME   = 'C:\AppStudio\maven\apache-maven-3.8.6'
$env:PATH      = "$env:JAVA_HOME\bin;$env:M2_HOME\bin;$env:PATH"
cd D:\path\to\nexacroN-fullstack\samples\runners\mvc-jdk17-jakarta
mvn -o clean package -DskipTests
# -> target\uiadapter.war

# 2) Isolated CATALINA_BASE (충돌 회피)
$BASE = 'D:\AI\testspace\tomcat10-instance'
New-Item -ItemType Directory -Path $BASE -Force | Out-Null
Copy-Item -Recurse -Path 'C:\AppStudio\server\apache-tomcat-10.1.15\conf' -Destination $BASE
New-Item -ItemType Directory -Path "$BASE\webapps","$BASE\logs","$BASE\temp","$BASE\work" -Force | Out-Null
Copy-Item -Path '.\target\uiadapter.war' -Destination "$BASE\webapps\"

# 3) 기동
$env:CATALINA_BASE = $BASE
$env:CATALINA_HOME = 'C:\AppStudio\server\apache-tomcat-10.1.15'
& "$env:CATALINA_HOME\bin\catalina.bat" start

# 4) Smoke
Start-Sleep -Seconds 15
curl -I  http://localhost:8080/uiadapter/packageN/index.html              # nxui 가 없으면 404 (예상)
curl -sv -X POST http://localhost:8080/uiadapter/select_datalist.do       # 500 + InvalidLicenseException (boot 런너와 동일 — parity OK)
```

**parity 판정 기준**: `D:\AI\testspace\tomcat10-instance\logs\localhost.*.log` 에서 root cause 가 `com.nexacro.java.xapi.license.InvalidLicenseException: License not found` 이면 MVC wiring 은 정확. 컨트롤러 → dispatcher → nexacroView → xapi.license 까지 도달했음을 의미. 라이선스를 드롭하면 boot 런너와 동일하게 정상 envelope 응답.

jdk8-javax lane 도 동일 절차, JDK 17 로 Tomcat 9 기동 (Tomcat 9 는 런타임 JDK 8+ 모두 호환, HSQLDB 2.7.3 가 class file 55.0 이라 JDK 11+ 필요).

### 9-MVC.6 PR 머지 이력 (MVC lane)

| PR | 제목 | 핵심 |
|---|---|---|
| #19 | feat(mvc-jdk17-jakarta): canonical Spring MVC war runner (jakarta lane) | 패키지 + WAR 골격 |
| #20 | feat(mvc-jdk8-javax): canonical Spring MVC war runner (javax lane) | 패키지 + WAR 골격 (Spring 5.3.39) |
| #21 | refactor(mvc-jdk17-jakarta): convert to XML-driven Spring MVC config | `@Configuration` 6개 → web.xml + context-*.xml |
| #22 | refactor(mvc-jdk8-javax): convert to XML-driven Spring MVC config | 동일 (javax lane) |
| #23 | fix(mvc-jdk17-jakarta): schema separator + EtcProperty bean (Tomcat 10 unblock) | XML 변환 누락 2건 — 위 9-MVC.4 #1, #2 |

---

## 10. 다음 작업 시 빠른 체크리스트

신규 엔드포인트 추가:
1. [ ] xfdl `transaction()` 호출의 `inDataset` / `outDataset` 이름 메모
2. [ ] Section 3 에서 가장 가까운 패턴 (3.1 ~ 3.8) 복사
3. [ ] mapper interface + XML 한 쌍 추가. SQL 재사용은 `<sql>` + `<include>`
4. [ ] service interface + impl 한 쌍
5. [ ] `mvn -o clean package` (둘 다 lane)
6. [ ] `curl -X POST` smoke
7. [ ] PR per file commit (CLAUDE.md 규칙), 커밋 즉시 push

신규 런너 (mvc-*, egov*-*, webflux-*) 추가:
1. [ ] skill `rejectedCombinations` 에서 status 확인
2. [ ] 본 cookbook Section 2 (4.2 layout), Section 5 (yml) 그대로 적용
3. [ ] lane 에 맞는 1st-party 모듈 좌표 선택 (Section 1)
4. [ ] xeni/xapi import prefix 가 jakarta/javax 어디인지 확인 (Section 1 표)
5. [ ] 빌드 + smoke 후 skill `rejectedCombinations` 에서 해당 조합 해제

---

_Last updated: 2026-05-12 (mvc-jdk17-jakarta / mvc-jdk8-javax XML config WAR 검증 완료 — Tomcat 10/9 parity)_
