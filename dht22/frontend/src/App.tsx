import React from "react";
/* import HeaderTitle from "./components/text/HeaderTitle"; // Importe o novo componente */
import HistoricalGraph from "./components/charts/HistoricalGraph";
import RealTimeGraph from "./components/charts/RealTimeGraph";
import RealTimeText from "./components/text/RealTimeText";
import "./assets/css/styles.css"; // Certifique-se de que este caminho está correto
//import TemperatureHumidityChart from "./components/charts/TemperatureHumidityChart";
import TemperatureHumidityChart2 from "./components/charts/TemperatureHumidityChart2";
import SensorDataCountText from "./components/text/SensorDataCountText";

const App: React.FC = () => {
  return (
    <div className="background">
      <div className="App">
        {/* <HeaderTitle /> */}
        <RealTimeText />
        {/*
        <div className="graph-container">
          <div className="panel-container"></div>
          <div className="panel-container"></div>
          <div className="panel-container"></div>
          <div className="panel-container"></div>
        </div>
        */}

        <div className="graph-container">
          <div className="chart-container">
            <HistoricalGraph />
          </div>
          <div className="chart-container">
            <RealTimeGraph />
          </div>
        </div>
        <div className="temperature-panel-container chart-container-humid">
          <TemperatureHumidityChart2 />
        </div>

      </div>
    </div>
  );
};

export default App;
