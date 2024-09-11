import React from "react";
import HistoricalGraph from "./components/charts/HistoricalGraph";
import RealTimeGraph from "./components/charts/RealTimeGraph";
import RealTimeText from "./components/text/RealTimeText";
import TemperatureHumidityChart2 from "./components/charts/TemperatureHumidityChart2";
import TemperatureHumidityHourChart from "./components/charts/TemperatureHumidityHourChart";

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
        <div className="espacamento-entre-charts"></div>
        <div className="graph-container">
          <div className="chart-container">
            <TemperatureHumidityHourChart />
          </div>
          <div className="chart-container">
            <TemperatureHumidityChart2 />
          </div>
        </div>
      </div>
    </div>
  );
};

export default App;
