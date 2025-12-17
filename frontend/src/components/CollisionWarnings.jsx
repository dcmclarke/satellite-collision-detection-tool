import React, { useState, useEffect } from 'react';
import { collisionApi } from '../services/api';
import './CollisionWarnings.css';

function CollisionWarnings() {
  const [collisions, setCollisions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('critical-warning');
  const [message, setMessage] = useState('');

  //load collisions when page opens
  useEffect(() => {
    loadCollisions();
  }, []);

  const loadCollisions = async () => {
    setLoading(true);
    try {
      const response = await collisionApi.getActiveCollisions();
      setCollisions(response.data);
      setMessage(`Loaded ${response.data.length} active collision predictions`);
    } catch (error) {
      setMessage(`Error: ${error.message}`);
      console.error('Error loading collisions:', error);
    } finally {
      setLoading(false);
    }
  };

  //filter collisions based on selected tab
  const filteredCollisions = collisions.filter(collision => {
    if (filter === 'all') return true;
    if (filter === 'critical-warning') {
      return collision.riskLevel.toLowerCase() === 'critical' || 
             collision.riskLevel.toLowerCase() === 'warning';
    }
    return collision.riskLevel.toLowerCase() === filter;
  });

  //get count each risk level
  const criticalCount = collisions.filter(c => c.riskLevel === 'CRITICAL').length;
  const warningCount = collisions.filter(c => c.riskLevel === 'WARNING').length;
  const infoCount = collisions.filter(c => c.riskLevel === 'INFO').length;
  const criticalWarningCount = criticalCount + warningCount;

  return (
    <div className="collision-warnings">
      <h1>Collision Warnings</h1>

      {/* filter tabs */}
      <div className="filter-tabs">
        <button 
          className={filter === 'critical-warning' ? 'active' : ''} 
          onClick={() => setFilter('critical-warning')}
        >
          Critical + Warning ({criticalWarningCount})
        </button>
        <button 
          className={filter === 'all' ? 'active' : ''} 
          onClick={() => setFilter('all')}
        >
          All ({collisions.length})
        </button>
        <button 
          className={filter === 'critical' ? 'active critical' : 'critical'} 
          onClick={() => setFilter('critical')}
        >
          Critical ({criticalCount})
        </button>
        <button 
          className={filter === 'warning' ? 'active warning' : 'warning'} 
          onClick={() => setFilter('warning')}
        >
          Warning ({warningCount})
        </button>
        <button 
          className={filter === 'info' ? 'active info' : 'info'} 
          onClick={() => setFilter('info')}
        >
          Info ({infoCount})
        </button>
      </div>

      {message && <div className="message">{message}</div>}
      {loading && <div className="loading">Loading collisions...</div>}

      {/* collision cards */}
      {!loading && filteredCollisions.length > 0 ? (
        <div className="collision-grid">
          {filteredCollisions.map(collision => (
            <div 
              key={collision.id} 
              className={`collision-card ${collision.riskLevel.toLowerCase()}`}
            >
              <div className="card-header">
                <span className="risk-badge">{collision.riskLevel}</span>
                <span className="distance">
                  {collision.minimumDistance.toFixed(2)} km
                </span>
              </div>

              <div className="card-body">
                <h3>
                  {collision.satellite1.name} ↔ {collision.satellite2.name}
                </h3>
                
                <div className="card-details">
                  <div className="detail-item">
                    <span className="label">Probability:</span>
                    <span className="value">{collision.probabilityScore}%</span>
                  </div>
                  <div className="detail-item">
                    <span className="label">NORAD IDs:</span>
                    <span className="value">
                      {collision.satellite1.noradId} / {collision.satellite2.noradId}
                    </span>
                  </div>
                  <div className="detail-item">
                    <span className="label">Status:</span>
                    <span className="value">{collision.status}</span>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        !loading && (
          <div className="no-data">
            {filter === 'all' 
              ? 'No collision predictions found. Run collision detection first!'
              : filter === 'critical-warning'
              ? 'No critical or warning collisions found.'
              : `No ${filter} risk collisions found.`
            }
          </div>
        )
      )}

      {/* counting collisions */}
      {filteredCollisions.length > 0 && (
        <p className="count">Showing {filteredCollisions.length} predictions</p>
      )}
    </div>
  );
}

export default CollisionWarnings;