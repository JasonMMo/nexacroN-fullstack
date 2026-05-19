# nexacroN-fullstack

End-to-end monorepo for Nexacro N v24 + Spring server stacks. Single `packageN` nxui front-end driving a matrix of 8 self-contained server runners that share an OpenAPI contract.

## Structure

| Folder | Purpose |
|---|---|
| `api-contract/` | OpenAPI spec + data-format reference (source of truth for the 14 `*.do` endpoints) |
| `core/` | Nexacro xapi / xeni / uiadapter coordinates (`javax` and `jakarta` variants — pulled from tobesoft Nexus, not vendored) |
| `nxui/` | `packageN` front-end project (xprj + xadl + xfdl forms, plus `nexacrolib/` UI assets) |
| `samples/seed-data/` | HSQL schema + seed data |
| `samples/shared-business*/` | Reference business code by lane (jdk8-javax / jdk17-jakarta / eGov 4 / eGov 5 / reactive) — kept for cookbook reference; runner samples are self-contained and do not import these |
| `samples/runners/` | 7 self-contained Spring/Boot/eGov/MVC/WebFlux runners with duplicated `controller/service/mapper/domain` trees + 1 reactive sibling outside the sync matrix |
| `scripts/` | Sync automation — `sync-from-canonical.sh`, `propagate-from-gitlab.sh`, `lane-transform.sh`, and per-runner `.exclude` filter files |
| `docs/sync-automation.md` | Operator guide for canonical → 6 siblings propagation flow |
| `docs/uiadapter-runner-cookbook.md` | jakarta/javax lane import cookbook + 7 verified endpoint patterns |
| `.github/workflows/runner-matrix.yml` | 7-runner build matrix CI (PR + nightly) |

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
