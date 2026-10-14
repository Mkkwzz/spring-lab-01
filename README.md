# spring-lab-01

## LAB 4 TESTING

Spring AOP: logging, timing, audit, slow-method log (Variant 2).
Run with the default profile (port 8080). Do not use the `prod` profile for
screenshots: it sets `kz.iitu` to WARN, so the INFO log lines are hidden.

```bash
./mvnw clean test        # Windows: mvnw.cmd clean test
./mvnw spring-boot:run   # Windows: mvnw.cmd spring-boot:run
```

Threshold: `app.aspect.slow-threshold-ms` in `application.yml` (default 200).

PowerShell note: use `curl.exe`, not `curl` (alias of Invoke-WebRequest).

| Endpoint | Purpose |
| --- | --- |
| `GET /api/lab4/item/{id}` | fast call (~50 ms), not audited |
| `GET /api/lab4/items?limit=5` | slow call (~300 ms), audited `CATALOG_LIST` |
| `DELETE /api/lab4/item/{id}` | `id <= 0` throws, audited `CATALOG_REMOVE` |
| `GET /api/lab4/proxy` | proxy class information |
| `GET /api/lab4/remove-twice/{id}` | self-invocation (advice bypassed) |
| `GET /api/lab4/remove-twice-fixed/{id}` | same logic via self reference (advice applied) |
| `GET /api/lab4/slow-calls` | Variant 2: recorded slow calls |

### Screenshot plan

1. **Project structure.** Project view with `aspect/`, `audit/`, `service/`,
   `web/Lab4Controller.java` expanded. Shows Pointcuts, LoggingAspect,
   TimingAspect, AuditAspect, SlowMethodAspect, `@Audited`, CatalogService.
2. **Startup.** Terminal after `./mvnw spring-boot:run`, ending with
   `Started SpringLab01Application`. Proves the project builds and the aspects
   do not break context start-up, and Lab 3 beans still load.
3. **Normal call with logging, timing and audit (order).** Open
   `http://localhost:8080/api/lab4/items?limit=5`, then screenshot the log.
   Expected order (number the lines on the screenshot):
   `[AUDIT] start CATALOG_LIST` -> `[LOG] -> findAll` -> `[TIME] SLOW: ... ms`
   -> `[LOG] <- findAll returned` -> `[AUDIT] CATALOG_LIST success`.
   Proves @Before, @AfterReturning, @Around and @Order 1/2/3. Also call
   `http://localhost:8080/api/lab4/item/3`: it is logged and timed but has no
   `[AUDIT]` lines.
4. **Exception.** `curl.exe -X DELETE http://localhost:8080/api/lab4/item/0`.
   Log shows `[LOG] ->`, `[LOG] !! ... threw IllegalArgumentException`,
   `[AUDIT] CATALOG_REMOVE failure`, no `[LOG] <-`. Response is HTTP 500.
   Proves @AfterThrowing fires instead of @AfterReturning and the exception is
   rethrown, not swallowed.
5. **Proxy.** `http://localhost:8080/api/lab4/proxy`. Expect `className`
   ending in `CatalogService$$SpringCGLIB$$0`, `superClass` `CatalogService`,
   `isAopProxy` and `isCglib` both `true`. Proves the controller receives a
   CGLIB subclass proxy, not the real object.
6. **Self-invocation, before and after.** Clear the log, open
   `http://localhost:8080/api/lab4/remove-twice/5`: one `[LOG] -> removeTwice`
   and no `[AUDIT]` lines (both `remove` calls bypass the proxy). Then open
   `http://localhost:8080/api/lab4/remove-twice-fixed/5`: `removeTwiceFixed`
   plus two `[LOG] -> remove` and two `[AUDIT] ... CATALOG_REMOVE` records.
   Proves that `this.remove()` skips the proxy and `self.remove()` does not.
7. **Variant 2, slow call recorded.** Call `.../items?limit=5` (and optionally
   `.../item/3`). The `[TIME] SLOW` warning appears in the log. Proves the
   300 ms method exceeds the configured 200 ms threshold.
8. **Slow-calls endpoint.** `http://localhost:8080/api/lab4/slow-calls`.
   Expect `count` >= 1 and entries with `method` (`CatalogService.findAll(..)`),
   `executionTimeMs` (~300), `thresholdMs` (200), `recordedAt`. `findById` must
   not appear. Proves only calls above the threshold are stored.
9. **Git.** `git status` and `git log --oneline -5` on branch `lab04`
   (`git checkout -b lab04`), after
   `git add . && git commit -m "lab04: logging, timing, audit and slow-method aspects"`.
