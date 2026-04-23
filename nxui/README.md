# nxui

Nexacro N v24 **packageN** UI project — the front-end all 8 runners ship.

Tokenized with `{{PROJECT_NAME}}`, `{{BACKEND_URL}}`, `{{CONTEXT_PATH}}`,
`{{SERVER_PORT}}` — the `nexacro-fullstack-starter` skill substitutes these
at scaffold time.

## Planned layout (Plan 2)

```
nxui/
├── packageN.xprj            # project manifest
├── packageN.xadl            # svcurl = {{BACKEND_URL}}
├── typedefinition.xml       # 31-component + frame:: service
├── appvariables.xml
├── bootstrap.xml
├── _resource_/
├── frame/                   # 8 frames (Login / MDI / Top / Left / Main / Bottom / Work / WorkTitle)
│   ├── frameLogin.xfdl
│   ├── frameMDI.xfdl
│   └── ...
├── Base/
│   └── main.xfdl
├── pattern/                 # pattern01-04.xfdl (reference layouts)
└── sample/                  # sample*.xfdl covering the 15 endpoints
```

Based on the official `nexacron/uiadapter-jakarta/packageN` sample
(MDI + VFrameSet[44,0,*,0] + HFrameSet[240,*] topology).

## Plan 1 status

**Placeholder.** The xfdl source tree is imported in Plan 2. This README
keeps the directory present for git + sparse-checkout.

## Backend wiring

Every runner serves the xfdl at `{{CONTEXT_PATH}}/` and the transport at
`{{CONTEXT_PATH}}/xapi`. Defaults:
- `{{BACKEND_URL}}` = `http://localhost:8080/uiadapter/`
- `{{CONTEXT_PATH}}` = `/uiadapter`
- `{{SERVER_PORT}}` = `8080`

Change via the skill's `--name` / `--port` arguments or manually edit
`packageN.xadl` + the runner's `application.yml`.
