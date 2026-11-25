# Lab 12 — evidence

Student: Syrym Yessengeldi, IT1-2411. Variant 2: `LoanService`, branch "a loan extended when a
queue exists", extra requirement: a pagination assertion in the `@DataJpaTest`.

## Starting point

There is no `lab11` branch in this repository (lab 11 was not done here), so `lab12` was created
from `lab10`, the latest branch. Lab 10 had no loans, so the variant class was added in this branch:
`loan/Loan`, `loan/Hold` (a reader queued for a book), `LoanRepository`, `HoldRepository`,
`LoanService`, `LoanNotFoundException`, `LoanExtensionRefusedException`, `web/LoanController`,
`web/LoanRequest` (Bean Validation), `web/LoanExceptionHandler` (`@RestControllerAdvice` returning
`ProblemDetail`) and the Flyway migration `V4__loans.sql`. `/api/loans/**` has its own security
chain: HTTP Basic, stateless, role USER or ADMIN.

Testcontainers in the Spring Boot 4.1.1 BOM is version 2.0.5, where the modules are called
`testcontainers-postgresql` and `testcontainers-junit-jupiter` (the old names `postgresql` and
`junit-jupiter` are not managed any more), so those two names are used, still without `<version>`.

## Tests

| Class | Kind | Tests |
|---|---|---|
| `loan/LoanServiceTest` | unit, Mockito only, no Spring context | 5 |
| `web/LoanControllerTest` | `@WebMvcTest` + `@MockitoBean`, real security chain imported | 4 |
| `loan/LoanRepositoryTest` | `@DataJpaTest` + `TestEntityManager` (H2, PostgreSQL mode) | 3 |
| `LoanApiIntegrationTest` | `@SpringBootTest` + `@Testcontainers` + `@ServiceConnection` PostgreSQL 17 | 1 (skipped: no Docker) |

Security in the web slice: `@WithMockUser(roles = "USER")` on the tests that must reach the
controller, plus one test without a user that expects 401. The chain is imported, not excluded.

Integration test: HTTP Basic as the seeded user `user` (from `V3__security.sql`), sent by
`RestTestClient` in the `Authorization` header.

H2 is used by `@DataJpaTest` (the embedded database that the slice substitutes). It is not
presented as the integration database.

## `./mvnw clean verify` — summary lines

Before the extra test of task 5:

```
[INFO] Tests run: 30, Failures: 0, Errors: 0, Skipped: 1
[INFO] BUILD SUCCESS
```

After the extra test:

```
[WARNING] Tests run: 1, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.001 s -- in kz.iitu.springlab.LoanApiIntegrationTest
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 1
[INFO] BUILD SUCCESS
```

The skipped test is `LoanApiIntegrationTest`: `@Testcontainers(disabledWithoutDocker = true)`
skips it on a machine without Docker instead of failing the build.

## Coverage of `kz.iitu.springlab.loan` (JaCoCo 0.8.16)

| | Instructions (report "Cov.") | Branches | Lines |
|---|---|---|---|
| Before | 68 % (59 of 186 missed) | 66 % (2 of 6 missed) | 38 of 52 covered = 73 % |
| After `aLoanIsNotExtendedAfterItHasBeenExtendedTwice` | 71 % (53 of 186 missed) | 83 % (1 of 6 missed) | 39 of 52 covered = 75 % |

The yellow line (class `LoanService`, line 47):

```
before:  <span class="pc bpc" id="L47" title="1 of 2 branches missed.">  if (loan.getExtensions() >= MAX_EXTENSIONS) {
after:   <span class="fc bfc" id="L47" title="All 2 branches covered.">  if (loan.getExtensions() >= MAX_EXTENSIONS) {
```

Every earlier test reached this condition with 0 extensions, so only the `false` way was taken.
The new test builds a loan that was already extended twice, takes the `true` way and asserts on the
exception message and on `save` never being called. Screenshots of the class view before and after
are in the report (figures "LoanService before" and "LoanService after").

Still uncovered: line 41, `if (loan.isReturned())` — extending a loan whose book is already back.
It should be tested, not deleted: without it a returned loan could be "extended" and a reader could
keep a due date on a book they no longer hold; the test is one more `given(...)` with a returned loan.

Also red: `Hold` getters and `LoanService.open` / `activeLoansOf`. `open` and `activeLoansOf` are
covered only by the Testcontainers test, which is skipped on this machine.

## Docker is not available

Command and error:

```
PS> docker info
docker : The term 'docker' is not recognized as the name of a cmdlet, function, script file,
or operable program.
```

Docker Desktop (and WSL) are not installed on this computer. Running the integration test with
`@Testcontainers` without `disabledWithoutDocker`:

```
./mvnw test -Dtest=LoanApiIntegrationTest
ERROR org.testcontainers.dockerclient.DockerClientProviderStrategy -- Could not find a valid Docker
environment. Please check configuration. Attempted configurations were:
As no valid configuration was found, execution cannot continue.
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0 <<< FAILURE! -- in kz.iitu.springlab.LoanApiIntegrationTest
org.testcontainers.containers.ContainerFetchException: Can't get Docker image: RemoteDockerImage(imageName=postgres:17-alpine, ...)
Caused by: java.lang.IllegalStateException: Could not find a valid Docker environment.
```

What the container test would prove that `@DataJpaTest` does not:

1. Flyway `V1`–`V4` run on a real, empty PostgreSQL 17, so PostgreSQL-only problems (types,
   `bigserial`, reserved words, constraint syntax) show up; H2 in PostgreSQL mode only imitates it.
2. The whole application starts: the two security chains, the `UserDetailsService` reading
   `app_user` from the database, BCrypt, Jackson, validation, the controller, the service with its
   `@Transactional` boundary and the repository — over real HTTP on a random port.
3. The full path POST `/api/loans` → INSERT in PostgreSQL → GET `/api/loans?reader=` returns the
   stored row as JSON, with the real `Authorization: Basic` header. `@DataJpaTest` loads only JPA
   and rolls back after each test; it never touches HTTP, security or the service.

## `em.flush()` experiment (defence item 8)

`em.flush()` was removed from `LoanRepositoryTest` and the class was run with SQL logging; all three
tests still pass. The `insert into loan` statements appear right at `em.persist(...)` because the id
is `GenerationType.IDENTITY`: Hibernate must execute the INSERT to learn the id. The derived query
also triggers an automatic flush (FlushMode AUTO) before it runs. With a sequence-generated id the
inserts would be delayed until that auto-flush. `em.clear()` is what makes the returned `Loan`
objects new instances built from the SELECT result instead of the persisted ones.
