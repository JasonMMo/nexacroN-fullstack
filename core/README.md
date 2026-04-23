# core

First-party Nexacro core modules, lane-split.

This directory holds `xapi`, `xeni`, and `uiadapter` — the three modules that
wire Nexacro client-server communication to Spring. Each has a **javax** and
**jakarta** variant because the servlet API packages are hard dependencies.

## Structure (to be filled in Plan 2)

```
core/
├── xapi-javax/       # nexacro transport for jdk8 / spring5 / servlet 4
├── xapi-jakarta/     # nexacro transport for jdk17 / spring6 / servlet 6
├── xeni-javax/       # nexacro Excel import/export (javax)
├── xeni-jakarta/     # nexacro Excel import/export (jakarta)
├── uiadapter-javax/  # nexacro ChildFrame adapter + file upload (javax)
└── uiadapter-jakarta/# nexacro ChildFrame adapter + file upload (jakarta)
```

## Sourcing

These are ports of the public `nexacro-webflux` / `nexacro` reference modules,
split at the package boundary. Source repositories and version matrix are
tracked in the top-level `pom.xml` properties block.

## Plan 1 status

**Skeleton only.** Source trees are not yet imported — that is scheduled for
Plan 2. This README is a placeholder so the directory exists in git and the
sparse-checkout path in the `nexacro-fullstack-starter` skill resolves
correctly.
