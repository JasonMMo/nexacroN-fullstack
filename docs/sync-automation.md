# 동기화 자동화 — 오퍼레이터 가이드

GitLab 캐노니컬에 변경이 반영된 뒤 `samples/runners/` 아래 7개 러너 샘플을 어떻게 동기화하는지, 그리고 `scripts/` 아래 두 스크립트로 어떻게 손 포팅 없이 처리하는지를 설명합니다.

- `scripts/sync-from-canonical.sh` — 모노레포 캐노니컬(`boot-jdk17-jakarta`)을 한 개의 형제 러너로 전파.
- `scripts/propagate-from-gitlab.sh` — GitLab → 모노레포 → 6개 형제까지 전체 흐름을 한 번에 실행.
- `scripts/lane-transform.sh` — `jakarta → javax` import 재작성기 (대상 이름에 `javax`가 포함되면 `sync-from-canonical.sh`가 자동 호출).
- `scripts/runner-config/<target>.exclude` — 러너별 rsync 필터 규칙 (`-` 제외, `P` 삭제 보호).

PR 및 야간 빌드에서 7개 러너를 모두 컴파일하는 매트릭스 CI(`.github/workflows/runner-matrix.yml`)가 최종 게이트입니다.

---

## 1. TL;DR — 세 가지 작업 흐름

**A. 수정이 이미 모노레포 캐노니컬에 들어가 있고, 형제만 전파.**

```bash
scripts/propagate-from-gitlab.sh --skip-stage1
# 또는 러너 단위로:
scripts/sync-from-canonical.sh boot-jdk8-javax
scripts/sync-from-canonical.sh mvc-jdk17-jakarta
scripts/sync-from-canonical.sh mvc-jdk8-javax
scripts/sync-from-canonical.sh egov5-boot-jdk17-jakarta
scripts/sync-from-canonical.sh egov4-boot-jdk8-javax
scripts/sync-from-canonical.sh egov4-mvc-jdk8-javax
```

**B. GitLab에 수정이 들어왔고, 모노레포로 미러링 + 전파까지.**

```bash
scripts/propagate-from-gitlab.sh /path/to/gitlab-nexacron
```

**C. 미리보기만 (쓰기·컴파일 없음).**

```bash
scripts/propagate-from-gitlab.sh /path/to/gitlab-nexacron --dry-run --no-compile
```

실제 실행 뒤: `git status && git diff --stat samples/runners` → 검토 → 커밋 → 푸시 → 매트릭스 CI가 머지를 게이팅합니다.

---

## 2. 토폴로지와 SoT(Source of Truth)

```
GitLab 캐노니컬 (상위 SoT, Boot + jakarta + Java 17)
        │  scripts/propagate-from-gitlab.sh  (Stage 1: 미러링)
        ▼
samples/runners/boot-jdk17-jakarta/   ← 모노레포 캐노니컬
        │  scripts/sync-from-canonical.sh   (Stage 2: 형제별)
        ├──► boot-jdk8-javax             (Boot, JAR, javax — lane-transform 적용)
        ├──► mvc-jdk17-jakarta           (MVC, WAR, jakarta — Application.java + config/ 제외)
        ├──► mvc-jdk8-javax              (MVC, WAR, javax — WAR 규칙 + lane-transform)
        ├──► egov5-boot-jdk17-jakarta    (Boot + eGov 5, JAR — egovframework 보호)
        ├──► egov4-boot-jdk8-javax       (Boot + eGov 4, JAR — eGov 규칙 + lane-transform)
        └──► egov4-mvc-jdk8-javax        (MVC + eGov 4, WAR — WAR + eGov + lane-transform)
```

범위 밖: `webflux-jdk17-jakarta`는 전파 매트릭스에 포함되지 않습니다. 절대 동기화하지 마세요.

전파 대상 트리는 아래뿐입니다 (목록에 없는 것은 어떤 타깃에서도 건드리지 않습니다):

| 캐노니컬 위치 | 타깃 위치 | 비고 |
|---|---|---|
| `src/main/java/com/nexacro/uiadapter/**` | 동일 경로 | 러너별 제외 규칙을 뺀 전체 미러링 |
| `src/main/resources/data.sql` | 동일 경로 | 단일 파일 |
| `src/main/resources/schema.sql` | 동일 경로 | 단일 파일 |
| `src/main/resources/mybatis/**` | 동일 경로 | 디렉터리 |
| `src/main/resources/static/**` | 동일 경로 | 디렉터리 |

그 외 `src/main/resources/` 아래의 모든 파일(`application.yml`, `application.properties`, `xeni.properties`, `NexacroN_server_license.xml`, `logback*.xml`, `log4j2*.xml`)과 `pom.xml`, `src/main/webapp/`, `src/main/java/egovframework/`, `src/main/resources/egovframework/`, `src/main/resources/spring/`, `src/main/resources/message/`는 러너별로 관리됩니다.

---

## 3. 사전 준비

| 도구 | 용도 | 없을 때 동작 |
|---|---|---|
| `bash` 4 이상 | 모든 스크립트 | 필수 |
| `mvn` | Stage 2 컴파일 게이트 | `--no-compile`로 건너뛸 수 있음 |
| `rsync` | 빠른 동기화 경로 | 자동으로 `cp + diff` 폴백 사용 |
| `find`, `diff`, `sed`, `cp`, `mkdir` | 항상 사용 | POSIX 기본 |

**Windows / Git Bash 참고.** `mvn`이 `PATH`에 없으면(IDE 내장 Maven만 설치된 경우가 흔함) `~/bin/mvn`에 다음 심을 만드세요.

```bash
#!/usr/bin/env bash
exec "/c/Path/To/apache-maven-3.9.9/bin/mvn.cmd" "$@"
```

`chmod +x ~/bin/mvn` 후 `~/bin`이 `PATH`에 있는지 확인합니다. 이 설정이 없으면 `sync-from-canonical.sh`는 `mvn not found — skipping compile gate` 경고만 남기고 컴파일 게이트를 건너뛰며, 회귀 검출은 매트릭스 CI에 의존하게 됩니다.

---

## 4. `scripts/sync-from-canonical.sh` — 레퍼런스

```
Usage: scripts/sync-from-canonical.sh <target-runner> [--dry-run] [--no-compile]
```

| 플래그 | 효과 |
|---|---|
| `--dry-run` | 계획을 **stderr**로 출력, 파일 변경 없음, 컴파일 없음. stdout은 마지막 요약 줄 전용으로 비워둠 (호출 측이 `$(...)`로 캡처할 수 있도록). |
| `--no-compile` | `mvn -o -q -DskipTests compile` 게이트를 건너뜀. |
| (플래그 없음) | 실제 동기화 + 오프라인 컴파일. 오프라인 실패 시 온라인 컴파일로 재시도 (예: 첫 실행, 로컬 캐시 비어있음). 컴파일 실패 시 비정상 종료. |

타깃: `boot-jdk8-javax`, `mvc-jdk17-jakarta`, `mvc-jdk8-javax`, `egov5-boot-jdk17-jakarta`, `egov4-boot-jdk8-javax`, `egov4-mvc-jdk8-javax`.
캐노니컬 자체(`boot-jdk17-jakarta`)를 인자로 주면 명시적 에러로 거부됩니다.

종료 요약 (stdout 한 줄):

```
[sync-from-canonical] target=<runner> files-changed=<N> compile=<status>
```

`compile`은 `OK`, `OK (online)`, `skipped`, `skipped (mvn not found)` 중 하나입니다. 그 외 값이 나오면 스크립트가 이전 단계에서 비정상 종료한 것입니다.

### 동작 순서

1. `SOURCE=samples/runners/boot-jdk17-jakarta`, `DEST=samples/runners/<target>`, `EXCLUDE_FILE=scripts/runner-config/<target>.exclude` 경로 해석. 모두 존재해야 함.
2. 러너별 `.exclude` 파일을 두 리스트로 분리해서 읽음: `TARGET_EXCLUDES`(`-` 규칙), `TARGET_PROTECTS`(`P` 규칙).
3. 먼저 `src/main/java/com/nexacro/uiadapter/**`를 동기화한 뒤, 리소스 4개(`data.sql`, `schema.sql`, `mybatis`, `static`)를 순회. 각 항목마다:
   - `rsync` 있는 경우: 공통 제외/보호 쌍과 `--filter=merge <exclude-file>`을 결합해 `rsync -av --delete-after` 실행. 변경 수는 출력의 `>` 라인 수로 계산.
   - `rsync` 없는 경우(`cp_sync`): 소스 트리를 순회하며 제외 경로를 건너뛰고, `diff -q`로 다른 파일만 복사. 그 다음 타깃 트리를 순회하며 소스에 없고 제외/보호 대상도 아닌 파일을 삭제.
4. 타깃 이름에 `javax`가 포함되면 `scripts/lane-transform.sh "$DEST/src/main/java"` 실행 (자카르타 import를 javax로 in-place 재작성, `*.java`만, 멱등).
5. `mvn`을 `PATH`에서 먼저 찾고, 없으면 `~/AppData/Local/Programs/IntelliJ IDEA Ultimate/plugins/maven/lib/maven3/bin/mvn`와 `/c/Users/mo/AppData/...` 폴백 경로 시도. 오프라인 컴파일 → 온라인 컴파일 순으로 실행.

### dry-run 출력 규칙

`--dry-run` 상태에서 스크립트는 stdout을 요약 한 줄 전용으로 유지해야 합니다 — `propagate-from-gitlab.sh`의 Stage 2(및 향후 `C=$(do_sync ...)` 형태로 호출하는 모든 호출자)가 변경 수를 stdout에서 읽기 때문입니다. 모든 계획 라인(`[dry-run] copy: …`, `[dry-run] delete: …`)은 **stderr**로 보냅니다.

rsync 경로에서도 동일합니다 (rsync 자체의 dry-run 출력은 stdout으로 나가지만 `grep -c`가 소비함). 결과적으로 어떤 백엔드가 실행되든 사용자에게 보이는 stdout 요약은 한 줄입니다.

---

## 5. `scripts/propagate-from-gitlab.sh` — 레퍼런스

```
Usage:
  scripts/propagate-from-gitlab.sh <gitlab-repo-path> [options]
  scripts/propagate-from-gitlab.sh --skip-stage1 [options]
```

| 플래그 | 효과 |
|---|---|
| `--dry-run` | 두 스테이지 모두 계획만. |
| `--no-compile` | Stage 2로 전달; 컴파일 게이트 건너뜀. |
| `--skip-stage1` | GitLab은 이미 미러링됨; 캐노니컬 → 형제만 전파. |
| `--skip-stage2` | GitLab → 캐노니컬만 미러링; 형제는 건드리지 않음. |
| `--only <runner>` | Stage 2를 한 러너로 제한 (Stage 1은 별도 skip하지 않는 한 계속 실행). |
| `-h`, `--help` | 스크립트 헤더 블록 출력. |

### Stage 1 — GitLab → 모노레포 캐노니컬

- 소스: `<gitlab-repo>/src/main/java/com/nexacro/uiadapter`와 리소스 화이트리스트 4개(`data.sql`, `schema.sql`, `mybatis`, `static`).
- 대상: 이 저장소의 `samples/runners/boot-jdk17-jakarta/`.
- Stage 1이 **절대 덮어쓰지 않는** 파일: `pom.xml`, `application.yml`, `application.properties`, `xeni.properties`, `NexacroN_server_license.xml`, `logback*.xml`, `log4j2*.xml` — 모노레포 소유로 수동 관리.
- `rsync`가 있으면 `rsync -a --delete-after`, 없으면 Stage 2와 동일한 `cp + diff` 순회.

### Stage 2 — 캐노니컬 → 6개 형제

`ALL_SIBLINGS` 배열(또는 `--only`로 지정한 단일 러너)마다 `scripts/sync-from-canonical.sh <runner>`를 전달된 플래그와 함께 호출합니다. 첫 번째 비정상 종료에서 루프를 멈춥니다 — 해당 러너의 실패가 오퍼레이터가 점검해야 한다는 신호입니다.

### 추천 명령

```bash
# 미리보기만 — 언제든 안전.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron --dry-run --no-compile

# 전체 전파 (기본).
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron

# GitLab 이미 수동 미러링됨 — 형제만 재동기화.
scripts/propagate-from-gitlab.sh --skip-stage1

# 한 형제만 (예: 수정 후 재시도).
scripts/propagate-from-gitlab.sh --skip-stage1 --only egov4-mvc-jdk8-javax

# GitLab만 미러링 — 형제는 건드리지 않음.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron --skip-stage2
```

스크립트 마지막 블록에서 추천 git/gh 후속 명령을 직접 출력하므로 매뉴얼을 다시 펴볼 필요가 없습니다.

---

## 6. 러너별 제외 규칙 레퍼런스

모든 타깃은 `sync-from-canonical.sh` 내부에서 적용되는 공통 제외 목록을 공유합니다 (`.exclude` 파일에는 없음):

```
pom.xml
application.yml
application.properties
xeni.properties
NexacroN_server_license.xml
logback*.xml
log4j2*.xml
```

basename으로 매칭되며 `--delete-after`로부터도 보호됩니다.

`.exclude` 파일 문법 (rsync 필터):

- `- <path>` — 동기화에서 제외 (복사 안 됨; 타깃에 있으면 삭제됨).
- `P <path>` — `--delete-after` 보호 (소스에 없어도 타깃에 살아남음).
- `#`으로 시작하는 줄과 빈 줄은 무시됨.
- 경로는 러너 루트 기준 상대 경로.

| 러너 | 제외 (`-`) | 보호 (`P`) |
|---|---|---|
| `boot-jdk8-javax` | (공통만) | — |
| `mvc-jdk17-jakarta` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/` |
| `mvc-jdk8-javax` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/` |
| `egov5-boot-jdk17-jakarta` | `Application.java` | `java/egovframework/`, `resources/egovframework/` |
| `egov4-boot-jdk8-javax` | `Application.java` | `java/egovframework/`, `resources/egovframework/` |
| `egov4-mvc-jdk8-javax` | `Application.java`, `config/` | `webapp/`, `resources/spring/`, `resources/message/`, `java/egovframework/`, `resources/egovframework/` |

근거:

- **`Application.java`** 는 WAR 러너(Boot 엔트리 포인트 없음)와 eGov Boot 러너(커스텀 `@ComponentScan({"com.nexacro.uiadapter","egovframework"})`를 캐노니컬의 평범한 `@SpringBootApplication`이 덮어쓰면 안 됨)에서 제외.
- **`config/`** 는 MVC/WAR 러너에서 제외 — XML 전용 Spring 설정(`webapp/WEB-INF/`, `resources/spring/`)을 쓰므로 `uiadapter/config/` 패키지 자체가 없음.
- **`egovframework/`** 는 보호 — 캐노니컬 트리에 대응이 없는 eGov 스캐폴딩이 `--delete-after`로 삭제되지 않도록.
- **`webapp/`** 는 보호 — WAR 러너만 소유하는 `WEB-INF/web.xml`, `dispatcher-servlet.xml` 등이 캐노니컬에 없기 때문.

---

## 7. Lane transform (`scripts/lane-transform.sh`)

타깃 이름에 `javax`가 포함되면 자동 실행. 세 개의 `sed -E` 패턴, 모두 `^import` 앵커:

| From | To |
|---|---|
| `import jakarta.servlet.` | `import javax.servlet.` |
| `import com.nexacro.uiadapter.jakarta.core.` | `import com.nexacro.uiadapter.spring.core.` |
| `import com.nexacro.uiadapter.jakarta.` | `import com.nexacro.uiadapter.spring.` |

순서가 중요합니다 — 더 구체적인 `…uiadapter.jakarta.core.` 규칙이 일반 폴백 `…uiadapter.jakarta.` 보다 먼저 실행되어야 합니다. 그렇지 않으면 둘 다 `…uiadapter.spring.`로 재작성되어 `.core` 세그먼트를 잃습니다.

이 변환은 멱등합니다 — 이미 변환된 트리에서 재실행해도 변화 없음. `*.java` 파일만 건드리고, XML과 properties는 절대 재작성하지 않습니다.

캐노니컬에 새로운 `jakarta.*` 패키지 계열(예: `jakarta.validation`, `jakarta.persistence`)이 도입되면 이 스크립트에 대응 패턴을 추가합니다. 그 전까지는 **선제적으로 패턴을 추가하지 마세요** — 추가하는 정규식 하나하나가 잠재적인 멱등성 위협입니다.

---

## 8. 일상 작업 흐름

### 8.1 GitLab 커밋 하나를 가져오기

```bash
# 1. 작업용 GitLab 클론에 pull.
cd /d/AI/workspace/gitlab-nexacron && git pull && cd -

# 2. 이 저장소에 어떤 변경이 들어올지 미리보기.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron --dry-run --no-compile

# 3. 실제 실행.
scripts/propagate-from-gitlab.sh /d/AI/workspace/gitlab-nexacron

# 4. 검토, 커밋, 푸시.
git status
git diff --stat samples/runners
git add samples/runners
git commit -m "sync: propagate <one-line summary> from GitLab canonical"
git push -u origin HEAD
gh pr create --fill
```

### 8.2 한 형제만 수동 수정 후 재동기화

```bash
scripts/sync-from-canonical.sh egov4-mvc-jdk8-javax --no-compile
# diff 확인
mvn -f samples/runners/egov4-mvc-jdk8-javax -DskipTests compile
```

`.exclude` 규칙을 다듬는 동안에는 `--no-compile`로 컴파일 비용을 미루고 diff부터 확인하면 편합니다.

### 8.3 수정을 모노레포 캐노니컬에 직접 작성

GitLab이 아니라 이 저장소의 `samples/runners/boot-jdk17-jakarta/`를 직접 편집한다면 Stage 1을 건너뜁니다:

```bash
# 여기에서 캐노니컬 편집...
scripts/propagate-from-gitlab.sh --skip-stage1
```

캐노니컬 변경은 별도로 GitLab에도 수동 포팅해야 합니다 — `boot-jdk17-jakarta/`가 로컬 SoT이긴 하지만, 코드 기준으로 이 저장소는 GitLab의 하위 스트림입니다.

### 8.4 커밋 전 멱등성 검증

```bash
scripts/propagate-from-gitlab.sh --skip-stage1
scripts/propagate-from-gitlab.sh --skip-stage1   # 두 번째 실행
git diff --stat samples/runners                  # 비어 있어야 함
```

두 번째 실행에서 diff가 비어 있지 않으면 어딘가에 `P` 보호 규칙이 빠져 있다는 신호입니다 — §9 참고.

---

## 9. 트러블슈팅

**`ERROR: exclude config not found: scripts/runner-config/<target>.exclude`**
파일명이 러너 디렉터리 이름과 정확히 일치해야 합니다. 내용이 비어 있어도 파일은 만들어두세요 (빈 파일은 "공통 규칙만" 의미).

**동기화 직후 `mvn compile` 실패 (javax 타깃)**
lane transform이 패키지를 놓친 것입니다. `grep -rn 'import jakarta\.' samples/runners/<runner>/src/main/java`로 검색해서 남은 hit가 있으면 sed 구멍. `lane-transform.sh`에 패턴을 추가하고 재실행.

**`WARNING: mvn not found — skipping compile gate`**
스크립트가 `PATH`와 IDE 폴백 경로에서 `mvn`을 찾지 못한 것. `~/bin/mvn` 심(§3 참고)을 만들거나 매트릭스 CI 게이트에 의존하세요.

**멱등성 깨짐 — 같은 스크립트를 재실행하면 새 diff가 발생**
타깃에만 존재하는 파일이 `P` 보호 규칙에 없어서 `--delete-after`로 삭제됩니다. 첫 실행 후 `git status`에서 의도치 않은 삭제를 찾고, 해당 파일을 커버하는 `P` 규칙을 추가한 다음 §8.4를 재실행.

**WAR 또는 eGov 러너에서 `Application.java`가 덮어써짐**
exclude 파일에 `- Application.java`가 빠져 있음. 줄이 있는지, rsync 소스 루트 기준 상대 경로가 맞는지 확인.

**eGov 스캐폴딩이 사라짐**
`P src/main/java/egovframework/` 또는 `P src/main/resources/egovframework/` 규칙이 빠졌거나 오타. 파일 복원 (`git checkout -- samples/runners/<runner>/src/main/java/egovframework`) 후 규칙 추가.

**WAR 러너에서 `webapp/WEB-INF/...`가 삭제됨**
위와 같은 원인 — `P src/main/webapp/`이 빠진 것. WAR 전용 콘텐츠는 형제에만 있고 캐노니컬엔 없습니다.

**dry-run 도중 `[dry-run] copy: ...: command not found` 발생**
커밋 `82ae9ad`에서 수정된 버그입니다 — `cp_sync`가 dry-run 계획 라인을 stdout으로 흘려서 호출 측이 파일 카운트로 캡처해버렸습니다. 체크아웃에 해당 커밋이 포함되어 있는지 확인하세요.

**오프라인 컴파일 중 `Cannot access central`**
Maven 로컬 캐시가 비어 있는 첫 실행에서 발생. 스크립트가 자동으로 온라인으로 재시도합니다. 진짜 오프라인이라면 `--no-compile`로 넘기고 CI에 게이트를 맡깁니다.

**Stage 1이 "GitLab repo missing src/main/java/com/nexacro/uiadapter"라고 보고**
전달한 경로가 GitLab 저장소 루트가 아니거나 레포 레이아웃이 변경된 것. `ls <gitlab-path>/src/main/java/com/nexacro/uiadapter`로 확인.

---

## 10. 새 러너 추가하기

1. `samples/runners/<new-runner>/`를 캐노니컬 레이아웃대로 생성.
2. `scripts/runner-config/<new-runner>.exclude`에 러너 고유 규칙 작성 (가장 유사한 기존 파일에서 출발 — WAR-vs-JAR과 eGov-vs-plain이 두 축).
3. `scripts/propagate-from-gitlab.sh`의 `ALL_SIBLINGS`에 `<new-runner>` 추가.
4. `.github/workflows/runner-matrix.yml`에 매트릭스 엔트리 추가 (`runner`, `jdk`, `smoke`).
5. dry-run: `scripts/sync-from-canonical.sh <new-runner> --dry-run --no-compile` → stderr의 계획 검토.
6. 실제 실행: `scripts/sync-from-canonical.sh <new-runner>` → `compile=OK` (또는 `OK (online)`) 확인.
7. PR 열기 전에 §8.4 (멱등성 검증) 반복.
8. (선택) 짝 저장소 `nexacro-claude-skills`의 `references/repo-map.md`에 러너 등록.

---

## 11. 이 자동화가 **하지 않는** 일

- `pom.xml`은 동기화하지 **않습니다** — 각 러너가 lane별 의존성을 따로 핀.
- 러너 고유 리소스(`application.yml`/`.properties`, `xeni.properties`, `NexacroN_server_license.xml`, 로그 설정, `webapp/`, `spring/`, `message/`, `egovframework/`)는 동기화하지 **않습니다**.
- PR을 자동으로 열지 **않습니다** (Tier 3, 유보).
- GitLab을 모노레포로 자동 리베이스/머지하지 **않습니다** — Stage 1은 미러링하려는 커밋에 이미 로컬 GitLab 클론이 맞춰져 있다고 가정.
- `webflux-jdk17-jakarta`로 전파하지 **않습니다**.
- `xapi`/`xeni`/`uiadapter` 버전 업은 하지 **않습니다** — 별도 매니페스트로 관리.
