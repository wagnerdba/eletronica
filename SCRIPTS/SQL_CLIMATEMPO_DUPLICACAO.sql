select * from sensor_data where data_hora::date >= '2025-12-15' order by id desc; --current_date order by id desc;
select * from sensor_data where uuid = 'b07cf602-3b6e-42f2-9af5-d376fe44b065'
select * from sensor_data order by id desc limit 15
select min(data_hora) from sensor_data

select * from sensor_data where id >= 782354 limit 4;
select * from sensor_data where fallback = true or id = 780670;
select * from sensor_data where fallback is true and data_hora::date = current_date order by id


-- delete from sensor_data

select now();
show timezone;
alter system set timezone = 'America/Sao_Paulo';
select pg_reload_conf();

select * from sensor_data order by id desc limit 10

-- delete from sensor_data where data_hora::date = current_date;

CREATE OR REPLACE FUNCTION trg_no_duplicate_day_hour_minute()
RETURNS trigger AS $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM sensor_data
        WHERE date_trunc('minute', data_hora) = date_trunc('minute', NEW.data_hora)
    ) THEN
        RAISE EXCEPTION 'Trigger ignorou registro duplicado.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-------------------------------------------------------------
CREATE OR REPLACE TRIGGER no_duplicate_day_hour_minute
BEFORE INSERT ON sensor_data
FOR EACH ROW
EXECUTE FUNCTION trg_no_duplicate_day_hour_minute();
-------------------------------------------------------------
DROP INDEX ux_sensor_data_day_hour_minute;
CREATE UNIQUE INDEX ux_sensor_data_day_hour_minute
ON sensor_data (date_trunc('minute', data_hora));


-- SQL para retornar registros que falharam no dia até a hora atual

with minutos_do_dia as (
    select generate_series(
        date_trunc('day', now()),    -- 00:00 de hoje
        date_trunc('minute', now()), -- minuto atual
        interval '1 minute'
    ) as minuto
),
existentes as (
    select date_trunc('minute', data_hora) as minuto
    from sensor_data
    where data_hora::date = now()::date
)
select m.minuto
from minutos_do_dia m
left join existentes e on e.minuto = m.minuto
where e.minuto is null -- minutos que faltam
order by m.minuto;

-------------------------------------------------------------
-- Dia anterior

WITH minutos_do_dia AS (
    SELECT generate_series(
        date_trunc('day', now() - interval '1 day'),                 -- 00:00 de ontem
        date_trunc('day', now()) - interval '1 minute',              -- 23:59 de ontem
        interval '1 minute'
    ) AS minuto
),
existentes AS (
    SELECT date_trunc('minute', data_hora) AS minuto
    FROM sensor_data
    WHERE data_hora::date = (now() - interval '1 day')::date
)
SELECT m.minuto
FROM minutos_do_dia m
LEFT JOIN existentes e ON e.minuto = m.minuto
WHERE e.minuto IS NULL          -- minutos que faltam
ORDER BY m.minuto;

-------------------------------------------------------------
-- Fallback

-- ALTER TABLE sensor_data ADD COLUMN fallback BOOLEAN NOT NULL DEFAULT FALSE;
-- ALTER TABLE sensor_data ADD COLUMN sensor_ip VARCHAR(14) NOT NULL DEFAULT '192.168.1.100';

select * from sensor_data where data_hora::varchar like '2026-02-09 17:32%'
select * from sensor_data where data_hora::varchar like '2026-02-08 15:38%'

select * from sensor_data where data_hora::varchar like '2026-02-09 17:33%'
select * from sensor_data where data_hora::varchar like '2026-02-09 17:34%'
select * from sensor_data where data_hora::varchar like '2026-02-09 17:35%'

-- delete from sensor_data where data_hora::varchar like '2026-02-09 17:33%';
-- delete from sensor_data where data_hora::varchar like '2026-02-09 17:34%';
-- delete from sensor_data where data_hora::varchar like '2026-02-09 17:35%';

/*
insert into sensor_data (temperatura_celsius, temperatura_fahrenheit, umidade, data_hora, uptime, fallback) values (26.23903, 79.23026, 60.61707, '2026-02-09 17:33:05', 0, true);
insert into sensor_data (temperatura_celsius, temperatura_fahrenheit, umidade, data_hora, uptime, fallback) values (26.23903, 79.23026, 60.61707, '2026-02-09 17:34:05', 0, true);
insert into sensor_data (temperatura_celsius, temperatura_fahrenheit, umidade, data_hora, uptime, fallback) values (26.23903, 79.23026, 60.61707, '2026-02-09 17:35:05', 0, true);
insert into sensor_data (temperatura_celsius, temperatura_fahrenheit, umidade, data_hora, uptime, fallback) values (26.23903, 79.23026, 60.61707, '2026-02-09 17:42:05', 0, true);
insert into sensor_data (temperatura_celsius, temperatura_fahrenheit, umidade, data_hora, uptime, fallback) values (26.23903, 79.23026, 60.61707, '2026-02-09 17:43:05', 0, true);
insert into sensor_data (temperatura_celsius, temperatura_fahrenheit, umidade, data_hora, uptime, fallback, sensor_ip) values (26.23903, 79.23026, 60.61707, '2026-02-10 16:52:05', 0, true, '0.0.0.0');




