# runner: egov5-boot-jdk17-jakarta

Minimal eGovFrame 5 scaffold aligned with the canonical `nexacron/egov5-boot-nexan` sample.
Extends the jakarta/Nexacro runner with eGov boot wiring (globals properties,
HikariCP, log4jdbc, message bundles) while keeping the lean eight-endpoint
transaction surface.

| Key         | Value                     |
|-------------|---------------------------|
| JDK         | 17                        |
| servletApi  | jakarta                   |
| framework   | egov-boot                 |
| spring-boot | 3.3.x                     |
| egovframe   | 5.0.0 (starter parent)    |
| packaging   | jar                       |
| run command | `mvn spring-boot:run`     |

## Build & Run

1. `cd samples/runners/egov5-boot-jdk17-jakarta`
2. `mvn -o clean package -DskipTests`
3. `mvn spring-boot:run`

Offline (`-o`) build mirrors CI. Drop the `-o` flag if the local Maven cache is
cold and dependencies need to be resolved online.

## Smoke Tests

The runner exposes the core Nexacro transaction contract (eight endpoints):

- `POST /uiadapter/login.do`
- `POST /uiadapter/logout.do`
- `POST /uiadapter/select_data_single.do`
- `POST /uiadapter/select_datalist.do`
- `POST /uiadapter/update_datalist_map.do`
- `POST /uiadapter/update_deptlist_map.do`
- `POST /uiadapter/sampleLargeData.do`
- `GET  /uiadapter/advancedDownloadFile.do`

Each endpoint responds with `NexacroResult` envelopes (or a file stream) and
maps 1:1 with the canonical Nexacro UI adapters. For download tests, drop a
sample payload under `samples/runners/egov5-boot-jdk17-jakarta/uploads/`
before invoking `/advancedDownloadFile.do`.

## References

- eGovFrame 5 boot starter parent: `org.egovframe.boot:egovframe-boot-starter-parent:5.0.0`
- Canonical sample: `gitlab.com/nexacron/egov5-spring-boot/jakarta/egov5-boot-nexan`
