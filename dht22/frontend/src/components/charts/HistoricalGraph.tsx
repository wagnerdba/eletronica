import React, { useEffect, useState } from 'react';
import axios from 'axios';
import Chart from 'react-apexcharts';
import { ApexOptions } from 'apexcharts';
import '../../assets/css/styles.css';

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
  if (isNaN(num)) return '0.00'; // Adiciona verificação para valores inválidos
  const [integer, decimal = ''] = num.toString().split('.');
  return `${integer}.${decimal.padEnd(2, '0').substring(0, 2)}`;
};

const HistoricalGraph: React.FC = () => {
  const [data, setData] = useState<DataItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = () => {
      axios
        .get('http://192.168.1.9:8081/api/dht22/statistics')
        .then((response) => {
          // console.log('Dados recebidos:', response.data);
          setData(response.data);
          setLoading(false);
        })
        .catch((error) => {
          setError('Erro ao carregar os dados...');
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
    (item) => item.datahoraTemperaturaMinima.split(' ')[0]
  );
  const tempMin = data.map((item) =>
    formatNumber(parseFloat(item.temperaturaMinima.replace('º', '').trim()))
  );
  const tempMax = data.map((item) =>
    formatNumber(parseFloat(item.temperaturaMaxima.replace('º', '').trim()))
  );
  const umidadeMin = data.map((item) =>
    formatNumber(parseFloat(item.umidadeMinima.replace('%', '').trim()))
  );
  const umidadeMax = data.map((item) =>
    formatNumber(parseFloat(item.umidadeMaxima.replace('%', '').trim()))
  );

  const options: ApexOptions = {
    chart: {
      type: 'line' as const,
      toolbar: {
        show: false, // Remove a barra de ferramentas
      },
      background: 'transparent' //'#ADD8E6' // Azul bebê
    },
    
    xaxis: {
      categories: dates,
      tickPlacement: 'on',
      labels: {
        format: 'dd/MM/yyyy',
      },
    },
    yaxis: [
      {
        title: {
          text: 'Temperatura (ºC)',
          style: {
            fontWeight: 'normal', // Remove o negrito
            fontSize:'16px',
            cssClass: 'font-smooth' // Aplica a suavização
          },
        },
        seriesName: 'Temperatura',
      },
      {
        opposite: true,
        title: {
          text: 'Umidade (%)',
          style: {
            fontWeight: 'normal', // Remove o negrito
            fontSize:'16px',
            cssClass: 'font-smooth' // Aplica a suavização
          },
        },
      },
    ],
    stroke: {
      curve: 'smooth',
      width: 5.0,
    },
    colors: ['#FF0000', '#0d00ff', '#f0c905', '#23eb58'],
    series: [
      {
        name: 'Temperatura Máxima (ºC)',
        type: 'line',
        data: tempMax.map((value) => parseFloat(value)),
      },
      {
        name: 'Temperatura Mínima (ºC)',
        type: 'line',
        data: tempMin.map((value) => parseFloat(value)),
      },
      {
        name: 'Umidade Mínima (%)',
        type: 'bar',
        data: umidadeMin.map((value) => parseFloat(value)),
      },
      {
        name: 'Umidade Máxima (%)',
        type: 'bar',
        data: umidadeMax.map((value) => parseFloat(value)),
      },
    ],
    plotOptions: {
      bar: {
        columnWidth: '75%',
        borderRadius: 0,
      },
    },
    tooltip: {
      custom: function ({ seriesIndex, dataPointIndex, w }) {
        const item = data[dataPointIndex];
        const tempMinTime = item.datahoraTemperaturaMinima.split(' ')[1];
        const tempMaxTime = item.datahoraTemperaturaMaxima.split(' ')[1];
        const umidMinTime = item.datahoraUmidadeMinima.split(' ')[1];
        const umidMaxTime = item.datahoraUmidadeMaxima.split(' ')[1];

        const tempMin = formatNumber(
          parseFloat(item.temperaturaMinima.replace('º', '').trim())
        );
        const tempMax = formatNumber(
          parseFloat(item.temperaturaMaxima.replace('º', '').trim())
        );
        const umidMin = formatNumber(
          parseFloat(item.umidadeMinima.replace('%', '').trim())
        );
        const umidMax = formatNumber(
          parseFloat(item.umidadeMaxima.replace('%', '').trim())
        );
        const varTemp = formatNumber(
          parseFloat(item.variacaoTemperatura.replace('%', '').trim())
        );
        const varUmid = formatNumber(
          parseFloat(item.variacaoUmidade.replace('%', '').trim())
        );

        return `
          <div class='tooltip-custom'>
            <b>Mínimas e Máximas</b><br/>
            ${dates[dataPointIndex]}<br/>
            <b>Temperatura</b><br/>
            <b>Manhã:</b> ${tempMin}ºC às ${tempMinTime}<br/>
            <b>Tarde:</b> ${tempMax}ºC às ${tempMaxTime}<br/>            
            <b>Variação:</b> ${varTemp}ºC<br/>
            <b>Umidade</b><br/>
            <b>Manhã:</b> ${umidMax}% às ${umidMaxTime}<br/>
            <b>Tarde:</b> ${umidMin}% às ${umidMinTime}<br/>
            <b>Variação:</b> ${varUmid}%<br/>
          </div>
        `;
      },
    },
  };

  if (loading) return <p>Carregando...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div className='graph-item'>
      <h3>Mínimas e Máximas por dia</h3>
      <Chart
        options={options}
        series={options.series}
        type='line'
        height='150%'
      />
    </div>
  );
};

export default HistoricalGraph;
