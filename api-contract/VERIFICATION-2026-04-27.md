# Plan8 Verification Report — 14-Endpoint Spec Compliance

**Date:** 2026-04-27
**Authority:** `.claude/rules/nexacro-fullstack-purpose.md` §2 + §8
**Verifier:** Opus (main session, direct)
**Method:** ripgrep `@(Get|Post|Request)Mapping` against both lane source trees → exact path-string match against rules §2.

---

## Result: PASS — 14/14 × 2 lanes + Runner Extra preserved

Both `boot-jdk17-jakarta` and `boot-jdk8-javax` declare exactly the 14 canonical endpoints under `/uiadapter/<action>.do` with correct HTTP verbs. The §8 runner extra `/uiadapter/check_testDataTypeList.do` is preserved on both lanes.

---

## Per-Endpoint Verification

| # | Method | Path | jakarta | javax |
|---|--------|------|---------|-------|
| 1  | POST | `/uiadapter/login.do` | ✅ LoginController | ✅ LoginController |
| 2  | POST | `/uiadapter/logout.do` | ✅ LoginController | ✅ LoginController |
| 3  | POST | `/uiadapter/select_data_single.do` | ✅ BoardController | ✅ BoardController |
| 4  | POST | `/uiadapter/select_datalist.do` | ✅ BoardController | ✅ BoardController |
| 5  | POST | `/uiadapter/update_datalist_map.do` | ✅ BoardController | ✅ BoardController |
| 6  | POST | `/uiadapter/update_deptlist_map.do` | ✅ DeptController | ✅ DeptController |
| 7  | POST | `/uiadapter/advancedUploadFiles.do` | ✅ FileController | ✅ FileController |
| 8  | **GET**  | `/uiadapter/advancedDownloadFile.do` | ✅ FileController | ✅ FileController |
| 9  | **GET**  | `/uiadapter/advancedDownloadList.do` | ✅ UiadapterFileController | ✅ UiadapterFileController |
| 10 | POST | `/uiadapter/sampleLargeData.do` | ✅ LargeController | ✅ LargeDataController |
| 11 | **GET**  | `/uiadapter/sampleVideoStream.do` | ✅ UiadapterVideoController | ✅ UiadapterVideoController |
| 12 | POST | `/uiadapter/sampleTestData.do` | ✅ UiadapterTestDataController | ✅ UiadapterTestDataController |
| 13 | POST | `/uiadapter/sampleWideColumns.do` | ✅ UiadapterBoardController | ✅ UiadapterBoardController |
| 14 | POST | `/uiadapter/relay/exim_exchange.do` | ✅ RelayController (stub) | ✅ RelayController (stub) |

### Runner Extras (§8)

| Path | Method | jakarta | javax |
|------|--------|---------|-------|
| `/uiadapter/check_testDataTypeList.do` | POST | ✅ UiadapterTestDataController | ✅ UiadapterTestDataController |

---

## Build Verification

| Lane | Command | Result |
|------|---------|--------|
| jakarta (JDK17) | `mvn -DskipTests compile -q` | BUILD SUCCESS |
| javax (JDK8 source / JDK17 launcher) | `mvn -DskipTests compile -q` | BUILD SUCCESS |

---

## Anti-Pattern Check

- ✅ No "non-5xx" success criterion — exact path-string match only.
- ✅ Authority anchored on rules §2, not on implementation or openapi.yaml.
- ✅ No new canonical scheme decisions made by implementer subagents.
- ✅ Verb spec respected: GET on #8/#9/#11 (web download semantics), POST elsewhere.

---

## Path Coverage Assertion

`union(jakarta paths) == union(javax paths) == {14 spec paths} ∪ {check_testDataTypeList.do}`

No extras detected outside §8. No spec paths missing.

---

## Final Verdict

**Plan8 endpoint contract recovery: COMPLETE.**

Both runners satisfy the authoritative 14-endpoint contract. Phase F stub for `RelayController` returns 200 with a placeholder body — full integration body is deferred to a dedicated phase outside Plan8 scope.
