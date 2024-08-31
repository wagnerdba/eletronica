import React, { useEffect, useState } from 'react';
import Chart from 'react-apexcharts';
import { ApexOptions } from 'apexcharts';
import axios from 'axios';
import { format } from 'date-fns'; // Importa a função de formatação

interface DataPoint {
  id: number;
  temperatura_celsius: number;
  temperatura_fahrenheit: number;
  umidade: number;
  data_hora: string;
  uuid: string;
}

const formatNumber = (value: number | string) => {
  const num = parseFloat(value.toString());
  if (isNaN(num)) return '0.00'; // Adiciona verificação para valores inválidos
  const [integer, decimal = ''] = num.toString().split('.');
  return `${integer}.${decimal.padEnd(2, '0').substring(0, 2)}`;
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  if (isNaN(date.getTime())) {
    return ''; // Retorna uma string vazia se a data for inválida
  }
  return format(date, 'dd/MM/yyyy HH:mm:ss'); // Formata a data no formato desejado
};

const TemperatureHumidityChart: React.FC = () => {
  const [data, setData] = useState<DataPoint[]>([]);
  
  const fetchData = async () => {
    try {
      const response = await axios.get<DataPoint[]>('http://192.168.1.14:8081/api/dht22/today');
      setData(response.data);
    } catch (error) {
      console.error('Erro ao buscar dados:', error);
    }
  };

  useEffect(() => {
    fetchData(); // Fetch data on component mount

    const interval = setInterval(() => {
      fetchData(); // Fetch data every minute
    }, 60000);

    // Cleanup interval on component unmount
    return () => clearInterval(interval);
  }, []);

  const dates = data.map(dataPoint => dataPoint.data_hora);
  const temperatures = data.map(dataPoint => dataPoint.temperatura_celsius);
  const humidities = data.map(dataPoint => dataPoint.umidade);

  const options: ApexOptions = {
    chart: {
      id: 'temperature-humidity-chart',
      type: 'line',
      height: 230,
      dropShadow: {
          enabled: true,
          enabledOnSeries: [0,1],
      },
      toolbar: {
        autoSelected: 'pan',
        show: false
      }
    },
    xaxis: {
      categories: dates,
      labels: {
        show: false // Hide x-axis labels
      },
      axisBorder: {
        show: false // Hide x-axis border
      },
      axisTicks: {
        show: false // Hide x-axis ticks
      },
      title: {
        text: '', // Ensure no title is shown
        style: {
          color: 'transparent' // Ensure the title is transparent
        }
      }
    },
    yaxis: [
      {
        title: {
          text: 'Temperatura (°C)'
        },
        labels: {
          formatter: (value: number) => formatNumber(value),
        }
      },
      {
        opposite: true,
        title: {
          text: 'Umidade (%)'
        },
        labels: {
          formatter: (value: number) => formatNumber(value),
        }
      }
    ],
    stroke: {
      curve: 'smooth', // Apply smooth curves to lines
      width: 2.5,
    },
    colors: ['#2350d9', '#FF9900'], // Define cores para as séries (Temperatura e Umidade)
    tooltip: {
      shared: true,
      intersect: false,
      y: {
        formatter: (value: number) => formatNumber(value),
      },
      x: {
        formatter: (value: number) => {
          // Aqui, o valor pode ser um número, então garantimos a conversão correta
          const index = Math.round(value);
          const date = dates[index] || '';
          return formatDate(date); // Formata a data no formato desejado
        }
      }
    }
  };

  const series = [
    {
      name: 'Temperatura (°C)',
      data: temperatures // Aqui, os dados devem ser números, não strings
    },
    {
      name: 'Umidade (%)',
      data: humidities // Aqui, os dados devem ser números, não strings
    }
  ];

  return (
    <div>
      <h3>Análise Diária Minuto a Minuto</h3>
      
      {/* <ApexCharts options={options} series={series} type="line" height={350} /> */}

      <Chart
        options={options}
        series={series}
        type="line"
        height="150%"
      />


    </div>
  );
};

export default TemperatureHumidityChart;
