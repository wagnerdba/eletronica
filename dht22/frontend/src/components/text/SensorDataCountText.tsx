import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface SensorDataCount {
  count: number;
}

const SensorDataCountText: React.FC = () => {
  const [count, setCount] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const apiUrl = process.env.REACT_APP_API_COUNT_URL;

  useEffect(() => {

    if (!apiUrl) {
      console.error("A URL da API de temperatura não está definida nas variáveis de ambiente.");
      return;
    }

    const fetchCount = async () => {
      try {
        const response = await axios.get<SensorDataCount>(apiUrl);
        setCount(response.data.count);
        setError(null); // Reset error on successful fetch
      } catch (err) {
        setError('Erro ao carregar o valor do count.');
      } finally {
        setLoading(false);
      }
    };

    // Faz a primeira consulta imediatamente
    fetchCount();

    // Define o intervalo para atualizar o count a cada 30 segundos (30000 milissegundos)
    const interval = setInterval(fetchCount, 30000);

    // Limpa o intervalo quando o componente for desmontado
    return () => clearInterval(interval);
  }, [apiUrl]);

  if (loading) {
    return <div>Carregando...</div>;
  }

  if (error) {
    return <div>{error}</div>;
  }

  return (
      <div>{count}</div>
  );
};

export default SensorDataCountText;
