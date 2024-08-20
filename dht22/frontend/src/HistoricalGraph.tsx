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

const formatNumber = (value: number | string) => {
  const num = parseFloat(value.toString());
  if (isNaN(num)) return "0.00"; // Adiciona verificação para valores inválidos
  const [integer, decimal = ""] = num.toString().split('.');
  return `${integer}.${decimal.padEnd(2, '0').substring(0, 2)}`;
};

const HistoricalGraph: React.FC = () => {
  const [data, setData] = useState<DataItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = () => {
      axios
        .get("http://192.168.1.9:8081/api/dht22/statistics")
        .then((response) => {
          // console.log("Dados recebidos:", response.data);
          setData(response.data);
          setLoading(false);
        })
        .catch((error) => {
          setError("Erro ao carregar os dados...");
          setLoading(false);
        });
    };

    fetchData();

    const intervalIdData = setInterval(() => {
      fetchData();
    }, 60000);

    return () => {
      clearInterval(intervalIdData);
    };
  }, []);

  const dates = data.map(
    (item) => item.datahoraTemperaturaMinima.split(" ")[0]
  );
  const tempMin = data.map((item) =>
    formatNumber(parseFloat(item.temperaturaMinima.replace("º", "").trim()))
  );
  const tempMax = data.map((item) =>
    formatNumber(parseFloat(item.temperaturaMaxima.replace("º", "").trim()))
  );
  const umidadeMin = data.map((item) =>
    formatNumber(parseFloat(item.umidadeMinima.replace("%", "").trim()))
  );
  const umidadeMax = data.map((item) =>
    formatNumber(parseFloat(item.umidadeMaxima.replace("%", "").trim()))
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
      width: 4.0,
    },
    colors: ["#FF0000", "#0d00ff", "#f0c905", "#23eb58"],
    series: [
      {
        name: "Temperatura Máxima (ºC)",
        type: "line",
        data: tempMax.map(value => parseFloat(value)),
      },
      {
        name: "Temperatura Mínima (ºC)",
        type: "line",
        data: tempMin.map(value => parseFloat(value)),
      },
      {
        name: "Umidade Mínima (%)",
        type: "bar",
        data: umidadeMin.map(value => parseFloat(value)),
      },
      {
        name: "Umidade Máxima (%)",
        type: "bar",
        data: umidadeMax.map(value => parseFloat(value)),
      },
    ],
    plotOptions: {
      bar: {
        columnWidth: "65%",
        borderRadius: 0,
      },
    },
    tooltip: {
      custom: function ({ seriesIndex, dataPointIndex, w }) {
        const item = data[dataPointIndex];
        const tempMinTime = item.datahoraTemperaturaMinima.split(" ")[1];
        const tempMaxTime = item.datahoraTemperaturaMaxima.split(" ")[1];
        const umidMinTime = item.datahoraUmidadeMinima.split(" ")[1];
        const umidMaxTime = item.datahoraUmidadeMaxima.split(" ")[1];

        const tempMin = formatNumber(parseFloat(item.temperaturaMinima.replace("º", "").trim()));
        const tempMax = formatNumber(parseFloat(item.temperaturaMaxima.replace("º", "").trim()));
        const umidMin = formatNumber(parseFloat(item.umidadeMinima.replace("%", "").trim()));
        const umidMax = formatNumber(parseFloat(item.umidadeMaxima.replace("%", "").trim()));

        return `
          <div class="tooltip-custom">
            <strong>Data:</strong> ${dates[dataPointIndex]}<br/>
            <strong>Temperatura Máxima:</strong> ${tempMax}ºC às ${tempMaxTime}<br/>
            <strong>Temperatura Mínima:</strong> ${tempMin}ºC às ${tempMinTime}<br/>
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
    <div className="graph-item">
      <h2>Temperatura e Umidade - Máximas e Mínimas</h2>
      <Chart
        options={options}
        series={options.series}
        type="line"
        height="150%"
      />
    </div>
  );
};

export default HistoricalGraph;