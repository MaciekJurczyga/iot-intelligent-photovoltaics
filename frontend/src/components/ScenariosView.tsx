import React, { useState } from 'react';
import { FaPlus, FaCog, FaCheckCircle, FaRegCheckCircle, FaChevronUp, FaChevronDown, FaTimes } from 'react-icons/fa';
import { DragDropContext, Droppable, Draggable, DropResult } from '@hello-pangea/dnd';

interface Scenario {
  id: number;
  name: string;
  devices: Device[];
}

interface Device {
  id: string;
  name: string;
}

const initialDevices: Device[] = [
  { id: 'dev-1', name: 'Ładowarka EV' },
  { id: 'dev-2', name: 'Klimatyzacja' },
  { id: 'dev-3', name: 'SmartPlug' },
  { id: 'dev-4', name: 'Zmywarka' },
  { id: 'dev-5', name: 'Pralka' },
  { id: 'dev-6', name: 'Urządzenie1' },
  { id: 'dev-7', name: 'Urządzenie2' },
];

const ScenariosView: React.FC = () => {
  const [selectedId, setSelectedId] = useState<number>(1);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [priorityDevices, setPriorityDevices] = useState<Device[]>(initialDevices);
  const [newScenarioName, setNewScenarioName] = useState('');
  const [editingScenarioId, setEditingScenarioId] = useState<number | null>(null);

  const [scenarios, setScenarios] = useState<Scenario[]>([
    { id: 1, name: 'Komfort', devices: [...initialDevices] },
    { id: 2, name: 'Maksymalne zużycie', devices: [...initialDevices] },
  ]);

  const handleOnDragEnd = (result: DropResult) => {
    if (!result.destination) return;

    const items = Array.from(priorityDevices);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);

    setPriorityDevices(items);
  };

  const openAddModal = () => {
    setEditingScenarioId(null);
    setNewScenarioName('');
    setPriorityDevices([...initialDevices]);
    setIsModalOpen(true);
  };

  const openEditModal = (scenario: Scenario) => {
    setEditingScenarioId(scenario.id);
    setNewScenarioName(scenario.name);
    setPriorityDevices([...scenario.devices]);
    setIsModalOpen(true);
  };

  const handleSave = () => {
    if (!newScenarioName.trim()) return;

    if (editingScenarioId !== null) {
      setScenarios(scenarios.map(s => 
        s.id === editingScenarioId 
          ? { ...s, name: newScenarioName, devices: priorityDevices }
          : s
      ));
    } else {
      const newId = scenarios.length > 0 ? Math.max(...scenarios.map(s => s.id)) + 1 : 1;
      const newScenario = { id: newId, name: newScenarioName, devices: priorityDevices };
      
      setScenarios([...scenarios, newScenario]);
      setSelectedId(newId);
    }

    setIsModalOpen(false);
    setNewScenarioName('');
    setEditingScenarioId(null);
  };

  return (
    <div className="scenarios-container">
      <button className="add-mode-button" onClick={openAddModal}>
        Dodaj własny tryb <FaPlus />
      </button>

      <h3>Tryby zużycia</h3>

      <ul className="modes-list">
        {scenarios.map((scenario) => (
          <li 
            key={scenario.id} 
            className={`mode-item ${selectedId === scenario.id ? 'active' : ''}`}
          >
            <div className="mode-name">
              {scenario.name}
            </div>
            
            <div className="mode-action-cell">
              <button className="icon-button" onClick={() => openEditModal(scenario)}>
                <FaCog />
              </button>
            </div>
            
            <div className="mode-action-cell">
              <button 
                className={`icon-button check-btn ${selectedId === scenario.id ? 'active' : ''}`}
                onClick={() => setSelectedId(scenario.id)}
              >
                {selectedId === scenario.id ? <FaCheckCircle /> : <FaRegCheckCircle />}
              </button>
            </div>
          </li>
        ))}
      </ul>

      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal-content">
            <div className="modal-header">
              <h2>{editingScenarioId ? 'Edytuj tryb' : 'Nowy tryb'}</h2>
              <button className="close-button" onClick={() => setIsModalOpen(false)}>
                <FaTimes />
              </button>
            </div>

            <div className="modal-input-group">
              <label>Nazwa trybu</label>
              <input 
                type="text" 
                className="modal-input"
                placeholder="Np. Tryb nocny"
                value={newScenarioName}
                onChange={(e) => setNewScenarioName(e.target.value)}
              />
            </div>

            <div className="modal-subheader">Ustal priorytety urządzeń</div>
            
            <DragDropContext onDragEnd={handleOnDragEnd}>
              <Droppable droppableId="devices">
                {(provided) => (
                  <ul className="priority-list" {...provided.droppableProps} ref={provided.innerRef}>
                    {priorityDevices.map((device, index) => (
                      <Draggable key={device.id} draggableId={device.id} index={index}>
                        {(provided) => (
                          <li 
                            className="priority-item" 
                            ref={provided.innerRef} 
                            {...provided.draggableProps} 
                            {...provided.dragHandleProps}
                          >
                            <div className="drag-handle">
                              <FaChevronUp size={10} />
                              <FaChevronDown size={10} />
                            </div>
                            <span className="device-name">{device.name}</span>
                          </li>
                        )}
                      </Draggable>
                    ))}
                    {provided.placeholder}
                  </ul>
                )}
              </Droppable>
            </DragDropContext>
            
            <div className="modal-actions">
               <button className="save-button" onClick={handleSave}>Zapisz</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ScenariosView;