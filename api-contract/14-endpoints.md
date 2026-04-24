# 14-Endpoint Common Contract (+ 1 WebFlux variant)

> Authoritative endpoint list for all nexacroN-fullstack runners. Derived from design spec §5.1 (14 common) + §5.2 (1 WebFlux variant). See sibling `openapi.yaml` for OpenAPI 3.1 schema and `data-formats.md` for NexacroEnvelope wire formats (XML/SSV/JSON).

## Authority & scope

- **Authoritative:** spec §5.1 for domain coverage; this doc for per-endpoint I/O shapes.
- **Non-authoritative (reference only):** `openapi.yaml` (path-style examples), current runner path audits.
- **Out of scope:** path-scheme unification across runners (deferred to plan7). `.do` suffix vs bare, `/sample/` prefix vs none — both are documented as-is below.

## Path scheme divergence (2026-04-24 state)

| Runner | Path prefix | Suffix | Example |
|---|---|---|---|
| boot-jdk17-jakarta | `/sample/<domain>/` (board only); bare for others | `.do` | `/sample/board/select.do`, `/login.do` |
| boot-jdk8-javax | (none) | (none for board/login); `.do` for dept/file/large | `/board/select`, `/login`, `/dept/list.do` |
| (future runners) | TBD (plan7) | TBD | — |

> Note: Path-scheme unification is deferred to plan7. Until then, treat each runner's current paths as canonical for that runner.

---

# 14 Common Endpoints

## 1. Auth — Login

- **Source:** spec §5.1 item 1
- **Purpose:** Stub login; validates USER_ID + USER_PASSWORD against USERS table and returns session token / login result.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: `POST /login.do`
  - boot-jdk8-javax: `POST /login`
- **Request envelope:**
  - Parameters: (none required)
  - Datasets:
    - `dsSearch` — variable columns:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | USER_ID | string | 32 | required |
      | USER_PASSWORD | string | 256 | required |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `output` (id used in tobe runner) / `dsList` (id in legacy spec notation) — 1 row:
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | USER_ID | string | 32 | logged-in user |
      | USER_NAME | string | 100 | display name |
      | ROLE | string | 20 | USER / ADMIN |
      | LOGIN_RESULT | string | 20 | "LOGIN_SUCCESS" (spec §5.1 literal) |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "invalid credentials"`
  - Known error cases: HTTP 200 with non-zero ErrorCode on bad credentials; HTTP 500 on DB failure.
- **Legacy source hint:** `example.nexacro.uiadapter.web.BoardController` (asis repos) does NOT cover login — login is new, no direct legacy source. Password-check pattern ported from `UserService.login()` in shared-business (`stub$` + userId pattern).
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented (`/login.do`, `LoginController`)
  - boot-jdk8-javax: ✅ implemented (`/login`, `LoginController`)

---

## 2. Board — Single-row select

- **Source:** spec §5.1 item 2
- **Purpose:** Fetch a single SAMPLE_BOARD row by BOARD_ID (pattern01 단건 조회).
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: `POST /sample/board/select.do` (multi-row; single-row variant not separately mapped — see Reconciliation §A)
  - boot-jdk8-javax: `POST /board/select` (same: multi-row endpoint covers this via `dsSearch.BOARD_ID` filter)
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsSearch` — variable columns:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | BOARD_ID | int | 10 | required (for single-row) |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `dsOutput` — 1 row (spec name); current runners return `ds_list` or `output`:
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | BOARD_ID | int | 10 | PK |
      | TITLE | string | 200 | |
      | CONTENT | string | 4000 | CLOB in DB |
      | AUTHOR_ID | string | 32 | FK → USERS |
      | VIEW_COUNT | int | 10 | |
      | CREATED_AT | datetime | 20 | |
      | UPDATED_AT | datetime | 20 | nullable |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "not found"` — when BOARD_ID not found or DELETED=TRUE.
- **Legacy source hint:** `example.nexacro.uiadapter.web.BoardController.select_datalist()` in `boot-jdk17-jakarta-legacy` (e49a17791d) — `BoardService.select_datalist()` with `dsSearch`. For single-row semantics, see `BoardController.select_datalist_firstrow()` in same repo.
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented (merged into `/sample/board/select.do`)
  - boot-jdk8-javax: ✅ implemented (merged into `/board/select`)

---

## 3. Board — List select

- **Source:** spec §5.1 item 3
- **Purpose:** Fetch multiple SAMPLE_BOARD rows filtered by dsSearch criteria (pattern01/02 공용 다행 조회).
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: `POST /sample/board/select.do`
  - boot-jdk8-javax: `POST /board/select`
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsSearch` — variable columns (all optional):
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | BOARD_ID | int | 10 | optional — omit for full list |
      | TITLE | string | 200 | optional — keyword filter |
      | AUTHOR_ID | string | 32 | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `ds_list` / `output` (id per runner implementation; spec calls it `dsList`):
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | BOARD_ID | int | 10 | PK |
      | TITLE | string | 200 | |
      | CONTENT | string | 4000 | |
      | AUTHOR_ID | string | 32 | |
      | VIEW_COUNT | int | 10 | |
      | CREATED_AT | datetime | 20 | |
      | UPDATED_AT | datetime | 20 | nullable |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<message>"`
- **Legacy source hint:** `example.nexacro.uiadapter.web.BoardController.select_datalist()` → `BoardService.select_datalist()` → `BoardMapper.xml` in `boot-jdk17-jakarta-legacy` (e49a17791d).
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented
  - boot-jdk8-javax: ✅ implemented

---

## 4. Board — Bulk save (I/U/D)

- **Source:** spec §5.1 item 4
- **Purpose:** Apply insert/update/delete operations to SAMPLE_BOARD using `_RowType_` flag per row (pattern01 저장).
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: `POST /sample/board/insert.do` (insert), `POST /sample/board/update.do` (update), `POST /sample/board/delete.do` (delete) — OpenAPI RESTful split; spec unifies under `update_datalist_map.do`
  - boot-jdk8-javax: `POST /board/insert`, `POST /board/update`, `POST /board/delete`
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `ds_list` (tobe runner name) / `dsList` (spec name) — multiple rows with `_RowType_`:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | _RowType_ | string | 1 | required — I/U/D/N/O |
      | BOARD_ID | int | 10 | required for U/D; auto for I |
      | TITLE | string | 200 | required for I/U |
      | CONTENT | string | 4000 | required for I/U |
      | AUTHOR_ID | string | 32 | required for I |
      | VIEW_COUNT | int | 10 | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `dsResult` (spec) — 0 rows (result indicator only); or empty datasets list with Parameters only.
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<SQL error or not found>"`
- **Legacy source hint:** `BoardController.update_datalist_map()` → `BoardService.update_datalist_map()` in `boot-jdk17-jakarta-legacy` (e49a17791d). SQL in `BoardMapper.xml`.
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented (3 separate endpoints)
  - boot-jdk8-javax: ✅ implemented (3 separate endpoints)

---

## 5. File — Upload

- **Source:** spec §5.1 item 5
- **Purpose:** Accept one or more multipart file uploads; store to filesystem; record metadata in FILE_META.
- **HTTP verb:** POST (multipart/form-data)
- **Paths:**
  - boot-jdk17-jakarta: `POST /file/upload.do`
  - boot-jdk8-javax: `POST /file/upload.do`
- **Request envelope:**
  - Parameters: `uploadedBy` (query param or form field, optional, default "anonymous")
  - Datasets: N/A — files sent as multipart parts (`files[]` per spec; `file` single-part per tobe runner)
  - Query params: `subFolder=` (spec §5.1 item 5; not in tobe runner — see Reconciliation §A)
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`, `fileId: <UUID>` (string, for last uploaded file)
  - Datasets: none (or `dsResult` per spec — see Reconciliation §A)
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<IO error or invalid file type>"`
  - HTTP 400 on empty file; HTTP 500 on storage failure.
- **Legacy source hint:** `example.nexacro.uiadapter.web.FileController.uploadFiles()` in `boot-jdk17-jakarta-legacy` (e49a17791d) — `advancedUploadFiles.do`, uses `NexacroMultiFileResult`, stores to `getFilePath() + subFolder`.
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented (`/file/upload.do`, `FileController`)
  - boot-jdk8-javax: ✅ implemented (`/file/upload.do`, `FileController`)

---

## 6. File — Single download

- **Source:** spec §5.1 item 6
- **Purpose:** Stream a single file binary to the client by file identifier.
- **HTTP verb:** GET
- **Paths:**
  - boot-jdk17-jakarta: `GET /file/download.do?fileId=<UUID>`
  - boot-jdk8-javax: `GET /file/download.do?fileId=<UUID>`
- **Request envelope:**
  - Parameters: `fileId` (query param, required); spec uses `subFolder=&fileName=` scheme — see Reconciliation §A.
  - Datasets: N/A
- **Response envelope (success):**
  - HTTP 200 with binary file body; `Content-Type` from FILE_META.CONTENT_TYPE; `Content-Disposition: attachment; filename="<ORIGINAL_NAME>"`
  - No NexacroEnvelope (raw binary response).
- **Response envelope (error):**
  - HTTP 404 when fileId not found; HTTP 500 on IO failure.
- **Legacy source hint:** `FileController.downloadFiles()` → `/advancedDownloadFiles.do` in `boot-jdk17-jakarta-legacy` (e49a17791d) — streams file using `NexacroFileResult`.
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented
  - boot-jdk8-javax: ✅ implemented

---

## 7. File — Multi-download (zip)

- **Source:** spec §5.1 item 7
- **Purpose:** Bundle multiple files into a ZIP stream and deliver as a single download.
- **HTTP verb:** GET
- **Paths:**
  - boot-jdk17-jakarta: ⏳ not yet implemented (gap — Task 6.2). Spec path: `GET /uiadapter/multiDownloadFiles.do?subFolder=`
  - boot-jdk8-javax: ⏳ not yet implemented (gap — Task 6.3). Spec path: `GET /uiadapter/multiDownloadFiles.do?subFolder=`
- **Request envelope:**
  - Parameters: `subFolder` (query param, optional); `fileIds` (comma-separated or repeated query param — shape TBD pending port from legacy)
  - Datasets: N/A
- **Response envelope (success):**
  - HTTP 200 with ZIP binary body; `Content-Type: application/zip`; `Content-Disposition: attachment; filename="download.zip"`
  - No NexacroEnvelope.
- **Response envelope (error):**
  - HTTP 404 when no files found; HTTP 500 on IO failure.
- **Legacy source hint:** `FileController` in `boot-jdk17-jakarta-legacy` (e49a17791d) — `multiDownloadFiles.do` method. Also present in all 4 legacy repos (`mvc-jdk17-jakarta-legacy` d9b45a5c1d, `mvc-jdk8-javax-legacy` 69cdaf1a9d, `boot-jdk8-javax-legacy` 810dd52672). Shape TBD — to be finalized when Task 6.2/6.3 implementers port from `FileController.multiDownloadFiles()` in `boot-jdk17-jakarta-legacy`.
- **Implementation status:**
  - boot-jdk17-jakarta: ⏳ gap (Task 6.2)
  - boot-jdk8-javax: ⏳ gap (Task 6.3)

---

## 8. File — List

- **Source:** spec §5.1 item 8
- **Purpose:** Return a list of uploaded file metadata records from FILE_META.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: `POST /file/list.do`
  - boot-jdk8-javax: `POST /file/list.do`
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsSearch` — optional filter (spec: `POST /uiadapter/advancedDownloadList.do?subFolder=`):
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | subFolder | string | 100 | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `files` (tobe runner id) / `dsList` (spec name):
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | FILE_ID | string | 40 | UUID |
      | ORIGINAL_NAME | string | 255 | original filename |
      | CONTENT_TYPE | string | 100 | MIME type |
      | SIZE_BYTES | int | 20 | file size in bytes |
      | UPLOADED_BY | string | 32 | FK → USERS |
      | UPLOADED_AT | datetime | 20 | upload timestamp |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<message>"`
- **Legacy source hint:** `FileController.downloadList()` → `/advancedDownloadList.do` in `boot-jdk17-jakarta-legacy` (e49a17791d).
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented
  - boot-jdk8-javax: ✅ implemented

---

## 9. Video — Streaming

- **Source:** spec §5.1 item 9
- **Purpose:** Stream a video file to the browser using HTTP range / NIO support.
- **HTTP verb:** GET
- **Paths:**
  - boot-jdk17-jakarta: ⏳ not yet implemented (gap — Task 6.2). Spec path: `GET /uiadapter/streamingVideo.do?fileName=&streamType=nio`
  - boot-jdk8-javax: ⏳ not yet implemented (gap — Task 6.3). Spec path: `GET /uiadapter/streamingVideo.do?fileName=&streamType=nio`
- **Request envelope:**
  - Parameters: `fileName` (query param, required); `streamType` (query param, optional, default "nio")
  - Datasets: N/A
- **Response envelope (success):**
  - HTTP 200 (or 206 Partial Content) with video binary; `Content-Type: video/*`; supports HTTP Range headers.
  - No NexacroEnvelope.
- **Response envelope (error):**
  - HTTP 404 when file not found; HTTP 500 on IO failure.
- **Legacy source hint:** `example.nexacro.uiadapter.web.StreamController.streamingVideo()` → `/streamingVideo.do` in `boot-jdk17-jakarta-legacy` (e49a17791d), returns `NexacroStreamResult`. Shape TBD — to be finalized when Task 6.2/6.3 implementers port from `StreamController` in `boot-jdk17-jakarta-legacy`.
- **Implementation status:**
  - boot-jdk17-jakarta: ⏳ gap (Task 6.2)
  - boot-jdk8-javax: ⏳ gap (Task 6.3)

---

## 10. TestData — Select by type

- **Source:** spec §5.1 item 10
- **Purpose:** Return test rows from SAMPLE_BOARD (or ephemeral dataset) demonstrating all Nexacro column data types: STRING, INT, BOOLEAN, LONG, FLOAT, DOUBLE, BIGDECIMAL, DATE, TIME, DATETIME, BYTES.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: ⏳ not yet implemented (gap — Task 6.2). Spec path: `POST /uiadapter/select_testDataTypeList.do`
  - boot-jdk8-javax: ⏳ not yet implemented (gap — Task 6.3). Spec path: `POST /uiadapter/select_testDataTypeList.do`
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsSearch` — optional filter:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | (filter field TBD) | string | 100 | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `dsList` — multiple rows, one per type sample:
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | stringValue | string | 256 | String column type demo |
      | intValue | int | 10 | Integer column type demo |
      | booleanValue | boolean | 1 | Boolean column type demo |
      | longValue | int | 20 | Long column type demo |
      | floatValue | float | 15 | Float column type demo |
      | doubleValue | float | 20 | Double column type demo |
      | bigDecimalValue | decimal | 20 | BigDecimal column type demo |
      | dateValue | date | 10 | Date column type demo |
      | timeValue | time | 8 | Time column type demo |
      | dateTimeValue | datetime | 20 | DateTime column type demo |
      | bytesValue | blob | — | byte[] column type demo |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<message>"`
- **Legacy source hint:** `ExampleDateTypeController.select_testDataTypeList()` → `ExampleDataTypeServcice` in `boot-jdk17-jakarta-legacy` (e49a17791d). Column definitions from `ExampleDataType` POJO (`example.nexacro.uiadapter.pojo.ExampleDataType`). Shape TBD — to be finalized when Task 6.2/6.3 implementers port from `ExampleDateTypeController` in `boot-jdk17-jakarta-legacy`.
- **Implementation status:**
  - boot-jdk17-jakarta: ⏳ gap (Task 6.2)
  - boot-jdk8-javax: ⏳ gap (Task 6.3)

---

## 11. TestData — Type check (echo)

- **Source:** spec §5.1 item 11
- **Purpose:** Accept a dataset with typed columns, log/validate each field, and echo the rows back — demonstrates Nexacro → Java type mapping correctness.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: ⏳ not yet implemented (gap — Task 6.2). Spec path: `POST /uiadapter/check_testDataTypeList.do`
  - boot-jdk8-javax: ⏳ not yet implemented (gap — Task 6.3). Spec path: `POST /uiadapter/check_testDataTypeList.do`
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsList` — required, multiple rows (mapped to `List<ExampleDataType>` by @ParamDataSet in legacy):
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | stringValue | string | 256 | optional |
      | intValue | int | 10 | optional |
      | booleanValue | boolean | 1 | optional |
      | longValue | int | 20 | optional |
      | floatValue | float | 15 | optional |
      | doubleValue | float | 20 | optional |
      | bigDecimalValue | decimal | 20 | optional |
      | dateValue | date | 10 | optional |
      | timeValue | time | 8 | optional |
      | dateTimeValue | datetime | 20 | optional |
      | bytesValue | blob | — | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `dsResult` — echoed rows (same columns as request `dsList`); spec says `dsResult` but legacy impl re-echoes the input dataset.
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<type mapping error>"`
- **Legacy source hint:** `ExampleDateTypeController.check_testDataTypeList()` in `boot-jdk17-jakarta-legacy` (e49a17791d), `@ParamDataSet(name = "dsList", required = true) List<ExampleDataType>`. Port column definitions from `ExampleDataType` POJO in same repo.
- **Implementation status:**
  - boot-jdk17-jakarta: ⏳ gap (Task 6.2)
  - boot-jdk8-javax: ⏳ gap (Task 6.3)

---

## 12. Dept — Bulk save (I/U/D)

- **Source:** spec §5.1 item 12
- **Purpose:** Apply insert/update/delete operations to DEPT table using `_RowType_` per row.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: No dedicated bulk-save endpoint for dept exists yet (only list/tree) — ⏳ pending (spec §5.1 item 12 `update_deptlist_map.do` not mapped in current runner). See note below.
  - boot-jdk8-javax: Same — ⏳ pending.

> **Note (2026-04-24):** Current runners implement `POST /dept/list.do` and `POST /dept/tree.do` (read-only). The spec §5.1 item 12 `update_deptlist_map.do` (I/U/D save) is NOT present. However, the current-state audit marks both runners as having 12 endpoints covering "9 of 14 spec functions." The 5 confirmed gaps are wide/video/testdata-select/testdata-check/file-multiDownload. Dept save is counted as a current-runner endpoint that is partially implemented (read-only) — the I/U/D path is the missing piece. Per Gate 2: document current reality; implementation alignment deferred to Task 6.2/6.3.

- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsList` — required:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | _RowType_ | string | 1 | required — I/U/D |
      | DEPT_ID | string | 20 | required |
      | DEPT_NAME | string | 100 | required for I/U |
      | PARENT_ID | string | 20 | optional |
      | SORT_ORDER | int | 10 | optional |
      | LEVEL_NO | int | 10 | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets: none (or empty `dsResult`).
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<SQL error>"`
- **Legacy source hint:** `DeptController.update_deptlist_map()` in `boot-jdk17-jakarta-legacy` (e49a17791d) and `boot-jdk8-javax-legacy` (810dd52672).
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ list/tree implemented; ⏳ I/U/D save path gap — not counted in the 5 primary gaps (Task 6.2 should add it)
  - boot-jdk8-javax: ✅ list/tree implemented; ⏳ I/U/D save path gap

---

## 13. Large Data — Bulk fetch

- **Source:** spec §5.1 item 13
- **Purpose:** Return tens of thousands of rows from LARGE_DATA for paging / streaming demo.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: `POST /large/page.do` (paged variant per current runner)
  - boot-jdk8-javax: `POST /large/page` (no `.do` suffix)
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsSearch` — optional filter/paging:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | page | int | 10 | optional (1-indexed, default 1) |
      | size | int | 10 | optional (default 100) |
      | CATEGORY | string | 10 | optional filter |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`, `totalCount: <int>`
  - Datasets:
    - `large` (tobe runner id) / `dsList` (spec name):
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | ROW_ID | int | 20 | BIGINT identity PK |
      | CATEGORY | string | 10 | category code |
      | SEQ_NO | int | 10 | sequence within category |
      | VALUE_1 | string | 100 | string value |
      | VALUE_2 | decimal | 18 | decimal value (18,4) |
      | VALUE_3 | int | 10 | integer value |
      | CREATED_AT | datetime | 20 | creation timestamp |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<message>"`
- **Legacy source hint:** `LargeDataController.mybatisLargeData()` → `/sampleLargeData.do` in `boot-jdk17-jakarta-legacy` (e49a17791d). Tobe runner splits into paged `/large/page.do` — OpenAPI also adds `/large/stream.do` (SSV chunked) per reconciliation; that stream variant is out of scope for this contract (not in spec §5.1).
- **Implementation status:**
  - boot-jdk17-jakarta: ✅ implemented (paged: `/large/page.do`)
  - boot-jdk8-javax: ✅ implemented (paged: `/large/page`)

---

## 14. Wide Columns — Search

- **Source:** spec §5.1 item 14
- **Purpose:** Return a row from WIDE_COLUMNS (50+ columns) to exercise nexacro wide-column dataset rendering.
- **HTTP verb:** POST
- **Paths:**
  - boot-jdk17-jakarta: ⏳ not yet implemented (gap — Task 6.2). Spec path: `POST /uiadapter/search_manyColumn_data.do`; OpenAPI: `POST /wide/load.do`
  - boot-jdk8-javax: ⏳ not yet implemented (gap — Task 6.3). Spec path: `POST /uiadapter/search_manyColumn_data.do`; OpenAPI: `POST /wide/load.do`
- **Request envelope:**
  - Parameters: (none)
  - Datasets:
    - `dsSearch` — optional filter:
      | Column id | Type | Size | Required |
      |---|---|---|---|
      | KEY_ID | string | 20 | optional |
- **Response envelope (success):**
  - Parameters: `ErrorCode: 0`, `ErrorMsg: ""`
  - Datasets:
    - `dsList` / `output`:
      | Column id | Type | Size | Notes |
      |---|---|---|---|
      | KEY_ID | string | 20 | PK |
      | COL_01–COL_10 | string | 50 | VARCHAR columns |
      | COL_11–COL_20 | int | 10 | INTEGER columns |
      | COL_21–COL_30 | decimal | 18 | DECIMAL(18,4) columns |
      | COL_31–COL_35 | date | 10 | DATE columns |
      | COL_36–COL_40 | datetime | 20 | TIMESTAMP columns |
      | COL_41–COL_45 | boolean | 1 | BOOLEAN columns |
      | COL_46–COL_50 | string | — | CLOB columns (string in Nexacro) |
- **Response envelope (error):**
  - `ErrorCode: -1`, `ErrorMsg: "<message>"`
- **Legacy source hint:** `UidapterBoardController` + `UidapterManyColumnService` in `mvc-jdk17-jakarta-legacy` (d9b45a5c1d) and `mvc-jdk8-javax-legacy` (69cdaf1a9d). Shape TBD — to be finalized when Task 6.2/6.3 implementers port from `UidapterManyColumnService` in those repos.
- **Implementation status:**
  - boot-jdk17-jakarta: ⏳ gap (Task 6.2)
  - boot-jdk8-javax: ⏳ gap (Task 6.3)

---

# WebFlux Variant (1 Additional Endpoint)

## 15. Reactive — Import/Export Exchange [WebFlux only]

- **Source:** spec §5.2 item 15
- **Purpose:** Demonstrate WebFlux-native reactive import + export streaming: accept multipart file upload via WebClient, process reactively (Mono/Flux chaining), and stream the result binary back. Non-WebFlux runners return HTTP 501.
- **Runners:** webflux-jdk17-jakarta only. MVC/Boot runners return `501 Not Implemented`.
- **HTTP verb:** POST (multipart/form-data)
- **Paths:**
  - webflux-jdk17-jakarta: `POST /reactive/exim_exchange.do` (OpenAPI operationId: `reactivEximExchange`; spec path: `POST /uiadapter/relay/exim_exchange.do`)
  - boot-jdk17-jakarta: 501 Not Implemented
  - boot-jdk8-javax: 501 Not Implemented
- **Request envelope:**
  - Parameters: (none)
  - Multipart: one or more file parts (MIME type per file); no NexacroDataset wrapper — raw multipart.
- **Response envelope (success):**
  - HTTP 200 with binary stream body; `Content-Type: application/octet-stream` (or MIME type of processed file).
  - No NexacroEnvelope — raw binary.
- **Response envelope (error):**
  - HTTP 501 on non-WebFlux runners.
  - HTTP 500 on processing failure.
  - For WebFlux runner: error propagated via Mono.error → mapped to HTTP 500 + JSON error body.
- **Legacy source hint:** New, no legacy source. WebFlux-specific pattern using `WebClient + Mono`; other runners fall back to `RestTemplate` (sync) or 501. Reference: spec §5.2 implementation note.
- **Implementation status:**
  - webflux-jdk17-jakarta: ⏳ planned (not yet a runner in monorepo — future plan)
  - boot-jdk17-jakarta: N/A (will return 501)
  - boot-jdk8-javax: N/A (will return 501)

---

# Appendices

## A. Reconciliation Notes

Spec §5.1 vs `openapi.yaml` divergences (from `tasks/api-contract-14-endpoints.md`). Per Gate 2 decision: **spec §5.1 is authoritative for domain coverage**; OpenAPI path style is acceptable but not authoritative.

| # | Topic | Spec §5.1 | openapi.yaml | This doc follows |
|---|---|---|---|---|
| R1 | Auth response schema | `dsList{LOGIN_RESULT="LOGIN_SUCCESS"}` | `LoginResponse{status, userId}` | Spec — response dataset `output` with LOGIN_RESULT |
| R2 | Logout endpoint | NOT in spec §5.1 | `POST /logout.do` present | openapi / current runners — logout IS implemented; treat as runner extension, not a "14th" endpoint |
| R3 | Board CRUD structure | Single `update_datalist_map.do` for I/U/D | 3 separate endpoints (insert/update/delete) | openapi path style acceptable; both runners use 3-endpoint split |
| R4 | Board select_data_single vs select_datalist | Two separate endpoints (#2 and #3) | Merged into single `POST /sample/board/select.do` | Current runner reality — single select endpoint; single-row filtered by BOARD_ID |
| R5 | Dept CRUD | `update_deptlist_map.do` (I/U/D) | Only list/tree (read-only) | Spec — I/U/D save is required; gap in both openapi and current runners |
| R6 | Large data | Single `sampleLargeData.do` | Split into `page.do` + `stream.do` | Spec for domain coverage; paged variant implemented; stream variant is runner extension (out of scope) |
| R7 | File download scheme | `?subFolder=&fileName=` query | `?fileId=<UUID>` | Current runner reality — UUID-based `fileId`; subFolder not implemented |
| R8 | File multi-download | `multiDownloadFiles.do` (GET, zip) | NOT present in openapi | Spec — required endpoint; gap in both runners and openapi |
| R9 | Excel export | NOT in spec §5.1 | `POST /excel/export.do` | Out of scope for this contract (plan6 exclusion) |
| R10 | Video streaming | `streamingVideo.do` (GET) | NOT in openapi | Spec — required endpoint; gap in both runners |
| R11 | Test data (2 endpoints) | `select_testDataTypeList.do` + `check_testDataTypeList.do` | NOT in openapi | Spec — both required; gaps in both runners |
| R12 | WebFlux reactive endpoint | `relay/exim_exchange.do` (WebFlux impl) | `POST /reactive/exim_exchange.do` (501 for non-webflux) | Both agree — WebFlux-only |

### openapi.yaml endpoints NOT in spec §5.1 — disposition

| openapi path | Disposition |
|---|---|
| `POST /logout.do` | Runner extension — implemented in both runners as session management; not counted in "14" |
| `POST /excel/export.do` | Out of scope for plan6 contract — xeni excel export; tracked separately |
| `POST /large/stream.do` | Runner extension — OpenAPI SSV chunked stream variant; not in spec §5.1; out of scope |

---

## B. Gap Summary (for plan6 Tasks 6.2 / 6.3)

The 5 confirmed gaps (missing from both runners as of 2026-04-24):

| Gap | Endpoint # | Spec path | boot-jdk17-jakarta | boot-jdk8-javax | Legacy source |
|---|---|---|---|---|---|
| (a) Wide columns | 14 | `search_manyColumn_data.do` | ⏳ Task 6.2 | ⏳ Task 6.3 | `UidapterManyColumnService` in mvc-jdk17-jakarta-legacy (d9b45a5c1d) |
| (b) Video streaming | 9 | `streamingVideo.do` | ⏳ Task 6.2 | ⏳ Task 6.3 | `StreamController` in boot-jdk17-jakarta-legacy (e49a17791d) |
| (c) TestData select | 10 | `select_testDataTypeList.do` | ⏳ Task 6.2 | ⏳ Task 6.3 | `ExampleDateTypeController` in boot-jdk17-jakarta-legacy (e49a17791d) |
| (d) TestData check | 11 | `check_testDataTypeList.do` | ⏳ Task 6.2 | ⏳ Task 6.3 | `ExampleDateTypeController` in boot-jdk17-jakarta-legacy (e49a17791d) |
| (e) File multi-download | 7 | `multiDownloadFiles.do` | ⏳ Task 6.2 | ⏳ Task 6.3 | `FileController` in all 4 legacy repos |

Full runner × endpoint status matrix:

| Endpoint # | Description | boot-jdk17-jakarta | boot-jdk8-javax |
|---|---|---|---|
| 1 | Login | ✅ | ✅ |
| 2 | Board single select | ✅ (merged with #3) | ✅ (merged with #3) |
| 3 | Board list select | ✅ | ✅ |
| 4 | Board bulk save (I/U/D) | ✅ (3 endpoints) | ✅ (3 endpoints) |
| 5 | File upload | ✅ | ✅ |
| 6 | File single download | ✅ | ✅ |
| 7 | File multi-download (zip) | ⏳ | ⏳ |
| 8 | File list | ✅ | ✅ |
| 9 | Video streaming | ⏳ | ⏳ |
| 10 | TestData select | ⏳ | ⏳ |
| 11 | TestData check | ⏳ | ⏳ |
| 12 | Dept bulk save (I/U/D) | ⏳ (list/tree ✅; save ⏳) | ⏳ (list/tree ✅; save ⏳) |
| 13 | Large data page | ✅ | ✅ |
| 14 | Wide columns | ⏳ | ⏳ |
| 15 | Reactive exim_exchange | N/A (501) | N/A (501) |

> Note: Both runners also implement `POST /logout.do` / `POST /logout` (runner extension, not in the 14-spec list) and `POST /dept/list.do` + `POST /dept/tree.do` (read-only dept, covering 2 of the 3 dept spec endpoints).

---

## C. Path-Scheme Unification (Deferred to plan7)

The two current runners have diverged on path naming conventions:

- **boot-jdk17-jakarta** uses `/sample/board/` prefix (board domain only) and `.do` suffix for all paths.
- **boot-jdk8-javax** uses no prefix and no suffix for board/login/logout, but retains `.do` for dept, file, and large.

This divergence is intentional for the current phase (plan6) — both runners are functional and the goal is endpoint coverage parity, not path unification.

**plan7 will propose a unified scheme.** Candidate approaches include:
1. Standardize on OpenAPI-style domain-prefixed paths with `.do` suffix for all runners.
2. Introduce a `/uiadapter/` context path (spec §5.1 canonical paths) as the standard.
3. Keep runner-specific paths but expose a reverse-proxy routing layer.

Until plan7 resolves this, implementers of Tasks 6.2 and 6.3 should follow each runner's existing path convention:
- **boot-jdk17-jakarta**: new endpoints use `POST /<domain>/<action>.do` (no `/sample/` prefix outside board).
- **boot-jdk8-javax**: new endpoints use `POST /<domain>/<action>` (no suffix).
