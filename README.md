<a id="readme-top"></a>

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]

<br />
<div align="center">
  <h3 align="center">Satellite Collision Detection System</h3>
  <p align="center">
    A real-time satellite tracking and collision detection system using live orbital data from Space-Track.org
  </p>
</div>

---

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#how-it-works">How It Works</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#api-reference">API Reference</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

---

## About The Project

<img width="1262" height="642" alt="image" src="https://github.com/user-attachments/assets/4dffb180-8113-4713-ae7b-45346092c7f4" />

A full-stack satellite collision detection system built as a final project while learning full-stack development for an HDip in Software Development. The system fetches live orbital data for 500+ satellites from NASA's Space-Track.org API, runs proximity-based collision detection across every satellite pair, and surfaces warnings through a React dashboard.

Key features:

- Fetch live TLE (Two-Line Element) orbital data from Space-Track.org (500+ satellites)
- 3D Euclidean distance-based collision detection across all satellite pairs
- Risk classification: **CRITICAL** (<2 km), **WARNING** (2–3.5 km), **INFO** (3.5–5 km)
- Colour-coded collision warnings with probability scores
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

**External API**

[![Space-Track][SpaceTrack-badge]][SpaceTrack-url]

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

```sh
cd backend
cp src/main/resources/templates/application-example.properties src/main/resources/application.properties
```

Edit `application.properties` and fill in your credentials:

```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD
nasa.api.username=YOUR_SPACETRACK_USERNAME
nasa.api.password=YOUR_SPACETRACK_PASSWORD
```

**4. Start the backend (Terminal 1)**

```sh
cd backend
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`. Keep this terminal running.

**5. Install and start the frontend (Terminal 2)**

```sh
cd frontend
npm install
npm start
```

The app will open automatically at `http://localhost:3000`.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Usage

1. On the **Satellites** page, click **Fetch NASA Data** for live data (500+ satellites), or **Load Backup Data** for a quick offline demo with 4 pre-configured satellites
2. Click **Run Collision Detection** — the system checks every satellite pair and saves any close approaches
3. Navigate to **Warnings** to view collision predictions filtered by risk level
4. Navigate to **Alerts** to review and acknowledge alerts

> **Note:** If the Space-Track API is unavailable, use **Load Backup Data**. It includes satellites in similar LEO orbits chosen specifically to demonstrate collision detection.

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

where R = 6,371.8 km (Earth's mean radius).

**Step 2** — Compute the straight-line distance between each pair using the Pythagorean theorem in 3D:

```
distance = √((x₂-x₁)² + (y₂-y₁)² + (z₂-z₁)²)
```

**Step 3** — Pairs within 5 km trigger a prediction, classified by distance:

| Risk Level | Distance | Probability Score |
|---|---|---|
| 🔴 CRITICAL | < 2 km | 90% |
| 🟡 WARNING | 2 – 3.5 km | 60% |
| 🔵 INFO | 3.5 – 5 km | 30% |

Note: This uses simplified 3D distance calculations. Production systems would use SGP4 orbital propagation for higher accuracy.

With 500 satellites loaded, the system evaluates ~125,000 unique pairs per detection run.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---
### Key Challenges

- [ ] Processing ~125,000 satellite pairs efficiently without excessive computation time
- [ ] Handling unreliable external API responses with fallback datasets
- [ ] Designing a clear UI to surface complex collision data meaningfully
- [ ] Managing data flow between backend detection logic and frontend visualisation

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---
## Project Structure

```
satellite-collision-detection-tool/
├── backend/
│   └── src/main/java/com/satellitesystem/collisiondetection/
│       ├── controller/         # REST endpoints (Satellite, Collision, Alert)
│       ├── model/              # JPA entities (Satellite, CollisionPrediction, Alert)
│       ├── repository/         # Spring Data JPA repositories
│       ├── service/
│       │   ├── CollisionDetectionService.java   # Core detection algorithm
│       │   ├── NasaApiService.java              # Space-Track.org integration
│       │   ├── AlertService.java
│       │   └── SatelliteService.java
│       └── config/             # CORS configuration
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
| `POST` | `/satellites/fetch-nasa-data` | Fetch live data from Space-Track.org |
| `POST` | `/satellites/load-backup-data` | Load offline demo dataset |
| `POST` | `/satellites/detect-collisions` | Run collision detection |
| `POST` | `/satellites/clear-all` | Clear all data |

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

## Roadmap

- [ ] Proper orbital propagation using the SGP4 model
- [ ] Scheduled automatic data refresh
- [ ] Email / push notifications for critical alerts
- [ ] 3D globe visualisation of satellite orbits
- [ ] Docker Compose setup for one-command startup

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

- [Space-Track.org](https://www.space-track.org) — for providing free access to satellite orbital data
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

[screenshot-satellites]: images/screenshot-satellites.png
[screenshot-warnings]: images/screenshot-warnings.png

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
[SpaceTrack-badge]: https://img.shields.io/badge/Space--Track.org-1a1a2e?style=for-the-badge&logo=nasa&logoColor=white
[SpaceTrack-url]: https://www.space-track.org
