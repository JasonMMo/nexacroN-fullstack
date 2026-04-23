# nexacroN-fullstack

End-to-end monorepo for Nexacro N v24 + Spring server stacks. Single `packageN` nxui front-end driving a matrix of 8 server runners that share an OpenAPI contract.

## Structure

| Folder | Purpose |
|---|---|
| `api-contract/` | OpenAPI spec + data-format reference (source of truth for 15 endpoints) |
| `core/` | Nexacro xapi / xeni / uiadapter in `javax` and `jakarta` variants |
| `nxui/` | `packageN` front-end project (xprj + xadl + xfdl forms) |
| `samples/seed-data/` | HSQL schema + seed data |
| `samples/shared-business/` | Plain Spring business code (jdk8-javax + jdk17-jakarta) |
| `samples/shared-business-egov4/` | eGov 4.x business code (jdk8-javax) |
| `samples/shared-business-egov5/` | eGov 5.x business code (jdk17-jakarta) |
| `samples/shared-business-reactive/` | WebFlux controller layer (reuses `shared-business/jdk17-jakarta` service/mapper) |
| `samples/runners/` | 8 thin entry-point modules (POM + Application/web.xml only) |

## Runner matrix

| Runner | JDK | servlet-api | Framework | eGov |
|---|---|---|---|---|
| `boot-jdk17-jakarta` | 17 | jakarta | Spring Boot 3 | — |
| `boot-jdk8-javax` | 8 | javax | Spring Boot 2 | — |
| `mvc-jdk17-jakarta` | 17 | jakarta | Spring 6 (war) | — |
| `mvc-jdk8-javax` | 8 | javax | Spring 5 (war) | — |
| `egov5-boot-jdk17-jakarta` | 17 | jakarta | Boot 3 | 5.x |
| `egov4-boot-jdk8-javax` | 8 | javax | Boot 2 | 4.x |
| `egov4-mvc-jdk8-javax` | 8 | javax | Spring 5 (war) | 4.x |
| `webflux-jdk17-jakarta` | 17 | jakarta | WebFlux (Boot 3) | — |

## Quick start (via Claude Code)

```
/plugin marketplace add JasonMMo/nexacro-claude-skills
/plugin install nexacro-fullstack-starter@nexacro-claude-skills
/nexacro-fullstack-starter
```

The skill will clone the runner(s) you select into your target directory.

## License

MIT — see `LICENSE`.
