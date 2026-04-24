# nxui — Nexacro N v24 `packageN` project body

This directory holds the **shared `packageN` project** that the `nexacro-fullstack-starter` plugin sparse-clones into a consumer's `src/main/nxui/` tree.

## Status

**As of 2026-04-24** — populated from `asis/jakarta-example/src/main/nxui/packageN` production tree (the same tree used by the existing jakarta sample runner). 56 MB, 1531 files, including:

- `packageN.xprj` / `packageN.xadl` — project + ADL
- `typedefinition.xml` — **patched** Services block (see below)
- `appvariables.xml`, `environment.xml`, `bootstrap.xml` (via xadl)
- `_resource_/` — `_theme_`, `_initvalue_`, `_images_`, `_font_`, `_xcss_` subtrees
- `nexacrolib/` — 1379-file runtime (`component/`, `framework/`, `resources/`)
- `frame/` — 8 frames (`frameBottom`, `frameLeft`, `frameLogin`, `frameMain`, `frameMDI`, `frameTop`, `frameWork`, `frameWorkTitle`)
- `pattern/` — 4 patterns (`pattern01-transaction`, `pattern02-excel`, `pattern03-FileUpTransfer`, `pattern04-largeData`)
- `sample/` — 13 sample forms (Calendar, Grid, Message, Popup, Script, FileUpDownloadTrans, streaming video, bulk columns, etc.)
- `cmm/` — `cmmAlert.xfdl`, `cmmConfirm.xfdl`
- `lib/cmmInclude.xjs`
- `debugging/`, `images/`

## Services contract (typedefinition.xml)

The Services block is the only file that **diverges from the jakarta-example source**. It has been restructured into two halves:

1. **Core 9 static services** (canonical nexacro N v24 layout — fixed at all times):

   | prefixid | type     | url                                    |
   |----------|----------|----------------------------------------|
   | theme        | resource | `./_resource_/_theme_/`            |
   | initvalue    | resource | `./_resource_/_initvalue_/`        |
   | imagerc      | resource | `./_resource_/_images_/`           |
   | font         | resource | `./_resource_/_font_/`             |
   | extPrototype | js       | `./nexacrolib/component/extPrototype/` |
   | lib          | js       | `./lib/`                           |
   | frame        | form     | `./frame/`                         |
   | xcssrc       | resource | `./_resource_/_xcss_/`             |
   | images       | file     | `./images/`                        |

2. **Scaffold-time auxiliaries** (tokenised or form-folder mounts required by the bundled sample/pattern forms):

   | prefixid | type | url                 | note                                       |
   |----------|------|---------------------|--------------------------------------------|
   | svcurl   | JSP  | `{{BACKEND_URL}}`   | replaced by the plugin during scaffold     |
   | cmm      | form | `./cmm/`            | required by `cmmAlert`/`cmmConfirm` popups |
   | sample   | form | `./sample/`         | required by frameLeft navigation           |
   | pattern  | form | `./pattern/`        | required by frameLeft navigation           |

When the plugin scaffolds a consumer project it substitutes `{{BACKEND_URL}}` with the value the caller supplied (e.g. `http://localhost:8080/uiadapter/`). No other tokens in this tree — the xadl reads the backend URL through `objEnv.services["svcurl"]`.

## Build path (xfdl → xjs)

The consumer project compiles this tree with `nexacrodeploy.exe` and emits `.xjs` files to its Spring static path:

| Runner         | Build path                                 |
|----------------|--------------------------------------------|
| `boot-*`       | `./src/main/resources/static/packageN/`    |
| `mvc-*`, `egov*-mvc-*` | `./src/main/webapp/packageN/`      |
| `webflux-*`    | `./src/main/resources/static/packageN/`    |

Use the `/nexacro-build` user skill (or `nexacrodeploy.exe -P packageN.xprj -O <build-path> -B ./nexacrolib -GENERATERULE <SDK>/generate`) to do the compile.

## Why ship `nexacrolib` in-tree

Shipping the 55 MB `nexacrolib/` runtime as part of the monorepo is the same pattern the asis jakarta-example uses. The plugin's sparse-clone picks this directory up in a single pull; no additional SDK copy step is required for the user. If a user has a different Nexacro SDK installed, they can overwrite this folder locally — everything is file-system, no install metadata.

## What's next (out of v1.8.0 scope)

The following were raised during v1.8.0 review and will land in **v1.8.1**:

- `core/` module import — `xapi`/`xeni`/`uiadapter` split into javax + jakarta jars; runner `pom.xml` files updated to depend on them
- 14-endpoint contract alignment — current controllers diverge between runners; spec requires shared `/login.do`, `/select_datalist.do`, `/advancedUploadFiles.do`, etc.
- `SKILL.md` Step 6 — nexacrodeploy build step inserted before server run, with auto-invocation of the `/nexacro-build` user skill
