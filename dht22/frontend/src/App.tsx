import React from "react";
import HeaderTitle from "./HeaderTitle"; // Importe o novo componente
import HistoricalGraph from "./HistoricalGraph";
import RealTimeGraph from "./RealTimeGraph";
import RealTimeText from "./RealTimeText";
import "./App.css"; // Certifique-se de que este caminho está correto

const App: React.FC = () => {
  return (
    <div className="App">
      <HeaderTitle /> {/* Adicione o título ao topo */}
      <div className="graph-container">
        <div className="chart-container">
          <HistoricalGraph />
        </div>
        <div className="chart-container">
          <RealTimeGraph />
        </div>
      </div>
      <div className="temperature-panel-container">
        <RealTimeText />
      </div>
    </div>
  );
};

export default App;
