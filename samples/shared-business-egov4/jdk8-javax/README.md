# shared-business-egov4 / jdk8-javax

**eGov 4.x** standard-framework business tier (javax lane).

Consumed by runners:
- `egov4-boot-jdk8-javax`
- `egov4-mvc-jdk8-javax`

## Planned contents (Plan 2)

```
jdk8-javax/
├── pom.xml                        # egovframework 4.x / spring5 / javax
└── src/main/
    ├── java/com/nexacro/sample/
    │   ├── controller/            # eGov-annotated controllers
    │   ├── service/               # @Service(EgovServiceAbstract)
    │   └── mapper/
    └── resources/egovProps/
```

eGov-specific additions vs plain spring5 tree:
- `egov-common` / `egov-cmmn` / `egov-simpl` starter jars
- `egovframework-bom` BOM import in pom.xml
- `globals.properties` at `resources/egovProps/`

## Plan 1 status

Placeholder. Source comes in Plan 2.
