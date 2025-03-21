import React from "react";
import HistoricalGraph from "./components/charts/HistoricalGraph";
import RealTimeGraph from "./components/charts/RealTimeGraph";
import RealTimeText from "./components/text/RealTimeText";
import TemperatureHumidityChart from "./components/charts/TemperatureHumidityChart";
import TemperatureHumidityHourChart from "./components/charts/TemperatureHumidityHourChart";
import FullGraphChart from "./components/charts/FullGraph";

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
        </div>
        <div className="espacamento-entre-charts"></div>
        <div className="graph-container">
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
            <FullGraphChart />
          </div>
          <div className="chart-container">
            <TemperatureHumidityChart />
          </div>
        </div>
      </div>
    </div>
  );
};

export default App;
