# api-contract

Framework-neutral API contract for the nexacroN-fullstack monorepo.

All 8 runners (servlet + reactive) MUST satisfy the endpoints listed here.
The contract is owned by this directory — if a runner needs to diverge, the
spec changes here first and all other runners follow.

## Files

| File | Purpose |
|---|---|
| `openapi.yaml` | OpenAPI 3.0 spec with 15 endpoints (14 common + 1 webflux-only) |
| `data-formats.md` | Normative spec for XML / SSV / JSON envelopes and `_RowType_` flags |
| `README.md` | (this file) |

## Endpoints (15)

| # | Path                          | Method | Tag          | Notes                          |
|---|-------------------------------|--------|--------------|--------------------------------|
| 1 | `/login.do`                   | POST   | auth         | Stub — always LOGIN_SUCCESS    |
| 2 | `/logout.do`                  | POST   | auth         |                                |
| 3 | `/sample/board/select.do`     | POST   | sample_board |                                |
| 4 | `/sample/board/insert.do`     | POST   | sample_board | N/I/U/D rows in one request    |
| 5 | `/sample/board/update.do`     | POST   | sample_board |                                |
| 6 | `/sample/board/delete.do`     | POST   | sample_board |                                |
| 7 | `/dept/list.do`               | POST   | dept         |                                |
| 8 | `/dept/tree.do`               | POST   | dept         | parent-child tree              |
| 9 | `/large/page.do`              | POST   | large_data   | paged select                   |
| 10| `/large/stream.do`            | POST   | large_data   | chunked SSV                    |
| 11| `/wide/load.do`               | POST   | wide         | 50 columns                     |
| 12| `/file/upload.do`             | POST   | file         | multipart                      |
| 13| `/file/download.do`           | GET    | file         |                                |
| 14| `/file/list.do`               | POST   | file         |                                |
| 15| `/excel/export.do`            | POST   | excel        | xeni xlsx export               |

Plus one **webflux-only** endpoint (#16 in spec, not in common set):

| #  | Path                            | Method | Tag      | Implemented by         |
|----|---------------------------------|--------|----------|------------------------|
| 16 | `/reactive/exim_exchange.do`    | POST   | reactive | webflux-jdk17-jakarta  |

Servlet runners return HTTP 501 for `/reactive/*`.

## Contract tests (Plan 3)

A `contract-tests/` sub-directory will be added in Plan 3 with RestAssured-
based tests that every runner must pass. Not present yet (Plan 1 scope is
skeleton only).

## How runners consume this

Runners do NOT import this directory as a Maven module. The OpenAPI spec is
a documentation + test-contract artifact, not runtime code. Runners include
it via Maven resource filtering only if they want to serve it at
`/api-contract/openapi.yaml` (optional — not required by the contract).
