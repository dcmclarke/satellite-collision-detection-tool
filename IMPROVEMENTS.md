# Improvements and remake plan

This document has three parts:

1. **[What changed on `improve/next-steps`](#part-1-the-commits-on-this-branch)**, commit by commit, written for interview prep. It continues [REVIEW.md](REVIEW.md), which covers the first cleanup branch.
2. **[An honest assessment](#part-2-assessment-where-the-project-stands)** of where the project stands.
3. **[A recommended remake](#part-3-the-remake-recommendation)** into a standout portfolio project, with a target architecture and a phased roadmap sized for one junior developer.

**Test status:** every commit on this branch passed CI. The last run had 37 backend tests (unit and PostgreSQL integration) and 4 frontend tests, all passing.

---

## Part 1: The commits on this branch

Each entry covers what was wrong, what changed, and why. "If asked" is the one-sentence interview version.

### Protecting the Space-Track account

The biggest remaining risk after the first review was that **anyone could press "Fetch Space-Track Data" over and over**. Each press logs in with your personal account, and [Space-Track's API guidelines](https://www.space-track.org/documentation) are strict:

- fewer than 30 requests a minute and 300 an hour
- GP (orbital element) data at most **once an hour**
- accounts that exceed the limits can be **suspended, permanently for repeat offenders**

Four commits deal with this, from the inside out.

#### 1. Run CI on every push, not just main (`d1631f7`)

**Wrong:** CI only ran for `main` and for PRs that target `main`. A branch had no tests until a PR was opened, and this PR, which is stacked on another branch, would never have been tested.

**Changed:** the workflow runs on every push, plus a manual "Run workflow" button (`workflow_dispatch`). GitHub attaches the result to the commit, so it shows on any PR that contains it.

**If asked:** "Every commit gets tested, wherever it was pushed. That's what let me keep CI green after every single commit."

#### 2. Use the configured Space-Track base URL for login too (`a57cec7`)

**Wrong:** the data request used the `spacetrack.api.url` setting, but the login URL was hard-coded to `https://www.space-track.org`. Overriding the setting would log in to one server and fetch from another. That's inconsistent and untestable.

**Changed:** both URLs are built from the setting. The default is unchanged.

#### 3. Test the Space-Track client against a local fake server (`b2c2e5f`)

**Wrong:** the HTTP code (login, session cookie, data request, errors) had no tests. It only ever ran against the real Space-Track.

**Changed:** `SpaceTrackApiServiceTest` starts the JDK's built-in `com.sun.net.httpserver.HttpServer` on a random local port. Like the real API, the fake server hands out a session cookie on login and refuses the data request without it. The repository and `SatelliteService` are Mockito mocks, so no database is needed. The four tests cover success, a failed login, an HTTP 500 and an unreachable server. The three failure tests also check `deleteAllData()` is **never** called, which proves the "keep the old data on failure" fix from the first PR.

**If asked:** "Constructor injection made this easy. I just call `new SpaceTrackApiService(mockRepo, mockService, ...)` and point it at a fake server. No Spring, no network, no account."

#### 4. Limit Space-Track fetches to once an hour (`f53bbd0`)

**Changed:** a new `FetchCooldown` component, called first in `fetchAndStoreSatellites()`. If the last attempt was less than an hour ago (configurable with `spacetrack.api.min-fetch-interval`), it throws `FetchTooSoonException` before any network call. The global handler turns that into **429 Too Many Requests** with a standard `Retry-After` header and a message the frontend already shows.

Things worth being able to explain:

- **Failed attempts count too.** Repeated failed logins are also a risk to the account.
- **`acquire()` is `synchronized`.** Without it, two requests arriving at the same moment could both read "last fetch was long ago" and both go through. That's a classic check-then-act race.
- **Time is injected as a `java.time.Clock`.** The tests move time forward instead of sleeping. Because there are two constructors (a public one for Spring, a package-private one for tests), `@Autowired` marks the one Spring should use.
- **The state is in memory**, so a restart resets it, and several server instances would each have their own. Fine for one instance. Shared state (database or Redis) would be the next step.

**If asked:** "The API had no login, so anyone could have used up my Space-Track quota and got my account suspended. I added a server-side cooldown that returns 429 with Retry-After, and tested it with a fake clock."

#### 5. Turn off the data-replacing endpoints unless explicitly enabled (`ef1fb36`)

**Wrong:** even with the cooldown, anyone could call `load-backup-data`, `fetch-spacetrack-data` or `detect-collisions` on a deployed server and wipe the data.

**Changed:** a new setting, `app.manual-data-refresh.enabled`, is **off by default**. When it's off, the three endpoints throw before touching any service and return **403** with an explanatory message. A new `application-dev.properties` turns it on. Spring Boot layers `application-{profile}.properties` over `application.properties` when a profile is active, so local development runs with `-Dspring-boot.run.profiles=dev`. docker-compose sets `SPRING_PROFILES_ACTIVE=dev`. A parameterised `@WebMvcTest` checks all three endpoints are refused and that no service is called.

**Why not Spring Security?** There's one owner and no user accounts. The requirement is "these actions don't exist in production", not "these users may do X". Adding a login system for that would be complexity without a reason. If user accounts are ever added, that's when to bring in Spring Security.

**If asked:** "Secure by default. Forgetting the setting leaves the dangerous endpoints off, not on."

#### 6. Add an optional scheduled refresh (`59b02c7`)

With manual refresh off in production, the data needs another way to stay current.

**Changed:** `SpaceTrackIngestJob` fetches and then re-runs detection on a timer:

- **`@Scheduled(fixedDelay)`** (default every 6 hours). The next run starts a fixed time after the previous one *finished*, so runs can never overlap. `fixedRate` would start runs on a fixed clock even if the last one is still going.
- **An initial delay** (7 minutes), so restarting the app repeatedly doesn't hit Space-Track each time.
- **Known failures are logged and skipped.** The old data was kept, so the job just tries again next time. The cooldown still applies, so even a mistyped interval can't break the hourly limit.
- **It's opt-in** (`spacetrack.ingest.enabled`). Both the job and the `@EnableScheduling` config class use `@ConditionalOnProperty`, so tests and local runs never start a background job.

One test checks the fetch-then-detect order and the failure handling with plain Mockito. A second starts the full Spring context with the job enabled, to prove the conditional beans and `@Scheduled` settings load.

### Proving things work

#### 7. Test that a failed detection run is rolled back (`1696642`)

**Wrong:** the first PR made `detectCollisions()` `@Transactional`, but no test showed the rollback happens. The existing tests pass with or without it.

**Changed:** `CollisionDetectionRollbackTest` does a successful run (1 prediction, 1 alert). Then it uses `@MockitoSpyBean` to make `AlertService.saveAlert()` throw during the second run, after the old rows were deleted and a new prediction inserted. It asserts the old prediction and alert are still there. A **spy** is the real bean with chosen methods overridden, so everything except the one simulated failure runs for real.

**If asked:** "I didn't just add `@Transactional`, I proved it works. The test injects a failure halfway through and checks the database is unchanged."

#### 8. Replace the broken default frontend test (`f85558f`)

**Wrong:** the only frontend test was Create React App's generated "renders learn react link". The app has no such link, so it could never pass. Worse, it didn't even load: CRA's Jest 27 can't resolve react-router v7's package `exports`.

**Changed:** `SatelliteList.test.jsx` tests the main page directly. `jest.mock()` replaces the API module with mock functions, so no HTTP happens. The tests check the table shows API data, that search filters it, and that a refused request (403) shows the backend's message.

#### 9. Test and build the frontend in CI (`208eb2c`)

**Changed:** a second, parallel CI job runs `npm ci` (an exact install from the lockfile), `npm test` and `npm run build`, all with `CI=true`. That flag runs the tests once instead of watching, and makes the build fail on lint warnings. The workflow is renamed from "Backend CI" to "CI".

### Starting real propagation

#### 10. An SGP4 propagation spike using Orekit, behind an interface (`05e738a`)

This is the most important commit for the project's future, and the most interesting one to talk about.

**The problem:** the app was meant to propagate orbits but doesn't, because SGP4 is heavy maths. **But you don't have to write SGP4 yourself.** [Orekit](https://www.orekit.org/) is a mature, open-source (Apache 2.0) Java flight-dynamics library from CS GROUP. It implements SGP4/SDP4 for TLE data and is used in real space projects. (I avoided predict4java: its orbital code descends from GPL-licensed C code, which doesn't fit this project's MIT licence.)

**Changed:**

- **`OrbitPropagator` interface:** `propagate(tle, time)` and `epochOf(tle)`. The rest of the app will depend on this, not on Orekit, so the library could be swapped and tests can use a fake. That's the Dependency Inversion Principle in practice.
- **`OrekitSgp4Propagator`:**
  - Checks the TLE's format and checksums first. Orekit's `TLE` constructor doesn't, and I found that out because my test caught it.
  - Lets `TLEPropagator.selectExtrapolator()` pick SGP4 (near-Earth) or SDP4 (deep space, orbital period of 225 minutes or more).
  - Returns a `StateVector` record in km and km/s, in the TEME frame SGP4 works in.
  - Converts times directly between `Instant` and Orekit's `AbsoluteDate`. Going via `java.util.Date` would round to milliseconds, which is up to about 7 m of position error at 7.5 km/s.
- **Leap seconds:** Orekit needs the leap-second table to convert UTC. I bundled the 3 KB public-domain USNO `tai-utc.dat` (the same file Orekit's own data repository ships) instead of the full orekit-data archive (planetary ephemerides, Earth-orientation data, gravity fields), loaded from the classpath.
- **`OrekitSgp4PropagatorTest`:** checks against the **published SGP4 verification case for satellite 00005** from Vallado et al., *Revisiting Spacetrack Report #3* (2006). That's the standard reference used to validate SGP4 implementations, and better than "an ISS position from a website", which has no reliable ground truth. Positions match to **within 1 m** and velocities to within 1 mm/s, at the epoch and 6 hours later. No database or network is needed.

The existing screening doesn't use it yet. Wiring it in is Phase 1 of the roadmap below.

**If asked:** "I didn't write SGP4. I used a library the industry uses, put it behind my own interface, and verified it against the published reference test cases. Knowing when not to write something is part of the job."

### Docs and small fixes

#### 11. Credentials in environment variables, never in a tracked file (`fc2d60e`)

**Wrong, and a real risk:** the setup steps said to copy `application-example.properties` over `application.properties` and type your passwords into it. That file is tracked by git. The `.gitignore` entry for it did nothing, because `.gitignore` only affects files git isn't already tracking. One `git add -A` would have published your Space-Track password.

**Changed:**
- Both READMEs explain environment variables (bash, PowerShell, IntelliJ). The properties file already read every secret from one.
- `application.properties` starts with a warning comment.
- The example file and the misleading `.gitignore` line are gone.
- A root `.gitignore` now ignores `.env` (docker-compose reads it automatically, and nothing stopped one being committed), with a committed `.env.example`.

**If asked:** "`.gitignore` doesn't untrack a file that's already committed, and I found the setup instructions were steering people to put secrets in exactly such a file."

#### 12. README brought up to date, with an honest status and roadmap (`03c980d`)

- Corrected claims that were no longer true, such as "CI/CD runs on every push".
- Documented the dev profile, the hourly limit, the 403/429/502 responses and the JSON error format.
- Updated the project structure.
- Added a configuration table and a "Running the Tests" section, with a warning that the integration tests delete the data in their database.
- Rewrote Project Status to say plainly what's real and what isn't, and replaced the wish-list roadmap with the phased plan below. The documented data-mapping limitation is kept as it was.

#### 13. Remove the in-memory recent-alerts cache (`208300d`)

`AlertService` kept a second copy of every alert in a plain `ArrayList` behind an unused `/api/alerts/in-memory` endpoint. The copy was:

- **not thread-safe:** the service is a singleton shared by all requests
- **never cleared** when the database is cleared
- **outside the transaction:** a rolled-back detection run left its alerts in the list
- **lost on restart**

The database is the source of truth, and `/alerts/recent` already answers the same question from it.

#### 14. CORS allows only the methods the API has (`68c4daa`)

PUT and DELETE were allowed cross-origin, but no endpoint uses them. The list is now GET, POST and OPTIONS (OPTIONS being the browser's preflight request). That's least privilege: widen it deliberately when a feature needs it.

#### 15. Refresh the table right away after loading data (`fc3ba77`)

**Wrong:** after a load, the page waited a fixed 2 or 5 seconds before refreshing the table. The request had already finished. The wait existed because the refresh *cleared the success message*, and the pause was the only thing keeping it on screen.

**Changed:** the refresh happens immediately, and only the action handlers set messages. A new test checks both the satellites and the success message appear.

**If asked:** "A sleep that hides an ordering problem is a smell. I fixed the order and the sleep wasn't needed."

---

### What I couldn't verify

- **No real Space-Track request was made.** No credentials were used. The client is tested against a local fake server that mimics the login, cookie and error behaviour, not against the live API.
- **Integration tests ran only in CI**, against GitHub Actions' throwaway PostgreSQL. Docker wasn't available locally, and I deliberately didn't point the tests at your local database, because they wipe it. Everything that doesn't need a database (Space-Track client, cooldown, scheduled job, controllers, DTOs, propagator, maths, frontend) was also run locally before each commit.
- **The rollback test wasn't run against code without `@Transactional`** to watch it fail. I reasoned through it instead: without the transaction, the second run would leave 0 alerts, and the test asserts 1.
- **The scheduled job has never actually fired.** Its wiring is tested, and its logic is tested with mocks, but no real 6-hour cycle has run.
- **The app wasn't clicked through in a browser.** The frontend was built and its component tests pass.
- **docker-compose wasn't started.** The `SPRING_PROFILES_ACTIVE: dev` line was checked by reading it.
- **Space-Track's limits come from its public documentation page, as of 11 October 2026.** The five-digit catalogue date comes from CelesTrak's GP data formats page.

### Still not fixed (carried over from REVIEW.md, updated)

Fixed on this branch, so removed from the list: CORS methods, the in-memory alert cache, the fixed frontend waits, the hard-coded login URL, CI only on main, and the authentication risk to the Space-Track account (mitigated without adding accounts).

1. **Positions are still computed from orbital elements** (the documented limitation). Roadmap Phase 1 fixes it properly.
2. **`parseSatelliteData` swallows exceptions and returns 0.** Malformed JSON with a 200 status would clear the data and report "fetched 0". Left alone because it's the mapping method you asked me not to touch. It'll be rewritten in Phase 1 anyway.
3. **No Flyway migrations.** `ddl-auto=update` lets Hibernate alter the schema. Add Flyway when the schema changes in Phase 1.
4. **Integration tests use whatever database they're pointed at.** Testcontainers would give each test run its own throwaway PostgreSQL. Recommended in Phase 0.
5. **Strings for risk level and status.** These should be enums.
6. **`Satellite.setId(long)`** takes a primitive while `getId()` returns a `Long`.
7. **No paging** on `GET /api/satellites`.
8. **The CORS `*.netlify.app` wildcard** lets any Netlify site call the API. Set it to your own site's URL when you deploy.
9. **The frontend shows all three data buttons even when they're disabled.** A small `GET /api/config` endpoint could let it hide them.
10. **docker-compose** still hard-codes the local Postgres password (fine for local use) and has the obsolete `version:` key.
11. **`.idea/` IDE files are committed.**

---

## Part 2: Assessment: where the project stands

**The honest one-line summary today:** a well-engineered Spring Boot + React CRUD app with good security hygiene and tests, wrapped around a screening algorithm that doesn't mean anything physically yet.

**Strengths a recruiter will see:**
- clean layering (controller → service → repository) and DTOs
- constructor injection
- transactions with a test that proves them
- consistent error handling (404/403/429/502)
- respect for an external API's rules
- secure defaults
- CI for both halves of the stack
- careful, honest documentation

**The gap:** the domain logic. Comparing inclination-as-latitude snapshots isn't collision detection, and anyone with a space background will spot it in seconds. Today the project shows you can build software. It doesn't yet show you can solve the problem the project is named after.

**The key insight:** the hard part, SGP4, is a **solved, open-source problem**. Orekit does it, and commit 10 proves it works in this codebase, to within 1 m of the reference values. What's left is *engineering*, which is exactly what a junior Java developer is hired for:

- ingest data on a schedule
- propagate thousands of objects efficiently
- filter the n² pairs down to a few to check
- search for the closest approach
- store and present the results

---

## Part 3: The remake recommendation

### The options weighed

| Option | What it is | For | Against |
|---|---|---|---|
| **(a) Real conjunction screening** | Propagate current elements 24–72 h ahead, filter pairs, find time of closest approach (TCA) and miss distance | It's what the project was always meant to be; very strong interview story; reuses everything built so far | Must be framed honestly: no covariance, so no real collision probability |
| **(b) Orbital data gateway** | A polite caching proxy for CelesTrak/Space-Track: scheduled ingestion, rate limits, OMM, staleness reporting | Solves a real, current problem; pure backend engineering (caching, scheduling, API design) | Not a product anyone can see; re-serving Space-Track data publicly means checking its **user agreement** on sharing data with third parties first; less memorable |
| **(c) Pass predictor + alerts** | "When will the ISS be visible over Dublin?" with notifications | Very visual and relatable; a natural demo | Needs Earth-fixed frames and orientation data (the full orekit-data archive); many hobby apps already do it |

**Why (b) matters now:** on **11 July 2026, the five-digit catalogue numbers ran out** ([CelesTrak](https://celestrak.org/NORAD/documentation/gp-data-formats.php)). The fixed-width TLE format can't represent objects numbered 100,000 or above. CelesTrak doesn't serve them as TLEs at all, and Space-Track's interim "Alpha-5" scheme (`A0000` = 100000) is a stopgap many tools can't read. The future is the **CCSDS OMM** format (JSON, XML, KVN or CSV), which allows nine-digit IDs. A project that handles this properly is genuinely current.

### Recommendation: (a) as the core, with (b)'s data discipline underneath, and (c) as an optional stretch

Build **"a conjunction screening service"**:

- **Core:** real SGP4 propagation with Orekit (already spiked) and a proper two-stage screening that reports **TCA, miss distance and relative velocity**. That's the feature, and the story.
- **Underneath:** the good parts of the gateway idea, as internal plumbing rather than a public mirror:
  - scheduled, rate-limited ingestion (half built)
  - OMM-first storage with 9-digit-safe catalogue numbers
  - element-set history, so you can show how a prediction changed as new data arrived
  - a staleness indicator ("these elements are 3 days old")
- **Stretch:** a single-satellite pass predictor ("next ISS passes over your location") reusing the same propagator. It's a nice visual for a demo, and it's when you'd add the full Orekit data context.

**Be explicit in the README that it's educational, not operational:**
- TLE/OMM mean elements are only accurate to roughly a kilometre at their epoch, and the error grows by kilometres per day.
- Real conjunction assessment uses higher-accuracy data with **covariance** (position uncertainty). Covariance is what makes a **probability of collision** computable, and public TLEs don't include it.
- So the app reports *geometry* (when and how close) and ranks by miss distance. It does not claim a probability.

Saying this plainly in an interview is a strength, not a weakness.

### Target architecture

```
                    ┌──────────────────────── Spring Boot backend ───────────────────────────┐
 Space-Track ──►    │  ingest/            scheduled, rate-limited (FetchCooldown), OMM JSON  │
 (CelesTrak as      │    └─► element_set table (history, 9-digit NORAD id, epoch, source)    │
  a fallback)       │                                                                        │
                    │  propagation/       OrbitPropagator ◄── OrekitSgp4Propagator           │
                    │                                                                        │
                    │  screening/         1. coarse filter: perigee/apogee overlap ± margin  │
                    │                     2. sample each candidate pair every ~60 s          │
                    │                     3. refine TCA with a 1-D minimiser (Hipparchus)    │
                    │    └─► conjunction table (pair, TCA, miss km, rel. velocity, run id)   │
                    │                                                                        │
                    │  api/               REST + DTOs + OpenAPI docs, JSON errors (done)     │
                    └────────────────────────────────────────────────────────────────────────┘
                                     │ PostgreSQL (Flyway migrations, Testcontainers in tests)
                                     ▼
                    React: conjunction list sorted by miss distance → detail page with a
                    distance-vs-time chart around TCA, and (stretch) a 3D view of both orbits
```

Two design points worth knowing cold for interviews:

- **Distances don't need Earth-fixed frames.** The distance between two objects at the same instant is the same in any frame, so screening can stay in SGP4's own TEME frame. That's why the 3 KB leap-second file is enough, and the full Earth-orientation data is only needed for the stretch pass predictor (ground locations rotate with the Earth).
- **The n² problem.** The full public catalogue is tens of thousands of objects, which means hundreds of millions of pairs. The classic answer is a cheap filter first: two orbits whose altitude ranges (perigee to apogee) don't overlap, plus a margin, can never meet. Then do the expensive time search only on what's left. Start with **one primary vs. the catalogue** (e.g. "what will the ISS pass close to this week?"), which is O(n), before attempting all-vs-all on a subset such as LEO.

### Phased roadmap (one junior, part-time)

Each phase ends with something demonstrable and green in CI. Rough sizes assume evenings and weekends.

**Phase 0: Foundations (about a week)**
- Merge the two cleanup PRs.
- Add **Testcontainers** for integration tests, so no more "the tests wiped my database".
- Add **Flyway** with a baseline migration.
- Add **springdoc-openapi** for interactive API docs.
- Make the docker-compose stack (with the frontend) start with one command.
- *Talking points:* why tests need isolated databases; schema migrations vs `ddl-auto`.
- *Recruiters notice:* "clone, `docker compose up`, it works", plus a Swagger UI.

**Phase 1: Real data, real positions (1–2 weeks)**
- Store **element sets** (OMM fields, including the raw TLE lines when present) with NORAD IDs as numbers that allow nine digits, and keep the history.
- Replace the lat/lon/alt pseudo-mapping with propagation through `OrbitPropagator`. Show each satellite's real current position and altitude.
- Switch ingestion to Space-Track's recommended `epoch/>now-10` filter, at a random minute each hour.
- *To research:* building Orekit's SGP4 propagator from OMM mean elements for objects without TLE lines. Orekit has a CCSDS OMM parser, and its 13.1.x patch notes mention Alpha-5 number parsing in the TLE class.
- *Talking points:* the 2026 catalogue-number rollover and why you store OMM; data modelling for history; verifying against reference cases.
- *Recruiters notice:* the README's "known issue" disappears.

**Phase 2: Conjunction screening (2–3 weeks)**
- One primary vs. the catalogue over 72 hours:
  - perigee/apogee filter
  - 60-second sampling
  - TCA refinement with a Hipparchus univariate optimiser (it comes with Orekit)
- Store results with a run ID. Unit-test the TCA search on two synthetic orbits with a known closest approach.
- Optionally, sanity-check results against CelesTrak's public SOCRATES conjunction reports, if available. Agreement within a few km is a great talking point; disagreement is a great discussion.
- *Talking points:* the algorithmic complexity and how the filter cuts it; floating-point precision in search; why there's no probability (covariance).
- *Recruiters notice:* "it predicts *when* and *how close*", plus a chart.

**Phase 3: Scale and polish (1–2 weeks)**
- All-vs-all for a LEO subset.
- Propagate in parallel (a fixed thread pool or `parallelStream`, with each worker using its own propagator instance, since propagators keep state).
- Measure and publish numbers ("screens N objects × 72 h in X seconds").
- Add Spring Boot Actuator health and metrics, including when data was last ingested and how stale it is.
- *Talking points:* profiling before optimising, thread safety, observability.

**Phase 4: Show it off (about a week, the stretch)**
- Conjunction detail page with a distance-vs-time chart.
- Optional 3D orbit view (CesiumJS or three.js).
- Optional ISS pass predictor for a chosen location.
- Deploy with the scheduled refresh on and manual refresh off. Put the live link, a GIF and the architecture diagram at the top of the README.

### What will make recruiters stop scrolling

1. **A live demo link and a 10-second GIF** at the top of the README. Most portfolio projects don't have one.
2. **A one-sentence pitch with numbers:** "Screens the ISS against X tracked objects over 72 hours using SGP4 (Orekit), verified against published reference cases."
3. **Visible engineering discipline:**
   - CI badge
   - test counts
   - "Decisions" notes (Orekit vs. writing SGP4; why no probability; why no Spring Security yet)
4. **Honesty about limits.** It reads as maturity, not weakness.
5. **Clean commit history.** This repo now has one, and REVIEW.md and IMPROVEMENTS.md show you can explain every change.
