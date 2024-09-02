import React, { useEffect, useState } from "react";
import axios from "axios";
import "../../assets/css/styles.css";

// Interface para os dados de temperatura
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

const RealTimeText: React.FC = () => {
  const [temperatureData, setTemperatureData] =
    useState<TemperatureData | null>(null);
  const [uuid, setUuid] = useState<string>("Carregando UUID...");

  useEffect(() => {
    const fetchTemperatureData = () => {
      axios
        .get("http://192.168.1.100/esp32/api/temperatura")
        .then((response) => {
          setTemperatureData(response.data);
        })
        .catch((error) => {
          console.error("Erro ao carregar os dados de temperatura:", error);
        });
    };

    const fetchUuidData = () => {
      axios
        .get("http://192.168.1.14:8081/api/dht22/last")
        .then((response) => {
          setUuid(response.data.uuid);
        })
        .catch((error) => {
          console.error("Erro ao carregar o UUID:", error);
        });
    };

    fetchTemperatureData();
    fetchUuidData();

    const intervalIdTemp = setInterval(() => {
      fetchTemperatureData();
    }, 1000);

    const intervalIdUuid = setInterval(() => {
      fetchUuidData();
    }, 60000);

    return () => {
      clearInterval(intervalIdTemp);
      clearInterval(intervalIdUuid);
    };
  }, []);

  const formatDateTime = (dateTime: string) => {
    const date = new Date(dateTime);
    const formattedDate = date.toLocaleDateString("pt-BR");
    const formattedTime = date.toLocaleTimeString("pt-BR");
    return `${formattedDate} ${formattedTime}`;
  };

  if (!temperatureData) return <p>Carregando dados em tempo real...</p>;

  return (
    <div className="temperature-panel">
      
      {/*}
      <p>
        <strong>Temperatura Atual:</strong>{" "}
        {formatNumber(temperatureData.temperatura_celsius)} ºC /{" "}
        {formatNumber(temperatureData.temperatura_fahrenheit)} ºF
      </p>
      <p>
        <strong>Umidade Atual:</strong>{" "}
        {formatNumber(temperatureData.umidade)} %
      </p>
      <p>
        <strong>Data e Hora da Leitura:</strong>{" "}
        {formatDateTime(temperatureData.data_hora)}
      </p>
      <p className="font-size-1">
        <strong>ID:</strong> {uuid}
      </p>
      */}

<p style={{ whiteSpace: "nowrap" }}>
  <strong>Temperatura Atual:</strong> {formatNumber(temperatureData.temperatura_celsius)} ºC / {formatNumber(temperatureData.temperatura_fahrenheit)} ºF{" "}
  <strong>Umidade Atual:</strong> {formatNumber(temperatureData.umidade)} %{" "}
  <strong>Data e Hora da Leitura:</strong> {formatDateTime(temperatureData.data_hora)} - <strong>ID:</strong> <span>{uuid}</span>
</p>


      {/*
      <p className="font-size-1">
        <strong>ID:</strong> {uuid}
      </p>
      */}
    </div>
  );
};

export default RealTimeText;
//57d64078-4543-45bd-b1a5-3e3cbf80122b
