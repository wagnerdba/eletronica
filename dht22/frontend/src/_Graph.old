import React, { useEffect, useState } from "react";
import axios from "axios";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";
import "./App.css";

interface DataItem {
  datahoraTemperaturaMinima: string;
  temperaturaMinima: string;
  datahoraTemperaturaMaxima: string;
  temperaturaMaxima: string;
  variacaoTemperatura: string;
  datahoraUmidadeMinima: string;
  umidadeMinima: string;
  datahoraUmidadeMaxima: string;
  umidadeMaxima: string;
  variacaoUmidade: string;
}

const Graph: React.FC = () => {
  const [data, setData] = useState<DataItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = () => {
      axios
        .get("http://192.168.1.9:8081/api/dht22/statistics")
        .then((response) => {
          setData(response.data);
          setLoading(false);
        })
        .catch((error) => {
          setError("Erro ao carregar os dados...");
          setLoading(false);
        });
    };

    fetchData();
    const intervalId = setInterval(fetchData, 60000);

    return () => clearInterval(intervalId);
  }, []);

  const dates = data.map(
    (item) => item.datahoraTemperaturaMinima.split(" ")[0]
  );
  const tempMin = data.map((item) =>
    parseFloat(item.temperaturaMinima.replace("º", "").trim())
  );
  const tempMax = data.map((item) =>
    parseFloat(item.temperaturaMaxima.replace("º", "").trim())
  );
  const umidadeMin = data.map((item) =>
    parseFloat(item.umidadeMinima.replace("%", "").trim())
  );
  const umidadeMax = data.map((item) =>
    parseFloat(item.umidadeMaxima.replace("%", "").trim())
  );

  const options: ApexOptions = {
    chart: {
      type: "line" as const,
    },
    xaxis: {
      categories: dates,
      tickPlacement: "on",
      labels: {
        format: "dd/MM/yyyy",
      },
    },
    yaxis: [
      {
        title: {
          text: "Temperatura (ºC)",
        },
        seriesName: "Temperatura",
      },
      {
        opposite: true,
        title: {
          text: "Umidade (%)",
        },
      },
    ],
    stroke: {
      curve: "smooth",
      width: 3,
    },
    colors: ["#FF0000", "#0d00ff", "#f0c905", "#23eb58"],
    series: [
      {
        name: "Temperatura Máxima (ºC)",
        type: "line",
        data: tempMax,
      },
      {
        name: "Temperatura Mínima (ºC)",
        type: "line",
        data: tempMin,
      },
      {
        name: "Umidade Mínima (%)",
        type: "bar",
        data: umidadeMin,
      },
      {
        name: "Umidade Máxima (%)",
        type: "bar",
        data: umidadeMax,
      },
    ],
    plotOptions: {
      bar: {
        columnWidth: "65%",
        borderRadius: 0, // Remove as bordas arredondadas
        //borderWidth: 0, // Remove as bordas das barras
        colors: {
          ranges: [
            {
              from: -Infinity,
              to: 29,
              color: "#fc6579", // Cor vermelha para valores abaixo de 12%
            },
            
            {
              from: 30,
              to: 34,
              color: "#28eefc", // Cor rosa para valores entre 13% e 30%
            },
            
          ],
        },
      },
    },
    tooltip: {
      custom: function ({ series, seriesIndex, dataPointIndex, w }) {
        const tempMinTime =
          data[dataPointIndex].datahoraTemperaturaMinima.split(" ")[1];
        const tempMaxTime =
          data[dataPointIndex].datahoraTemperaturaMaxima.split(" ")[1];
        const umidMinTime =
          data[dataPointIndex].datahoraUmidadeMinima.split(" ")[1];
        const umidMaxTime =
          data[dataPointIndex].datahoraUmidadeMaxima.split(" ")[1];

        const tempMin = series[1][dataPointIndex];
        const tempMax = series[0][dataPointIndex];
        const umidMin = series[2][dataPointIndex];
        const umidMax = series[3][dataPointIndex];

        return `
          <div class="tooltip-custom">
            <strong>Data:</strong> ${dates[dataPointIndex]}<br/>
            <strong>Temperatura Máxima:</strong> ${tempMax}ºc às ${tempMaxTime}<br/>
            <strong>Temperatura Mínima:</strong> ${tempMin}ºc às ${tempMinTime}<br/>
            <strong>Umidade Máxima:</strong> ${umidMax}% às ${umidMaxTime}<br/>
            <strong>Umidade Mínima:</strong> ${umidMin}% às ${umidMinTime}
          </div>
        `;
      },
    },
  };

  if (loading) return <p>Carregando...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="graph-container">
      <h2>Gráfico de Temperatura e Umidade</h2>
      <Chart
        options={options}
        series={options.series}
        type="line"
        height={400}
      />
    </div>
  );
};

export default Graph;
