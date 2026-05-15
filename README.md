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
    A deployed full-stack satellite tracking and collision detection system using live orbital data from Space-Track.org.
    <br />
    <br />
    <a href="https://satellite-collision-detector.netlify.app"><strong>View Live Demo »</strong></a>
    <br />
    <br />
    <a href="https://github.com/dcmclarke/satellite-collision-detection-tool/issues">Report Bug</a>
    ·
    <a href="https://github.com/dcmclarke/satellite-collision-detection-tool/issues">Request Feature</a>
  </p>
</div>

---

<details>
  <summary>Table of Contents</summary>
  <ol>
    <li><a href="#live-demo">Live Demo</a></li>
    <li><a href="#about-the-project">About The Project</a></li>
    <li><a href="#built-with">Built With</a></li>
    <li><a href="#deployment">Deployment</a></li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#option-1-run-with-docker-compose-recommended">Option 1: Run with Docker Compose</a></li>
        <li><a href="#option-2-manual-local-setup">Option 2: Manual Local Setup</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#how-it-works">How It Works</a></li>
    <li><a href="#testing-and-cicd">Testing and CI/CD</a></li>
    <li><a href="#project-structure">Project Structure</a></li>
    <li><a href="#api-reference">API Reference</a></li>
    <li><a href="#limitations">Limitations</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

---

## Live Demo

- **Frontend:** https://satellite-collision-detector.netlify.app
- **Backend API:** https://satellite-backend-87h6.onrender.com/api

> Note: The backend may take 30–60 seconds to wake up on first request due to Render free-tier cold starts.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## About The Project

<img width="1262" height="642" alt="Satellite dashboard screenshot" src="https://github.com/user-attachments/assets/4dffb180-8113-4713-ae7b-45346092c7f4" />

This is a full-stack satellite collision detection system built as a final project while studying software development.

The application fetches live orbital data for 500+ satellites from Space-Track.org, stores satellite records in PostgreSQL, runs proximity-based collision detection across every unique satellite pair, and surfaces risk-classified warnings through a React dashboard.

The project demonstrates:

- Full-stack application architecture
- Java Spring Boot REST API development
- PostgreSQL persistence using Spring Data JPA
- External authenticated API integration
- Docker containerisation
- Cloud deployment using Render and Netlify
- GitHub Actions CI testing
- Frontend data visualisation using React

### Key Features

- Fetch live TLE orbital data from Space-Track.org
- Store and retrieve satellite records using PostgreSQL
- Run proximity analysis across ~125,000 unique satellite pairs per detection run
- Classify collision risk into **Critical**, **Warning**, and **Info** levels
- Display colour-coded collision warnings with probability scores
- Alert acknowledgement workflow
- Backup dataset for offline demo / presentation use
- Sortable, searchable satellite table
- Docker Compose setup for local backend + database startup
- GitHub Actions CI pipeline for automated backend testing

<img width="901" height="788" alt="Collision warnings screenshot" src="https://github.com/user-attachments/assets/7f797af5-c4e1-4a2f-abea-dba92d2ebfd3" />

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

The project is deployed as a split full-stack application:

| Layer | Platform |
|---|---|
| Frontend | Netlify |
| Backend | Render |
| Database | Render PostgreSQL |
| CI/CD | GitHub Actions |
| Containerisation | Docker / Docker Compose |

The backend is containerised with Docker and deployed to Render. The frontend is deployed separately on Netlify and communicates with the Render backend through an environment-based API URL.

GitHub Actions runs backend tests automatically on pushes and pull requests to `main`.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Getting Started

There are two ways to run the project locally:

1. **Docker Compose** — recommended for quickly starting the backend and PostgreSQL database.
2. **Manual setup** — useful if you want to run each part yourself.

---

### Option 1: Run with Docker Compose Recommended

#### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Space-Track.org account](https://www.space-track.org/auth/createAccount)

#### 1. Clone the repository

```sh
git clone https://github.com/dcmclarke/satellite-collision-detection-tool.git
cd satellite-collision-detection-tool
