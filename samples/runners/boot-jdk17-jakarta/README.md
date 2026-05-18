# runner: boot-jdk17-jakarta  ★ canonical

이 모노레포 내 **동기화 SoT(Source of Truth)** 런너. `scripts/sync-from-canonical.sh`가
6개 형제 런너로 비즈니스 코드를 전파하는 출발점입니다.

| Key            | Value                                   |
|----------------|-----------------------------------------|
| JDK            | 17                                      |
| servletApi     | jakarta (6.0.0, provided)               |
| framework      | spring-boot 3.3.x                       |
| spring         | 6.1.x                                   |
| packaging      | jar                                     |
| run command    | `mvn -o spring-boot:run`                |
| canonical role | ⭐ source for `sync-from-canonical.sh`   |

## Build & Run

```bash
mvn -o clean package
mvn -o spring-boot:run
```

오프라인(`-o`) 빌드는 CI와 동작이 같습니다. 로컬 Maven 캐시가 비어 있다면 `-o`를 빼고
처음 한 번은 온라인으로 받습니다.

후 기동:

- Static UI: `http://localhost:8080/uiadapter/packageN/index.html`
- Service: `POST http://localhost:8080/uiadapter/select_datalist.do`

## Layout

런너는 self-contained입니다 — parent pom 없음, shared-business 모듈 import 없음.
패키지 트리는 7개 런너 공통의 canonical layout을 따릅니다.

```
src/main/
├── java/com/nexacro/uiadapter/
│   ├── Application.java          @SpringBootApplication (plain, no @ComponentScan override)
│   ├── config/                   UiadapterWebMvcConfig, MyBatisConfig, WebConfig, RelayHttpConfig…
│   ├── controller/               @Controller + @RequestMapping("/*.do")
│   ├── service/                  interface
│   ├── service/impl/             *ServiceImpl @Service
│   ├── mapper/                   MyBatis @Mapper interfaces
│   └── domain/                   POJO/VO (NexacroBase 상속)
└── resources/
    ├── application.yml           context-path=/uiadapter, HSQLDB, mybatis path
    ├── xeni.properties           xeni 설정
    ├── schema.sql / data.sql     HSQLDB seed, '^^' separator
    ├── logback.xml
    ├── static/                   packageN 빌드 산출물 배포 위치
    └── mybatis/
        ├── sql-mapper-config.xml
        └── mappers/*-mapper.xml
```

엔드포인트 패턴과 사용 가능한 7개 컨트롤러 패턴은
[docs/uiadapter-runner-cookbook.md](../../../docs/uiadapter-runner-cookbook.md) 참조.

## Lane coordinates

| Artifact | Version |
|----------|---------|
| `nexacroN-xapi-jakarta` | 1.2.4-SNAPSHOT |
| `nexacroN-xeni-jakarta` | 1.5.21-SNAPSHOT |
| `uiadapter-jakarta-core` | 1.0.27.1-SNAPSHOT |
| `uiadapter-jakarta-dataaccess` | 1.0.14-SNAPSHOT |
| `uiadapter-jakarta-excel` | 1.5.4.3-SNAPSHOT |

## Sync — 이 런너가 캐노니컬인 이유

이 런너의 `src/main/java/com/nexacro/uiadapter/**`와 `src/main/resources/`의 일부 화이트리스트
(`data.sql`, `schema.sql`, `mybatis/`, `static/`)가 6개 형제 런너로 전파됩니다.

```bash
# 이 런너에서 수정한 뒤 다른 런너로 전파.
scripts/propagate-from-gitlab.sh --skip-stage1
```

자세한 흐름·플래그·트러블슈팅은 [docs/sync-automation.md](../../../docs/sync-automation.md).

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
