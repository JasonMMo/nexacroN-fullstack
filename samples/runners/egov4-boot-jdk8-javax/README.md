# egov4-boot-jdk8-javax

Minimal eGovFrame 4 scaffold over the canonical Nexacro uiadapter pattern
(javax lane, JDK 8, Spring Boot 2.7).

| Spec              | Value                                                       |
|-------------------|-------------------------------------------------------------|
| JDK               | 8                                                           |
| Servlet API       | javax                                                       |
| Framework         | egov-boot (Spring Boot 2.7.x)                               |
| Spring            | 5.3.x (managed by SB 2.7 BOM)                               |
| eGov              | 4 lineage (legacy `egovframework.rte` 3.10.0)               |
| Packaging         | jar                                                         |
| Run command       | `mvn spring-boot:run`                                       |
| Mapper layout     | `com.nexacro.uiadapter.mapper`                              |
| DB                | HSQLDB in-memory (canonical `^^` separator)                 |

## Why no eGov 4 boot-starter-parent

No `org.egovframe.boot:egovframe-boot-starter-parent` artifact has been
published for the 4.x line — only 5.x exists on
`https://maven.egovframe.go.kr/maven`. To stay on the eGov 4 lineage this
runner pulls the legacy 3.10.0 RTE module
(`egovframework.rte:egovframework.rte.fdl.property:3.10.0`) directly and
imports the Spring Boot 2.7 BOM. The legacy module ships Spring 4.3.25
transitively; all `org.springframework:spring-*` artifacts are excluded so
SB 2.7's Spring 5.3 stack wins.

## Build & run

```bash
# JDK 8 source/target. HSQLDB 2.7.3 requires Java 11+ at runtime, so run
# with JAVA_HOME pointing at a JDK 11 or 17 install (compile with JDK 8).
export JAVA_HOME=/c/AppStudio/jdk/jdk-8

mvn -o clean package
mvn spring-boot:run
```

After startup:

- Static: `http://localhost:8080/uiadapter/packageN/index.html`
- Service: `POST http://localhost:8080/uiadapter/select_datalist.do`
- Large data streaming: `POST http://localhost:8080/uiadapter/sampleLargeData.do`

## Layout

```
src/main/
├── java/
│   ├── com/nexacro/uiadapter/        — canonical layout (config, controller, service, service/impl, mapper, domain)
│   └── egovframework/com/config/     — minimal eGov scaffold (5 EgovConfigApp* classes)
└── resources/
    ├── application.yml               — context-path /uiadapter, log4jdbc-wrapped HSQLDB
    ├── globals.properties            — Globals.OsType / DbType / MainPage / fileStorePath
    ├── schema.sql / data.sql         — HSQLDB seed, ^^ separator
    ├── mybatis/                      — sql-mapper-config.xml + mappers/*-mapper.xml
    └── egovframework/message/com/    — message-common_{ko,en}.properties
```

The `egovframework.com.config` package only provides 5 classes — properties
(`EgovPropertyServiceImpl` over `globals.properties`), datasource
(HikariCP + log4jdbc DriverSpy), MyBatis SqlSessionFactory,
DataSourceTransactionManager, and a ReloadableResourceBundleMessageSource.
No `egovframework.let.*` boilerplate is included — board / user / login /
file / schedule modules are intentionally out of scope for this minimal
scaffold.

## Runtime license

xapi performs a classpath lookup for the literal filename
`NexacroN_server_license.xml` at startup (and again on the first
`PlatformTransaction.checkLicense()` call inside
`HttpPlatformRequest.receiveData()`). Drop your operator-issued license
at:

```
src/main/resources/NexacroN_server_license.xml
```

The file is gitignored repo-wide — never commit a TOBESOFT-issued
runtime license to a public repository. Without it, `POST` requests to
any `*.do` endpoint fail with a license error before the controller
runs.

## Reference

- Canonical (jakarta sibling): https://gitlab.com/nexacron/egov5-spring-boot/jakarta/egov5-boot-nexan
- Sister runner (jakarta): `samples/runners/egov5-boot-jdk17-jakarta`
