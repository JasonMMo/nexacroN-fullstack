# core — Nexacro transport / Excel / UI-adapter modules

This directory holds the three Nexacro-side Spring integration modules:

- **xapi** — transport (client ↔ server dataset envelope serialization)
- **xeni** — Excel import / export over the xapi envelope
- **uiadapter** — Spring MVC / annotation surface that consumer apps depend on
  (`@ParamDataSet`, `@ParamVariable`, `NexacroFileHandler`, view resolvers, …)

Each module has a **javax** variant (Spring 5 / Boot 2 / servlet-api 4) and a
**jakarta** variant (Spring 6 / Boot 3 / servlet-api 6). The two variants are
not drop-in compatible — they differ in the `javax.servlet.*` vs
`jakarta.servlet.*` package import and in a handful of MVC SPI extension points
that moved between Spring 5 and 6.

## What ships here (v1.8.2)

```
core/
├── libs/
│   └── jakarta/
│       ├── nexacroN-xapi-jakarta-1.2.4-20260312.005603-1.jar       520 KB
│       └── nexacroN-xeni-jakarta-1.5.21-20260312.012638-1.jar      202 KB
│
└── uiadapter-javax/
    ├── pom.xml                                     (parent: uiadapter-spring 1.0.0)
    ├── uiadapter-spring-core/          — 65 .java — MVC surface
    ├── uiadapter-spring-dataaccess/    — 25 .java — DAO helpers (ibatis / jdbc / mybatis)
    └── uiadapter-spring-excel/         —  2 .java — Excel servlet + extensions
```

Total: 2 binary jars (~710 KB) + 102 Java source files under `uiadapter-javax/`.

### Provenance

- `libs/jakarta/*.jar` — verbatim copy from `asis/xapi/` and `asis/xeni/`
  build-deploy trees. SNAPSHOT timestamp `20260312` matches the binary
  currently in production use by the jakarta-example runner in the asis
  workspace.
- `uiadapter-javax/` — verbatim copy of
  `nexacro-webflux/asis/uiadapter/uiadapter-spring/` (Spring 5 / javax lane
  reference implementation). IDE metadata (`.idea/`, `*.iml`) and generated
  javadoc (`doc/`) were removed; the Maven source tree is intact.

## What does NOT ship here — and why

| Missing piece | Why | How to restore |
|---|---|---|
| `libs/javax/nexacroN-xapi-javax-*.jar` | Not present in this workspace. The javax lane on `asis/` consumes xapi via pre-built binaries from tobesoft's private Nexus. | Pull from `https://mangosteen.tobesoft.co.kr/repository/nexacro-releases/` with valid credentials. |
| `libs/javax/nexacroN-xeni-javax-*.jar` | Same as above — tobesoft Nexus only. | Same. |
| `uiadapter-jakarta/` | The jakarta lane's `uiadapter-spring` source tree is not in this workspace. The jakarta-example runner loads it as a binary dependency from tobesoft Nexus. | Pull the published `uiadapter-spring-*-jakarta.jar` artefacts from tobesoft Nexus, or port `uiadapter-javax/` sources to jakarta (mechanical: `javax.servlet.*` → `jakarta.servlet.*`, a handful of Spring 5 → 6 SPI shifts). |

This asymmetry is **not accidental**: the two proven runners already compile
and run without these missing pieces because they ship the nexacro envelope
surface **inline** inside their `shared-business-*` business trees
(`com.nexacro.fullstack.business.xapi.*`,
`com.nexacro.fullstack.business.uiadapter.*`). The `core/` contents here are
provided for consumers who want to **replace** those inline classes with the
official first-party modules — that swap is not required for the runner to
boot.

## How runners consume `core/`

### `samples/runners/boot-jdk17-jakarta`

The jakarta runner can pick up the local jars via `systemPath` (see
`samples/runners/boot-jdk17-jakarta/pom.xml`):

```xml
<dependency>
  <groupId>com.nexacro</groupId>
  <artifactId>nexacroN-xapi-jakarta</artifactId>
  <version>1.2.4-SNAPSHOT</version>
  <scope>system</scope>
  <systemPath>${project.basedir}/../../../core/libs/jakarta/nexacroN-xapi-jakarta-1.2.4-20260312.005603-1.jar</systemPath>
  <optional>true</optional>
</dependency>
```

`<optional>true</optional>` — the runner's inline business classes remain the
source of record; these dependencies are declared so consumers can pick them
up when they want to migrate off the inline surface.

### `samples/runners/boot-jdk8-javax`

The javax runner can pick up `uiadapter-javax` as a Maven module reference.
The xapi / xeni gap remains — document it in the runner README and let the
consumer decide whether to pull from tobesoft Nexus.

## Build path for consumers who want the real thing

```
# 1. Install tobesoft-published artefacts into local repo
mvn install:install-file -Dfile=nexacroN-xapi-javax-1.x.x.jar ...
mvn install:install-file -Dfile=nexacroN-xeni-javax-1.x.x.jar ...

# 2. Swap <scope>system</scope> in runner pom.xml for <scope>compile</scope>
# 3. Delete the inline classes under shared-business-*/src/main/java/com/nexacro/fullstack/business/xapi|uiadapter
```

## Versioning

Jar filenames include the tobesoft-Nexus timestamp suffix
(`-20260312.005603-1`). These are SNAPSHOT builds; update them by copying the
newer timestamped jar into `libs/jakarta/` and bumping the `systemPath`
reference in any runner pom that pins a specific jar.

## See also

- `../samples/runners/boot-jdk17-jakarta/pom.xml` — jakarta wiring
- `../samples/runners/boot-jdk8-javax/pom.xml`    — javax wiring
- `../samples/shared-business/jdk17-jakarta/`     — inline xapi/uiadapter classes
- `../samples/shared-business/jdk8-javax/`        — inline xapi/uiadapter classes
