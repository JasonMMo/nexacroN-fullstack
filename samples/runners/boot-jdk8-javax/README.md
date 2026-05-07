# runner: boot-jdk8-javax

Thin Spring Boot 2 entry-point (legacy jdk8 path).

| Key            | Value                                 |
|----------------|---------------------------------------|
| JDK            | 8                                     |
| servletApi     | javax                                 |
| framework      | spring-boot                           |
| spring         | 5.x                                   |
| spring-boot    | 2.x                                   |
| packaging      | jar                                   |
| run command    | `mvn spring-boot:run`                 |
| business tree  | `samples/shared-business/jdk8-javax`  |

## Runtime license

xapi performs a classpath lookup for the literal filename
`NexacroN_server_license.xml` at startup (and again on the first
`PlatformTransaction.checkLicense()` call inside
`HttpPlatformRequest.receiveData()`). Drop your operator-issued license at:

```
src/main/resources/NexacroN_server_license.xml
```

The file is gitignored repo-wide — never commit a TOBESOFT-issued
runtime license to a public repository. Without it, `POST` requests to
any `*.do` endpoint fail with a license error before the controller
runs.

## Plan 1 status

Placeholder. Runner body comes in Plan 2.
