import React, { useEffect, useState } from 'react';
import ApexCharts from 'react-apexcharts';
import { ApexOptions } from 'apexcharts';
import { format } from 'date-fns'; // Importa a função format da biblioteca date-fns

interface DataPoint {
  id: number;
  temperatura_celsius: number;
  temperatura_fahrenheit: number;
  umidade: number;
  data_hora: string;
  uuid: string;
}

/*
const formatNumber = (value: number | string) => {
  const [integer, decimal = ""] = value.toString().split(".");
  return `${integer}.${decimal.padEnd(2, "0").substring(0, 2)}`;
};
*/

const TemperatureHumidityChart: React.FC = () => {
  const [data, setData] = useState<DataPoint[]>([]);
  const apiUrl = process.env.REACT_APP_API_TODAY_URL; // Usa a variável de ambiente

  useEffect(() => {
    const fetchData = async () => {
      if (!apiUrl) {
        console.error('A URL da API de temperatura não está definida nas variáveis de ambiente.');
        return;
      }
    
      try {
        const response = await fetch(apiUrl);
        if (!response.ok) {
          throw new Error('Network response problem');
        }
        const result = await response.json();
          
        // Formata a data e hora
        const formattedData = result.map((item: DataPoint) => ({
          ...item,
          data_hora: format(new Date(item.data_hora), 'dd/MM/yyyy HH:mm:ss')
        }));

        setData(formattedData);
      } catch (error) {
        console.error('Error fetching data:', error);
      }
    };

    fetchData();

    // as duas próximas linhas setam a atualização do gráfico a cada 60 segundos
    const intervalId = setInterval(fetchData, 60000); // Fetch data every minute
    return () => clearInterval(intervalId);           // Clear interval on component unmount

  }, [apiUrl]);

  const dates = data.map(item => item.data_hora);
  const temperatures = data.map(item => item.temperatura_celsius);
  const humidities = data.map(item => item.umidade);

  const options: ApexOptions = {
    chart: {
      id: 'temperature-humidity-chart',
      type: 'line',
      height: 230,
      dropShadow: {
        enabled: true,
        enabledOnSeries: [0, 1],
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
          text: 'Temperatura (°C)',  // Mantém o título do eixo y (Temperatura)
          style: {
            fontWeight: 'normal', // Remove o negrito
            fontSize: '16px',
            cssClass: 'font-smooth' // Aplica a suavização
          },
        }
      },
      {
        opposite: true,
        title: {
          text: 'Umidade (%)',  // Mantém o título do eixo y (Umidade)
          style: {
            fontWeight: 'normal', // Remove o negrito
            fontSize: '16px',
            cssClass: 'font-smooth' // Aplica a suavização
          },
        }
      }
    ],
    stroke: {
      curve: 'smooth',
      width: 2.5  // Define a espessura das linhas
    },
    colors: ['#2350d9', '#d9ff00'], // Define cores para as séries (Temperatura e Umidade)
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
      <h3>Análise Diária Minuto a Minuto</h3>
      <ApexCharts options={options} series={series} type="line" height="160%" />
    </div>
  );
};

export default TemperatureHumidityChart;
