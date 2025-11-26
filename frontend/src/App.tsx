import React, { useState } from 'react';
import './App.css';
import Header from './components/Header';
import DashboardView from './components/DashboardView';
import DevicesView from './components/DevicesView';
import ChartsView from './components/ChartsView';
import ScenariosView from './components/ScenariosView';

export type ViewName = 'dashboard' | 'devices' | 'charts' | 'scenarios';

const App: React.FC = () => {
  const [activeView, setActiveView] = useState<ViewName>('dashboard');
  const renderView = () => {
    switch (activeView) {
      case 'dashboard':
        return <DashboardView />; 
      case 'devices':
        return <DevicesView />;
      case 'charts':
        return <ChartsView />;
      case 'scenarios':
        return <ScenariosView />; 
      default:
        return <DashboardView />;
    }
  };

  return (
    <div className="app-container">
      <Header activeView={activeView} setActiveView={setActiveView} />
      <main className="content-container">
        {renderView()}
      </main>
    </div>
  );
};

export default App;