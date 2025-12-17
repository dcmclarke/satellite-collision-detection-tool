import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navigation from './components/Navigation';
import SatelliteList from './components/SatelliteList';
import CollisionWarnings from './components/CollisionWarnings';
import AlertHistory from './components/AlertHistory';
import Guide from './components/Guide';
import './App.css';

function App() {
  return (
    <BrowserRouter>
      <Navigation />
      
      <div className="app-container">
        <Routes>
          <Route path="/" element={<SatelliteList />} />
          <Route path="/collisions" element={<CollisionWarnings />} />
          <Route path="/alerts" element={<AlertHistory />} />
          <Route path="/how-to-use" element={<Guide />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;