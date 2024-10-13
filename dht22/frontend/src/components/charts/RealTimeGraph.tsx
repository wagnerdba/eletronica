import React, { useEffect, useState } from "react";
import axios from "axios";
import Chart from "react-apexcharts";
import { ApexOptions } from "apexcharts";
import "../../assets/css/styles.css";

interface TemperatureData {
  temperatura_celsius: number;
  temperatura_fahrenheit: number;
  umidade: number;
  data_hora: string;
}

const formatNumber = (value: number | string) => {
  const [integer, decimal = ""] = value.toString().split(".");
  return `${integer}.${decimal.padEnd(2, "0").substring(0, 2)}`;
};

const adjustTimeZone = (dateTime: string): number => {
  const date = new Date(dateTime);
  const localOffset = date.getTimezoneOffset() * 60000; // Offset em milissegundos
  return date.getTime() - localOffset; // Ajusta para o horário local
};

const formatDateTime = (timestamp: number) => {
  const date = new Date(timestamp);
  return date.toLocaleString(); // Formata data e hora no horário local
};

const RealTimeGraph: React.FC = () => {
  const [temperatureData, setTemperatureData] = useState<TemperatureData[]>([]);
  const [chartSeries, setChartSeries] = useState([
    {
      name: "Temperatura (ºC)",
      data: [] as { x: number; y: number }[],
    },
    {
      name: "Umidade (%)",
      data: [] as { x: number; y: number }[],
    },
  ]);
  const [temperatureColor, setTemperatureColor] = useState<string>("#FF5733"); // Cor inicial
  const [umidadeColor, setUmidadeColor] = useState<string>("#FFCA28"); // Cor inicial

  useEffect(() => {
    const fetchTemperatureData = () => {
      axios
        .get("http://192.168.1.100/esp32/api/temperatura")
        .then((response) => {
          const newData = response.data;
          setTemperatureData((prevData) => {
            const updatedData = [...prevData, newData];
            if (updatedData.length > 20) updatedData.shift(); // Limitar o número de pontos no gráfico
            return updatedData;
          });
        })
        .catch((error) => {
          console.error("Erro ao carregar os dados de temperatura:", error);
        });
    };

    fetchTemperatureData();

    const intervalIdTemp = setInterval(() => {
      fetchTemperatureData();
    }, 1000);

    return () => {
      clearInterval(intervalIdTemp);
    };
  }, []);

  useEffect(() => {
    if (temperatureData.length > 0) {
      const lastTemperature =
        temperatureData[temperatureData.length - 1].temperatura_celsius;

      const lastUmidade = temperatureData[temperatureData.length - 1].umidade;

      // Define a cor com base na temperatura
      if (lastTemperature <= 25) {
        setTemperatureColor("#0000FF"); // Azul para temperatura > 23
      } else {
        setTemperatureColor("#FF5733"); // Cor padrão
      }

      // Define a cor com base na umidade
      if (lastUmidade > 30) {
        setUmidadeColor("#369c03"); // Verde para umidade > 30
      } else {
        setUmidadeColor("#FFCA28"); // Cor padrão
      }
    }

    const formattedSeries = [
      {
        name: "Temperatura (ºC)",
        data: temperatureData.map((data) => ({
          x: adjustTimeZone(data.data_hora),
          y: data.temperatura_celsius,
        })),
      },
      {
        name: "Umidade (%)",
        data: temperatureData.map((data) => ({
          x: adjustTimeZone(data.data_hora),
          y: data.umidade,
        })),
      },
    ];

    setChartSeries(formattedSeries);
  }, [temperatureData]);

  const formatTooltipValue = (value: number): string => {
    return formatNumber(value);
  };

  const formatTooltipDate = (timestamp: number): string => {
    return formatDateTime(timestamp);
  };

  const chartOptions: ApexOptions = {
    chart: {
      type: "area",
      toolbar: {
        show: false, // Remove a barra de ferramentas
      },
      background: "transparent", // '#ADD8E6', // Azul bebê
      animations: {
        enabled: true,
        easing: "linear",
        dynamicAnimation: {
          speed: 2000,
        },
      },
      id: "realtime", // Identificador para controlar atualizações
    },

    dataLabels: {
      enabled: false,
    },

    xaxis: {
      type: "datetime",
    },
    yaxis: [
      {
        title: {
          text: "Temperatura (ºC)",
          style: {
            fontWeight: "normal", // Remove o negrito
            fontSize: "16px",
            cssClass: "font-smooth", // Aplica a suavização
          },
        },
        labels: {
          formatter: (val: number) => formatNumber(val), // Formata os valores do eixo Y para temperatura
        },
      },
      {
        opposite: true,
        title: {
          text: "Umidade (%)",
          style: {
            fontWeight: "normal", // Remove o negrito
            fontSize: "16px",
            cssClass: "font-smooth", // Aplica a suavização
          },
        },
        labels: {
          formatter: (val: number) => formatNumber(val), // Formata os valores do eixo Y para umidade
        },
      },
    ],
    stroke: {
      curve: "smooth",
      width: 4.0, // Define a largura das linhas
    },
    markers: {
      size: 0,
    },
    colors: [temperatureColor, umidadeColor], // Usa a cor definida dinamicamente para temperatura

    tooltip: {
      enabled: false,
      x: {
        formatter: (value: number) => formatTooltipDate(value), // Formata a data na tooltip
      },
      y: [
        {
          formatter: (val: number) => formatTooltipValue(val), // Formata a temperatura com 2 casas decimais na tooltip
        },
        {
          formatter: (val: number) => formatTooltipValue(val), // Formata a umidade com 2 casas decimais na tooltip
        },
      ],
    },
  };

  if (temperatureData.length === 0)
    return <p>Carregando dados em tempo real...</p>;

  return (
    <div className="graph-item">
      {" "}
      {/* Use a classe do gráfico para garantir a altura correta */}
      <h3>Análise em Tempo Real</h3>
      <Chart
        options={chartOptions}
        series={chartSeries}
        type="area"
        height="150%"
      />
    </div>
  );
};

export default RealTimeGraph;
