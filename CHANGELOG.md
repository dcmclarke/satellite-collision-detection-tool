# Changelog

Notable changes to this project. The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.1.0] - 2026-10-11

A security, reliability and code-quality pass over the v1.0 coursework release. The screening algorithm and its documented data-mapping limitation are unchanged.

### Security
- Removed the unauthenticated `clear-all`, `POST /api/satellites` and `POST /api/alerts` endpoints. The last two could overwrite any record by id.
- The endpoints that replace data (`fetch-spacetrack-data`, `load-backup-data`, `detect-collisions`) are off by default and return `403`. The `dev` Spring profile turns them on for local use.
- Space-Track fetches are limited to one per hour (`429` with `Retry-After`), in line with Space-Track's API guidelines, to protect the account from suspension.
- The Space-Track login response and session cookies are no longer logged, and the cookie manager is no longer installed JVM-wide.
- Credentials are read from environment variables only. The setup steps no longer tell you to edit the tracked `application.properties`, and `.env` files are git-ignored.
- CORS allows only the HTTP methods the API uses.

### Added
- Optional scheduled refresh from Space-Track (`spacetrack.ingest.enabled`).
- JSON error responses through a `@RestControllerAdvice`: `404`, `403`, `429` and `502`.
- Response DTOs (Java records), so JPA entities are no longer exposed by the API.
- An SGP4 orbit propagator using Orekit, behind an `OrbitPropagator` interface and verified against the published SGP4 test case for satellite 00005. It isn't used by the screening yet.
- Tests:
  - Space-Track client against a local fake HTTP server
  - fetch cooldown with a controllable clock
  - transaction rollback
  - web layer (`@WebMvcTest`)
  - DTO JSON shape
  - scheduled job
  - frontend `SatelliteList` component
- CI runs on every push and tests and builds the frontend as well as the backend.

### Changed
- Renamed NASA references to Space-Track: the service, the `spacetrack.api.*` properties, the `SPACETRACK_API_*` environment variables, the `/fetch-spacetrack-data` route and the UI.
- Constructor injection everywhere, SLF4J logging, and repository access moved out of the controller into services.
- `detectCollisions()` and the data reset are transactional.
- A failed Space-Track fetch keeps the existing data instead of deleting it first.
- The frontend shows the backend's error messages, and refreshes the table immediately instead of after a fixed delay.
- README: honest project status and roadmap, configuration table and test instructions.

### Removed
- The unused webflux and httpclient5 dependencies, dead methods and imports, the in-memory alert cache and its `/api/alerts/in-memory` endpoint, the example properties template, and the broken default Create React App test.

### Upgrading from 1.0
- Rename the `NASA_API_*` environment variables to `SPACETRACK_API_*`.
- Run the backend locally with the `dev` profile: `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`.

## [1.0.0] - coursework

The original 12-week coursework version, preserved at the [`v1.0-coursework`](https://github.com/dcmclarke/satellite-collision-detection-tool/tree/v1.0-coursework) tag.

[1.1.0]: https://github.com/dcmclarke/satellite-collision-detection-tool/compare/v1.0-coursework...v1.1.0
[1.0.0]: https://github.com/dcmclarke/satellite-collision-detection-tool/tree/v1.0-coursework
