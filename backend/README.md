# Satellite Collision Detection System

A real-time satellite tracking and collision detection system using data from Space-Track.org.

## Prerequisites

Before you begin, ensure you have the following installed:

- **Java 21**
- **Node.js 18+** and npm
- **PostgreSQL 18**
- **Space-Track.org account** (free registration at https://www.space-track.org/auth/createAccount)

## Backend Setup

### 1. Configure Database

Create a PostgreSQL database:

```bash
psql -U postgres
CREATE DATABASE satellitedb;
\q
```

### 2. Configure Application Properties

Copy the example configuration:

```bash
cd backend
cp src/main/resources/application-example.properties src/main/resources/application.properties
```

Edit `application.properties` and set your credentials:

```properties
spring.datasource.password=YOUR_POSTGRES_PASSWORD
nasa.api.username=YOUR_SPACETRACK_USERNAME
nasa.api.password=YOUR_SPACETRACK_PASSWORD
```

### 3. Start Backend (Terminal 1)

```bash
./mvnw spring-boot:run
```

Backend will start on http://localhost:8080

**⚠️ Keep this terminal running**

## Frontend Setup

Open a new terminal window for the frontend.

### 1. Install Dependencies

```bash
cd satellite-collision-detection-project/frontend
npm install
```

### 2. Start Frontend (Terminal 2)

```bash
npm start
```

The frontend will start on http://localhost:3000 and automatically open in your browser.

**⚠️ Keep both terminals running**

## Verification

1. Navigate to http://localhost:3000
2. Click **"Fetch NASA Data"**
3. Click **"Run Collision Detection"**
4. Navigate to the **"Warnings"** page to see results

## Project Structure

- **Backend**: REST API running on port 8080
- **Frontend**: React SPA running on port 3000
- **Database**: PostgreSQL running on port 5432

## Troubleshooting

If you encounter issues:

- Ensure all prerequisites are installed
- Verify your Space-Track.org credentials are correct
- Check that PostgreSQL is running
- Make sure ports 3000, 8080, and 5432 are not in use by other applications