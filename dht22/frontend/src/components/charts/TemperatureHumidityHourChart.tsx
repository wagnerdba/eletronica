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

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await axios.get<SensorData[]>('http://192.168.1.14:8081/api/dht22/hour');
        setData(response.data);
      } catch (error) {
        console.error('Error fetching data:', error);
      }
    };

    fetchData();

    // Set up an interval to fetch data every hour
    const intervalId = setInterval(fetchData, 60000); // 3600000); // 3600000 ms = 1 hour

    return () => clearInterval(intervalId); // Clear interval on component unmount
  }, []);

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
            offset: 40,
            color: '#F9440A',
            opacity: 1
          },
          {
            offset: 50,
            color: '#FFE663',
            opacity: 1
          },
          {
            offset: 60,
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
            fontSize: '12px',
            color: '#000', // Cor da fonte preta
            background: 'rgba(0, 0, 0, 0)', // Cor de fundo transparente
            borderRadius: 2,
            padding: {
              left: 5,
              right: 5,
              top: 3,
              bottom: 3,
            },
          },
          position: 'top', // Posiciona a anotação abaixo do eixo X
          offsetY: -42, // Ajusta verticalmente a posição abaixo do eixo X
        },
      })),
    },
  };

  return (
    <div>
      <h3>Máximas por Hora</h3>
      <Chart options={options} series={series} type="line" height={320} />
    </div>
  );
};

export default TemperatureHumidityHourChart;
