import React, { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api/v1/aeroflow';

function App() {
  const [logs, setLogs] = useState([]);
  const [delayedPassengers, setDelayedPassengers] = useState([]);
  const logsEndRef = useRef(null);

  const planes = Array.from({ length: 8 }).map((_, i) => ({
    id: i,
    top: Math.random() * 80 + '%',
    speed: (Math.random() * 15 + 10) + 's',
    size: (Math.random() * 20 + 20) + 'px',
    delay: (Math.random() * 10) + 's'
  }));

  useEffect(() => {
    const interval = setInterval(() => {
      axios.get(`${API_BASE_URL}/logs`).then(res => setLogs(res.data)).catch(() => {});
      axios.get(`${API_BASE_URL}/analytics/severely-delayed-passengers`).then(res => setDelayedPassengers(res.data)).catch(() => {});
    }, 1000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => logsEndRef.current?.scrollIntoView({ behavior: 'smooth' }), [logs]);

  const execute = async (url, data) => { await axios.post(`${API_BASE_URL}/${url}`, data); };

  return (
  <div className="app-container">

    <div className="background-layer">
      <div className="world-map"></div>
      <div className="world-glow"></div>

      <div
        className="flight-line"
        style={{
          width: "500px",
          top: "32%",
          left: "18%",
          transform: "rotate(12deg)"
        }}
      />

      <div
        className="flight-line"
        style={{
          width: "420px",
          top: "58%",
          left: "40%",
          transform: "rotate(-18deg)"
        }}
      />

      <div
        className="flight-line"
        style={{
          width: "600px",
          top: "45%",
          left: "22%",
          transform: "rotate(6deg)"
        }}
      />

      {planes.map((p) => (
        <div
          key={p.id}
          className="plane"
          style={{
            fontSize: p.size,
            animationDuration: p.speed,
            animationDelay: p.delay,
            offsetPath:
              p.id % 2 === 0
                ? 'path("M 150 300 Q 700 100 1400 450")'
                : 'path("M 200 700 Q 900 250 1700 500")'
          }}
        >
          ✈
        </div>
      ))}
    </div>

    <div className="content-layer">

      <header className="header glass-panel">
        <h1 className="title">
          AEROFLOW <span>ENGINE</span>
        </h1>

        <div className="status">
          ● SYSTEM_LIVE
        </div>
      </header>

      <main className="main-grid">

        <div className="panel glass-panel">

          <div className="section-title">
            CONTROL_MODULE
          </div>

          <button
            onClick={() => execute('setup', {})}
            className="btn-primary"
          >
            INITIALIZE_MEMORY
          </button>

          <div className="btn-grid">

            <button
              onClick={() =>
                execute('events/flight-update', {
                  flightNumber:'LH123',
                  status:'DELAYED',
                  delayMinutes:65
                })
              }
              className="btn"
            >
              REBOOKING
            </button>

            <button
              onClick={() =>
                execute('events/simulate-bottleneck', {})
              }
              className="btn"
            >
              HUB_CRISIS
            </button>

            <button
              onClick={() =>
                execute('events/passenger-scan', {
                  passengerId:'P-1001',
                  location:'GATE_DEPARTURE'
                })
              }
              className="btn"
            >
              BAGGAGE
            </button>

            <button
              onClick={() =>
                execute('events/flight-update', {
                  flightNumber:'JU500',
                  status:'DELAYED',
                  delayMinutes:10
                })
              }
              className="btn"
            >
              SMART_HOLD
            </button>

            <button
              onClick={() =>
                execute('events/gate-change-anomaly', {
                  flightNumber:'OS789',
                  passengerId:'P-2002',
                  wrongTerminal:'T1'
                })
              }
              className="btn"
            >
              ANOMALY
            </button>

          </div>

          <div
            className="section-title"
            style={{ marginTop: '28px' }}
          >
            THREAT_MONITOR
          </div>

          <table className="table">
            <thead>
              <tr>
                <th>ID</th>
                <th>NAME</th>
                <th>STATUS</th>
              </tr>
            </thead>

            <tbody>
              {delayedPassengers.map((p) => (
                <tr key={p.passengerId}>
                  <td>{p.passengerId}</td>
                  <td>{p.name}</td>
                  <td className="alert">
                    {p.currentStatus}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

        </div>

        <div className="console glass-panel">

          <div className="section-title">
            BACKEND_KERNEL_LOGS
          </div>

          <div className="logs-container">
            {logs.map((l, i) => (
              <div key={i} style={{ marginBottom: '8px' }}>
                {l}
              </div>
            ))}

            <div ref={logsEndRef} />
          </div>

        </div>

      </main>
    </div>
  </div>
);}

const styles = {
  header: { display: 'flex', justifyContent: 'space-between', padding: '15px 20px', background: 'rgba(22, 27, 34, 0.8)', borderRadius: '10px', border: '1px solid #30363d' },
  title: { fontSize: '1.2rem', color: '#fff', margin: 0 },
  panel: { background: 'rgba(22, 27, 34, 0.85)', backdropFilter: 'blur(10px)', padding: '20px', borderRadius: '15px', border: '1px solid #30363d', display: 'flex', flexDirection: 'column' },
  console: { background: 'rgba(0, 0, 0, 0.85)', backdropFilter: 'blur(10px)', borderRadius: '15px', border: '1px solid #38bdf8', padding: '20px', display: 'flex', flexDirection: 'column', height: '100%' },
  btnGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginTop: '10px' },
  btn: { background: 'transparent', border: '1px solid #38bdf8', color: '#38bdf8', padding: '10px', borderRadius: '5px', cursor: 'pointer', fontFamily: 'monospace' },
  btnPrimary: { width: '100%', background: '#38bdf8', border: 'none', padding: '12px', color: '#000', borderRadius: '5px', fontWeight: 'bold' },
  logsContainer: { fontFamily: 'monospace', fontSize: '12px', color: '#22c55e', overflowY: 'auto', flex: 1, marginTop: '10px' },
  h3: { color: '#38bdf8', fontSize: '12px', margin: '0 0 10px 0', letterSpacing: '1px' },
  status: { color: '#22c55e', fontSize: '12px', fontFamily: 'monospace' },
  table: { width: '100%', textAlign: 'left', borderCollapse: 'collapse', marginTop:'10px' }
};

export default App;