# Plan11 사용자 테스트 플랜 — RelayController(#14) 실배포 전 검증

**대상 엔드포인트:** `POST /uiadapter/relay/exim_exchange.do` (rules §2 #14)
**대상 레인:** `boot-jdk17-jakarta`, `boot-jdk8-javax`
**선결 조건:** Plan11 verification 보고서 PASS (`api-contract/PLAN11-VERIFICATION-2026-04-28.md`)

---

## 0. 사전 준비

### 0-1. 환경 확인

| 항목 | jakarta 레인 | javax 레인 |
|---|---|---|
| Java | JDK17 (`JAVA_HOME='/c/Program Files/Java/jdk-17'`) | JDK17 launcher (소스/타겟 1.8) |
| 포트 | `18080` | `18081` |
| context-path | `/uiadapter` | `/uiadapter` |
| Maven 명령 | `mvn -DskipTests spring-boot:run` (in 해당 runner 디렉토리) | 동일 |

### 0-2. 빌드 (각 레인 한 번씩)

```bash
cd /d/AI/workspace/nexacroN-fullstack/samples/runners/boot-jdk17-jakarta
JAVA_HOME='/c/Program Files/Java/jdk-17' mvn -DskipTests compile -q   # → BUILD SUCCESS

cd /d/AI/workspace/nexacroN-fullstack/samples/runners/boot-jdk8-javax
JAVA_HOME='/c/Program Files/Java/jdk-17' mvn -DskipTests compile -q   # → BUILD SUCCESS
```

---

## 1. 정적 검증 (코드 + 설정 일관성)

### TC-S1. 14-엔드포인트 path 보존 확인
**목적:** 양 레인 `RelayController`의 `@PostMapping` path/consumes/produces가 rules §2 §14 그대로인지.

```bash
cd /d/AI/workspace/nexacroN-fullstack
grep -nE 'relay/exim_exchange' samples/runners/boot-jdk17-jakarta/src/main/java/com/nexacro/fullstack/runner/boot17/controller/RelayController.java
grep -nE 'relay/exim_exchange' samples/runners/boot-jdk8-javax/src/main/java/com/nexacro/fullstack/runner/boot8/controller/RelayController.java
```

**기대:** 각 레인 1줄, `value = "/relay/exim_exchange.do"` 정확히 일치.

### TC-S2. 스텁 잔재 제거 확인
```bash
grep -r 'RELAY_STUB_OK' /d/AI/workspace/nexacroN-fullstack/samples/runners
```
**기대:** 0 hits.

### TC-S3. application.yml `nexacro.relay.exim` 블록 존재
```bash
grep -A4 'nexacro:' /d/AI/workspace/nexacroN-fullstack/samples/runners/boot-jdk17-jakarta/src/main/resources/application.yml | head -10
grep -A4 'nexacro:' /d/AI/workspace/nexacroN-fullstack/samples/runners/boot-jdk8-javax/src/main/resources/application.yml | head -10
```
**기대:** 양 레인 모두 `relay.exim.url` 키 존재 (값은 빈 상태가 기본).

---

## 2. 런타임 동적 검증 (각 레인별 4 케이스 × 2 = 총 8 테스트)

> **공통 호출 URL** — Tomcat context-path(`/uiadapter`) + controller mapping(`/uiadapter`) 이중 prefix:
> `http://localhost:<포트>/uiadapter/uiadapter/relay/exim_exchange.do`

### 시나리오 A — URL 미설정 → 503 + nexacro 에러 envelope

#### TC-R1 (jakarta, 18080)
1. `boot-jdk17-jakarta/src/main/resources/application.yml`에서 `nexacro.relay.exim.url:` 값을 비움 (빈 문자열).
2. 서버 기동: `cd boot-jdk17-jakarta && JAVA_HOME='/c/Program Files/Java/jdk-17' mvn spring-boot:run -DskipTests`
3. 호출:
   ```bash
   curl -sS -w "\n---HTTP=%{http_code} CT=%{content_type}\n" \
     -X POST -F field_a=hello -F field_b=world \
     http://localhost:18080/uiadapter/uiadapter/relay/exim_exchange.do
   ```
4. **기대:**
   - HTTP `503`
   - Content-Type `application/json`
   - 본문 (raw, 따옴표로 감싸지지 않음):
     ```json
     {"version":"1.0","Parameters":[{"id":"ErrorCode","value":-1},{"id":"ErrorMsg","value":"EXIM relay url not configured"}],"Datasets":[]}
     ```
5. **체크포인트 (CRITICAL):** 본문이 `"eyJ2ZXJ..."` 같은 base64 JSON-string이면 **FAIL**. raw JSON이 그대로 떠야 PASS.

#### TC-R2 (javax, 18081) — TC-R1과 동일하되 포트 18081, javax 레인 yml 사용

---

### 시나리오 B — httpbin 통신 → 200 + 에코 본문 + Content-Type 전파

#### TC-R3 (jakarta)
1. yml의 `nexacro.relay.exim.url`을 `https://httpbin.org/post` 로 설정 후 재기동.
2. 호출:
   ```bash
   curl -sS -w "\n---HTTP=%{http_code} CT=%{content_type}\n" \
     -X POST -F field_a=hello -F field_b=world \
     http://localhost:18080/uiadapter/uiadapter/relay/exim_exchange.do
   ```
3. **기대:**
   - HTTP `200`
   - 본문에 `"form": { "field_a": "hello", "field_b": "world" }` 블록 포함 (httpbin 에코 패턴)
   - Content-Type은 httpbin이 응답한 것 (`application/json`) 그대로 전파
4. **체크포인트:** raw JSON 텍스트여야 함 — base64-wrapped string이면 FAIL.

#### TC-R4 (javax) — TC-R3과 동일, 포트 18081

---

### 시나리오 C — 파일 part 포함 (선택 검증, 실제 nexacro UI에 가까움)

#### TC-R5 (jakarta + httpbin)
```bash
echo "binary-test-payload-12345" > /tmp/relay-test.bin
curl -sS -w "\n---HTTP=%{http_code} CT=%{content_type}\n" \
  -X POST \
  -F "field_a=hello" \
  -F "attachment=@/tmp/relay-test.bin;type=application/octet-stream" \
  http://localhost:18080/uiadapter/uiadapter/relay/exim_exchange.do
```
**기대:** httpbin `files` 블록에 `attachment` 키와 base64 바이너리 (httpbin이 base64로 echo함 — 우리 서버가 그렇게 만든 게 아니라 upstream 응답을 그대로 전달한 것임) 포함. HTTP 200.

#### TC-R6 (javax) — TC-R5와 동일, 포트 18081

---

### 시나리오 D — 잘못된 URL → 503 + upstream call 실패 envelope

#### TC-R7 (jakarta)
1. yml의 url을 `http://localhost:1` 같은 도달 불가 주소로.
2. 호출 → **기대:** HTTP `503`, 본문 `ErrorCode: -2`, `ErrorMsg`에 `EXIM upstream call failed:` 접두사.

#### TC-R8 (javax) — TC-R7과 동일

---

## 3. 회귀 검증 (기존 13개 엔드포인트가 깨지지 않았는지)

```bash
for ep in dummyMethod.do invoke.do invokeDataset.do excel_export.do excel_import.do \
          fileDown.do fileUp.do fileDel.do fileList.do mock.do imageupload.do \
          ajaxBean.do test.do; do
  echo "--- $ep ---"
  curl -sS -o /dev/null -w "HTTP=%{http_code}\n" \
    -X POST -F a=b "http://localhost:18080/uiadapter/uiadapter/$ep"
done
```
**기대:** 모두 비-5xx (200 / 400 / 401 등 — Plan8 baseline과 동일). 5xx 발생 시 회귀.

javax 레인 18081에서도 동일 반복.

---

## 4. 로그 확인 (INFO 레벨 1줄/요청)

서버 콘솔에서 각 호출 직후 다음 패턴이 보여야 함:
```
[RelayService] Relaying 2 part(s) [field_a, field_b] to URL: https://httpbin.org/post
[RelayService] Upstream responded with status: 200 OK
```
또는 (URL 미설정 시):
```
[RelayService] nexacro.relay.exim.url is not configured — returning 503
```

> **민감정보 점검:** body 내용이 로그에 찍히면 안 됨. part **이름**과 **개수**, upstream **status** 만 로깅.

---

## 5. 문서/이력 검증

| 항목 | 위치 | 기대 |
|---|---|---|
| Plan11 verification 리포트 | `nexacroN-fullstack/api-contract/PLAN11-VERIFICATION-2026-04-28.md` | 존재, 10섹션, 4/4 PASS 표기 |
| todo.md Plan11 closure | `nexacro-claude-skills/tasks/todo.md` | "Current status (2026-04-28, plan11 closure)" 섹션 존재 |
| GitHub 동기 | `nexacroN-fullstack` main, `nexacro-claude-skills` master | `git status` clean, `git log origin/<branch>..HEAD` empty |

---

## 6. PASS / FAIL 기준

| 카테고리 | PASS 조건 |
|---|---|
| 빌드 | 양 레인 BUILD SUCCESS (0-2 step) |
| 정적 (TC-S1~S3) | 모두 기대값 일치 |
| 런타임 시나리오 A (TC-R1, R2) | 양 레인 503 + raw JSON envelope |
| 런타임 시나리오 B (TC-R3, R4) | 양 레인 200 + httpbin 에코 raw 본문 + base64 wrap 없음 |
| 런타임 시나리오 D (TC-R7, R8) | 양 레인 503 + ErrorCode=-2 |
| 회귀 (Section 3) | 기존 13 엔드포인트 비-5xx 유지 |
| 로그 (Section 4) | 본문 미노출, status/part 메타만 노출 |
| 시나리오 C (TC-R5, R6) | (선택) 200 + files 블록 |

**Critical 실패 신호:**
- 본문이 `"eyJ..."` base64 JSON-string으로 떨어짐 → 컨버터 우회 fix가 회귀한 것. `RelayService`의 `getOutputStream().write()` 경로 재확인 필요.
- 503에서 Content-Type이 `application/octet-stream`으로 떨어짐 → fallback 로직 분기 오류.
- 회귀로 기존 엔드포인트 5xx → Plan11이 다른 컨트롤러 컨텍스트를 깬 것 (이론상 발생 불가, 발견 시 즉시 보고).

---

## 7. 사후 정리

테스트 완료 후 양 레인 yml의 `nexacro.relay.exim.url`은 다시 빈 값으로 복원 (PR/배포 기본값 = 미설정 → 503 fallback). 실제 EXIM endpoint는 운영 환경 변수/외부 설정으로 주입.

---

## 8. 시간 견적

| 단계 | 예상 소요 |
|---|---|
| 빌드 (양 레인) | 2~3분 |
| 정적 검증 | 1분 |
| 런타임 A+B (4 테스트) | 5~7분 (서버 재기동 2회 포함) |
| 런타임 C+D (4 테스트) | 5분 |
| 회귀 | 3분 |
| 로그/문서 검증 | 2분 |
| **합계** | **약 20~25분** |

---

**시작 권장 순서:** Section 0 (빌드) → 1 (정적) → **2 시나리오 A** (가장 결정적인 base64 회귀 감지) → 2 시나리오 B → 3 (회귀) → 나머지.
