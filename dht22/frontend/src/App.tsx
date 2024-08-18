import React from 'react';
import Graph from './Graph.tsx';
import './App.css';

const App: React.FC = () => {
  return (
    <div className="App">
      <h1>ESP32 com DTH22</h1>
      <Graph />
    </div>
  );
};

export default App;
