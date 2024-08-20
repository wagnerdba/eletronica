import React, { useEffect, useState } from "react";
import axios from "axios";

interface TemperatureData {
  temperatura_celsius: number;
  temperatura_fahrenheit: number;
  umidade: number;
  data_hora: string;
}

const TemperaturePanel: React.FC = () => {
  const [temperatureData, setTemperatureData] = useState<TemperatureData | null>(null);

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

    fetchTemperatureData();
    const intervalId = setInterval(fetchTemperatureData, 60000);

    return () => clearInterval(intervalId);
  }, []);

  return (
    <div className="temperature-panel">
      {temperatureData ? (
        <>
          <h3>Dados Atuais</h3>
          <p><strong>Temperatura Atual:</strong> {temperatureData.temperatura_celsius} ºC / {temperatureData.temperatura_fahrenheit.toFixed(2)} ºF</p>
          <p><strong>Umidade Atual:</strong> {temperatureData.umidade.toFixed(2)} %</p>
          <p><strong>Data e Hora da Leitura:</strong> {temperatureData.data_hora}</p>
        </>
      ) : (
        <p>Carregando dados...</p>
      )}
    </div>
  );
};

export default TemperaturePanel;
