# runner: webflux-jdk17-jakarta

Spring WebFlux 리액티브 런너 (jdk17 / jakarta / netty). 동기화 매트릭스 **밖**에 있는
독립 런너입니다 — `scripts/sync-from-canonical.sh`와 `scripts/propagate-from-gitlab.sh`는
이 런너를 절대 건드리지 않습니다.

| Key            | Value                                                     |
|----------------|-----------------------------------------------------------|
| JDK            | 17                                                        |
| servletApi     | jakarta (런타임에는 사용 안 함 — reactive netty)            |
| framework      | webflux                                                   |
| spring         | 6.x                                                       |
| spring-boot    | 3.x                                                       |
| packaging      | jar                                                       |
| run command    | `mvn -o spring-boot:run`                                  |
| sync 대상 여부 | ✗ (동기화 화이트리스트에서 제외)                          |

## Why WebFlux is excluded from sync

WebFlux는 servlet 기반 6개 런너와 컨트롤러 시그니처(`Mono`/`Flux`, `ServerHttpRequest` 등)
및 비즈니스 코드가 본질적으로 달라서 캐노니컬 `boot-jdk17-jakarta`에서 직접 전파할 수 없습니다.
이 런너는 수동으로 유지·관리됩니다.

## Special endpoint

WebFlux 전용 엔드포인트 `/reactive/exim_exchange.do` 구현 (multipart 인입 + chunked 응답).
servlet 런너에서는 같은 경로가 501을 반환합니다.

## Why no `webflux-jdk8`?

매트릭스에서 거부 — WebFlux는 spring6 / jakarta / jdk17+ 가 전제. 자세한 근거는 짝
플러그인의 `runner-selection-guide.md` 참고.
