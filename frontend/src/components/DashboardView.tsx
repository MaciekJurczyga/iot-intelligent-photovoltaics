import React from 'react';

const DashboardView: React.FC = () => {
  return (
    <div className="dashboard-overview">
      <div className="stat-card">
        <div className="stat-value-large">
          <span className="text-positive">1.26</span> <span className="unit">kWh</span>
        </div>
        <span className="stat-label">Aktualna produkcja</span>
      </div>
      <div className="stat-card">
        <div className="stat-value-large">
          <span className="text-negative">0.78</span> <span className="unit">kWh</span>
        </div>
        <span className="stat-label">Aktualne zużycie</span>
      </div>
      <div className="stat-card">
        <div className="stat-value-large">
          <span className="text-positive">0.48</span> <span className="unit">kWh</span>
        </div>
        <span className="stat-label">Aktualna nadwyżka</span>
      </div>
    </div>
  );
};

export default DashboardView;