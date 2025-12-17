import React from 'react';
import './Guide.css';

function Guide() {
  return (
    <div className="how-to-use">
      <div className="guide-header">
        <h1>User Guide</h1>
        <p>Learn how to use the Satellite Collision Detection System</p>
      </div>

      <div className="guide-content">
        <section className="guide-section">
          <h2>Getting Started</h2>
          <ol>
            <li>Navigate to the Satellites page</li>
            <li>Then click either Load Backup Data (4 satellites in case API down) or Fetch NASA Data (500 satellites)</li>
            <li>Data will take a few seconds to load and you'll see a table of satellites</li>
            <li>Click Run Collision Detection</li>
            <li>Continue onto Warnigns page to view results or Alert page to acknowledge alerts</li>
          </ol>
        </section>

        <section className="guide-section">
          <h2>Understanding Risk Levels</h2>
          <div className="risk-guide">
            <div className="risk-item critical">
              <span className="risk-badge">CRITICAL</span>
              <p>Satellites less than 2km apart means attention required immediately</p>
            </div>
            <div className="risk-item warning">
              <span className="risk-badge">WARNING</span>
              <p>Satellites 2-3.5km apart means monitor more closely</p>
            </div>
            <div className="risk-item info">
              <span className="risk-badge">INFO</span>
              <p>Satellites 3.5-5km apart means informational only</p>
            </div>
          </div>
        </section>

        <section className="guide-section">
          <h2>Page Descriptions</h2>
          
          <h3>Satellites Page</h3>
          <p><strong>Load Backup Data:</strong> Loads 4 pre-configured satellites for quick testing</p>
          <p><strong>Fetch NASA Data:</strong> Downloads real-time data from NASA Space-Track (takes 5-10 seconds)</p>
          <p><strong>Run Collision Detection:</strong> Analyses all satellite pairs for potential collisions</p>
          <p><strong>Search:</strong> Filter satellites by name or NORAD ID</p>
          <p><strong>Table Headers:</strong> Click to sort by that column</p>

          <h3>Warnings Page</h3>
          <p><strong>Filter Tabs:</strong> Show all warnings or filter by risk level (Critical/Warning/Info)</p>
          <p><strong>Collision Cards:</strong> Display details about each potential collision including distance, probability, and satellite names</p>

          <h3>Alerts Page</h3>
          <p><strong>All Alerts:</strong> View all alerts from last 24 hours</p>
          <p><strong>Unacknowledged:</strong> Filter to show only alerts that haven't been reviewed</p>
          <p><strong>Acknowledge Alert:</strong> Mark an alert as reviewed (will sort to the bottom of the page)</p>
        </section>

        <section className="guide-section">
          <h2>Technical Details</h2>
          <p><strong>Algorithm:</strong> Distance-based collision detection using 3D Euclidean distance calculation</p>
          <p><strong>Data Source:</strong> NASA Space-Track.org TLE (Two-Line Element) orbital data</p>
          <p><strong>Update Frequency:</strong> Manually done click buttons to refresh data</p>
          <p><strong>Threshold:</strong> Detects satellites within 5km of each other</p>
        </section>
      </div>
    </div>
  );
}

export default Guide;