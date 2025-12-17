import React from 'react';
import { Link } from 'react-router-dom';
import './Navigation.css';

function Navigation() {
  return (
    <nav className="navbar">
      <div className="navbar-content">
        <div className="navbar-brand">
          <div className="brand-text">
            <h1>Satellite Collision Detection</h1>
          </div>
        </div>
        
        <div className="navbar-links">
          <Link to="/" className="nav-link">Satellites</Link>
          <Link to="/collisions" className="nav-link">Warnings</Link>
          <Link to="/alerts" className="nav-link">Alerts</Link>
          <Link to="/how-to-use" className="nav-link">Guide</Link>
        </div>
      </div>
    </nav>
  );
}

export default Navigation;