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

## Plan 1 status

Placeholder. Runner body comes in Plan 2.
