import React, { useState, useEffect } from 'react';
import { alertApi } from '../services/api';
import './AlertHistory.css';

function AlertHistory() {
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('all');
  const [message, setMessage] = useState('');

  //load alerts when component mounts
  useEffect(() => {
    loadAlerts();
  }, []);

  const loadAlerts = async () => {
    setLoading(true);
    try {
      const response = await alertApi.getRecentAlerts();
      setAlerts(response.data);
      setMessage(`Loaded ${response.data.length} alerts from last 24 hours`);
    } catch (error) {
      setMessage(`Error loading alerts: ${error.message}`);
      console.error('Error loading alerts:', error);
    } finally {
      setLoading(false);
    }
  };

  //acknowledge alert
  const handleAcknowledge = async (id) => {
    try {
      await alertApi.acknowledgeAlert(id);
      setMessage('Alert acknowledged successfully');
      //refresh alerts list
      await loadAlerts();
    } catch (error) {
      setMessage(`Error acknowledging alert: ${error.message}`);
      console.error('Error acknowledging alert:', error);
    }
  };

  //filter & sort alerts, unacknowledged at top
const filteredAlerts = alerts
  .filter(alert => {
    if (filter === 'all') return true;
    return !alert.acknowledged;
  })
  .sort((a, b) => {
    if (a.acknowledged === b.acknowledged) {
      return new Date(b.sentAt) - new Date(a.sentAt);
    }
    return a.acknowledged ? 1 : -1;
  });	

  //helper formatting timestamp relative time
  const formatTimeAgo = (timestamp) => {
    const now = new Date();
    const alertTime = new Date(timestamp);
    const diffMs = now - alertTime;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
  };

  return (
    <div className="alert-history">
      <h1>Alert History</h1>

      {/* filter controls */}
      <div className="filter-controls">
        <button 
          className={filter === 'all' ? 'active' : ''} 
          onClick={() => setFilter('all')}
        >
          All Alerts ({alerts.length})
        </button>
        <button 
          className={filter === 'unacknowledged' ? 'active' : ''} 
          onClick={() => setFilter('unacknowledged')}
        >
          Unacknowledged ({alerts.filter(a => !a.acknowledged).length})
        </button>
      </div>

      {/* message */}
      {message && <div className="message">{message}</div>}

      {/* loading state */}
      {loading && <div className="loading">Loading alert history...</div>}

      {/* alert list */}
      {!loading && filteredAlerts.length > 0 ? (
        <div className="alert-list">
          {filteredAlerts.map(alert => (
            <div 
              key={alert.id} 
              className={`alert-item ${alert.alertLevel.toLowerCase()}`}
            >
              <div className="alert-header">
                <span className="alert-level">{alert.alertLevel}</span>
                <span className="alert-time">{formatTimeAgo(alert.sentAt)}</span>
              </div>

              <div className="alert-message">{alert.message}</div>

              <div className="alert-details">
                <div className="detail-row">
                  <span className="detail-label">Satellites:</span>
                  <span className="detail-value">
                    {alert.prediction?.satellite1?.name} ↔ {alert.prediction?.satellite2?.name}
                  </span>
                </div>
              </div>

              <div className="alert-actions">
                {alert.acknowledged ? (
                  <span className="acknowledged">Acknowledged</span>
                ) : (
                  <button 
                    className="acknowledge-btn"
                    onClick={() => handleAcknowledge(alert.id)}
                  >
                    Acknowledge Alert
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        !loading && (
          <div className="no-data">
            {filter === 'all' 
              ? 'No alerts in the last 24 hours. Run collision detection to generate alerts.'
              : 'No unacknowledged alerts.'
            }
          </div>
        )
      )}

      {filteredAlerts.length > 0 && (
        <p className="count">Showing {filteredAlerts.length} alert{filteredAlerts.length !== 1 ? 's' : ''}</p>
      )}
    </div>
  );
}

export default AlertHistory;