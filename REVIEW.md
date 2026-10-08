# Spring review: `cleanup/spring-review`

This branch is a cleanup and security pass over the backend, with the small frontend changes the backend changes needed. Every commit is self-contained and passed CI on its own. The screening algorithm and the orbital-element mapping in `parseSatelliteData` were deliberately **not** changed (see "Deliberately not fixed").

**Test results**

- Baseline before any change: CI run on `e57391e` (the branch point), 12/12 tests passed.
- After every commit: CI passed. The final run has 17/17 tests (the original 12, plus 2 DTO JSON tests and 3 controller tests).

**One action for you:** the environment variables were renamed from `NASA_API_*` to `SPACETRACK_API_*`. Update your local environment (or `.env` for docker-compose) before running the app.

---

## The commits, in order

Each entry covers what was wrong, what changed, and why it's better. The "If asked" line is the one-sentence version for an interview.

### 1. Stop logging the Space-Track login response and session cookies (`b02c493`)

**Wrong:** after logging in to Space-Track, the code printed the login response body and the whole cookie store. Those cookies are a live, authenticated session. Anyone who could read the logs (a hosting dashboard, a shared terminal, a log file) could copy them and act as your Space-Track account.

**Changed:** deleted those two print lines. The HTTP status code is still logged, which is enough to see whether login worked.

**Why it's better:** secrets don't end up in logs. Treat logs as semi-public: never log passwords, tokens, cookies or full auth responses.

**If asked:** "Session cookies are credentials, so I removed the code that logged them."

### 2. Remove unauthenticated destructive and write endpoints (`8246d45`)

**Wrong:** the API has no login, and three endpoints let anyone on the internet change the database:
- `POST /api/satellites/clear-all` deleted everything.
- `POST /api/satellites` saved whatever JSON it was sent. Include an existing `"id"` and it **overwrote** that record, because JPA's `save()` does an update when the id already exists.
- `POST /api/alerts` did the same for alerts.

**Changed:** removed all three. The frontend never used them. I didn't add a dev-profile reset because none is needed: the "load data" endpoints already clear the old data first, which is the job `clear-all` was added for. `SatelliteService.saveSatellite()` became unused and was removed. The README API table no longer lists `clear-all`.

**Why it's better:** the safest endpoint is one that doesn't exist. Without authentication, any write endpoint can be used by anyone.

**If asked:** "`save()` with a client-supplied id is an update, so a public create endpoint was also a public overwrite endpoint."

### 3. Stop installing the Space-Track cookie manager JVM-wide (`1408a09`)

**Wrong:** `CookieHandler.setDefault(cookieManager)` made the Space-Track cookie store the default for *every* HTTP connection in the JVM. The session cookie stayed in global state after the fetch finished.

**Changed:** removed that line. The `HttpClient` is already given the cookie manager via `.cookieHandler(cookieManager)`, which is all it needs.

**Why it's better:** no global side effects. The cookies live only as long as the one client that uses them.

### 4. Rename NASA references to Space-Track (`ad2b6f7`)

**Wrong:** the data comes from Space-Track.org, which is run by the US Space Force, not NASA. The class, properties, environment variables, route, button and guide text all said NASA.

**Changed:**
- `NasaApiService` → `SpaceTrackApiService`
- `nasa.api.*` properties → `spacetrack.api.*`
- `NASA_API_*` environment variables → `SPACETRACK_API_*` (docker-compose and CI)
- `/fetch-nasa-data` → `/fetch-spacetrack-data`, and `fetchNasaData` → `fetchSpaceTrackData` in `api.js`
- the "Fetch NASA Data" button and the Guide text
- both READMEs

**Why one commit:** a half-done rename breaks the app. Rename the route without the frontend and the button calls a URL that doesn't exist. Rename the property without the environment variables and Spring fails to start because it can't resolve `${spacetrack.api.username}`.

### 5. Use constructor injection instead of `@Autowired` fields (`29758c0`)

**Wrong:** dependencies were injected straight into private fields with `@Autowired`. That works, but the fields can't be `final`, the class's dependencies are hidden, and you can't create the class in a test without Spring (or reflection).

**Changed:** every controller and service now has `private final` fields and a constructor that sets them. When a class has exactly one constructor, Spring uses it automatically, so `@Autowired` isn't needed. The `@Value` settings in `SpaceTrackApiService` moved onto constructor parameters.

**Why it's better:**
- The object is fully built and can't be changed afterwards.
- The constructor shows what the class depends on. A constructor with too many parameters is a visible sign the class does too much.
- A unit test can call `new SatelliteService(mockRepo, ...)` directly.

The test classes still use field injection. JUnit creates test classes itself, so that's normal.

**If asked:** "Constructor injection gives me immutability, explicit dependencies, and easy testing. It's what the Spring team recommends."

### 6. Replace `System.out.println` with an SLF4J logger (`3a844f2`)

**Wrong:** the services printed to stdout and called `e.printStackTrace()`. That output has no timestamp, level or class name, can't be filtered or turned off, and sends stack traces to stderr away from the related message.

**Changed:** each class has `private static final Logger log = LoggerFactory.getLogger(X.class);`. SLF4J with Logback already comes with Spring Boot, so no new dependency. Messages use placeholders (`log.info("Found {} collisions", n)`). Errors use `log.error("message", e)`, which logs the full stack trace with the message. Noisy detail is at DEBUG level.

**Why it's better:** log levels can be configured per package in `application.properties`, every line carries context, and a placeholder message is only built if that level is enabled.

### 7. Move repository access out of `SatelliteController` into `SatelliteService` (`be8de43`)

**Wrong:** the controller injected three repositories and ran the same three `deleteAll()` calls in two endpoints. Database logic sat in the web layer, and it was duplicated.

**Changed:** added `SatelliteService.deleteAllData()`, which owns the delete order: alerts, then predictions, then satellites. The order matters because each table has a foreign key to the next. The controller now only talks to services.

**Why it's better:** each layer has one job. The controller handles HTTP, the service holds the rules, and the repository talks to the database. The delete rule now exists in one place, which made the next commit a one-line change.

### 8. Make collision detection and data reset transactional; drop double save (`36cfca3`)

**Wrong (transactions):** `detectCollisions()` deletes all alerts and predictions, then rebuilds them. Without a transaction, every repository call commits on its own. An exception halfway through leaves the old results deleted and the new ones half-saved. `deleteAllData()` had the same problem across its three deletes.

**Changed:** both methods are `@Transactional`. Spring wraps the bean in a proxy. The proxy opens a transaction when the method starts, commits if it returns normally, and rolls back if it throws a `RuntimeException`.

**Gotcha worth knowing:** this only works when the call comes from *another* bean, as it does here (controller → service). If a method in the same class calls a `@Transactional` method, the call skips the proxy and gets no transaction.

**Wrong (double save):** `createPrediction()` saved each prediction, and then `detectCollisions()` called `saveAll()` on the same list.

**Changed:** removed the `saveAll()`. The first save had to stay: the `Alert` created right after each prediction has a foreign key to it, so the prediction must already be saved (and have an id). Inside the transaction the predictions are managed entities anyway, so the extra `saveAll()` did nothing useful.

**If asked:** "The method was delete-then-rebuild, so it has to be all-or-nothing. That's what a transaction gives you."

### 9. Fix stale and wrong comments (`559f975`)

- "11 satellites, checking 55 pairs" came from an early dataset. It now gives the formula, n·(n−1)/2, and the 500-satellite figure of 124,750 pairs, which shows the O(n²) cost.
- A comment pointed to a URL that doesn't exist (`/detection-collisions`).
- A comment said the fetch gets 100 satellites. The query limit is 500.

A wrong comment is worse than no comment, because readers trust it.

### 10. Restore the interrupt flag when the Space-Track fetch is interrupted (`2abe05d`)

**Wrong:** `HttpClient.send()` can throw `InterruptedException`, for example when the app is shutting down. The code caught it together with `IOException` and carried on. Catching `InterruptedException` *clears* the thread's interrupted flag, so the code higher up never finds out the thread was asked to stop.

**Changed:** it has its own catch block, which calls `Thread.currentThread().interrupt()` to set the flag again.

**If asked:** "If you can't rethrow `InterruptedException`, you restore the flag. Otherwise you've swallowed a stop request."

### 11. Remove unused methods and imports (`775c38a`)

Removed `getCollisionCount()`, `CollisionPredictionService.getAllPredictions()` and `savePrediction()`, `AlertRepository.findByAlertLevel()`, and two unused imports. Nothing called them. Dead code makes the codebase look bigger and makes every change harder to reason about. Git history keeps them if they're ever needed.

### 12. Remove unused webflux and httpclient5 dependencies (`15ea50c`)

The pom included `spring-boot-starter-webflux` and Apache `httpclient5`, but the Space-Track client uses the JDK's own `java.net.http.HttpClient`. Nothing used WebClient, Reactor or Apache HTTP. Removing them shrinks the jar, cuts libraries that would need security updates, and makes it clear this is a plain Spring MVC app. (With both web and webflux present, Boot picks MVC, but a reader has to know that.)

### 13. Return DTOs from the API instead of JPA entities (`3ba4811`)

**Wrong:** controllers returned `@Entity` objects, so the database model *was* the API. Adding a column would silently change the JSON. A two-way relationship could cause infinite recursion or lazy-loading errors during serialisation. Internal fields could leak.

**Changed:** a new `dto` package with three Java **records**: `SatelliteResponse`, `CollisionPredictionResponse` and `AlertResponse`. Each has a static `from(entity)` method, and the controllers map with `.stream().map(SatelliteResponse::from).toList()`. Services still work with entities, so JPA stays inside the service and repository layers.

**Why records:** they're immutable, have no boilerplate, and Jackson serialises them by component name.

**The JSON shape is deliberately identical**, so the frontend didn't change. `ResponseJsonShapeTest` proves it by serialising an entity and its DTO and asserting the two JSON trees are equal. It needs no database.

**If asked:** "DTOs decouple the API contract from the database schema. I can change one without breaking the other."

### 14. Add exception handling with `@RestControllerAdvice` and JSON responses (`13bebdd`)

**Wrong:** every failure came back as HTTP **200**.
- An unknown satellite or alert id returned 200 with an empty body, because the service returned `null`.
- A failed Space-Track login or a network error became a String like `"Login failed …"`, sent with 200. A client couldn't tell success from failure without parsing the text. One path also forwarded Space-Track's raw error body to the browser.

**Changed:**
- **Exceptions:** `ResourceNotFoundException` and `SpaceTrackException`. Both are `RuntimeException`s, so methods don't need `throws` clauses.
- **`GlobalExceptionHandler`:** a `@RestControllerAdvice`, which works like one `@ExceptionHandler` shared by every controller. It maps not-found to **404** and Space-Track failures to **502 Bad Gateway** (our server is fine, the upstream server it depends on failed). Both return a JSON body: `{"message": "..."}`.
- **Services:** `findById(id).orElseThrow(...)` replaces `orElse(null)`. The Space-Track methods return a count or throw.
- **Action endpoints:** `detect-collisions`, `fetch-spacetrack-data` and `load-backup-data` return `ResponseEntity<MessageResponse>`, which is `{"message": "..."}` with an explicit status. The list `GET`s stay as plain return values, which Spring sends as 200.
- **Frontend:** `SatelliteList.jsx` reads `response.data.message`, and on error shows the backend's message instead of axios's generic "Request failed with status code 502".

**I deliberately didn't add a catch-all `@ExceptionHandler(Exception.class)`.** It would also catch Spring's own exceptions (a bad path variable is a 400, an unknown URL a 404) and turn them all into 500s.

`SatelliteControllerTest` is a `@WebMvcTest`. It loads only the web layer, with the services replaced by `@MockitoBean` mocks, so it runs without a database. It checks the 404, the 502 and the JSON message.

**If asked:** "Status codes are part of the API. `@RestControllerAdvice` keeps the error-to-HTTP mapping in one place, so services just throw meaningful exceptions."

### 15. Keep the existing data when a Space-Track fetch fails (`01e08d8`)

**Wrong:** the fetch endpoint deleted every satellite, prediction and alert *first* and only then contacted Space-Track. A failed login, a network blip or a Space-Track outage left the database empty.

**Changed:** the delete now happens inside `SpaceTrackApiService`, after the download succeeds and just before saving. A failed fetch throws `SpaceTrackException` before anything is deleted, so the old dataset stays and the client gets a 502. The backup loader clears in the same place for consistency. `SpaceTrackApiService` now uses `SatelliteService.deleteAllData()`. There's no circular dependency, because `SatelliteService` only uses repositories. The mapping in `parseSatelliteData` is unchanged.

### About Priority 3 item 9, `@Valid`

No commit, on purpose. `@Valid` checks a `@RequestBody`, and after commit 2 the API has no request bodies left: every remaining endpoint takes nothing or a numeric path variable. Spring already rejects a non-numeric id with 400. Adding `spring-boot-starter-validation` would be a dependency with nothing to do. If a request body is added later (for example, behind authentication), use a request DTO with `@NotBlank` and similar annotations, plus `@Valid`. A `MethodArgumentNotValidException` handler can then be added to `GlobalExceptionHandler`.

---

## What I couldn't verify

- **Integration tests were not run locally.** Docker wasn't running on this machine, so there was no throwaway Postgres. I also deliberately didn't point the tests at your local database, because they wipe it. The `@SpringBootTest` tests were verified **only through GitHub Actions CI**, which passed for every commit. Locally I compiled and ran the DB-free tests (`CollisionMathTest`, `ResponseJsonShapeTest`, `SatelliteControllerTest`) before each commit.
- **No real Space-Track call was made.** No credentials were used. The login, fetch and failure paths are covered only by compiling and by the mocked controller test, not by a live request.
- **The app wasn't run end to end.** I haven't clicked through the UI in a browser. The frontend was only built (`npm ci` then `npm run build`, which succeeded), and its tests weren't run.
- **Rollback isn't tested directly.** `@Transactional` rollback is relied on, not proven by a test. The existing tests pass, but none forces an exception halfway through `detectCollisions()`.
- **docker-compose wasn't run.** The environment variable rename there was checked by reading it, not by starting the stack.

---

## Found but deliberately not fixed

These are larger, structural, or a design decision for you to make.

**Security and operations**
1. **No authentication at all.** `fetch-spacetrack-data`, `load-backup-data` and `detect-collisions` are still public POSTs that replace data, and every fetch uses *your* Space-Track account, which is rate-limited. Anyone can trigger them. The real fix is authentication (out of scope; Spring Security was excluded), or disabling these routes in a deployed demo.
2. **CORS** allows any `*.netlify.app` origin, so any Netlify site can call the API from a browser. It also allows PUT and DELETE, which no endpoint uses.
3. **The tests wipe whatever database they point at.** By default that's the local dev database. A test profile with its own database, or Testcontainers, would isolate them.
4. **`application.properties`** falls back to the password `postgres`. The file is also committed, even though `.gitignore` lists it. `.gitignore` doesn't apply to files that are already tracked. It currently holds only `${ENV_VAR}` placeholders, so nothing secret is in it.
5. **`spring.jpa.hibernate.ddl-auto=update`** lets Hibernate change the schema. A real deployment would use migrations, for example with Flyway.
6. **docker-compose** hard-codes the Postgres credentials and still has the obsolete `version:` key.
7. **CI only runs on pushes to main and on PRs to main.** A pushed branch isn't tested until a PR is open.

**Correctness and design**

8. **The physics model** maps INCLINATION, RAAN and MEAN_MOTION into lat/lon/alt. This is the known, documented limitation of an educational demo. Left alone as instructed.
9. **`parseSatelliteData` swallows exceptions and returns 0.** If Space-Track returns 200 with malformed JSON, the old data has already been cleared and the API reports "fetched 0". Also, the clear and the save are separate transactions. The cleaner fix is to parse into a list first, then clear and save in one transaction. Left because it means restructuring the method that holds the mapping you asked me not to touch.
10. **`deleteAll()` loads every row and deletes them one at a time.** `deleteAllInBatch()` is a single SQL statement each. That's a performance change with cascade implications, so I only noted it.
11. **`AlertService` keeps an in-memory list of recent alerts** that isn't thread-safe (a plain `ArrayList` in a singleton), isn't cleared when the database is, and is lost on restart. The `/api/alerts/in-memory` endpoint that exposes it isn't used by the frontend. Consider removing both.
12. **O(n²) screening.** 500 satellites means 124,750 distance checks per run. That's fine at this size. A spatial index would be needed for the full catalogue.
13. **`riskLevel`, `alertLevel` and `status` are Strings.** Enums would stop typos like `"CRTICAL"`.
14. **`Satellite.setId(long)` takes a primitive** while `getId()` returns a `Long`, so you can't call `setId(null)`. Harmless now that entities aren't deserialised from requests.
15. **Space-Track client details:**
    - The login URL is hard-coded, while the data URL comes from config.
    - The client never logs out.
    - A new `HttpClient` is built on every call.
16. **No paging.** `GET /api/satellites` returns every row.
17. **The frontend waits a fixed 2–5 seconds** with `setTimeout` after loading data before refreshing. The request has already finished when the promise resolves, so the wait isn't needed.
18. **Committed IDE files.** `.idea/` is committed even though it's in `.gitignore`.
19. **Small typos** in comments and test messages ("detecitn", "databse", "fine no collisions"). Left alone to avoid style-only churn.
