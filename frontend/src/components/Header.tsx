import React from 'react';
import { FaHome, FaSlidersH, FaChartBar, FaUser } from 'react-icons/fa';
import { ViewName } from '../App'; 
import logo from '../logo.svg';

interface HeaderProps {
  activeView: ViewName;
  setActiveView: (view: ViewName) => void;
}

const Header: React.FC<HeaderProps> = ({ activeView, setActiveView }) => {
  return (
    <nav className="header-nav">
      <div className="logo-container">
        <img src={logo} alt="Logo" className="app-logo" />
      </div>
      <div className="nav-buttons">
        <button
          className={`nav-button ${activeView === 'dashboard' ? 'active' : ''}`}
          onClick={() => setActiveView('dashboard')}
        >
          <FaHome /> Panel główny
        </button>
        <button
          className={`nav-button ${activeView === 'devices' ? 'active' : ''}`}
          onClick={() => setActiveView('devices')}
        >
          <FaSlidersH /> Moje urządzenia
        </button>
        <button
          className={`nav-button ${activeView === 'charts' ? 'active' : ''}`}
          onClick={() => setActiveView('charts')}
        >
          <FaChartBar /> Wykresy
        </button>
        <button
          className={`nav-button ${activeView === 'scenarios' ? 'active' : ''}`}
          onClick={() => setActiveView('scenarios')}
        >
          <FaUser /> Scenariusze
        </button>
      </div>
    </nav>
  );
};

export default Header;