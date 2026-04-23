# shared-business-reactive / jdk17-mybatis

WebFlux controllers only — **reuses** `shared-business/jdk17-jakarta` for
service + mapper layers.

Consumed by runner:
- `webflux-jdk17-jakarta`

This is the "Option A" reactive strategy from §2.3 of the design spec:
wrap synchronous MyBatis mappers with `Mono.fromCallable(...)` +
`.subscribeOn(Schedulers.boundedElastic())`. No R2DBC.

## Planned contents (Plan 2)

```
jdk17-mybatis/
├── pom.xml                        # depends on shared-business/jdk17-jakarta
└── src/main/java/com/nexacro/sample/reactive/
    ├── controller/                # 15 RouterFunction / @Controller reactive handlers
    ├── filter/                    # WebFilter (multipart bypass)
    └── config/                    # WebFluxConfigurer (base-path, resources)
```

## Why this tree exists

The webflux runner cannot use servlet-based controllers from
`shared-business/jdk17-jakarta`, but CAN use its Service + Mapper layer.
Splitting the thin reactive controller layer into its own tree keeps the
non-reactive tree un-polluted with `Mono<>` / `Flux<>` types.

## Plan 1 status

Placeholder. Source comes in Plan 2.
