import React, { useEffect, useState } from 'react';
import axios from 'axios';

interface SensorDataCount {
  count: number;
}

const SensorDataCountText: React.FC = () => {
  const [count, setCount] = useState<number | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchCount = async () => {
      try {
        const response = await axios.get<SensorDataCount>('http://192.168.1.14:8081/api/dht22/count');
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

    // Define o intervalo para atualizar o count a cada 1 minuto (60000 milissegundos)
    const interval = setInterval(fetchCount, 60000);

    // Limpa o intervalo quando o componente for desmontado
    return () => clearInterval(interval);
  }, []);

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
