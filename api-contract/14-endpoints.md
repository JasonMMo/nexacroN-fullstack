# Canonical 14-Endpoint API Contract

**Authority:** `.claude/rules/nexacro-fullstack-purpose.md` §2 (Authoritative 14-Endpoint Contract — 사용자 사양)

---

## Overview

This document specifies the 14 canonical endpoints that nexacroN UI communicates with. The specification is the single source of truth for path strings, HTTP methods, and business purpose. 

**Authority chain:** User Spec (this document) > openapi.yaml (reference) > Implementation.

Conflicts always resolve in favor of this specification.

---

## 14 Canonical Endpoints

All endpoints are POST. Paths must match exactly (character-for-character) against the table below.

| # | Method | Path | Purpose | Domain |
|---|--------|------|---------|--------|
| 1 | POST | `/uiadapter/login.do` | 로그인 | Auth |
| 2 | POST | `/uiadapter/logout.do` | 로그아웃 | Auth |
| 3 | POST | `/uiadapter/select_data_single.do` | 단건 select | Data |
| 4 | POST | `/uiadapter/select_datalist.do` | 리스트 select | Data |
| 5 | POST | `/uiadapter/update_datalist_map.do` | 리스트 insert/update/delete | Data |
| 6 | POST | `/uiadapter/update_deptlist_map.do` | 부서 트리 갱신 | Data |
| 7 | POST | `/uiadapter/advancedUploadFiles.do` | 다중 업로드 | File |
| 8 | POST | `/uiadapter/advancedDownloadFile.do` | 단일 다운로드 | File |
| 9 | POST | `/uiadapter/advancedDownloadList.do` | 다운로드 리스트 | File |
| 10 | POST | `/uiadapter/sampleLargeData.do` | 대용량 select | Data |
| 11 | POST | `/uiadapter/sampleVideoStream.do` | 비디오 스트리밍 | Stream |
| 12 | POST | `/uiadapter/sampleTestData.do` | 테스트 데이터 | Data |
| 13 | POST | `/uiadapter/sampleWideColumns.do` | 와이드 컬럼 | Data |
| 14 | POST | `/uiadapter/relay/exim_exchange.do` | 외부 시스템 릴레이 | Integration |

---

## HTTP Method Note

All 14 endpoints use **POST** per specification. If implementation uses alternative HTTP methods (e.g., GET for file downloads), the specification POST method takes precedence. Reconciliation of alternative HTTP semantics is deferred to Plan8 Phase D/E.

---

## Verification Rule

**Exact path-string matching required.** Verification compares URL strings character-for-character against the "Path" column above. No semantic aliasing, path parameter substitution, or domain-specific routing is permitted. Each endpoint path must match exactly.

---

## Implementation Status

**Current gap (2026-04-27):** 0/14 endpoints implemented per Plan8 Phase A audit.

Remediation phases:

| Phase | Scope |
|-------|-------|
| Phase C | OpenAPI spec generation from this contract |
| Phase D | Endpoint implementation (boot-jdk17-jakarta) |
| Phase E | Endpoint implementation (boot-jdk8-javax) |
| Phase F | UAT validation + sign-off |

See Plan8 documentation for phase details.

---

마지막 업데이트: 2026-04-27 (Plan8 Phase B)

