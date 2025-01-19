import React from "react";
import Chart from "react-apexcharts";

interface DataPoint {
  monthYear: string;
  tempMin: number;
  tempMax: number;
  tempVariation: number;
  humidityMin: number;
  humidityMax: number;
  humidityVariation: number;
  dataHoraTempMinima: string;
  dataHoraTemperaturaMaxima: string;
  dataHoraUmidadeMinima: string;
  dataHoraUmidadeMaxima: string;
}

const mockData: DataPoint[] = [
  {
    monthYear: "08/2024",
    tempMin: 15.6,
    tempMax: 32.9,
    tempVariation: -17.3,
    humidityMin: 12.6,
    humidityMax: 64.3,
    humidityVariation: 51.7,
    dataHoraTempMinima: "26/08/2024 06:26:32",
    dataHoraTemperaturaMaxima: "25/08/2024 13:11:11",
    dataHoraUmidadeMinima: "12/08/2024 16:38:09",
    dataHoraUmidadeMaxima: "26/08/2024 06:22:32",
  },
  {
    monthYear: "09/2024",
    tempMin: 22.7,
    tempMax: 33.2,
    tempVariation: -10.5,
    humidityMin: 14.0,
    humidityMax: 54.4,
    humidityVariation: 40.4,
    dataHoraTempMinima: "06/09/2024 06:51:35",
    dataHoraTemperaturaMaxima: "05/09/2024 14:40:32",
    dataHoraUmidadeMinima: "03/09/2024 17:22:31",
    dataHoraUmidadeMaxima: "25/09/2024 13:20:59",
  },
  {
    monthYear: "10/2024",
    tempMin: 20.6,
    tempMax: 33.3,
    tempVariation: -12.7,
    humidityMin: 18.0,
    humidityMax: 93.1,
    humidityVariation: 75.1,
    dataHoraTempMinima: "20/10/2024 05:28:48",
    dataHoraTemperaturaMaxima: "05/10/2024 16:19:56",
    dataHoraUmidadeMinima: "04/10/2024 13:37:24",
    dataHoraUmidadeMaxima: "19/10/2024 15:21:18",
  },
  {
    monthYear: "11/2024",
    tempMin: 20.8,
    tempMax: 32.4,
    tempVariation: 11.6,
    humidityMin: 43.6,
    humidityMax: 89.6,
    humidityVariation: -46.0,
    dataHoraTempMinima: "17/11/2024 10:33:33",
    dataHoraTemperaturaMaxima: "20/11/2024 16:15:10",
    dataHoraUmidadeMinima: "20/11/2024 15:49:10",
    dataHoraUmidadeMaxima: "17/11/2024 10:33:33",
  },
  {
    monthYear: "12/2024",
    tempMin: 20.6,
    tempMax: 32.8,
    tempVariation: -12.2,
    humidityMin: 34.0,
    humidityMax: 87.2,
    humidityVariation: 53.2,
    dataHoraTempMinima: "29/12/2024 02:12:00",
    dataHoraTemperaturaMaxima: "13/12/2024 16:01:48",
    dataHoraUmidadeMinima: "06/12/2024 15:34:51",
    dataHoraUmidadeMaxima: "31/12/2024 23:59:54",
  },
  {
    monthYear: "01/2025",
    tempMin: 19.5,
    tempMax: 31.7,
    tempVariation: -12.2,
    humidityMin: 42.9,
    humidityMax: 99.9,
    humidityVariation: 57.0,
    dataHoraTempMinima: "17/01/2025 06:26:23",
    dataHoraTemperaturaMaxima: "04/01/2025 13:19:46",
    dataHoraUmidadeMinima: "03/01/2025 16:28:48",
    dataHoraUmidadeMaxima: "18/01/2025 06:56:21",
  },
];

const FullGraphChart: React.FC = () => {
  const options: ApexCharts.ApexOptions = {
    chart: {
      type: "bar",
      toolbar: { show: false },
    },
    xaxis: {
      categories: mockData.map((data) => data.monthYear),
      title: { text: "" },
    },
    yaxis: {
      title: { text: "Valores", 
        style: {
            fontWeight: 'normal', // Remove o negrito
            fontSize:'16px',
            cssClass: 'font-smooth' // Aplica a suavização
          },
      },
      
      min: -80, // Ajusta o valor mínimo para que todos os valores negativos fiquem abaixo da linha do zero
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
        const data = mockData[dataPointIndex];
        const tempMinTime = data.dataHoraTempMinima;
        const tempMaxTime = data.dataHoraTemperaturaMaxima;
        const humidityMinTime = data.dataHoraUmidadeMinima;
        const humidityMaxTime = data.dataHoraUmidadeMaxima;

        return `
          <div class="tooltip-custom">
            <strong>${data.monthYear}</strong>
            <br/><strong>Temperatura Mínima:</strong> ${data.tempMin}º (Hora: ${tempMinTime})
            <br/><strong>Temperatura Máxima:</strong> ${data.tempMax}º (Hora: ${tempMaxTime})
            <br/><strong>Variação de Temperatura:</strong> ${data.tempVariation}º
            <br/><strong>Umidade Mínima:</strong> ${data.humidityMin}% (Hora: ${humidityMinTime})
            <br/><strong>Umidade Máxima:</strong> ${data.humidityMax}% (Hora: ${humidityMaxTime})
            <br/><strong>Variação de Umidade:</strong> ${data.humidityVariation}%
          </div>
        `;
      },
    },
    plotOptions: {
      bar: {
        horizontal: false,
        columnWidth: "70%", // Ajuste fino para barras mais proporcionais
        borderRadius: 4, // Suaviza as bordas
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
      data: mockData.map((data) => data.tempMin),
    },
    {
      name: "Temperatura Máxima (ºC)",
      data: mockData.map((data) => data.tempMax),
    },
    {
      name: "Variação de Temperatura (ºC)",
      data: mockData.map((data) => data.tempVariation),
    },
    {
      name: "Umidade Mínima (%)",
      data: mockData.map((data) => data.humidityMin),
    },
    {
      name: "Umidade Máxima (%)",
      data: mockData.map((data) => data.humidityMax),
    },
    {
      name: "Variação de Umidade (%)",
      data: mockData.map((data) => data.humidityVariation),
    },
  ];

  return (<>
    <div className='graph-item'>
      <h3>Máximas por Mês/Ano</h3>
    </div>  
    <div style={{ maxWidth: "900px", margin: "0 auto" }}>
      <Chart options={options} series={series} type="bar" height={340} />
    </div>
    </>
  );
};

export default FullGraphChart;
