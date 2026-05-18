# Sync Automation — Operator Guide

This guide explains how to keep the seven runner samples under `samples/runners/` in sync after a change lands in the GitLab canonical, and how to use the two scripts under `scripts/` to do it without hand-porting.

- `scripts/sync-from-canonical.sh` — propagate the monorepo canonical (`boot-jdk17-jakarta`) to one sibling runner.
- `scripts/propagate-from-gitlab.sh` — wraps the full GitLab → monorepo → 6 siblings flow.
- `scripts/lane-transform.sh` — `jakarta → javax` import rewriter (invoked automatically by `sync-from-canonical.sh` for `*javax*` targets).
- `scripts/runner-config/<target>.exclude` — per-target rsync filter rules (`-` exclude, `P` protect-from-delete).

The runner matrix CI (`.github/workflows/runner-matrix.yml`) builds all 7 runners on PR and nightly; it is the final gate.

---

## 1. TL;DR — three workflows

**A. Fix already merged to monorepo canonical, propagate to siblings only.**

```bash
scripts/propagate-from-gitlab.sh --skip-stage1
# or one runner at a time:
scripts/sync-from-canonical.sh boot-jdk8-javax
scripts/sync-from-canonical.sh mvc-jdk17-jakarta
scripts/sync-from-canonical.sh mvc-jdk8-javax
scripts/sync-from-canonical.sh egov5-boot-jdk17-jakarta
scripts/sync-from-canonical.sh egov4-boot-jdk8-javax
scripts/sync-from-canonical.sh egov4-mvc-jdk8-javax
```

**B. Fix landed in GitLab, mirror into monorepo + propagate.**

```bash
scripts/propagate-from-gitlab.sh /path/to/gitlab-nexacron
```

**C. Preview only (no writes, no compile).**

```bash
scripts/propagate-from-gitlab.sh /path/to/gitlab-nexacron --dry-run --no-compile
```

After a real run: `git status && git diff --stat samples/runners` → review → commit → push → matrix CI gates the merge.

---

## 2. Topology and source of truth

```
GitLab canonical (upstream SoT, Boot + jakarta + Java 17)
        │  scripts/propagate-from-gitlab.sh  (Stage 1: mirror)
        ▼
samples/runners/boot-jdk17-jakarta/   ← monorepo canonical
        │  scripts/sync-from-canonical.sh   (Stage 2: per sibling)
        ├──► boot-jdk8-javax             (Boot, JAR, javax — lane-transform applied)
        ├──► mvc-jdk17-jakarta           (MVC, WAR, jakarta — Application.java + config/ excluded)
        ├──► mvc-jdk8-javax              (MVC, WAR, javax — WAR rules + lane-transform)
        ├──► egov5-boot-jdk17-jakarta    (Boot + eGov 5, JAR — egovframework protected)
        ├──► egov4-boot-jdk8-javax       (Boot + eGov 4, JAR — eGov rules + lane-transform)
        └──► egov4-mvc-jdk8-javax        (MVC + eGov 4, WAR — WAR + eGov + lane-transform)
```

Out of scope: `webflux-jdk17-jakarta` is not part of the propagation matrix — never sync it.

Only the following trees are propagated (anything not listed is left untouched in every target):

| From canonical | To target | Note |
|---|---|---|
| `src/main/java/com/nexacro/uiadapter/**` | same path | Full mirror, minus per-target excludes |
| `src/main/resources/data.sql` | same path | Single file |
| `src/main/resources/schema.sql` | same path | Single file |
| `src/main/resources/mybatis/**` | same path | Directory |
| `src/main/resources/static/**` | same path | Directory |

Everything else under `src/main/resources/` (`application.yml`, `application.properties`, `xeni.properties`, `NexacroN_server_license.xml`, `logback*.xml`, `log4j2*.xml`) and `pom.xml`, `src/main/webapp/`, `src/main/java/egovframework/`, `src/main/resources/egovframework/`, `src/main/resources/spring/`, `src/main/resources/message/` are managed per-target.

---

## 3. Prerequisites

| Tool | Required for | Behavior if missing |
|---|---|---|
| `bash` 4+ | All scripts | Hard requirement |
| `mvn` | Stage 2 compile gate | Use `--no-compile` to skip |
| `rsync` | Faster sync path | Script falls back to a `cp + diff` loop automatically |
| `find`, `diff`, `sed`, `cp`, `mkdir` | Always | POSIX baseline |

**Windows / Git Bash note.** If `mvn` is not on `PATH` (common when only the IDE-bundled Maven is installed), create a shim at `~/bin/mvn`:

```bash
#!/usr/bin/env bash
exec "/c/Path/To/apache-maven-3.9.9/bin/mvn.cmd" "$@"
```

`chmod +x ~/bin/mvn` and make sure `~/bin` is on `PATH`. Without this, `sync-from-canonical.sh` will warn `mvn not found — skipping compile gate` and the matrix CI must catch any regression instead.

---

## 4. `scripts/sync-from-canonical.sh` — reference

```
Usage: scripts/sync-from-canonical.sh <target-runner> [--dry-run] [--no-compile]
```

| Flag | Effect |
|---|---|
| `--dry-run` | Print the plan to **stderr**; no files written; no compile. Stdout reserved for the final summary line so it can be captured by callers. |
| `--no-compile` | Skip the `mvn -o -q -DskipTests compile` gate. |
| (no flag) | Real sync + offline compile. Falls back to online compile if offline fails (e.g., first run, empty local cache). Non-zero compile exits non-zero. |

Targets: `boot-jdk8-javax`, `mvc-jdk17-jakarta`, `mvc-jdk8-javax`, `egov5-boot-jdk17-jakarta`, `egov4-boot-jdk8-javax`, `egov4-mvc-jdk8-javax`.
Passing `boot-jdk17-jakarta` (the canonical itself) is rejected with an explicit error.

Exit summary (single line on stdout):

```
[sync-from-canonical] target=<runner> files-changed=<N> compile=<status>
```

`compile` is one of `OK`, `OK (online)`, `skipped`, `skipped (mvn not found)`. Any other state means the script aborted earlier with a non-zero exit.

### Behavior, step by step

1. Resolve `SOURCE=samples/runners/boot-jdk17-jakarta`, `DEST=samples/runners/<target>`, `EXCLUDE_FILE=scripts/runner-config/<target>.exclude`. Each must exist.
2. Read the per-target `.exclude` file into two lists: `TARGET_EXCLUDES` (`-` rules) and `TARGET_PROTECTS` (`P` rules).
3. Sync `src/main/java/com/nexacro/uiadapter/**` first, then loop over the four resource entries (`data.sql`, `schema.sql`, `mybatis`, `static`). For each:
   - With `rsync`: build a filter list combining the common-exclude/protect pairs and `--filter=merge <exclude-file>`, run `rsync -av --delete-after`. Count is taken from the `>` lines in rsync output.
   - Without `rsync` (`cp_sync`): walk the source tree, skip excluded paths, copy any file whose target differs (`diff -q`); then walk the target tree and remove anything not in the source unless it is excluded or protected.
4. If the target name contains `javax`, run `scripts/lane-transform.sh "$DEST/src/main/java"` to rewrite jakarta imports to javax in place (`*.java` only, idempotent).
5. Locate `mvn` via `PATH`, then fall back to `~/AppData/Local/Programs/IntelliJ IDEA Ultimate/plugins/maven/lib/maven3/bin/mvn` and `/c/Users/mo/AppData/...` for IDE-only setups. Run offline compile first, online compile as a fallback.

### Dry-run output discipline

Under `--dry-run` the script must keep stdout reserved for the single summary line — Stage 2 in `propagate-from-gitlab.sh` (and any future caller that does `C=$(do_sync ...)`) reads the count from stdout. All plan lines (`[dry-run] copy: …`, `[dry-run] delete: …`) are sent to **stderr**.

This is also true for the rsync path (rsync's own dry-run output stays on stdout but is consumed by `grep -c`), so the user-visible summary on stdout is one line regardless of which backend ran.

---

## 5. `scripts/propagate-from-gitlab.sh` — reference

```
Usage:
  scripts/propagate-from-gitlab.sh <gitlab-repo-path> [options]
  scripts/propagate-from-gitlab.sh --skip-stage1 [options]
```

| Flag | Effect |
|---|---|
| `--dry-run` | Plan only (both stages). |
| `--no-compile` | Forward to Stage 2; skip the compile gate. |
| `--skip-stage1` | GitLab is already mirrored; only propagate canonical → siblings. |
| `--skip-stage2` | Mirror GitLab → canonical only; don't touch siblings. |
| `--only <runner>` | Restrict Stage 2 to one sibling (still runs Stage 1 unless skipped). |
| `-h`, `--help` | Print the header block from the script. |

### Stage 1 — GitLab → monorepo canonical

- Source: `<gitlab-repo>/src/main/java/com/nexacro/uiadapter` plus the four whitelisted resource entries (`data.sql`, `schema.sql`, `mybatis`, `static`).
- Destination: `samples/runners/boot-jdk17-jakarta/` in this repo.
- Files **never** overwritten by Stage 1: `pom.xml`, `application.yml`, `application.properties`, `xeni.properties`, `NexacroN_server_license.xml`, `logback*.xml`, `log4j2*.xml` — these are monorepo-owned and managed by hand.
- Uses `rsync -a --delete-after` when available; otherwise the same `cp + diff` walk as Stage 2.

### Stage 2 — canonical → 6 siblings

For each runner in `ALL_SIBLINGS` (or just the one passed via `--only`), invokes `scripts/sync-from-canonical.sh <runner>` with the forwarded flags. The first non-zero exit stops the loop — that runner's failure is the operator's signal to inspect before re-running.

### Recommended commands

```bash
# Preview only — safe to run any time.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron --dry-run --no-compile

# Full propagation (default).
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron

# GitLab already mirrored manually — only resync siblings.
scripts/propagate-from-gitlab.sh --skip-stage1

# Only one sibling (e.g., to retry after a fix).
scripts/propagate-from-gitlab.sh --skip-stage1 --only egov4-mvc-jdk8-javax

# Mirror GitLab only — don't touch siblings.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron --skip-stage2
```

The script's final block prints the suggested git/gh follow-up so you don't have to look it up.

---

## 6. Per-target exclusion reference

All targets share a baseline exclude list applied inside `sync-from-canonical.sh` (not in the `.exclude` files):

```
pom.xml
application.yml
application.properties
xeni.properties
NexacroN_server_license.xml
logback*.xml
log4j2*.xml
```

These are matched on basename and also protected from `--delete-after`.

`.exclude` file syntax (rsync filter):

- `- <path>` — exclude from sync (won't be copied; will be deleted from target if it exists there).
- `P <path>` — protect from `--delete-after` (target-only file survives a sync that doesn't include it from source).
- Lines starting with `#` and blank lines are ignored.
- Paths are relative to the runner root.

| Runner | Excluded (`-`) | Protected (`P`) |
|---|---|---|
| `boot-jdk8-javax` | (baseline only) | — |
| `mvc-jdk17-jakarta` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/` |
| `mvc-jdk8-javax` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/` |
| `egov5-boot-jdk17-jakarta` | `Application.java` | `java/egovframework/`, `resources/egovframework/` |
| `egov4-boot-jdk8-javax` | `Application.java` | `java/egovframework/`, `resources/egovframework/` |
| `egov4-mvc-jdk8-javax` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/`, `java/egovframework/`, `resources/egovframework/` |

Rationale:

- **`Application.java`** is excluded for WAR runners (they have no Boot entry point) and for eGov Boot runners (they have a custom `@ComponentScan({"com.nexacro.uiadapter","egovframework"})` that the canonical's plain `@SpringBootApplication` would clobber).
- **`config/`** is excluded for MVC/WAR runners because those use XML-only Spring config under `webapp/WEB-INF/` and `resources/spring/` — they have no `uiadapter/config/` package.
- **`egovframework/`** is protected so that `--delete-after` cannot remove eGov scaffolding that has no equivalent in the canonical tree.
- **`webapp/`** is protected because WAR runners own `WEB-INF/web.xml`, `dispatcher-servlet.xml`, etc., that the canonical tree does not provide.

---

## 7. Lane transform (`scripts/lane-transform.sh`)

Triggered automatically when the target name contains `javax`. Three `sed -E` patterns, all anchored on `^import`:

| From | To |
|---|---|
| `import jakarta.servlet.` | `import javax.servlet.` |
| `import com.nexacro.uiadapter.jakarta.core.` | `import com.nexacro.uiadapter.spring.core.` |
| `import com.nexacro.uiadapter.jakarta.` | `import com.nexacro.uiadapter.spring.` |

Order matters — the more specific `…uiadapter.jakarta.core.` rule must run before the generic `…uiadapter.jakarta.` fallback, otherwise both rewrite to `…uiadapter.spring.` and you lose the `.core` segment.

The transform is idempotent: re-running on an already-transformed tree is a no-op. Only `*.java` files are touched; XML and properties are never rewritten.

If the canonical introduces a new `jakarta.*` package family (e.g. `jakarta.validation`, `jakarta.persistence`), extend this script with a matching pattern. Until then, do **not** add patterns speculatively — every regex you add is a potential idempotency hazard.

---

## 8. Day-to-day workflows

### 8.1 Cherry-pick a single GitLab commit

```bash
# 1. Pull GitLab into a working clone.
cd /d/AI/workspace/gitlab-nexacron && git pull && cd -

# 2. Preview what would change in this repo.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron --dry-run --no-compile

# 3. Real run.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron

# 4. Inspect, commit, push.
git status
git diff --stat samples/runners
git add samples/runners
git commit -m "sync: propagate <one-line summary> from GitLab canonical"
git push -u origin HEAD
gh pr create --fill
```

### 8.2 Re-sync a single sibling after fixing it manually

```bash
scripts/sync-from-canonical.sh egov4-mvc-jdk8-javax --no-compile
# inspect changes
mvn -f samples/runners/egov4-mvc-jdk8-javax -DskipTests compile
```

`--no-compile` is useful while iterating on `.exclude` rules so you can see the diff before paying the compile cost.

### 8.3 Author a fix directly in the monorepo canonical

If you're editing `samples/runners/boot-jdk17-jakarta/` in this repo instead of GitLab, skip Stage 1:

```bash
# Edit canonical here…
scripts/propagate-from-gitlab.sh --skip-stage1
```

The canonical change also needs to be ported back to GitLab manually — this repo is downstream of GitLab for code, even though `boot-jdk17-jakarta/` is the local SoT.

### 8.4 Verify idempotency before committing

```bash
scripts/propagate-from-gitlab.sh --skip-stage1
scripts/propagate-from-gitlab.sh --skip-stage1   # second run
git diff --stat samples/runners                  # should be empty
```

A non-empty diff on the second run means a `P` protect rule is missing somewhere — see §9.

---

## 9. Troubleshooting

**`ERROR: exclude config not found: scripts/runner-config/<target>.exclude`**
Filename must match the runner directory exactly. Create the file, even if empty (a blank file means "baseline rules only").

**`mvn compile` fails immediately after sync (javax target)**
Lane transform missed a package. Run `grep -rn 'import jakarta\.' samples/runners/<runner>/src/main/java` — any hit is a sed gap. Add a pattern to `lane-transform.sh` and re-run.

**`WARNING: mvn not found — skipping compile gate`**
The script could not locate `mvn` on `PATH` or in the IDE fallback paths. Add a shim at `~/bin/mvn` (see §3) or rely on the matrix CI for the gate.

**Idempotency broken — re-running the same script produces a new diff**
A file that exists only in the target is being deleted by `--delete-after` because it's not in a `P` protect rule. Find the unintended deletion (`git status` after first run), add a `P` rule covering it, and re-test §8.4.

**`Application.java` was overwritten in a WAR or eGov runner**
The exclude file is missing `- Application.java`. Verify the line is present and that the rsync source root matches the rule's relative path.

**eGov scaffolding disappeared**
The `P src/main/java/egovframework/` and/or `P src/main/resources/egovframework/` rules are missing or misspelled. Restore the files (`git checkout -- samples/runners/<runner>/src/main/java/egovframework`) and add the rules.

**`webapp/WEB-INF/...` got deleted in a WAR runner**
Same root cause as above for `P src/main/webapp/`. WAR-owned content lives only in the sibling, never in the canonical.

**`[dry-run] copy: ...: command not found` during a dry-run**
This is the bug fixed in commit `82ae9ad` — `cp_sync` was leaking dry-run plan lines onto stdout, which the caller captured as the file count. Make sure your checkout includes that commit.

**`Cannot access central` during offline compile**
First run with an empty local Maven cache. The script automatically retries online; if you're truly offline, pass `--no-compile` and let CI do the gate.

**Stage 1 reports "GitLab repo missing src/main/java/com/nexacro/uiadapter"**
The path you passed is not the GitLab repo root, or the repo layout has changed. Confirm with `ls <gitlab-path>/src/main/java/com/nexacro/uiadapter`.

---

## 10. How to add a new runner

1. Create `samples/runners/<new-runner>/` following the canonical layout.
2. Add `scripts/runner-config/<new-runner>.exclude` with target-specific rules (start from the closest existing file — WAR-vs-JAR and eGov-vs-plain are the two axes).
3. Add `<new-runner>` to `ALL_SIBLINGS` in `scripts/propagate-from-gitlab.sh`.
4. Add a matrix entry to `.github/workflows/runner-matrix.yml` (`runner`, `jdk`, `smoke`).
5. Dry-run: `scripts/sync-from-canonical.sh <new-runner> --dry-run --no-compile` → review the plan on stderr.
6. Real run: `scripts/sync-from-canonical.sh <new-runner>` → verify `compile=OK` (or `OK (online)`).
7. Repeat §8.4 (idempotency check) before opening the PR.
8. Optional: register the runner in `references/repo-map.md` of the `nexacro-claude-skills` companion repo.

---

## 11. What this automation does **not** do

- Does **not** sync `pom.xml` — every runner pins its own lane dependencies.
- Does **not** sync runner-specific resources (`application.yml`/`.properties`, `xeni.properties`, `NexacroN_server_license.xml`, log configs, `webapp/`, `spring/`, `message/`, `egovframework/`).
- Does **not** open PRs (Tier 3, deferred).
- Does **not** rebase or merge GitLab into the monorepo automatically — Stage 1 expects you to have a local GitLab clone already at the commit you want to mirror.
- Does **not** propagate to `webflux-jdk17-jakarta`.
- Does **not** bump `xapi`/`xeni`/`uiadapter` versions — that is a separate manifest tracked outside this script.
