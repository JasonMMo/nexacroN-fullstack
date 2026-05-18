# runner: mvc-jdk17-jakarta

Spring MVC 6.x WAR runner, deployable to Tomcat 10 / Jetty 11. This is the
jakarta-lane twin of `mvc-jdk8-javax`.

| Key                | Value                                              |
|--------------------|----------------------------------------------------|
| JDK                | 17                                                 |
| servletApi         | jakarta (6.0.0, provided)                          |
| framework          | spring-mvc                                         |
| spring             | 6.1.x (raw, no Boot BOM)                           |
| spring-boot        | n/a                                                |
| packaging          | war                                                |
| servlet container  | Tomcat 10 / Jetty 11                               |
| run command        | `mvn -o clean package` → deploy `target/uiadapter.war` |

## Build

```bash
mvn -o clean package
# Output: target/uiadapter.war
```

The runner is self-contained: no parent pom, no shared-business module.

## Deploy to Tomcat 10

1. `cp target/uiadapter.war $TOMCAT_HOME/webapps/`
2. Start Tomcat. Static UI: `http://localhost:8080/uiadapter/packageN/index.html`
3. Service: `POST http://localhost:8080/uiadapter/select_datalist.do`

Context path is determined by the WAR filename (`uiadapter.war` → `/uiadapter/`).

## Endpoints

| Path | Description |
|------|-------------|
| `/uiadapter/packageN/login.do` | Login info |
| `/uiadapter/packageN/board/select_datalist.do` | Board list |
| `/XExportImport.do` | Grid export/import (xeni) |

전체 14-endpoint 카탈로그는 [uiadapter-runner-cookbook.md](../../../docs/uiadapter-runner-cookbook.md) 참조.

## Key differences from `boot-jdk17-jakarta`

- No `spring-boot-*` dependencies — `web.xml` + XML application context wires Spring.
- `Application.java` 없음 (Boot 엔트리 포인트 사용 안 함).
- `src/main/java/com/nexacro/uiadapter/config/` 없음 — Spring 설정은 `src/main/webapp/WEB-INF/config/`와 `src/main/resources/spring/`의 XML에서 관리.
- `schema.sql` / `data.sql`을 `^^` separator로 로드하는 초기화 로직이 `WEB-INF/config/dataAccess-context.xml` 등 XML 측에 정의됨.
- `jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1`이 `provided` (Tomcat 10이 JSP 런타임 제공).
- Relay/file-storage는 `xeni.properties` + `application.properties`로 설정 (`application.yml` 사용 안 함).

## Lane coordinates

| Artifact | Version |
|----------|---------|
| `nexacroN-xapi-jakarta` | 1.2.4-SNAPSHOT |
| `nexacroN-xeni-jakarta` | 1.5.21-SNAPSHOT |
| `uiadapter-jakarta-core` | 1.0.27.1-SNAPSHOT |
| `uiadapter-jakarta-dataaccess` | 1.0.14-SNAPSHOT |
| `uiadapter-jakarta-excel` | 1.5.4.3-SNAPSHOT |

## Sync

캐노니컬(`boot-jdk17-jakarta`)에서 비즈니스 코드(`controller/service/mapper/domain`)와
`mybatis/`, `static/`, `data.sql`/`schema.sql`이 자동 전파됩니다. WAR 전용 자산
(`webapp/`, `resources/spring/`, `resources/message/`, `Application.java`, `config/` 패키지)은
보호되거나 제외됩니다. 자세한 내용은 [docs/sync-automation.md](../../../docs/sync-automation.md).

## Runtime license

xapi가 시작 시(그리고 `HttpPlatformRequest.receiveData()` 안의 첫
`PlatformTransaction.checkLicense()` 호출 시) classpath에서 `NexacroN_server_license.xml`
파일을 찾습니다. operator가 발급받은 라이선스를 아래 경로에 넣으세요.

```
src/main/resources/NexacroN_server_license.xml
```

이 파일은 저장소 전역 gitignore 대상입니다 — TOBESOFT 발급 운영 라이선스를 절대 public
저장소에 커밋하지 마세요. 없으면 모든 `*.do` 엔드포인트로의 POST 요청이 컨트롤러 진입
전에 license error로 실패합니다.
