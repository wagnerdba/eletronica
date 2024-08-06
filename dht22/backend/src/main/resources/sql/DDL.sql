-- Criar a extensão pgcrypto se ainda não estiver instalada
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Remover a tabela se existir
DROP TABLE IF EXISTS sensor_data;

-- Criar a tabela sensor_data
CREATE TABLE sensor_data (
    id SERIAL PRIMARY KEY,
    temperatura_celsius FLOAT NOT NULL,
    temperatura_fahrenheit FLOAT NOT NULL,
    umidade FLOAT NOT NULL,
    data_hora TIMESTAMP NOT NULL,
    uuid UUID DEFAULT uuid_generate_v4() UNIQUE NOT NULL
);