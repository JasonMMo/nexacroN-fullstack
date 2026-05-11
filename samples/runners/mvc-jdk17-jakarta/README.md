# runner: mvc-jdk17-jakarta

Traditional Spring MVC 6.x WAR runner, deployable to standalone Tomcat 10 / Jetty 11.

| Key            | Value                                   |
|----------------|-----------------------------------------|
| JDK            | 17                                       |
| servletApi     | jakarta                                  |
| framework      | spring-mvc                               |
| spring         | 6.1.x (raw, no Boot BOM)                 |
| spring-boot    | n/a                                      |
| packaging      | war                                      |
| run command    | deploy to Tomcat 10 / Jetty 11           |
| business tree  | shared with boot-jdk17-jakarta           |

## Build



## Deploy to Tomcat 10



## Endpoints

Context path is determined by the WAR filename ( → ).

| Path | Description |
|------|-------------|
|  | Login info |
|  | Board list |
|   | File list  |
|  | Grid export/import (xeni) |

For the full endpoint catalogue see [](../../../docs/uiadapter-runner-cookbook.md).

## Key differences from boot-jdk17-jakarta

- No  dependencies
-  replaces 
-  initializes schema/data via  with  separator
-  is  scope (Tomcat 10 supplies the JSP runtime)
- Relay and file storage configured via 
