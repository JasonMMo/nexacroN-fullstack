# runner: boot-jdk8-javax

`boot-jdk17-jakarta`의 javax-lane 쌍둥이 런너. Spring Boot 2.7, JDK 8, javax servlet.

| Key            | Value                                 |
|----------------|---------------------------------------|
| JDK            | 8                                     |
| servletApi     | javax (4.0.1, provided)               |
| framework      | spring-boot 2.7.x                     |
| spring         | 5.3.x (Boot 2.7 BOM)                  |
| packaging      | jar                                   |
| run command    | `mvn -o spring-boot:run`              |

## Build & Run

```bash
# HSQLDB 2.7.3은 런타임에 Java 11+가 필요하므로 JDK 11/17로 실행하되 source/target은 8.
export JAVA_HOME=/c/AppStudio/jdk/jdk-8
mvn -o clean package
mvn -o spring-boot:run
```

기동 후:

- Static UI: `http://localhost:8080/uiadapter/packageN/index.html`
- Service: `POST http://localhost:8080/uiadapter/select_datalist.do`

## Lane coordinates

| Artifact | Version |
|----------|---------|
| `nexacroN-xapi` | 1.2.4-SNAPSHOT |
| `nexacroN-xeni` | 1.5.21-SNAPSHOT |
| `uiadapter-spring-core` | 1.4.19.2-SNAPSHOT |
| `uiadapter-spring-dataaccess` | 1.4.19-SNAPSHOT |
| `uiadapter-spring-excel` | 1.5.21-SNAPSHOT |

`uiadapter.jakarta.{core,dao}` 대신 `uiadapter.spring.{core,dao}` 패키지를 사용한다는 점이
캐노니컬과의 핵심 차이입니다. import 매핑 표는
[docs/uiadapter-runner-cookbook.md](../../../docs/uiadapter-runner-cookbook.md) §1 참조.

## Sync

이 런너는 `boot-jdk17-jakarta` 캐노니컬에서 자동 전파됩니다.
`scripts/sync-from-canonical.sh boot-jdk8-javax`가 실행될 때 `*javax*` 패턴으로
`scripts/lane-transform.sh`가 자동 호출되어 jakarta import가 javax로 재작성됩니다.

이 런너에서 직접 코드를 수정하지 마세요 — 다음 동기화 때 `boot-jdk17-jakarta`에서
온 변경으로 덮어써집니다. 캐노니컬에서 수정 후 전파하는 흐름은
[docs/sync-automation.md](../../../docs/sync-automation.md).

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
