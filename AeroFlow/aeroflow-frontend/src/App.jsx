import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api/v1/aeroflow';

function App() {
  const [logs, setLogs] = useState([]);
  const [delayedPassengers, setDelayedPassengers] = useState([]);
  const [smartHoldFlights, setSmartHoldFlights] = useState([]); // NOVI STATE
  const logsEndRef = useRef(null);
  const [baggageAlerts, setBaggageAlerts] = useState([]);
  const [gateAnomalies, setGateAnomalies] = useState([]);
  const bottleneckLog = Array.isArray(logs) ? logs.find(log => log.includes("[CEP PRAVILO 2]")) : null;
  const cleanBottleneckText = bottleneckLog ? bottleneckLog.replace("\n", "") : "";
  const smartHoldLog = Array.isArray(logs) ? logs.find(log => log.includes("SMART HOLD AKTIVIRAN:")) : null;
  const [compensations, setCompensations] = useState([]);
  // MODAL STATE
  const [modalConfig, setModalConfig] = useState(null); // null = zatvoren, objekt = otvoren
  const [formData, setFormData] = useState({});

  const [virtualTime, setVirtualTime] = useState(new Date().toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }));

  const planes = Array.from({ length: 8 }).map((_, i) => ({
    id: i, top: Math.random() * 80 + '%', speed: (Math.random() * 15 + 10) + 's', size: (Math.random() * 20 + 20) + 'px', delay: (Math.random() * 10) + 's'
  }));

  // Konfiguracija za svako pravilo (Cheat Sheet za frontend)
  const RULE_CONFIGS = {
      REBOOKING: { 
          title: 'TRIGGER: CEP 1 (Rebooking)', 
          endpoint: 'events/flight-update', 
          fields: { 
              flightNumber: 'LH123', 
              status: 'DELAYED', 
              delayMinutes: 65 
          } 
      },
      BOTTLENECK: { 
          title: 'TRIGGER: CEP 2 (Hub Crisis)', 
          endpoint: 'events/simulate-bottleneck', 
          fields: {} 
      },
      BAGGAGE: { 
          title: 'TRIGGER: CEP 3 (Baggage Alarm)', 
          endpoint: 'events/passenger-scan', 
          fields: { 
              passengerId: 'P-1001', 
              location: 'GATE_DEPARTURE' 
          } 
      },
      SMART_HOLD: { 
          title: 'TRIGGER: CEP 4 (Smart Hold)', 
          endpoint: 'events/flight-update', 
          fields: { 
              flightNumber: 'JU500', 
              status: 'DELAYED', 
              delayMinutes: 10 
          } 
      },
      ANOMALY: { 
          title: 'TRIGGER: CEP 5 (Gate Anomaly)', 
          endpoint: 'events/gate-change-anomaly', 
          fields: { 
              flightNumber: 'OS789', 
              passengerId: 'P-2002', 
              wrongTerminal: 'T1' 
          } 
      },
      FAULT: { 
          title: 'TRIGGER: Airline Fault', 
          endpoint: 'events/simulate-fault', 
          fields: { party: 'Airline_Fault' } 
      },
      
      SCENARIO_DAY: { 
          title: 'SCENARIO: Dnevno čekanje', 
          endpoint: 'setup/day', 
          fields: { passengerId: 'P-DAY', flightNumber: 'LH123' } 
      },
      SCENARIO_NIGHT: { 
          title: 'SCENARIO: Noćenje', 
          endpoint: 'setup/night', 
          fields: { passengerId: 'P-NIGHT', flightNumber: 'LH123' } 
      },
      SCENARIO_GROUP: { 
          title: 'SCENARIO: Grupni let', 
          endpoint: 'setup/group', 
          fields: { groupSize: 16 }
      },
  };

  const openModal = (type) => {
    setModalConfig(RULE_CONFIGS[type]);
    setFormData(RULE_CONFIGS[type].fields);
  };

  const execute = async () => {
    await axios.post(`${API_BASE_URL}/${modalConfig.endpoint}`, formData);
    setModalConfig(null);
  };

  const initializeSystem = async () => {
    // 1. Instantno čišćenje UI-a
    setLogs(["Inicijalizacija sistema..."]); // Ovo je prvi ispis
    setDelayedPassengers([]);
    setSmartHoldFlights([]);
    setBaggageAlerts([]);
    setGateAnomalies([]);

    // 2. Poziv na backend
    try {
      await axios.post(`${API_BASE_URL}/setup`);
      setLogs(["Sistem spreman. Baza je inicijalizovana."]);
    } catch (error) {
      setLogs(["Greška: Nije moguće inicijalizovati bazu."]);
    }
  };

  const rebookPassenger = async (id) => {
    try {
      await axios.post(`${API_BASE_URL}/rebook/${id}`);
      // Osvježi listu nakon akcije
      const res = await axios.get(`${API_BASE_URL}/analytics/severely-delayed-passengers`);
      setDelayedPassengers(res.data);
    } catch (err) {
      console.error("Greška pri rebookingu:", err);
    }
  };

  useEffect(() => {
    const interval = setInterval(() => {
      axios.get(`${API_BASE_URL}/logs`).then(res => setLogs(res.data)).catch(() => {});
      axios.get(`${API_BASE_URL}/analytics/severely-delayed-passengers`).then(res => setDelayedPassengers(res.data)).catch(() => {});
      axios.get(`${API_BASE_URL}/analytics/smart-hold-flights`).then(res => setSmartHoldFlights(res.data)).catch(() => {});
      axios.get(`${API_BASE_URL}/analytics/baggage-alerts`).then(res => setBaggageAlerts(res.data)).catch(() => {});
      axios.get(`${API_BASE_URL}/analytics/gate-anomalies`).then(res => setGateAnomalies(res.data)).catch(() => {});
      axios.get(`${API_BASE_URL}/analytics/compensations`).then(res => setCompensations(res.data)).catch(() => {});
    }, 1000);
    
    return () => clearInterval(interval);
  }, []);

  useEffect(() => logsEndRef.current?.scrollIntoView({ behavior: 'smooth' }), [logs]);

  let smartHoldData = null;
  
  if (smartHoldLog) {
    const letMatch = smartHoldLog.match(/leta ([A-Z0-9]+) jer/);
    const brojLjudiMatch = smartHoldLog.match(/grupa od (\d+)/);
    
    if (letMatch && brojLjudiMatch) {
      smartHoldData = {
        flight: letMatch[1],
        peopleCount: brojLjudiMatch[1]
      };
    }
  }

return (
    <div className="app-container">
      {/* MODAL */}
      {modalConfig && (
        <div className="modal-overlay">
          <div className="modal-content glass-panel">
            <h3 className="modal-title">{modalConfig.title}</h3>
            {Object.keys(formData).map(key => (
              <div key={key} style={{marginBottom: '10px'}}>
                <label style={{fontSize: '10px', color: '#94a3b8'}}>{key.toUpperCase()}</label>
                <input className="input-field" value={formData[key]} onChange={e => setFormData({...formData, [key]: e.target.value})} />
              </div>
            ))}
            <button className="btn-primary" onClick={execute}>TRIGGER EVENT</button>
            <button className="close-btn" onClick={() => setModalConfig(null)}>CANCEL</button>
          </div>
        </div>
      )}

      {/* POZADINA */}
      <div className="background-layer">
        <div className="world-map"></div>
        {planes.map((p) => (
          <div key={p.id} className="plane" style={{ fontSize: p.size, animationDuration: p.speed, animationDelay: p.delay }}>✈</div>
        ))}
      </div>

      <div className="content-layer">
        <header className="header glass-panel">
          <h1 className="title">AEROFLOW <span>ENGINE</span></h1>
          <div className="status">● SYSTEM_LIVE | TIME: {virtualTime}</div>
        </header>

        <main className="main-grid">
          <div className="panel glass-panel">
            <div className="section-title">CONTROL_MODULE</div>
            <button onClick={initializeSystem} className="btn-primary mb-20" style={{width: '100%'}}>
              [0] INITIALIZE DATABASE SEEDER
            </button>
            
            <div className="section-title">EVENT DISPATCHER</div>
            <div className="btn-grid">
              <button className="btn" onClick={() => openModal('REBOOKING')}>REBOOKING</button>
              <button className="btn" onClick={() => openModal('BOTTLENECK')}>HUB_CRISIS</button>
              <button className="btn" onClick={() => openModal('BAGGAGE')}>BAGGAGE_SCAN</button>
              <button className="btn" onClick={() => openModal('SMART_HOLD')}>SMART_HOLD</button>
              <button className="btn" onClick={() => openModal('ANOMALY')}>GATE_ANOMALY</button>
              <button className="btn" style={{backgroundColor: '#eab308', color: '#000'}} onClick={() => openModal('FAULT')}>TRIGGER FAULT</button>
            </div>

            <div className="section-title" style={{marginTop: '25px'}}>SCENARIO TESTER</div>
            <div className="btn-grid">
              <button className="btn" style={{backgroundColor: '#3b82f6'}} onClick={() => openModal('SCENARIO_DAY')}>SCENARIO: DAY</button>
              <button className="btn" style={{backgroundColor: '#3b82f6'}} onClick={() => openModal('SCENARIO_NIGHT')}>SCENARIO: NIGHT</button>
              <button className="btn" style={{backgroundColor: '#3b82f6'}} onClick={() => openModal('SCENARIO_GROUP')}>SCENARIO: GROUP</button>
            </div>

            <div className="section-title" style={{marginTop: '25px'}}>ACTIVE_COMPENSATIONS</div>
            <div className="table-container" style={{maxHeight: '200px', overflowY: 'auto'}}>
              <table className="table">
                <thead><tr><th>PASSENGER</th><th>TYPE</th></tr></thead>
                <tbody>
                  {Array.isArray(compensations) && compensations.map((c, i) => (
                    <tr key={i} style={{ backgroundColor: 'rgba(234, 179, 8, 0.1)' }}>
                      <td style={{fontSize: '11px'}}>{c.passenger?.passengerId || "N/A"}</td>
                      <td style={{fontSize: '11px'}}>{c.type}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="right-column">
            <div className="panel glass-panel threat-panel">
              <div className="section-title">THREAT_MONITOR_DASHBOARD</div>
              <div className="table-container">
                <table className="table">
                  <thead><tr><th>ID</th><th>NAME / OBAVIJEST</th><th>STATUS</th></tr></thead>
                  <tbody>
                    
                    {smartHoldData && (
                      <tr style={{ backgroundColor: 'rgba(234, 179, 8, 0.05)' }}>
                        <td style={{ color: '#eab308', fontWeight: 'bold' }}>{smartHoldData.flight}</td>
                        <td style={{ color: '#eab308' }}>
                          Kaskadno kašnjenje u toku. Čeka se {smartHoldData.peopleCount} osoba.
                        </td>
                        <td style={{ color: '#eab308', fontWeight: 'bold', letterSpacing: '1px' }}>SMART_HOLD</td>
                      </tr>
                    )}

                    {delayedPassengers.map((p) => (
                      <tr key={p.passengerId}>
                        <td>{p.passengerId}</td>
                        <td>{p.name}</td>
                        <td className={p.currentStatus === 'REBOOKED' ? 'success' : 'alert'}>
                          {p.currentStatus}
                        </td>
                        <td>
                          {p.currentStatus === 'SEVERELY_DELAYED' && (
                            <button 
                              className="btn-submit" 
                              onClick={() => rebookPassenger(p.passengerId)}
                              style={{padding: '5px 10px', fontSize: '10px'}}
                            >
                              REBOOK
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}

                    {Array.isArray(baggageAlerts) && baggageAlerts.map((b) => (
                      <tr key={b.baggageTag} style={{ backgroundColor: b.status === 'RE_ROUTED' ? 'rgba(34, 197, 94, 0.15)' : 'rgba(239, 68, 68, 0.15)' }}>
                        <td>{b.passengerId}</td>
                        <td>
                          {b.status === 'RE_ROUTED' 
                            ? `✅ Riješeno: Kofer ${b.baggageTag} preusmjeren na let ${b.nextFlight}` 
                            : `❌ ALARM: Prtljag ${b.baggageTag} nije stigao`}
                        </td>
                        <td style={{ fontWeight: 'bold' }}>{b.status}</td>
                      </tr>
                    ))}

                    {Array.isArray(gateAnomalies) && gateAnomalies.map((a, index) => (
                      <tr key={`anomaly-${index}`} style={{ backgroundColor: 'rgba(168, 85, 247, 0.2)' }}>
                        <td style={{ color: '#c084fc' }}>{a.passengerId}</td>
                        <td style={{ color: '#c084fc' }}>
                          ⚠️ ANOMALIJA: Putnik na pogrešnom terminalu ({a.location})
                        </td>
                        <td style={{ color: '#c084fc', fontWeight: 'bold' }}>GATE_ANOMALY</td>
                      </tr>
                    ))}
                    
                    {cleanBottleneckText && (
                      <tr style={{ backgroundColor: 'rgba(159, 18, 57, 0.3)' }}>
                        <td>ALERT</td>
                        <td style={{ color: '#fb7185' }}>{cleanBottleneckText}</td>
                        <td style={{ color: '#fb7185', fontWeight: 'bold' }}>HUB_CRISIS</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="console glass-panel log-panel">
              <div className="section-title">BACKEND_KERNEL_LOGS</div>
              <div className="logs-container">
                {logs.map((l, i) => <div key={i}>{l}</div>)}
                <div ref={logsEndRef} />
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );}

export default App;