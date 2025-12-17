import React, { useState, useEffect } from 'react';
import { satelliteApi } from '../services/api';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faDatabase, faSatellite, faPlay, faSearch, faInfoCircle } from '@fortawesome/free-solid-svg-icons';
import './SatelliteList.css';

function SatelliteList() {
  const [satellites, setSatellites] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [message, setMessage] = useState('');
  const [messageType, setMessageType] = useState('');
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });

  useEffect(() => {
    loadSatellites();
  }, []);

  const loadSatellites = async () => {
    setLoading(true);
    try {
      const response = await satelliteApi.getAllSatellites();
      setSatellites(response.data);
      setMessage('');
      setMessageType('');
    } catch (error) {
      setMessage(`Error: ${error.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleLoadBackup = async () => { 
    setLoading(true);
    setMessage('Loading backup satellite data...');
    setMessageType('info');
    try {
      const response = await satelliteApi.loadBackupData();
      setMessage(`${response.data}`);
      setMessageType('success');
      await new Promise(resolve => setTimeout(resolve, 2000));
      await loadSatellites();
    } catch (err) {
      setMessage(`Error: ${err.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const runCollisionDetection = async () => {
    if (satellites.length === 0) {
      setMessage('Load satellites first!');
      setMessageType('error');
      return;
    }
    
    setLoading(true);
    setMessage('Running collision detection...');
    setMessageType('info');
    try {
      const response = await satelliteApi.detectCollisions();
      setMessage(`${response.data}`);
      setMessageType('success');
    } catch (err) {
      setMessage(`${err.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const handleSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const handleFetchNasa = async () => {
    setLoading(true);
    setMessage('Fetching live NASA data from Space-Track.org...');
    setMessageType('info');

    try {
      const response = await satelliteApi.fetchNasaData();
      setMessage(`${response.data}`);
      setMessageType('success');
      await new Promise(resolve => setTimeout(resolve, 5000));
      await loadSatellites();
    } catch (error) {
      setMessage(`Error: ${error.message}`);
      setMessageType('error');
    } finally {
      setLoading(false);
    }
  };

  const filteredSatellites = satellites.filter(sat =>
    String(sat.name).toLowerCase().includes(searchTerm.toLowerCase()) ||
    String(sat.noradId).toLowerCase().includes(searchTerm.toLowerCase())
  );

  const sortedSatellites = [...filteredSatellites].sort((a, b) => {
    if (!sortConfig.key) return 0;
    const aValue = a[sortConfig.key];
    const bValue = b[sortConfig.key];
    if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
    if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
    return 0;
  });

  return (
    <div className="satellite-list">
      
      {/* new user info */}
      <div className="new-user-info">
        <FontAwesomeIcon icon={faInfoCircle} />
        <span>New here? Check the <a href="/how-to-use">Guide page</a> to learn how to use this system</span>
      </div>

      {/* quick start */}
      <div className="quick-start-section">
        <div className="quick-start-header">
          <h2>Quick Start</h2>
        </div>
        <p className="quick-start-subtitle">Choose a data source, then run collision detection</p>

        {/* backup data note */}
        <div className="backup-note">
          <strong>Note:</strong> Backup data is for demo/presentation purposes if the NASA Space-Track API is unavailable
        </div>
        
        <div className="data-source-buttons">
          <button 
            onClick={handleLoadBackup} 
            disabled={loading}
            className="data-source-btn"
          >
            <div className="btn-icon">
              <FontAwesomeIcon icon={faDatabase} />
            </div>
            <div className="btn-content">
              <div className="btn-title">Load Backup Data</div>
              <div className="btn-description">4 satellites • Quick test</div>
            </div>
          </button>
          
          <button 
            onClick={handleFetchNasa} 
            disabled={loading}
            className="data-source-btn primary"
          >
            <div className="btn-icon">
              <FontAwesomeIcon icon={faSatellite} />
            </div>
            <div className="btn-content">
              <div className="btn-title">Fetch NASA Data</div>
              <div className="btn-description">500+ satellites • Live data</div>
            </div>
          </button>
        </div>

        {satellites.length > 0 && (
          <div className="detection-section">
            <button 
              onClick={runCollisionDetection} 
              disabled={loading}
              className="btn-detect"
            >
              <FontAwesomeIcon icon={faPlay} />
              Run Collision Detection
            </button>
          </div>
        )}
      </div>

      {/* status message */}
      {message && (
        <div className={`message ${messageType}`}>
          {message}
        </div>
      )}

      {loading && <div className="loading">Processing...</div>}

      {/* data table */}
      {satellites.length > 0 && (
        <div className="data-section">
          <div className="data-header">
            <h3>Loaded Satellites ({satellites.length})</h3>
            <div className="search-box">
              <FontAwesomeIcon icon={faSearch} className="search-icon" />
              <input
                type="text"
                placeholder="Search by name or NORAD ID..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="search-input"
              />
            </div>
          </div>

          <table className="satellite-table">
            <thead>
              <tr>
                <th onClick={() => handleSort('name')}>
                  Name {sortConfig.key === 'name' && (sortConfig.direction === 'asc' ? '▲' : '▼')}
                </th>
                <th onClick={() => handleSort('noradId')}>
                  NORAD ID {sortConfig.key === 'noradId' && (sortConfig.direction === 'asc' ? '▲' : '▼')}
                </th>
                <th onClick={() => handleSort('altitude')}>
                  Altitude (km) {sortConfig.key === 'altitude' && (sortConfig.direction === 'asc' ? '▲' : '▼')}
                </th>
                <th>Position</th>
              </tr>
            </thead>
            <tbody>
              {sortedSatellites.map(sat => (
                <tr key={sat.id}>
                  <td>{sat.name}</td>
                  <td>{sat.noradId}</td>
                  <td>{sat.altitude.toFixed(2)}</td>
                  <td>{sat.latitude.toFixed(2)}°, {sat.longitude.toFixed(2)}°</td>
                </tr>
              ))}
            </tbody>
          </table>

          <p className="count">Showing {sortedSatellites.length} of {satellites.length} satellites</p>
        </div>
      )}

      {!loading && satellites.length === 0 && (
        <div className="empty-state">
          <div className="empty-icon">
            <FontAwesomeIcon icon={faSatellite} />
          </div>
          <h3>No Satellite Data Loaded</h3>
          <p>No satellites loaded. Use the buttons above to load data.</p>
          <a href="/how-to-use" className="guide-link">
            <FontAwesomeIcon icon={faInfoCircle} />
            Read the guide to learn how
          </a>
        </div>
      )}
    </div>
  );
}

export default SatelliteList;