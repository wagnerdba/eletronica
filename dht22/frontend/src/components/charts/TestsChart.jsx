
import React from 'react';
import ApexCharts from 'react-apexcharts';
import { ApexOptions } from 'apexcharts';

interface DataPoint {
  id: number;
  temperatura_celsius: number;
  temperatura_fahrenheit: number;
  umidade: number;
  data_hora: string;
  uuid: string;
}

const mockData: DataPoint[] = [
  {
    "id": 40980,
    "temperatura_celsius": 20.3,
    "temperatura_fahrenheit": 68.54,
    "umidade": 40.4,
    "data_hora": "2024-08-30 00:00:48",
    "uuid": "a1ec8c0e-c29d-4e75-b343-c0c0e1fc1a6c"
  },
  {
    "id": 40985,
    "temperatura_celsius": 20.3,
    "temperatura_fahrenheit": 68.54,
    "umidade": 40.5,
    "data_hora": "2024-08-30 00:05:48",
    "uuid": "790431e8-64fb-4a7f-8aa0-08b5788e46da"
  },
  {
    "id": 40990,
    "temperatura_celsius": 20.2,
    "temperatura_fahrenheit": 68.36,
    "umidade": 40.6,
    "data_hora": "2024-08-30 00:10:48",
    "uuid": "3d816c81-1908-441d-8c47-b8c3f7a41f35"
  },
  {
    "id": 40995,
    "temperatura_celsius": 20.1,
    "temperatura_fahrenheit": 68.18,
    "umidade": 40.5,
    "data_hora": "2024-08-30 00:15:48",
    "uuid": "0977633c-95d4-40a8-939d-96f755ac8cfa"
  },
  {
    "id": 41000,
    "temperatura_celsius": 20.2,
    "temperatura_fahrenheit": 68.36,
    "umidade": 40.7,
    "data_hora": "2024-08-30 00:20:48",
    "uuid": "64bf4d14-8820-4139-be53-86a8a3a0a3e7"
  },
  {
    "id": 41005,
    "temperatura_celsius": 20.1,
    "temperatura_fahrenheit": 68.18,
    "umidade": 40.7,
    "data_hora": "2024-08-30 00:25:48",
    "uuid": "6c7a9ac2-829c-4fbd-b35b-a6aaf3d7f264"
  },
  {
    "id": 41010,
    "temperatura_celsius": 20.1,
    "temperatura_fahrenheit": 68.18,
    "umidade": 40.7,
    "data_hora": "2024-08-30 00:30:48",
    "uuid": "76b6d7e4-9bc0-426d-8a48-1758e42dfc0a"
  }
];

const TemperatureHumidityChart: React.FC = () => {
  const dates = mockData.map(data => data.data_hora);
  const temperatures = mockData.map(data => data.temperatura_celsius);
  const humidities = mockData.map(data => data.umidade);

  const options: ApexOptions = {
    chart: {
      id: 'temperature-humidity-chart',
      type: 'line'
    },
    xaxis: {
      categories: dates,
      title: {
        text: 'Data e Hora'
      }
    },
    yaxis: [
      {
        title: {
          text: 'Temperatura (°C)'
        }
      },
      {
        opposite: true,
        title: {
          text: 'Umidade (%)'
        }
      }
    ],
    tooltip: {
      shared: true,
      intersect: false
    }
  };

  const series = [
    {
      name: 'Temperatura (°C)',
      data: temperatures
    },
    {
      name: 'Umidade (%)',
      data: humidities
    }
  ];

  return (
    <div>
      <h2>Gráfico de Temperatura e Umidade</h2>
      <ApexCharts options={options} series={series} type="line" height={400} />
    </div>
  );
};

export default TemperatureHumidityChart;
