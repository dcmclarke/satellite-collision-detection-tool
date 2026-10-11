<a id="readme-top"></a>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]
[![CI/CD](https://github.com/dcmclarke/satellite-collision-detection-tool/actions/workflows/ci.yml/badge.svg)](https://github.com/dcmclarke/satellite-collision-detection-tool/actions/workflows/ci.yml)


<br />
<div align="center">
  <h3 align="center">Satellite Collision Detection System</h3>
  <p align="center">
    An educational full-stack demo that screens satellite pairs for close approaches using orbital data from Space-Track.org
  </p>
</div>

---

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#project-status">Project Status &amp; Roadmap</a></li>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#deployment">Deployment</a></li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
        <li><a href="#configuration">Configuration</a></li>
        <li><a href="#running-the-tests">Running the Tests</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#how-it-works">How It Works</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#api-reference">API Reference</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>
---

## Project Status

The hosted deployment is currently offline while the project is being reworked, so there is no live demo at the moment. The screenshots below show the app running, and you can run it locally by following [Getting Started](#getting-started).

**What it is today**

- v1.0 was built as 12-week coursework and is preserved at the [`v1.0-coursework`](https://github.com/dcmclarke/satellite-collision-detection-tool/tree/v1.0-coursework) tag. Since then the backend has had a security and code-quality pass ([REVIEW.md](REVIEW.md), [IMPROVEMENTS.md](IMPROVEMENTS.md)).
- It is an **educational demo**, not an operational conjunction-assessment tool.
- **What's real:**
  - live orbital data from Space-Track.org, with fetches limited to once an hour to respect Space-Track's usage rules, and an optional scheduled refresh
  - consistent JSON errors
  - automated tests for the backend and frontend on every push
- **Known issue:** the screening doesn't propagate orbits yet. It compares orbital *elements* as if they were positions: inclination is stored as latitude, right ascension of the ascending node as longitude, and mean motion × 100 as altitude. The computed distances are therefore illustrative only and don't reflect real separations.
- The "probability" shown for each warning is a fixed score per risk tier, not a calculated collision probability. A real probability needs position uncertainty (covariance), which public TLE data doesn't include.

**Roadmap** (the full plan, with the reasoning, is in [IMPROVEMENTS.md](IMPROVEMENTS.md))

1. ✅ Security and reliability: no public destructive endpoints, transactional detection, rate-limited Space-Track access, CI for backend and frontend
2. 🚧 Real orbit propagation with [Orekit](https://www.orekit.org/)'s SGP4. The propagator exists and matches the published SGP4 verification case to within 1 m, but the screening doesn't use it yet.
3. ⬜ Store the orbital elements themselves (OMM/JSON, which supports catalogue numbers above 99,999) instead of pseudo-positions
4. ⬜ Screen over time: propagate every object over the next 24–72 hours, filter pairs by orbit altitude, then find each close pair's **time of closest approach** and **miss distance**
5. ⬜ Show the results: closest-approach timeline, relative speed, a 3D view of the two orbits; deploy with the scheduled refresh switched on

---

## About The Project

<img width="1262" height="642" alt="image" src="https://github.com/user-attachments/assets/4dffb180-8113-4713-ae7b-45346092c7f4" />

A full-stack satellite collision detection demo built as a final project for the Higher Diploma in Science in Computing (Software) at NCI. The system fetches orbital element data for up to 500 catalogued objects from the US Space Force's Space-Track.org API, runs a distance-based proximity check across every pair, and shows warnings in a React dashboard. See [Project Status](#project-status) for its limitations.

Key features:

- Fetch current orbital element data from Space-Track.org (up to 500 objects per fetch)
- 3D Euclidean distance screening across all satellite pairs
- Risk classification: **CRITICAL** (<2 km), **WARNING** (2–3.5 km), **INFO** (3.5–5 km)
- Colour-coded collision warnings with a fixed score per risk tier
- Alert acknowledgement workflow
- Backup dataset for offline demo / presentation use
- Sortable, searchable satellite table

<img width="901" height="788" alt="image" src="https://github.com/user-attachments/assets/7f797af5-c4e1-4a2f-abea-dba92d2ebfd3" />

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Built With

**Backend**

[![Java][Java-badge]][Java-url]
[![Spring Boot][SpringBoot-badge]][SpringBoot-url]
[![PostgreSQL][PostgreSQL-badge]][PostgreSQL-url]
[![Maven][Maven-badge]][Maven-url]

**Frontend**

[![React][React-badge]][React-url]
[![Axios][Axios-badge]][Axios-url]
[![React Router][ReactRouter-badge]][ReactRouter-url]

**Deployment & DevOps**

[![Docker][Docker-badge]][Docker-url]
[![Render][Render-badge]][Render-url]
[![Netlify][Netlify-badge]][Netlify-url]

**External API**

[![Space-Track][SpaceTrack-badge]][SpaceTrack-url]

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Deployment

**Backend:** Dockerized; previously deployed on Render  
**Frontend:** Previously deployed on Netlify  
**CI:** GitHub Actions builds and tests the backend (against a real PostgreSQL) and the frontend on every push

The hosted deployment is currently offline (see [Project Status](#project-status)).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Getting Started

### Prerequisites

- **Java 21** — [Download](https://adoptium.net/)
- **Node.js 18+** and npm — [Download](https://nodejs.org/)
- **PostgreSQL** — [Download](https://www.postgresql.org/download/)
- **Space-Track.org account** — [Register free](https://www.space-track.org/auth/createAccount)

### Installation

**1. Clone the repository**

```sh
git clone https://github.com/dcmclarke/satellite-collision-detection-tool.git
cd satellite-collision-detection-tool
```

**2. Set up the database**

```sh
psql -U postgres
CREATE DATABASE satellitedb;
\q
```

**3. Configure the backend**

Credentials are read from **environment variables**. Don't type them into `application.properties`: that file is tracked by git, so a password saved there is one `git add` away from being published.

macOS / Linux / Git Bash:

```sh
export SPACETRACK_API_USERNAME=your-username
export SPACETRACK_API_PASSWORD=your-password
export SPRING_DATASOURCE_PASSWORD=your-postgres-password   # only if it isn't "postgres"
```

Windows PowerShell:

```powershell
$env:SPACETRACK_API_USERNAME = "your-username"
$env:SPACETRACK_API_PASSWORD = "your-password"
$env:SPRING_DATASOURCE_PASSWORD = "your-postgres-password"   # only if it isn't "postgres"
```

In IntelliJ, add the same variables under **Run → Edit Configurations → Environment variables**. For docker-compose, copy `.env.example` to `.env` (git ignores it) and fill it in.

**4. Start the backend (Terminal 1)**

```sh
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at `http://localhost:8080`. Keep this terminal running.

The `dev` profile turns on the buttons that load and replace data. They're off by default, because the API has no login and anyone could otherwise wipe the data or use up your Space-Track quota. In IntelliJ, set **Active profiles** to `dev` in the run configuration.

**5. Install and start the frontend (Terminal 2)**

```sh
cd frontend
npm install
npm start
```

The app will open automatically at `http://localhost:3000`.

### Configuration

Everything is set through environment variables (Spring Boot maps them onto `application.properties`):

| Variable | Default | What it does |
|---|---|---|
| `SPACETRACK_API_USERNAME` / `SPACETRACK_API_PASSWORD` | *(empty)* | Space-Track.org login |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | local `satellitedb`, `postgres` / `postgres` | Database connection |
| `SPRING_PROFILES_ACTIVE` | *(none)* | `dev` turns on the data buttons for local use |
| `APP_MANUAL_DATA_REFRESH_ENABLED` | `false` | Same switch without the profile |
| `SPACETRACK_API_MIN_FETCH_INTERVAL` | `1h` | Minimum time between Space-Track fetches |
| `SPACETRACK_INGEST_ENABLED` | `false` | Turn on the scheduled refresh (for a deployed server) |
| `SPACETRACK_INGEST_INTERVAL` | `PT6H` | Time between scheduled refreshes |

### Running the Tests

```sh
cd backend && ./mvnw test        # backend: unit tests plus integration tests against PostgreSQL
cd frontend && npm test          # frontend: React Testing Library
```

> **Warning:** the backend integration tests **delete all data** in the database they connect to. Point them at a separate, empty database (for example `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/satellitedb_test`), not the one you use for the app. CI uses a throwaway PostgreSQL container.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Usage

1. On the **Satellites** page, click **Fetch Space-Track Data** to pull data from Space-Track.org (up to 500 objects), or **Load Backup Data** for a quick offline demo with 4 pre-configured satellites
2. Click **Run Collision Detection** — the system checks every satellite pair and saves any close approaches
3. Navigate to **Warnings** to view collision predictions filtered by risk level
4. Navigate to **Alerts** to review and acknowledge alerts

> **Note:** If the Space-Track API is unavailable, use **Load Backup Data**. It includes satellites in similar LEO orbits chosen specifically to demonstrate collision detection.

> **Note:** the three data buttons only work when the backend runs with the `dev` profile (see [Installation](#installation)). **Fetch Space-Track Data** can be used once an hour: Space-Track asks for data no more often than that and can suspend accounts that ask more, so the backend refuses earlier fetches with a message saying how long to wait.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## How It Works

### Collision Detection Algorithm

The system uses a **3D Euclidean distance** approach to check every unique satellite pair.

**Step 1** — Convert each satellite's geodetic coordinates (latitude, longitude, altitude) to 3D Cartesian (X, Y, Z):

```
X = (R + altitude) × cos(lat) × cos(lon)
Y = (R + altitude) × cos(lat) × sin(lon)
Z = (R + altitude) × sin(lat)
```

where R = 6,371.8 km (Earth's mean radius, as used in the code).

> **Known issue:** in v1.0 the inputs to this step aren't real geodetic coordinates. Inclination is used as latitude, RAAN as longitude and mean motion × 100 as altitude, so the resulting distances are illustrative only. See [Project Status](#project-status).

**Step 2** — Compute the straight-line distance between each pair using the Pythagorean theorem in 3D:

```
distance = √((x₂-x₁)² + (y₂-y₁)² + (z₂-z₁)²)
```

**Step 3** — Pairs within 5 km trigger a prediction, classified by distance:

| Risk Level | Distance | Score shown (fixed per tier) |
|---|---|---|
| 🔴 CRITICAL | < 2 km | 90% |
| 🟡 WARNING | 2 – 3.5 km | 60% |
| 🔵 INFO | 3.5 – 5 km | 30% |

The score is a fixed lookup based on the risk tier, not a calculated collision probability. This is a single-snapshot distance check. Production systems propagate orbits over time (e.g. with SGP4) and compute probability of collision from position uncertainty.

With 500 satellites loaded, the system evaluates ~125,000 unique pairs per detection run.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---
### Key Challenges

- Processing ~125,000 satellite pairs efficiently without excessive computation time
- Handling unreliable external API responses with fallback datasets
- Designing a clear UI to surface complex collision data meaningfully
- Managing data flow between backend detection logic and frontend visualisation

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---
## Project Structure

```
satellite-collision-detection-tool/
├── backend/
│   └── src/main/java/com/satellitesystem/collisiondetection/
│       ├── controller/         # REST endpoints (Satellite, Collision, Alert)
│       ├── dto/                # JSON response records (the API never exposes JPA entities)
│       ├── exception/          # Custom exceptions + GlobalExceptionHandler (HTTP status mapping)
│       ├── model/              # JPA entities (Satellite, CollisionPrediction, Alert)
│       ├── repository/         # Spring Data JPA repositories
│       ├── propagation/        # SGP4 orbit propagation with Orekit (not used by the screening yet)
│       ├── service/
│       │   ├── CollisionDetectionService.java   # Core detection algorithm
│       │   ├── SpaceTrackApiService.java        # Space-Track.org integration
│       │   ├── FetchCooldown.java               # Once-an-hour limit on Space-Track requests
│       │   ├── SpaceTrackIngestJob.java         # Optional scheduled refresh
│       │   ├── AlertService.java
│       │   └── SatelliteService.java
│       └── config/             # CORS and scheduling configuration
│
└── frontend/
    └── src/
        ├── components/
        │   ├── SatelliteList.jsx       # Main dashboard & data controls
        │   ├── CollisionWarnings.jsx   # Collision predictions view
        │   ├── AlertHistory.jsx        # Alert management
        │   ├── Navigation.jsx
        │   └── Guide.jsx
        └── services/
            └── api.js                  # Axios API client
```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## API Reference

Base URL: `http://localhost:8080/api`

### Satellites

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/satellites` | Get all satellites |
| `POST` | `/satellites/fetch-spacetrack-data` | Fetch orbital data from Space-Track.org |
| `POST` | `/satellites/load-backup-data` | Load offline demo dataset |
| `POST` | `/satellites/detect-collisions` | Run collision detection |

The three `POST` endpoints above replace data and the API has no login, so they return `403` unless `app.manual-data-refresh.enabled=true` (the `dev` profile sets it). `fetch-spacetrack-data` returns `429` with a `Retry-After` header if Space-Track was contacted less than an hour ago.

Errors use one JSON shape, for example `{"message": "Satellite not found: 42"}`, with status `404` (not found), `403` (disabled), `429` (too soon) or `502` (Space-Track failed).

### Collisions

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/collisions/active` | Get all active predictions |
| `GET` | `/collisions/critical` | Get critical predictions only |

### Alerts

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/alerts` | Get all alerts |
| `GET` | `/alerts/recent` | Get alerts from last 24 hours |
| `POST` | `/alerts/{id}/acknowledge` | Acknowledge an alert |

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Contributing

Contributions are welcome. Fork the repo and open a pull request, or open an issue tagged `enhancement`.

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Acknowledgments

- [Space-Track.org](https://www.space-track.org) (US Space Force) — for providing free access to satellite orbital data
- [Orekit](https://www.orekit.org/) (CS GROUP, Apache 2.0) — SGP4/SDP4 orbit propagation
- D. A. Vallado et al., *Revisiting Spacetrack Report #3* (AIAA 2006-6753) — the SGP4 verification cases used in the tests
- [US Naval Observatory](https://maia.usno.navy.mil/) — leap-second table (`tai-utc.dat`) bundled for Orekit
- [othneildrew/Best-README-Template](https://github.com/othneildrew/Best-README-Template) — README structure reference
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Create React App](https://github.com/facebook/create-react-app)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

<!-- MARKDOWN LINKS & IMAGES -->
[contributors-shield]: https://img.shields.io/github/contributors/dcmclarke/satellite-collision-detection-tool.svg?style=for-the-badge
[contributors-url]: https://github.com/dcmclarke/satellite-collision-detection-tool/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/dcmclarke/satellite-collision-detection-tool.svg?style=for-the-badge
[forks-url]: https://github.com/dcmclarke/satellite-collision-detection-tool/network/members
[stars-shield]: https://img.shields.io/github/stars/dcmclarke/satellite-collision-detection-tool.svg?style=for-the-badge
[stars-url]: https://github.com/dcmclarke/satellite-collision-detection-tool/stargazers
[issues-shield]: https://img.shields.io/github/issues/dcmclarke/satellite-collision-detection-tool.svg?style=for-the-badge
[issues-url]: https://github.com/dcmclarke/satellite-collision-detection-tool/issues
[license-shield]: https://img.shields.io/github/license/dcmclarke/satellite-collision-detection-tool.svg?style=for-the-badge
[license-url]: https://github.com/dcmclarke/satellite-collision-detection-tool/blob/main/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/dc-clarke/

[Java-badge]: https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white
[Java-url]: https://adoptium.net/
[SpringBoot-badge]: https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white
[SpringBoot-url]: https://spring.io/projects/spring-boot
[PostgreSQL-badge]: https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white
[PostgreSQL-url]: https://www.postgresql.org/
[Maven-badge]: https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white
[Maven-url]: https://maven.apache.org/
[React-badge]: https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB
[React-url]: https://reactjs.org/
[Axios-badge]: https://img.shields.io/badge/Axios-5A29E4?style=for-the-badge&logo=axios&logoColor=white
[Axios-url]: https://axios-http.com/
[ReactRouter-badge]: https://img.shields.io/badge/React_Router-CA4245?style=for-the-badge&logo=react-router&logoColor=white
[ReactRouter-url]: https://reactrouter.com/
[SpaceTrack-badge]: https://img.shields.io/badge/Space--Track.org-1a1a2e?style=for-the-badge
[SpaceTrack-url]: https://www.space-track.org
[Docker-badge]: https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white
[Docker-url]: https://www.docker.com/
[Render-badge]: https://img.shields.io/badge/Render-46E3B7?style=for-the-badge&logo=render&logoColor=white
[Render-url]: https://render.com/
[Netlify-badge]: https://img.shields.io/badge/Netlify-00C7B7?style=for-the-badge&logo=netlify&logoColor=white
[Netlify-url]: https://www.netlify.com/
