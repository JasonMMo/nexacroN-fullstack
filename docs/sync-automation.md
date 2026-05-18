# Sync Automation — Operator Guide

## TL;DR

The canonical source of truth is `samples/runners/boot-jdk17-jakarta/`. When a fix lands there, propagate it to each sibling in 3 steps:

```bash
# From repo root
scripts/sync-from-canonical.sh boot-jdk8-javax
scripts/sync-from-canonical.sh mvc-jdk17-jakarta
scripts/sync-from-canonical.sh mvc-jdk8-javax
scripts/sync-from-canonical.sh egov5-boot-jdk17-jakarta
scripts/sync-from-canonical.sh egov4-boot-jdk8-javax
scripts/sync-from-canonical.sh egov4-mvc-jdk8-javax
```

Each call: rsyncs Java sources + canonical resources → applies lane-transform (javax targets) → runs `mvn -o compile`. Non-zero compile = script exits non-zero. Push the resulting diff as a single PR.

---

## Topology

```
GitLab canonical (upstream SoT)
        │  git pull / manual port
        ▼
samples/runners/boot-jdk17-jakarta/   ← canonical in this repo
        │
        │  sync-from-canonical.sh
        ├──► boot-jdk8-javax          (lane-transform: jakarta→javax)
        ├──► mvc-jdk17-jakarta        (WAR; Application.java excluded)
        ├──► mvc-jdk8-javax           (WAR + lane-transform)
        ├──► egov5-boot-jdk17-jakarta (eGov 5; Application.java excluded)
        ├──► egov4-boot-jdk8-javax    (eGov 4 + lane-transform)
        └──► egov4-mvc-jdk8-javax     (WAR + eGov 4 + lane-transform)
```

`webflux-jdk17-jakarta` is out of scope — do not sync it.

---

## Per-target Exclusion Reference

| Runner | Exclude | Protect |
|---|---|---|
| `boot-jdk8-javax` | (none beyond common) | — |
| `mvc-jdk17-jakarta` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/` |
| `mvc-jdk8-javax` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/` |
| `egov5-boot-jdk17-jakarta` | `Application.java` | `java/egovframework/`, `resources/egovframework/` |
| `egov4-boot-jdk8-javax` | `Application.java` | `java/egovframework/`, `resources/egovframework/` |
| `egov4-mvc-jdk8-javax` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/`, `java/egovframework/`, `resources/egovframework/` |

**Common rules** (applied via in-script preamble, not in `.exclude` files):
`pom.xml`, `application.yml`, `application.properties`, `xeni.properties`, `NexacroN_server_license.xml`, `logback*.xml`, `log4j2*.xml` — all excluded AND protected.

---

## Troubleshooting Cookbook

**`rsync: exclude file not found`** — ensure `scripts/runner-config/<runner>.exclude` exists. Filename must exactly match the runner directory name.

**`mvn compile` fails after sync** — check lane-transform: run `grep -r 'import jakarta' samples/runners/<runner>/src/main/java` on a javax target. If hits remain, the sed pattern missed a sub-package — extend `lane-transform.sh`.

**Idempotency broken (re-run produces a diff)** — a `P` protect rule is missing. Add the relevant path to the runner's `.exclude` file.

**`Application.java` was clobbered in a WAR runner** — `Application.java` must appear in the exclude file as `- Application.java`. Verify the path relative to the rsync source (uiadapter/ root).

**eGov source disappeared** — the `P src/main/java/egovframework/` protect rule is missing or misspelled. rsync `--delete-after` removed it. Restore from git (`git checkout -- <file>`) then fix the exclude file.

**Offline Maven fails (`Cannot access central`)** — drop `-o` for local runs: edit the script temporarily or pass `--no-compile` and run `mvn compile` manually. The committed script keeps `-o`; CI is online.

---

## How to Add a New Runner

1. Create `samples/runners/<new-runner>/` following the canonical layout.
2. Add `scripts/runner-config/<new-runner>.exclude` with target-specific rules (see existing files for examples).
3. Add a matrix entry to `.github/workflows/runner-matrix.yml` (`runner`, `jdk`, `smoke`).
4. Run `scripts/sync-from-canonical.sh <new-runner> --no-compile` to do a dry check, then without `--no-compile`.
5. Open a PR; matrix CI will gate the build.
6. *(Optional)* Update `references/repo-map.md` in `nexacro-claude-skills` to register the new runner.
