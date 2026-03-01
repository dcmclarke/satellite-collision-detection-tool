<a id="readme-top"></a>
<!-- PROJECT SHIELDS -->
Show Image
Show Image
Show Image
Show Image
Show Image
Show Image
<br />
<div align="center">
  <h3 align="center">Satellite Collision Detection System</h3>
  <p align="center">
    A real-time satellite tracking and collision detection system using live orbital data from Space-Track.org
    <br />
    <a href="https://github.com/github_username/satellite-collision-detection"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/github_username/satellite-collision-detection">View Demo</a>
    &middot;
    <a href="https://github.com/github_username/satellite-collision-detection/issues/new?labels=bug&template=bug-report---.md">Report Bug</a>
    &middot;
    <a href="https://github.com/github_username/satellite-collision-detection/issues/new?labels=enhancement&template=feature-request---.md">Request Feature</a>
  </p>
</div>

<!-- TABLE OF CONTENTS -->
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
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

About The Project
Show Image
A full-stack satellite collision detection system built as a final project for an HDip in Software Development. The system fetches live orbital data for 500+ satellites from NASA's Space-Track.org API, runs proximity-based collision detection across every satellite pair, and surfaces warnings through a React dashboard.
Key features:

Fetch live TLE (Two-Line Element) orbital data from Space-Track.org (500+ satellites)
3D Euclidean distance-based collision detection across all satellite pairs
Risk classification: CRITICAL (<2 km), WARNING (2–3.5 km), INFO (3.5–5 km)
Colour-coded collision warnings with probability scores
Alert acknowledgement workflow
Backup dataset for offline demo / presentation use
Sortable, searchable satellite table

Show Image
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Built With
Backend
Show Image
Show Image
Show Image
Show Image
Frontend
Show Image
Show Image
Show Image
External API
Show Image
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Getting Started
Prerequisites

Java 21 — Download
Node.js 18+ and npm — Download
PostgreSQL — Download
Space-Track.org account — Register free

Installation
1. Clone the repository
shgit clone https://github.com/github_username/satellite-collision-detection.git
cd satellite-collision-detection
2. Set up the database
shpsql -U postgres
CREATE DATABASE satellitedb;
\q
3. Configure the backend
shcd backend
cp src/main/resources/templates/application-example.properties src/main/resources/application.properties
Edit application.properties and fill in your credentials:
propertiesspring.datasource.password=YOUR_POSTGRES_PASSWORD
nasa.api.username=YOUR_SPACETRACK_USERNAME
nasa.api.password=YOUR_SPACETRACK_PASSWORD
4. Start the backend (Terminal 1)
shcd backend
./mvnw spring-boot:run
API available at http://localhost:8080. Keep this terminal running.
5. Install and start the frontend (Terminal 2)
shcd frontend
npm install
npm start
App opens automatically at http://localhost:3000.
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Usage

On the Satellites page, click Fetch NASA Data for live data (500+ satellites), or Load Backup Data for a quick offline demo with 4 pre-configured satellites
Click Run Collision Detection — the system checks every satellite pair and saves any close approaches
Navigate to Warnings to view collision predictions filtered by risk level
Navigate to Alerts to review and acknowledge alerts


Note: If the Space-Track API is unavailable (e.g. during a presentation), use Load Backup Data. It includes satellites in similar LEO orbits chosen specifically to demonstrate collision detection.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

How It Works
Collision Detection Algorithm
The system uses a 3D Euclidean distance approach to check every unique satellite pair.
Step 1 — Convert each satellite's geodetic coordinates (latitude, longitude, altitude) to 3D Cartesian (X, Y, Z):
X = (R + altitude) × cos(lat) × cos(lon)
Y = (R + altitude) × cos(lat) × sin(lon)
Z = (R + altitude) × sin(lat)
where R = 6,371.8 km (Earth's mean radius).
Step 2 — Compute the straight-line distance between each pair using the Pythagorean theorem in 3D:
distance = √((x₂-x₁)² + (y₂-y₁)² + (z₂-z₁)²)
Step 3 — Pairs within 5 km trigger a prediction, classified by distance:
Risk LevelDistanceProbability Score🔴 CRITICAL< 2 km90%🟡 WARNING2 – 3.5 km60%🔵 INFO3.5 – 5 km30%
With 500 satellites loaded, the system evaluates ~125,000 unique pairs per detection run.
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Project Structure
satellite-collision-detection/
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
<p align="right">(<a href="#readme-top">back to top</a>)</p>

API Reference
Base URL: http://localhost:8080/api
Satellites
MethodEndpointDescriptionGET/satellitesGet all satellitesPOST/satellites/fetch-nasa-dataFetch live data from Space-Track.orgPOST/satellites/load-backup-dataLoad offline demo datasetPOST/satellites/detect-collisionsRun collision detectionPOST/satellites/clear-allClear all data
Collisions
MethodEndpointDescriptionGET/collisions/activeGet all active predictionsGET/collisions/criticalGet critical predictions only
Alerts
MethodEndpointDescriptionGET/alertsGet all alertsGET/alerts/recentGet alerts from last 24 hoursPOST/alerts/{id}/acknowledgeAcknowledge an alert
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Roadmap

 Proper orbital propagation using the SGP4 model
 Scheduled automatic data refresh
 Email / push notifications for critical alerts
 3D globe visualisation of satellite orbits
 Docker Compose setup for one-command startup

See the open issues for a full list of proposed features and known issues.
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Contributing
Contributions are welcome. Fork the repo and open a pull request, or open an issue tagged enhancement.

Fork the Project
Create your Feature Branch (git checkout -b feature/AmazingFeature)
Commit your Changes (git commit -m 'Add some AmazingFeature')
Push to the Branch (git push origin feature/AmazingFeature)
Open a Pull Request

<p align="right">(<a href="#readme-top">back to top</a>)</p>

License
Distributed under the MIT License. See LICENSE.txt for more information.
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Contact
Your Name — @twitter_handle — email@example.com
Project Link: https://github.com/github_username/satellite-collision-detection
<p align="right">(<a href="#readme-top">back to top</a>)</p>

Acknowledgments

Space-Track.org — for providing free access to satellite orbital data
othneildrew/Best-README-Template — README structure reference
Spring Boot
Create React App

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
