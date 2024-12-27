import React, { useEffect, useState } from "react";
import axios from "axios";
import SensorDataCountText from "./SensorDataCountText";

// Interface para os dados de temperatura
interface TemperatureData {
  temperatura_celsius: number;
  temperatura_fahrenheit: number;
  umidade: number;
  data_hora: string;
}

/*
const formatNumber = (value: number | string) => {
  const [integer, decimal = ""] = value.toString().split(".");
  return `${integer}.${decimal.padEnd(2, "0").substring(0, 2)}`;
};
*/

/*
const formatNumber = (value: number | string) => {
  return Math.floor(Number(value)).toString();
};
*/

const formatNumber = (value: number | string) => {
  return value.toString().split(".")[0];
};

const RealTimeText: React.FC = () => {
  const [temperatureData, setTemperatureData] =
    useState<TemperatureData | null>(null);
  const [dateTimeNow, setDateTimeNow] = useState<string>("");
  const [uuid, setUuid] = useState<string>("Carregando UUID...");
  const [data_hora, setDataHora] = useState<string>("Carregando Data e Hora Último Registro");

  const apiUrl = process.env.REACT_APP_API_TEMPERATURE_URL;
  const apiUrlNow = process.env.REACT_APP_API_NOW_URL;
  const apiUrlUuid = process.env.REACT_APP_API_UUID_URL;

  useEffect(() => {

    if (!apiUrl || !apiUrlNow || !apiUrlUuid) {
      console.error("A URL da API de temperatura não está definida nas variáveis de ambiente.");
      return;
    }

    const fetchTemperatureData = () => {
      axios
        .get(apiUrl) // Mantém o endpoint original para dados de temperatura
        .then((response) => {
          setTemperatureData(response.data);
        })
        .catch((error) => {
          console.error("Erro ao carregar os dados de temperatura:", error);
        });
    };

    const fetchDateTimeNow = () => {
      axios
        .get(apiUrlNow) // Novo endpoint para a data e hora
        .then((response) => {
          setDateTimeNow(response.data.data_hora);
        })
        .catch((error) => {
          console.error("Erro ao carregar a data e hora:", error);
        });
    };

    const fetchUuidData = () => {
      axios
        .get(apiUrlUuid) // Mantém o endpoint original para o UUID
        .then((response) => {
          setUuid(response.data.uuid);
          setDataHora(response.data.data_hora);
        })
        .catch((error) => {
          console.error("Erro ao carregar o UUID/Data Hora:", error);
        });
    };

    // Buscar dados de temperatura e UUID a cada 1 segundo
    fetchTemperatureData();
    fetchDateTimeNow();
    fetchUuidData();

    const intervalIdTemp = setInterval(() => {
      fetchTemperatureData();
    }, 10000); // Mantém a atualização de temperatura a cada 10 segundos

    const intervalIdDateTime = setInterval(() => {
      fetchDateTimeNow();
    }, 1000); // Atualiza a data e hora a cada 1 segundo

    const intervalIdUuid = setInterval(() => {
      fetchUuidData();
    }, 60000); // Mantém a atualização de UUID a cada 60 segundos

    return () => {
      clearInterval(intervalIdTemp);
      clearInterval(intervalIdDateTime);
      clearInterval(intervalIdUuid);
    };
  }, [apiUrl, apiUrlNow, apiUrlUuid]);

  const formatDateTime = (dateTime: string) => {
    const date = new Date(dateTime);
    const formattedDate = date.toLocaleDateString("pt-BR");
    const formattedTime = date.toLocaleTimeString("pt-BR");
    return `${formattedDate} ${formattedTime}`;
  };

  if (!temperatureData) return <p>Carregando dados em tempo real...</p>;

  return (
    <div className="temperature-panel">
      <div className="display-text">
        <div className="panel-container">
          <strong>Celsius</strong>
          <span
            className={
              temperatureData.temperatura_celsius > 25.99
                ? "temp-hot"
                : "temp-cold"
            }
          >
            {formatNumber(temperatureData.temperatura_celsius)} ºC
          </span>
        </div>
        <div className="panel-container">
          <strong>Fahrenheit</strong>
          <span>{formatNumber(temperatureData.temperatura_fahrenheit)} ºF</span>
        </div>
        <div className="panel-container">
          <strong>Umidade</strong>
          <span
            className={
              temperatureData.umidade < 30 ? "humidity-low" : "humidity-high"
            }
          >
             {formatNumber(temperatureData.umidade)} % 
          </span>
        </div>
        <div className="panel-container">
          <strong>Data e Hora</strong>
          <span>{formatDateTime(dateTimeNow)}</span> {/* Exibindo a data e hora do novo endpoint */}
        </div>

        <div className="panel-container">
          <strong>Registros</strong>
          <span><SensorDataCountText /></span>
          <div className="font-size-1">{uuid}</div>
          <div className="font-size-1">{formatDateTime(data_hora)}</div>
        </div>
      </div>
    </div>
  );
};

export default RealTimeText;
