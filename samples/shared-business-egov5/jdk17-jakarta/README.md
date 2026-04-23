# shared-business-egov5 / jdk17-jakarta

**eGov 5.x** standard-framework business tier (jakarta lane).

Consumed by runners:
- `egov5-boot-jdk17-jakarta`

## Planned contents (Plan 2)

```
jdk17-jakarta/
├── pom.xml                        # egovframework 5.x / spring6 / jakarta
└── src/main/
    ├── java/com/nexacro/sample/
    │   ├── controller/
    │   ├── service/
    │   └── mapper/
    └── resources/egovProps/
```

## Plan 1 status

Placeholder. Source comes in Plan 2.

## Why no `egov5-mvc` / `egov4-mvc-jdk17`?

The matrix in the `nexacro-fullstack-starter` plugin rejects those
combinations at scaffold-time — official eGov samples do not exist for
those pairs. See `plugins/nexacro-fullstack-starter/skills/nexacro-fullstack-starter/references/compatibility-matrix.md`
in the sibling repo.
