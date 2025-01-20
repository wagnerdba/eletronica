import React, { useEffect, useState } from "react";
import axios from "axios";
import Chart from "react-apexcharts";

interface DataPoint {
  mesAno: string;
  temperaturaMinima: number;
  temperaturaMaxima: number;
  variacaoTemperatura: number;
  umidadeMinima: number;
  umidadeMaxima: number;
  variacaoUmidade: number;
  datahoraTemperaturaMinima: string;
  datahoraTemperaturaMaxima: string;
  datahoraUmidadeMinima: string;
  datahoraUmidadeMaxima: string;
}

const FullGraphChart: React.FC = () => {
  const [data, setData] = useState<DataPoint[]>([]);

  const apiUrl = process.env.REACT_APP_API_STATISTICS_YEAR_URL;

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (apiUrl) {
          const response = await axios.get<DataPoint[]>(apiUrl);

          console.log(apiUrl);

          setData(response.data);
        } else {
          console.error("API URL não está definida.");
        }
      } catch (error) {
        console.error("Erro ao buscar os dados:", error);
      }
    };

    fetchData();

    const interval = setInterval(fetchData, 5 * 60 * 1000); // Atualiza a cada 5 minutos

    return () => clearInterval(interval); // Limpa o intervalo ao desmontar o componente
  }, [apiUrl]);

  const options: ApexCharts.ApexOptions = {
    chart: {
      type: "bar",
      toolbar: { show: false },
    },
    xaxis: {
      categories: data.map((d) => d.mesAno),
      title: { text: "" },
    },
    yaxis: {
      title: {
        text: "Valores",
        style: {
          fontWeight: "normal", // Remove o negrito
          fontSize: "16px",
          cssClass: "font-smooth", // Aplica a suavização
        },
      },
      min: -80,
      labels: {
        formatter: (value) => `${value}`, // Formata os valores para melhor leitura
      },
    },
    tooltip: {
      shared: true,
      intersect: false,
      y: {
        formatter: (value) => `${value}º`, // Adiciona o símbolo de grau para as variações de temperatura
      },
      custom: ({ series, seriesIndex, dataPointIndex, w }) => {
        const currentData = data[dataPointIndex];
        if (!currentData) return "";

        const tempMinTime = currentData.datahoraTemperaturaMinima;
        const tempMaxTime = currentData.datahoraTemperaturaMaxima;
        const humidityMinTime = currentData.datahoraUmidadeMinima;
        const humidityMaxTime = currentData.datahoraUmidadeMaxima;

        console.log(tempMinTime);

        return `
          <div class="tooltip-custom">
            <strong>${currentData.mesAno}</strong>
            <br/><strong>Temperatura Mínima:</strong> ${currentData.temperaturaMinima} (Hora: ${tempMinTime})
            <br/><strong>Temperatura Máxima:</strong> ${currentData.temperaturaMaxima} (Hora: ${tempMaxTime})
            <br/><strong>Variação de Temperatura:</strong> ${currentData.variacaoTemperatura}
            <br/><strong>Umidade Mínima:</strong> ${currentData.umidadeMinima} (Hora: ${humidityMinTime})
            <br/><strong>Umidade Máxima:</strong> ${currentData.umidadeMaxima} (Hora: ${humidityMaxTime})
            <br/><strong>Variação de Umidade:</strong> ${currentData.variacaoUmidade}
          </div>
        `;
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "70%",
        borderRadius: 4,
      },
    },
    colors: ["#0056B3", "#FFEA00", "#9B00FF", "#D73722", "#37f511", "#E12B5A"],
    dataLabels: {
      enabled: false,
    },
    legend: {
      position: "top",
      horizontalAlign: "center",
    },
  };

  const series = [
    {
      name: "Temperatura Mínima (ºC)",
      data: data.map((d) => d.temperaturaMinima),
    },
    {
      name: "Temperatura Máxima (ºC)",
      data: data.map((d) => d.temperaturaMaxima),
    },
    {
      name: "Variação de Temperatura (ºC)",
      data: data.map((d) => d.variacaoTemperatura),
    },
    {
      name: "Umidade Mínima (%)",
      data: data.map((d) => d.umidadeMinima),
    },
    {
      name: "Umidade Máxima (%)",
      data: data.map((d) => d.umidadeMaxima),
    },
    {
      name: "Variação de Umidade (%)",
      data: data.map((d) => d.variacaoUmidade),
    },
  ];

  return (
    <>
      <div className="graph-item">
        <h3>Máximas por Mês/Ano</h3>
      </div>
      <div style={{ maxWidth: "900px", margin: "0 auto" }}>
        <Chart options={options} series={series} type="bar" height={340} />
      </div>
    </>
  );
};

export default FullGraphChart;
