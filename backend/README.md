# SATELLITE COLLISION DETECTION SYSTEM

Prerequisites:
Java 21
Node.js 18+ and npm
PostgreSQL 18
Space-Track.org account (free registration at https://www.space-track.org/auth/createAccount)

# Backup Setup

### 1. Configure Database
- Create PostgreSQL database

psql -U postgres
- CREATE DATABASE satellitedb;
\q

### 2. Configure Application properties
- Copy example configuration:

cd backend
cp src/main/resources/application-example.properties src/main/resources/application.properties

**Edit** application.properties and set:

spring.datasource.password=YOUR_POSTGRES_PASSWORD
nasa.api.username=YOUR_SPACETRACK_USERNAME
nasa.api.password=YOUR_SPACETRACK_PASSWORD

### 3. Start backend (terminal 1)

./mvnw spring-boot:run

Backend will start on http://localhost:8080
KEEP THIS RUNNING

# **Frontend Setup**

- New terminal/window for frontend
### 1. Install Dependencies
cd satellite-collision-detection-project/frotend
npm install

### 2. Start Frontend (terminal 2)
npm start

Then frontend will start on http://localhost:3000 and automatically open in browser
Keep both terminals running.

## Verfication
1. Navigate to http://localhost:3000
2.  Click Fetch Nasa Data 
3. Click "Run Collision Detection"
4. Navigate to  "Warnings" page to see results


Project Structure
Backend runs on port 8080 (REST API)
Frontend runs on port 3000 (React SPA)
PostgreSQL runs on port 5432 (Database)