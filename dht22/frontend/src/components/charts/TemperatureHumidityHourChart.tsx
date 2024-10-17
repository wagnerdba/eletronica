import React, { useEffect, useState } from 'react';
import Chart from 'react-apexcharts';
import { ApexOptions } from 'apexcharts';
import axios from 'axios';

interface SensorData {
  hora: string;
  temperaturaCelsius: string;
  umidade: string;
}

const TemperatureHumidityHourChart: React.FC = () => {
  const [data, setData] = useState<SensorData[]>([]);

  const apiUrl = process.env.REACT_APP_API_HOUR_URL; // Usa a variável de ambiente

  useEffect(() => {

  const fetchData = async () => {
    if (!apiUrl) {
      console.error('A URL da API de temperatura não está definida nas variáveis de ambiente.');
      return;
    }

    try {
        const response = await axios.get<SensorData[]>(apiUrl);
        setData(response.data);
      } catch (error) {
        console.error('Error fetching data:', error);
      }
    };

    fetchData();

    // Set up an interval to fetch data every hour
    const intervalId = setInterval(fetchData, 60000); // 3600000); // 3600000 ms = 1 hour

    return () => clearInterval(intervalId); // Clear interval on component unmount
  }, [apiUrl]);

  // Preparando os dados para o gráfico
  const series = [{
    name: 'Temperatura (°C)',
    data: data.map(item => parseFloat(item.temperaturaCelsius)),
  }];

  const options: ApexOptions = {
    chart: {
      type: 'line',
      height: 350,
      toolbar: {
        show: false, // Remove os botões de zoom e outras interações
      },
    },
    stroke: {
      curve: 'smooth',
      width: 0.5,
    },
    xaxis: {
      categories: data.map(item => item.hora), // Eixo X com as horas
      title: {
        text: '', // Remove o título "Hora" do eixo X
      
      },
    },
    yaxis: {
      // Oculta o título do eixo Y
      labels: {
        show: false, // Exibe os rótulos dos eixos Y
      },
    },
    // Oculta a legenda
    legend: {
      show: false,
    },
    fill: {
      type: 'gradient',
      gradient: {
        type: 'vertical',
        shadeIntensity: 1,
        opacityFrom: 1,
        opacityTo: 1,
        colorStops: [
          {
            offset: 50,
            color: '#F9440A',
            opacity: 1
          },
          {
            offset: 70,
            color: '#FFE663',
            opacity: 1
          },
          {
            offset: 80,
            color: '#2a6af5',
            opacity: 1
          }
        ]
      }
    },
    annotations: {
      xaxis: data.map((item) => ({
        x: item.hora,
        label: {
          text: `${item.umidade}%`, // Remove "Umidade:" e exibe apenas o valor
          style: {
            fontSize: '10px',
            color: '#000', // Cor da fonte preta
            background: 'rgba(0, 0, 0, 0)', // Cor de fundo transparente
            borderRadius: 1,
            padding: {
              left: 1,
              right: 2,
              top: 2,
              bottom: 2,
            },
          },
          position: 'bottom', // Posiciona a anotação abaixo do eixo X
          offsetY: 2, // Ajusta verticalmente a posição abaixo do eixo X
        },
      })),
    },
    tooltip: {
      enabled: true, // Desativa a tooltip
    },
    markers: {
      size: 3, // Remove os marcadores de pontos
    },
    dataLabels: {
      enabled: true,
      offsetX: 0,
      offsetY: -4,
      style: {
        fontSize: '12px',
        colors: ['#000000'], // Cor da fonte preta
      },
      background: {
        enabled: false, // Remove o fundo dos rótulos de dados
      },
    },
  };

  return (
    <div>
      <h3>Máximas por Hora</h3>
      <Chart options={options} series={series} type="area" height={320} />
    </div>
  );
};

export default TemperatureHumidityHourChart;
