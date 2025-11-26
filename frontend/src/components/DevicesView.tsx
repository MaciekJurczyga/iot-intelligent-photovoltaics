import React, { useState } from 'react';
import { FaCog, FaTimes, FaPowerOff } from 'react-icons/fa';

interface Device {
  name: string;
  status: string;
  statusColor: string;
  consumption: string;
}

const DevicesView: React.FC = () => {
  const [selectedDevice, setSelectedDevice] = useState<Device | null>(null);

  // Dane z obrazka
  const [devices, setDevices] = useState<Device[]>([
    { name: 'Ładowarka EV', status: 'Działa', statusColor: 'positive', consumption: '0.56 kWh' },
    { name: 'Klimatyzacja', status: 'Działa', statusColor: 'positive', consumption: '0.23 kWh' },
    { name: 'SmartPlug', status: 'Działa', statusColor: 'positive', consumption: '0.12 kWh' },
    { name: 'Zmywarka', status: 'Czeka', statusColor: 'warning', consumption: '0.00 kWh' },
    { name: 'Pralka', status: 'Czeka', statusColor: 'warning', consumption: '0.00 kWh' },
    { name: 'Urządzenie1', status: 'Offline', statusColor: 'negative', consumption: 'brak danych' },
    { name: 'Urządzenie2', status: 'Offline', statusColor: 'negative', consumption: 'brak danych' },
  ]);

  const handleToggleDevice = () => {
    if (!selectedDevice) return;

    setDevices(prevDevices => prevDevices.map(dev => {
      if (dev.name === selectedDevice.name) {
        if (dev.status === 'Działa') {
          return { ...dev, status: 'Czeka', statusColor: 'warning', consumption: '0.00 kWh' };
        } else {
          // Symulacja włączenia - losowy pobór lub stała wartość
          const randomConsumption = (Math.random() * 1.5 + 0.1).toFixed(2);
          return { ...dev, status: 'Działa', statusColor: 'positive', consumption: `${randomConsumption} kWh` };
        }
      }
      return dev;
    }));
    setSelectedDevice(null);
  };

  return (
    <div className="devices-table-container">
      <table className="devices-table">
        <thead>
          <tr>
            <th>Urządzenie</th>
            <th>Status</th>
            <th>Aktualny pobór</th>
            <th></th> {/* Kolumna na ikonę trybika */}
          </tr>
        </thead>
        <tbody>
          {devices.map((device, index) => (
            <tr key={index}>
              <td className={device.statusColor === 'negative' ? 'text-negative' : ''}>
                {device.name}
              </td>
              <td className={`text-${device.statusColor}`}>{device.status}</td>
              <td className={device.statusColor === 'negative' ? 'text-negative' : ''}>
                {device.consumption}
              </td>
              <td>
                <button className="icon-button" onClick={() => setSelectedDevice(device)}>
                  <FaCog />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {selectedDevice && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>Ustawienia: {selectedDevice.name}</h2>
              <button className="close-button" onClick={() => setSelectedDevice(null)}>
                <FaTimes />
              </button>
            </div>
            
            <div style={{ marginBottom: '24px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', borderBottom: '1px solid #383838', paddingBottom: '8px' }}>
                    <span style={{ color: '#888' }}>Dzienne zużycie:</span>
                    <span style={{ color: '#fff', fontWeight: 'bold' }}>2.4 kWh</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '12px', borderBottom: '1px solid #383838', paddingBottom: '8px' }}>
                    <span style={{ color: '#888' }}>Tygodniowe zużycie:</span>
                    <span style={{ color: '#fff', fontWeight: 'bold' }}>14.5 kWh</span>
                </div>
            </div>

            <div className="modal-actions" style={{ justifyContent: 'center' }}>
               <button 
                className="save-button" 
                style={{ 
                    backgroundColor: selectedDevice.status === 'Działa' ? '#f44336' : '#4caf50',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '8px'
                }}
                onClick={handleToggleDevice}
               >
                <FaPowerOff />
                {selectedDevice.status === 'Działa' ? 'Wyłącz' : 'Włącz'}
               </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DevicesView;