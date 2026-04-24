# core — Nexacro first-party modules

Nexacro's `xapi`, `xeni`, and `uiadapter` modules are **not vendored here**.
They are pulled at build time from tobesoft's Nexus repository.

## Why the directory still exists

This directory used to ship a partial local snapshot (2 jakarta jars + the
Spring 5 `uiadapter-spring` source tree). That snapshot is gone as of
`v0.6.0-core-from-nexus`. Keeping a hand-copied snapshot in-tree had two
problems:

1. **Staleness** — a pinned SNAPSHOT jar is frozen at one timestamp, while
   the canonical Nexus artefact rolls forward.
2. **Asymmetry** — only the jakarta lane had jars; the javax xapi/xeni and
   jakarta uiadapter sources were never available in this workspace anyway.

The directory is kept so the sparse-checkout path in the
`nexacro-fullstack-starter` plugin still resolves, and so the
`core/README.md` is reachable as a jumping-off point.

## Where the real modules live

Tobesoft Nexus, snapshots repo (public, anonymous access):

```
https://mangosteen.tobesoft.co.kr/nexus/repository/tobesoft-snapshots/com/nexacro/<artifactId>/
```

### Jakarta lane (JDK 17 / Spring 6 / Boot 3)

| artifactId                        | default version pin       |
|-----------------------------------|---------------------------|
| `nexacroN-xapi-jakarta`           | `1.2.4-SNAPSHOT`          |
| `nexacroN-xeni-jakarta`           | `1.5.21-SNAPSHOT`         |
| `uiadapter-jakarta-core`          | `1.0.27.1-SNAPSHOT`       |
| `uiadapter-jakarta-dataaccess`    | `1.0.14-SNAPSHOT`         |
| `uiadapter-jakarta-excel`         | `1.5.4.3-SNAPSHOT`        |

### Javax lane (JDK 8 / Spring 5 / Boot 2)

| artifactId                        | default version pin       |
|-----------------------------------|---------------------------|
| `nexacroN-xapi`                   | `1.2.4-SNAPSHOT`          |
| `nexacroN-xeni`                   | `1.5.21-SNAPSHOT`         |
| `uiadapter-spring-core`           | `1.4.19.2-SNAPSHOT`       |
| `uiadapter-spring-dataaccess`     | `1.4.19-SNAPSHOT`         |
| `uiadapter-spring-excel`          | `1.5.21-SNAPSHOT`         |

## Configuration

Both the `<repository>` entry and the 10 version properties live in the
root [`pom.xml`](../pom.xml). Runners inherit everything and only list the
5 `<groupId>com.nexacro</groupId>` artifactIds they need — no versions,
no systemPath.

### No credentials needed

`mangosteen.tobesoft.co.kr/nexus/repository/tobesoft-snapshots/` is a
**public repository**. Both `maven-metadata.xml` reads and `.jar`
downloads are anonymous. `mvn compile` works with a stock
`~/.m2/settings.xml` — no `<server>` entry required.

## Bumping versions

Probe Nexus for the latest published version of any artefact:

```bash
curl -s https://mangosteen.tobesoft.co.kr/nexus/repository/tobesoft-snapshots/com/nexacro/<artifactId>/maven-metadata.xml \
  | grep -oE '<version>[^<]+' | sort -V | tail -3
```

The tobesoft-managed `<latest>` element in `maven-metadata.xml` is not
always trustworthy (older versions re-published can reset it). Always sort
the full `<versions>` list by semver and pick the highest.

Update the corresponding `<nexacro.*.version>` property in
[`../pom.xml`](../pom.xml); the runners pick it up automatically because
version is managed through the parent's `dependencyManagement`.

## Build verification

Both proven runners have been verified to compile with Nexus-resolved
nexacro dependencies:

- `samples/runners/boot-jdk17-jakarta` — `mvn compile` → BUILD SUCCESS
  (resolves 5 jakarta artefacts + transitive `nexacroN-xeni-compatible`)
- `samples/runners/boot-jdk8-javax` — `mvn compile` → BUILD SUCCESS
  (resolves 5 javax artefacts + transitive `nexacroN-xapi-main`,
  `nexacroN-xeni-compatible`)

## See also

- [`../pom.xml`](../pom.xml) — repository + version properties + `dependencyManagement`
- [`../samples/runners/boot-jdk17-jakarta/pom.xml`](../samples/runners/boot-jdk17-jakarta/pom.xml)
- [`../samples/runners/boot-jdk8-javax/pom.xml`](../samples/runners/boot-jdk8-javax/pom.xml)
