# runner: egov4-mvc-jdk8-javax

eGov 4.x + Spring MVC 5.x WAR runner (javax lane, JDK 8). Deployable to Tomcat 9.

| Key            | Value                                                  |
|----------------|--------------------------------------------------------|
| JDK            | 1.8                                                    |
| servletApi     | javax (4.0.1)                                          |
| framework      | egov-mvc (Spring 5.3.39 + RTE 4.x environment)         |
| egovframework  | 4.x                                                    |
| packaging      | war                                                    |
| servlet container | Tomcat 9                                            |
| run command    | mvn -o clean package → deploy `target/uiadapter.war`   |

## Build & Run

```bash
export JAVA_HOME=/c/AppStudio/jdk/jdk-8
mvn -o clean package
# target/uiadapter.war 생성
# Tomcat 9 webapps/ 에 deploy
# → http://localhost:8080/uiadapter/select_datalist.do
```

## What's layered on top of `mvc-jdk8-javax`

This runner takes the validated `mvc-jdk8-javax` canonical baseline (WAR + 7 context XMLs +
DispatcherServlet `action` mapping `*.do` + 11 controllers / 8 domains / 7 mappers) and
layers a **minimal eGov 4 environment**:

- `src/main/java/egovframework/com/config/EgovConfigAppProperties.java`
  — `@Configuration` that loads `globals.properties` via `@PropertySource`.
  No `EgovPropertyServiceImpl` bean (dead in scaffold, would abort startup with
  `NoSuchMessageException` on `message-fdl`).
- `src/main/resources/globals.properties`
  — `Globals.OsType`, `Globals.DbType`, `Globals.MainPage`, `Globals.fileStorePath`.
- `src/main/resources/egovframework/message/com/message-common_{ko,en}.properties`
  — eGov canonical i18n bundle path. Replaces the baseline `message/message-common*`.
- `context-common.xml`
  — added `<context:component-scan base-package="egovframework"/>` (picks up
  `EgovConfigAppProperties`) and re-pointed `messageSource` `basenames` to the eGov
  canonical path.
- `pom.xml`
  — added `egovframe` maven repository (eGov RTE artifacts). No RTE component dependency
  added yet (minimal scaffold).

Datasource / transaction / MyBatis remain XML-configured (mvc baseline pattern).
No eGov RTE component (`EgovAbstractDAO`, `IdGen`, etc.) is wired — minimal scaffold.

## Endpoints

Context path: `/uiadapter/` (WAR filename). DispatcherServlet `action` maps `*.do`.

11 controllers from the mvc-jdk8-javax baseline are retained verbatim (Board, Dept,
ExcelExport, File, Large, Login, Relay, Stream, TestData, Video, Wide).

## Lane coordinates

| Artifact | Version |
|----------|---------|
| `nexacroN-xapi` | 1.2.4-SNAPSHOT |
| `nexacroN-xeni` | 1.5.21-SNAPSHOT |
| `uiadapter-spring-core` | 1.4.19.2-SNAPSHOT |
| `uiadapter-spring-dataaccess` | 1.4.19-SNAPSHOT |
| `uiadapter-spring-excel` | 1.5.21-SNAPSHOT |

## Reference

- Canonical: https://gitlab.com/nexacron/egov-spring-framework/egov43x/egov43-nexacron
- Base scaffold: `samples/runners/mvc-jdk8-javax/`
- eGov 4 boot sibling: `samples/runners/egov4-boot-jdk8-javax/`
