# runner: mvc-jdk8-javax

Traditional Spring MVC 5.x WAR runner, deployable to standalone Tomcat 9 / Jetty 9.
This is the javax twin of ``mvc-jdk17-jakarta``.

| Key | Value |
|---|---|
| JDK | 8 |
| servletApi | javax |
| framework | spring-mvc |
| spring | 5.3.x (raw, no Boot BOM) |
| packaging | war |
| run command | deploy to Tomcat 9 / Jetty 9 |

## Build

```bash
mvn clean package
# Output: target/uiadapter.war
```

## Deploy to Tomcat 9

1. Copy `target/uiadapter.war` to Tomcat 9 `webapps/` directory.
2. Start Tomcat and access: `http://localhost:8080/uiadapter/packageN/index.html`

## Endpoints

Context path: `/uiadapter/` (WAR filename).

| Path | Description |
|------|-------------|
| `/uiadapter/packageN/login.do` | Login info |
| `/uiadapter/packageN/board/select_datalist.do` | Board list |
| `/XExportImport.do` | Grid export/import (xeni) |

See [uiadapter-runner-cookbook.md](../../../docs/uiadapter-runner-cookbook.md).

## Key differences from boot-jdk8-javax

- No `spring-boot-*` dependencies
- `web.xml` + Java `@Configuration` replaces Spring Boot auto-config
- `javax.servlet-api` and `javax.servlet.jsp-api` are `provided` scope
- `mybatis-spring:2.1.2` (Spring 5 / javax lane)

## Lane coordinates

| Artifact | Version |
|----------|---------|
| `nexacroN-xapi` | 1.2.4-SNAPSHOT |
| `nexacroN-xeni` | 1.5.21-SNAPSHOT |
| `uiadapter-spring-core` | 1.4.19.2-SNAPSHOT |
| `uiadapter-spring-dataaccess` | 1.4.19-SNAPSHOT |
| `uiadapter-spring-excel` | 1.5.21-SNAPSHOT |

