# Plan11 Verification Report — RelayController(#14) Real Implementation

**Date:** 2026-04-28 (Tuesday)
**Authority:** `.claude/rules/nexacro-fullstack-purpose.md` §2 (endpoint #14)
**Plan source:** `C:\Users\mo\.claude\plans\snuggly-noodling-nygaard.md`
**Verifier:** Opus (main session, direct)
**Method:** Build verification + 4-case smoke test (503 fallback × httpbin passthrough × jakarta/javax) with raw curl + path-string + Content-Type + body-fidelity assertions.

---

## Result: PASS — 4/4 smoke tests, both lanes BUILD SUCCESS

The Plan8 Phase F stub (`RELAY_STUB_OK`) for endpoint #14 has been replaced
with a real configurable HTTP passthrough relay on both `boot-jdk17-jakarta`
and `boot-jdk8-javax`. Body fidelity is preserved by writing directly to
`HttpServletResponse.getOutputStream()`, bypassing Spring's
`HttpMessageConverter` selection.

---

## 1. Authoritative Spec (recap)

| # | Method | Path | Consumes | Produces |
|---|--------|------|----------|----------|
| 14 | POST | `/uiadapter/relay/exim_exchange.do` | `multipart/form-data` | `application/octet-stream` (Content-Type forwarded from upstream) |

Path/consumes/produces declarations remain **unchanged** (rules §2 contract preserved).

---

## 2. Design (user-approved 2026-04-28)

| Concern | Decision |
|---|---|
| Q1 — Hardcoded vs configurable URL | **Configurable** via `nexacro.relay.exim.url` in `application.yml` |
| Q2 — Response framing | **Simple but nexacro-format compatible** — pass upstream `Content-Type` through, body as raw bytes |
| Q3 — HTTP client | **`RestTemplate`** with `SimpleClientHttpRequestFactory` + `ByteArrayHttpMessageConverter` + `FormHttpMessageConverter` + `StringHttpMessageConverter` |
| Fallback when URL empty | **HTTP 503** + nexacro-envelope JSON `{"version":"1.0","Parameters":[{"id":"ErrorCode","value":-1},{"id":"ErrorMsg","value":"EXIM relay url not configured"}],"Datasets":[]}` |

---

## 3. Component Layout (per lane, mirrored)

```
runner/boot{17,8}/
├── config/
│   ├── RelayProperties.java          NEW  @ConfigurationProperties(prefix="nexacro.relay.exim")
│   └── RelayHttpConfig.java          NEW  @Bean("relayRestTemplate")
├── service/
│   └── RelayService.java             NEW  Multipart passthrough core (writes to HttpServletResponse)
└── controller/
    └── RelayController.java          REPLACE  Stub → service delegation, void return
```

Plus `application.yml` per lane:

```yaml
nexacro:
  relay:
    exim:
      url:               # blank → 503 fallback
      connect-timeout: 5000
      read-timeout: 30000
      forward-headers:   # optional static headers
```

---

## 4. Defect Encountered + Fixed (in-flight)

### Symptom
Initial javax-lane Tests C/D returned the response body **base64-wrapped as a JSON string**:
```
"eyJ2ZXJzaW9uIjoiMS4wIiwiUGFyYW1ldGVycyI6..."
---HTTP=503 CT=application/json
```

### Root cause
Spring Boot 2.7's `AbstractMessageConverterMethodProcessor.writeWithMessageConverters` picks the converter based on the **preset** response `Content-Type` (set via `ResponseEntity.headers().setContentType(application/json)`) — not the controller's `produces` declaration. For `application/json`, `MappingJackson2HttpMessageConverter` is selected before `ByteArrayHttpMessageConverter` (its `supportedMediaTypes` does not include `application/json`). Jackson's default `byte[]` serialization is base64 → JSON string.

Spring Boot 3.3 (jakarta lane) coincidentally selects `ByteArrayHttpMessageConverter` correctly for the same scenario, so the defect was lane-specific until the fix.

### Failed approach
`WebMvcConfigurer.extendMessageConverters` to pin a wide-mediaType `ByteArrayHttpMessageConverter` at index 0. Did not take effect (likely overridden by Spring Boot autoconfiguration ordering).

### Final fix
Both `RelayService` implementations rewritten to **bypass converter selection entirely** by writing the response body directly to `HttpServletResponse.getOutputStream()`:

```java
public void relay(MultipartHttpServletRequest req, HttpServletResponse resp) throws IOException {
    ...
    resp.setStatus(upstream.getStatusCode().value());
    resp.setContentType(upstreamContentType.toString());
    resp.setContentLength(body.length);
    resp.getOutputStream().write(body);
    resp.getOutputStream().flush();
}
```

`RelayController` signature changed to `public void relayEximExchange(MultipartHttpServletRequest, HttpServletResponse) throws IOException`. Path/consumes/produces annotations remain identical (rules §2 contract).

Jakarta lane mirrored for byte-for-byte consistency even though it worked without the fix — predictability outweighs minimal diff.

---

## 5. Smoke Test Results

| # | Lane | Port | URL configured | HTTP | Content-Type | Body | Verdict |
|---|------|------|----------------|------|--------------|------|---------|
| A | jakarta | 18080 | (empty) | 503 | application/json | Raw nexacro-envelope JSON, `ErrorCode=-1` | ✅ PASS |
| B | jakarta | 18080 | `https://httpbin.org/post` | 200 | application/json | Raw httpbin echo (`field_a=hello`, `field_b=world` confirmed in `form` block) | ✅ PASS |
| C | javax | 18081 | (empty) | 503 | application/json | Raw nexacro-envelope JSON, `ErrorCode=-1` | ✅ PASS |
| D | javax | 18081 | `https://httpbin.org/post` | 200 | application/json | Raw httpbin echo (`field_a=hello`, `field_b=world` confirmed) | ✅ PASS |

### Sample (Test C — javax + empty URL, post-fix)
```
$ curl -sS -w "\n---HTTP=%{http_code} CT=%{content_type}\n" \
    -X POST -F field_a=hello -F field_b=world \
    http://localhost:18081/uiadapter/uiadapter/relay/exim_exchange.do

{"version":"1.0","Parameters":[{"id":"ErrorCode","value":-1},{"id":"ErrorMsg","value":"EXIM relay url not configured"}],"Datasets":[]}
---HTTP=503 CT=application/json
```

### Sample (Test D — javax + httpbin, post-fix)
```
{
  "args": {},
  "data": "",
  "files": {},
  "form": {
    "field_a": "hello",
    "field_b": "world"
  },
  "headers": {
    "Accept": "application/octet-stream, */*",
    "Content-Type": "multipart/form-data;boundary=...",
    ...
  },
  ...
}
---HTTP=200 CT=application/json
```

> **Path note:** Tomcat `server.servlet.context-path=/uiadapter` + `@RequestMapping("/uiadapter")` results in a double-prefix URL `/uiadapter/uiadapter/relay/exim_exchange.do`. This matches the pattern of all other 13 controllers in the runner — the contract path itself (`/uiadapter/relay/exim_exchange.do`) is what nexacro UI submits via transaction; the leading `/uiadapter` is the servlet container context.

---

## 6. Build Verification

| Lane | Command | Result |
|------|---------|--------|
| jakarta (JDK17 source/target) | `mvn -DskipTests compile -q` | BUILD SUCCESS |
| javax (JDK8 source/target, JDK17 launcher) | `mvn -DskipTests compile -q` | BUILD SUCCESS |

---

## 7. Git Commits (per-file rule preserved)

### Phase A+B+C+D (initial implementation, pre-fix)
| SHA | Lane | File |
|---|---|---|
| `656425e` | jakarta | `config/RelayProperties.java` |
| `c262984` | jakarta | `config/RelayHttpConfig.java` |
| `bbcf224` | jakarta | `service/RelayService.java` |
| `4d0177b` | jakarta | `controller/RelayController.java` |
| `8b6cb7e` | jakarta | `application.yml` |
| `2d6781c` | javax | `config/RelayProperties.java` |
| `7772797` | javax | `config/RelayHttpConfig.java` |
| `53a3285` | javax | `service/RelayService.java` |
| `9ebb16c` | javax | `controller/RelayController.java` |
| `83562a7` | javax | `application.yml` |

### Phase F follow-up (defect fix)
| SHA | Lane | File | Change |
|---|---|---|---|
| `634f813` | javax | `service/RelayService.java` | Direct `HttpServletResponse.getOutputStream()` write |
| `f66a9f1` | javax | `controller/RelayController.java` | Signature → `void` + `HttpServletResponse` arg |
| `f166be9` | jakarta | `service/RelayService.java` | Mirror of javax fix |
| `92cfbf2` | jakarta | `controller/RelayController.java` | Mirror of javax fix |

### Phase G (closure)
| Repo | SHA | File |
|---|---|---|
| `nexacro-claude-skills` | `bd43490` | `tasks/todo.md` (Plan11 closure entry) |

---

## 8. Anti-Pattern Check

- ✅ Authority anchored on rules §2, not on implementation or openapi.yaml.
- ✅ No `@PostMapping` path/consumes/produces tampering on endpoint #14.
- ✅ No "happens to work" tolerance — javax defect was diagnosed to root cause and fixed before closure.
- ✅ Per-file commits maintained throughout (15 commits across 2 repos).
- ✅ Dual-lane mirror preserved (jakarta + javax byte-for-byte except for servlet import).
- ✅ Smoke tests assert raw-byte body fidelity, not "non-5xx".

---

## 9. Out of Scope (deferred to future plans)

- Authentication beyond static header injection (mTLS / OAuth refresh)
- Retry / circuit-breaker (Resilience4j)
- Streaming for >10 MB payloads (`StreamingResponseBody` + `ResponseExtractor`)
- `core/` shared extraction (current scope keeps code per-lane like the other 13 controllers)
- WebFlux variant (separate `nexacro-webflux` repo per rules §7)

---

## 10. Final Verdict

**Plan11: COMPLETE.**

Endpoint #14 now satisfies the rules §2 contract not only by path/verb but by real "외부 시스템 릴레이" passthrough body. Both runner lanes pass identical 4-case smoke tests. The Spring Boot 2.7 Jackson byte[] base64 quirk is permanently neutralized via direct OutputStream write — this pattern is a recommended template for any future relay/proxy/passthrough endpoint in this monorepo.

Pushed to GitHub: `nexacroN-fullstack` main, `nexacro-claude-skills` master.
