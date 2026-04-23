# runner: webflux-jdk17-jakarta

Spring WebFlux reactive runner (jdk17 / jakarta / netty).

| Key            | Value                                                     |
|----------------|-----------------------------------------------------------|
| JDK            | 17                                                         |
| servletApi     | jakarta (not used at runtime — reactive netty)             |
| framework      | webflux                                                    |
| spring         | 6.x                                                        |
| spring-boot    | 3.x                                                        |
| packaging      | jar                                                        |
| run command    | `mvn spring-boot:run`                                      |
| business trees | `samples/shared-business-reactive/jdk17-mybatis` (primary) |
|                | `samples/shared-business/jdk17-jakarta` (imported)         |

## Special endpoints

Implements the WebFlux-only `/reactive/exim_exchange.do` endpoint
(multipart ingest + chunked response). Servlet runners return 501.

## Plan 1 status

Placeholder. Runner body comes in Plan 2.

## Why no `webflux-jdk8`?

Rejected by the matrix — WebFlux requires spring6 / jakarta / jdk17+.
See the sibling plugin's `runner-selection-guide.md`.
