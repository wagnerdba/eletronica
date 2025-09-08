-- Criar o database
CREATE DATABASE sensordb WITH OWNER postgres;

-- Criar a extensão pgcrypto se ainda não estiver instalada
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- FUNCTION: public.uuid_generate_v4()

-- DROP FUNCTION IF EXISTS public.uuid_generate_v4();

CREATE OR REPLACE FUNCTION public.uuid_generate_v4(
	)
    RETURNS uuid
    LANGUAGE 'c'
    COST 1
    VOLATILE STRICT PARALLEL SAFE 
AS '$libdir/uuid-ossp', 'uuid_generate_v4'
;

ALTER FUNCTION public.uuid_generate_v4()
    OWNER TO postgres;


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


-- 1. Tabela de Auditoria
create table audit_log (
    id serial primary key,
    table_name text,
    operation text,
    data jsonb,
    timestamp timestamp default now()
);

-- 2. Trigger de captura
create or replace function audit_trigger() returns trigger as $$
begin
    if tg_op = 'DELETE' then
        insert into audit_log (table_name, operation, data)
        values (tg_table_name, tg_op, row_to_json(old));
    -- else
     --   insert into audit_log (table_name, operation, data)
     --   values (tg_table_name, tg_op, row_to_json(new));
    end if;
    return null; -- Para AFTER triggers, retornar NULL é o esperado.
end;
$$ language plpgsql;

-- 3. Associar trigger a uma Tabela
create or replace trigger sensor_data_audit_trigger
 after /* insert or update or*/ delete on sensor_data
   for each row execute function audit_trigger();
