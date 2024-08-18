import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Chart from 'react-apexcharts';
import { ApexOptions } from 'apexcharts'; // Importar ApexOptions para tipagem
import './App.css'; // Importe o arquivo CSS

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
      axios.get('http://192.168.1.9:8081/api/dht22/statistics')
        .then(response => {
          setData(response.data);
          setLoading(false);
        })
        .catch(error => {
          setError('Erro ao carregar os dados');
          setLoading(false);
        });
    };

    // Buscar dados iniciais
    fetchData();

    // Configure o intervalo para buscar dados a cada 60 segundos
    const intervalId = setInterval(fetchData, 60000);

    // Limpe o intervalo na desmontagem do componente
    return () => clearInterval(intervalId);
  }, []);

  const dates = data.map(item => item.datahoraTemperaturaMinima.split(' ')[0]);
  const tempMin = data.map(item => parseFloat(item.temperaturaMinima.replace('º', '').trim()));
  const tempMax = data.map(item => parseFloat(item.temperaturaMaxima.replace('º', '').trim()));
  const umidadeMin = data.map(item => parseFloat(item.umidadeMinima.replace('%', '').trim()));
  const umidadeMax = data.map(item => parseFloat(item.umidadeMaxima.replace('%', '').trim()));

  const options: ApexOptions = {
    chart: {
      type: 'line' as const, // Especificar o tipo literal corretamente
    },
    xaxis: {
      categories: dates,
      tickPlacement: 'on',
      labels: {
        format: 'dd/MM/yyyy'
      }
    },
    yaxis: [
      {
        title: {
          text: 'Temperatura (ºC)'
        },
        seriesName: 'Temperatura'
      },
      {
        opposite: true,
        title: {
          text: 'Umidade (%)'
        }
      }
    ],
    stroke: {
      curve: 'smooth'
    },
    colors: ['#FF0000', '#3073f0', '#e3da29', '#d781fc'],
    series: [
      {
        name: 'Temperatura Máxima (ºC)',
        type: 'line', // Especificar o tipo literal corretamente
        data: tempMax
      },
      {
        name: 'Temperatura Mínima (ºC)',
        type: 'line', // Especificar o tipo literal corretamente
        data: tempMin
      },
      {
        name: 'Umidade Mínima (%)',
        type: 'bar', // Especificar o tipo literal corretamente
        data: umidadeMin
      },
      {
        name: 'Umidade Máxima (%)',
        type: 'bar', // Especificar o tipo literal corretamente
        data: umidadeMax
      }
    ],
    plotOptions: {
      bar: {
        columnWidth: '50%'
      }
    },
    tooltip: {
      custom: function({ series, seriesIndex, dataPointIndex, w }) {
        const tempMinTime = data[dataPointIndex].datahoraTemperaturaMinima.split(' ')[1];
        const tempMaxTime = data[dataPointIndex].datahoraTemperaturaMaxima.split(' ')[1];
        const umidMinTime = data[dataPointIndex].datahoraUmidadeMinima.split(' ')[1];
        const umidMaxTime = data[dataPointIndex].datahoraUmidadeMaxima.split(' ')[1];
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
      }
    }
  };

  if (loading) return <p>Loading...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className="graph-container">
      <h2>Gráfico de Temperatura e Umidade</h2>
      <Chart options={options} series={options.series} type="line" height={400} />
    </div>
  );
};

export default Graph;
