# runner: boot-jdk17-jakarta  ★ default

Thin Spring Boot 3 entry-point.

| Key            | Value                                   |
|----------------|-----------------------------------------|
| JDK            | 17                                       |
| servletApi     | jakarta                                  |
| framework      | spring-boot                              |
| spring         | 6.x                                      |
| spring-boot    | 3.x                                      |
| packaging      | jar                                      |
| run command    | `mvn spring-boot:run`                    |
| business tree  | `samples/shared-business/jdk17-jakarta`  |

## Planned contents (Plan 2)

```
boot-jdk17-jakarta/
├── pom.xml                            # imports shared-business/jdk17-jakarta
├── src/main/java/com/nexacro/sample/Application.java
└── src/main/resources/
    ├── application.yml               # server.port=8080, context-path=/uiadapter
    └── static/                        # deploy target for nexacro build output
```

## Runtime license

xapi performs a classpath lookup for the literal filename
`NexacroN_server_license.xml` at startup (and again on the first
`PlatformTransaction.checkLicense()` call inside
`HttpPlatformRequest.receiveData()`). Drop your operator-issued license at:

```
src/main/resources/NexacroN_server_license.xml
```

The file is gitignored repo-wide — never commit a TOBESOFT-issued
runtime license to a public repository. Without it, `POST` requests to
any `*.do` endpoint fail with a license error before the controller
runs.

## Plan 1 status

Placeholder. Runner body comes in Plan 2.
