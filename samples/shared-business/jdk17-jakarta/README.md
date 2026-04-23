# shared-business / jdk17-jakarta

Plain Spring business tier for the **jakarta lane** (jdk17 / spring6).

Consumed by runners:
- `boot-jdk17-jakarta`
- `mvc-jdk17-jakarta`
- `webflux-jdk17-jakarta` (imports this + adds reactive controllers via
  `shared-business-reactive/jdk17-mybatis`)

## Planned contents (Plan 2)

```
jdk17-jakarta/
├── pom.xml                        # spring6 / jakarta.servlet 6
└── src/main/java/com/nexacro/sample/
    ├── controller/                # 15 REST endpoints (jakarta.servlet)
    ├── service/                   # business logic
    └── mapper/                    # MyBatis (mybatis-spring 3.x)
```

## Plan 1 status

Placeholder. Source comes in Plan 2.
