# Nexacro data formats

All 15 endpoints in `openapi.yaml` exchange payloads using one of three
Nexacro client-server formats: **XML**, **SSV**, or **JSON**. The choice is
made per-request by `packageN.xadl` (`services[*].dataformat`) and the server
honors whatever the client sent (Content-Type negotiation).

This document is the normative specification the runners must agree on.

## The three formats

### XML

- Official reference: https://docs.tobesoft.com/advanced_development_guide_nexacro_n_v24_ko/418fa47dbe2984b1
- Content-Type: `application/xml`
- Root element: `<Root xmlns="http://www.nexacroplatform.com/platform/dataset" ver="4000">`
- Most verbose; best for debugging.

Example:

```xml
<?xml version="1.0" encoding="utf-8"?>
<Root xmlns="http://www.nexacroplatform.com/platform/dataset" ver="4000">
  <Parameters>
    <Parameter id="service">stock</Parameter>
    <Parameter id="method">search</Parameter>
  </Parameters>
  <Dataset id="output">
    <ColumnInfo>
      <ConstColumn id="market" size="10" type="STRING" value="kse"/>
      <ConstColumn id="openprice" size="10" type="INT" value="15000"/>
      <Column id="stockCode" size="5" type="STRING"/>
      <Column id="currentprice" size="10" type="INT"/>
    </ColumnInfo>
    <Rows>
      <Row><Col id="stockCode">10001</Col><Col id="currentprice">5700</Col></Row>
      <Row><Col id="stockCode">10002</Col><Col id="currentprice">14500</Col></Row>
    </Rows>
  </Dataset>
</Root>
```

### SSV (Sprintf-Separated Values)

- Official reference: https://docs.tobesoft.com/advanced_development_guide_nexacro_n_v24_ko/c0e662b5844feb1b
- Content-Type: `text/plain` (with `SSV:utf-8` prelude)
- Delimiters:
  - `▼` (U+25BC) — record / section separator
  - `•` (U+2022) — field separator
  - `:` — metadata separator (type, size)
  - `,` — list separator inside metadata
- Smallest wire size; best for high-throughput endpoints (e.g. `/large/stream.do`).

Example (two Datasets in one body):

```
SSV:utf-8▼
Dataset:dataset0▼
_Const_•ConstCol1:STRING(20)=Name•ConstCol2:INT=1•ConstCol3:DECIMAL=0.8▼
_RowType_•Col1:STRING(20)•Col2:INT:SUM•Col3:DECIMAL:AVG▼
N•Test•0•1•1▼
I•Abc•1•2•2▼
U•Def•2•3•3▼
O•Chk•2•3•3▼
D•Ghi•3•4•4▼
▼
Dataset:dataset1▼
_RowType_•Col1:INT(4):Summ,Col2:STRING(30):Text▼
N•0•test▼
I•1•test1▼
U•2•test3▼
O•2•test3-1▼
D•3•test4▼
▼
```

### JSON

- Official reference: https://docs.tobesoft.com/advanced_development_guide_nexacro_n_v24_ko/e29aff991b5ddfd0
- Content-Type: `application/json`
- Most tooling-friendly; default for REST consumers / OpenAPI specs.

Example (two Datasets):

```json
{
  "version": "1.0",
  "Parameters": [
    { "id": "ErrorCode", "value": 0 },
    { "id": "ErrorMsg", "value": "" },
    { "id": "param1", "value": "0", "type": "string" }
  ],
  "Datasets": [
    {
      "id": "indata",
      "ColumnInfo": {
        "ConstColumn": [
          { "id": "ConstCol1", "value": 10 },
          { "id": "ConstCol2", "type": "string", "size": "256", "value": 10 }
        ],
        "Column": [
          { "id": "Column0" },
          { "id": "Column1", "type": "string", "size": "256" }
        ]
      },
      "Rows": [
        { "_RowType_": "U", "Column0": "", "Column1": "zzz" },
        { "_RowType_": "O", "Column0": "", "Column2": "" },
        { "_RowType_": "N", "Column0": "A", "Column1": "B" },
        { "_RowType_": "D", "Column0": "a", "Column1": "b" },
        { "_RowType_": "I", "Column0": "",  "Column1": "" }
      ]
    },
    {
      "id": "indata2",
      "ColumnInfo": {
        "Column": [
          { "id": "Column0" },
          { "id": "Column1", "type": "string", "size": "256" },
          { "id": "Column2", "type": "string", "size": "256" }
        ]
      },
      "Rows": [
        { "Column0": "A", "Column1": "B" },
        { "Column0": "a", "Column1": "b", "Column2": "c" },
        { "Column0": "",  "Column1": "",  "Column2": "" }
      ]
    }
  ]
}
```

## `_RowType_` semantics

Every `Rows[]` entry carries a single-character state flag. The server MUST
dispatch row operations accordingly.

| Flag | Meaning                  | Server action              |
|------|--------------------------|----------------------------|
| `N`  | Normal / unchanged        | Ignore for DML             |
| `I`  | Inserted (client-side)    | `INSERT`                   |
| `U`  | Updated (client-side)     | `UPDATE`                   |
| `D`  | Deleted (client-side)     | `DELETE`                   |
| `O`  | Original snapshot of U/D  | Compare for optimistic lock |

- `O` rows travel alongside `U` / `D` rows to ship the pre-image for
  optimistic concurrency checks. Servers that don't support optimistic
  locking may ignore them.
- A single Dataset may contain a mix of N / I / U / D / O rows. Batch insert
  endpoints (`/sample/board/insert.do`) handle all four ops in one request.

## Format selection — which to use

| Endpoint family      | Recommended           | Reason                                        |
|----------------------|-----------------------|-----------------------------------------------|
| Small CRUD (dept, board) | JSON              | Readable, OpenAPI-friendly                    |
| Wide column load     | JSON                  | ColumnInfo metadata clearer                   |
| Paging / list        | JSON or SSV           | JSON for tooling, SSV when payload > ~1 MB    |
| Large streaming      | SSV                   | Smallest wire size, chunk-friendly            |
| File upload/download | multipart/form-data   | Separate from Dataset formats                 |
| Debug / integration  | XML                   | Easiest to read in browser devtools           |
| Excel export         | xlsx binary           | Not a Dataset format                          |

## Content negotiation

Runners use the request `Content-Type` to pick a parser:

- `application/xml` → XML parser
- `application/json` → JSON parser
- `text/plain` starting with `SSV:` → SSV parser
- `multipart/form-data` → file upload handler (no Dataset envelope)

Response `Content-Type` mirrors the request unless the client sent `Accept:`
with a different preference.

## Source of truth

When this doc and a runner's behavior disagree, the runner is wrong. File an
issue at https://github.com/JasonMMo/nexacroN-fullstack/issues .
