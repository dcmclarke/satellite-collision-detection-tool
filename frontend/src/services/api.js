import axios from 'axios';

const API_BASE = 'http://localhost:8080/api';

export const satelliteApi = {
  getAllSatellites: () => {
    return axios.get(`${API_BASE}/satellites`);
  },

  loadBackupData: () => {
    return axios.post(`${API_BASE}/satellites/load-backup-data`);
  },

  detectCollisions: () => {
    return axios.post(`${API_BASE}/satellites/detect-collisions`);
  },

  fetchNasaData: () => {
  return axios.post(`${API_BASE}/satellites/fetch-nasa-data`);
  },
};

export const collisionApi = {
  getActiveCollisions: () => {
    return axios.get(`${API_BASE}/collisions/active`);
  },

  getCriticalCollisions: () => {
    return axios.get(`${API_BASE}/collisions/critical`);
  },
};

export const alertApi = {
  getAllAlerts: () => {
    return axios.get(`${API_BASE}/alerts`);
  },

  getRecentAlerts: () => {
    return axios.get(`${API_BASE}/alerts/recent`);
  },

  acknowledgeAlert: (id) => {
    return axios.post(`${API_BASE}/alerts/${id}/acknowledge`);
  },
};
