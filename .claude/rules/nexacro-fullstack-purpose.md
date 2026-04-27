# nexacro-fullstack / nexacro-fullstack-starter Project Rules

> **Authority:** This file is the canonical source of user requirements for `nexacro-fullstack` (monorepo) and `nexacro-fullstack-starter` (Claude Code plugin). All verification phases MUST check work against the rules below.

---

## 1. Purpose (목적)

`nexacro-fullstack` 모노레포와 `nexacro-fullstack-starter` 플러그인은 **넥사크로 UI ↔ Spring 백엔드 풀스택 개발**을 즉시 시작할 수 있도록 만든 표준 스타터다.

- **Dual-lane**: jakarta(JDK17, Spring Boot 3.3.x) / javax(JDK8, Spring Boot 2.7.x) 양쪽 모두 동일한 비즈니스 코드를 공유
- **GitLab 레거시 → GitHub 통합**: 흩어져 있던 nexacro 관련 모듈(uiadapter, xapi, xeni, nexacrolib)을 단일 모노레포로 통합
- **단일 표준 endpoint 계약**: 넥사크로 UI에서 transaction으로 호출하는 `*.do` URL과 1:1 일치

---

## 2. Authoritative 14-Endpoint Contract (사용자 사양)

**Authority chain:** `사용자 사양(이 문서) > openapi.yaml > 실제 구현`. 충돌 시 항상 위 항목이 이김.

모든 endpoint는 **`/uiadapter/<action>.do`** prefix를 따른다. 넥사크로 UI에서 transaction으로 호출하는 `*.do` 목록 기준.

| # | Method | Path | 용도 |
|---|--------|------|------|
| 1 | POST | `/uiadapter/login.do` | 로그인 |
| 2 | POST | `/uiadapter/logout.do` | 로그아웃 |
| 3 | POST | `/uiadapter/select_data_single.do` | 단건 select |
| 4 | POST | `/uiadapter/select_datalist.do` | 리스트 select |
| 5 | POST | `/uiadapter/update_datalist_map.do` | 리스트 insert/update/delete |
| 6 | POST | `/uiadapter/update_deptlist_map.do` | 부서 트리 갱신 |
| 7 | POST | `/uiadapter/advancedUploadFiles.do` | 다중 업로드 |
| 8 | POST | `/uiadapter/advancedDownloadFile.do` | 단일 다운로드 |
| 9 | POST | `/uiadapter/advancedDownloadList.do` | 다운로드 리스트 |
| 10 | POST | `/uiadapter/sampleLargeData.do` | 대용량 select |
| 11 | POST | `/uiadapter/sampleVideoStream.do` | 비디오 스트리밍 |
| 12 | POST | `/uiadapter/sampleTestData.do` | 테스트 데이터 |
| 13 | POST | `/uiadapter/sampleWideColumns.do` | 와이드 컬럼 |
| 14 | POST | `/uiadapter/relay/exim_exchange.do` | 외부 시스템 릴레이 |

> **현재 구현 상태(2026-04-27):** 5/14 일치. 9개 path 불일치 + 1개 누락. → Plan8에서 복구.

---

## 3. P0/P1 Compliance Criteria (지난주 사용자 지적사항)

검증 단계에서 아래 5개 항목을 반드시 체크:

| # | 항목 | 검증 방법 |
|---|------|-----------|
| P0-1 | 14-endpoint 사양 준수 | jakarta/javax 양쪽에서 14/14 path 정확히 일치 (substring 매칭 아닌 exact match) |
| P0-2 | `core/` 모듈에 xapi/xeni/uiadapter 의존성 포함 | `core/pom.xml` 확인 |
| P0-3 | `nxui/` 템플릿 완전성 | `_resource_/`, `images/`, `nexacrolib/` 디렉토리 존재 |
| P0-4 | `typedefinition.xml`에 Services 블록 존재 | XML 파싱 후 `<Services>` 노드 확인 |
| P1-5 | `SKILL.md` Step 6에 `nexacro-build` skill 호출 명시 | 텍스트 검색 |

---

## 4. Verification Criteria (검증 기준)

### 4-1. Endpoint 일치 검증
- ❌ **금지**: "non-5xx 17/17" 같은 응답 상태 기반 검증
- ✅ **필수**: HTTP path 문자열이 §2의 14개 path와 **exact match**

### 4-2. Authority Chain 위반 검증
검증 subagent는 반드시 **이 문서(§2)를 권위 사양으로** 참조. openapi.yaml이나 실제 구현을 기준 삼는 self-validation echo chamber 패턴 금지.

### 4-3. 빌드 검증
양쪽 lane 모두 `mvn -pl <runner> -am -DskipTests compile -q` BUILD SUCCESS.

---

## 5. Orchestration Model (역할 위임 모델)

**Opus(메인 세션) = 관리자/조율자 + 검증자.**

| 역할 | 담당 | 호출 방식 |
|------|------|-----------|
| 코딩(구현) | Sonnet 서브에이전트 | `Agent(model="sonnet", subagent_type="general-purpose", ...)` |
| 검증(spec/quality review) | **Opus 직접** (메인 세션) | 권위 사양(룰 §2)을 가진 Opus가 산출물을 직접 검증. 검증 subagent에 위임하지 않음. |
| 기타(파일 정리, 문서 작성, 단순 조회) | Haiku 서브에이전트 | `Agent(model="haiku", ...)` |

**Opus 직접 검증의 근거:** Opus는 룰 §2 권위 사양과 phase 간 맥락을 모두 보유한다. 검증을 별도 subagent에 위임하면 권위 사양 컨텍스트가 단절되어 echo chamber 위험이 커진다. Opus가 직접 grep + path 비교 + diff 확인으로 검증한다.

**(이력)**
- 2026-04-27 초기: 코딩=`codex:codex-rescue`, 검증=Sonnet
- 2026-04-27 갱신1: codex OAuth 마찰로 코딩→Sonnet 이관
- 2026-04-27 갱신2: 검증도 Opus 직접 수행으로 변경 (권위 사양 컨텍스트 보존)

---

## 6. Workflow Rules (작업 규칙)

### 6-1. Plan Mode 필수
- 모든 비자명 작업은 `EnterPlanMode`로 시작
- 단계별 플랜을 plan window에 표시 → 사용자 승인 후 실행

### 6-2. Autonomous Guard Rule (자동 진행 가드)
다음 상황에서는 **반드시 일시정지하고 사용자 확인**:
1. 사용자 원본 사양과 다른 방향으로 분기할 때
2. Authority source 간 충돌이 있을 때
3. 일방향(되돌릴 수 없는) 또는 breaking 결정을 내릴 때
4. 검증 기준을 변경할 때
5. Compaction 직후 첫 결정

### 6-3. Per-File Commit
파일별로 별도 커밋. 여러 파일 변경을 하나의 커밋에 묶지 말 것.

### 6-4. Self-Validation Echo Chamber 금지
검증 subagent에 항상 **§2의 권위 사양**을 함께 전달. subagent가 내가 만든 draft만 보고 "PASS" 하는 패턴 차단.

---

## 7. Out of Scope

- 신규 도메인 추가 (e.g., 결제, 차트) — 현재 14-endpoint 범위 외
- WebFlux 변환 — 별개 프로젝트 `nexacro-webflux`에서 진행

---

**마지막 업데이트:** 2026-04-27 (Monday) — Plan7 폐기 후 Plan8 시작 직전
