import React from "react";
import HeaderTitle from "./components/text/HeaderTitle"; // Importe o novo componente
import HistoricalGraph from "./components/charts/HistoricalGraph";
import RealTimeGraph from "./components/charts/RealTimeGraph";
import RealTimeText from "./components/text/RealTimeText";
import './assets/css/styles.css'; // Certifique-se de que este caminho está correto

const App: React.FC = () => {
  return (
    <div className="background">
   
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


    </div>
  );
};

export default App;

