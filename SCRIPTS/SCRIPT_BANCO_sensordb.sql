--
-- PostgreSQL database dump
--

\restrict 98SghTl7GhpfWVb1KDJfwNvCnJIobdOe4ua1bWEHhN1f0kaCkR4UsVP9nYb2LfI

-- Dumped from database version 18.1 (Debian 18.1-1.pgdg13+2)
-- Dumped by pg_dump version 18.0

-- Started on 2025-12-09 20:20:44

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- TOC entry 3515 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- TOC entry 263 (class 1255 OID 24696)
-- Name: audit_trigger(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.audit_trigger() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
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
$$;


ALTER FUNCTION public.audit_trigger() OWNER TO postgres;

--
-- TOC entry 276 (class 1255 OID 24697)
-- Name: generate_uuid_v7(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.generate_uuid_v7() RETURNS uuid
    LANGUAGE plpgsql
    AS $$
DECLARE
    current_millis BIGINT;
    timestamp_part BIGINT;
    random_part BYTEA;
    uuid_v7 BYTEA;
    uuid_hex TEXT;
BEGIN
    -- Obter o timestamp atual em milissegundos desde a época UNIX (1 de janeiro de 1970)
    current_millis := (extract(epoch FROM clock_timestamp()) * 1000)::BIGINT;
    
    -- Os primeiros 48 bits são para o timestamp (6 bytes)
    timestamp_part := (current_millis & ((1::BIGINT << 48) - 1));

    -- Gerar 10 bytes aleatórios para completar os 128 bits do UUID
    random_part := gen_random_bytes(10);
    
    -- Inicializar uuid_v7 com 16 bytes vazios
    uuid_v7 := '\x00000000000000000000000000000000'::bytea;

    -- Inserir os primeiros 6 bytes de timestamp na estrutura UUID (com máscara 0xFF para garantir 1 byte por operação)
    uuid_v7 := set_byte(uuid_v7, 0, ((timestamp_part >> 40) & 0xFF)::INT);
    uuid_v7 := set_byte(uuid_v7, 1, ((timestamp_part >> 32) & 0xFF)::INT);
    uuid_v7 := set_byte(uuid_v7, 2, ((timestamp_part >> 24) & 0xFF)::INT);
    uuid_v7 := set_byte(uuid_v7, 3, ((timestamp_part >> 16) & 0xFF)::INT);
    uuid_v7 := set_byte(uuid_v7, 4, ((timestamp_part >> 8) & 0xFF)::INT);
    uuid_v7 := set_byte(uuid_v7, 5, (timestamp_part & 0xFF)::INT);

    -- Aplicar a versão (v7) e a variante de UUID (RFC 4122)
    uuid_v7 := set_byte(uuid_v7, 6, (get_byte(random_part, 0) & 0x0F) | 0x70);  -- Definir a versão 7
    uuid_v7 := set_byte(uuid_v7, 8, (get_byte(random_part, 2) & 0x3F) | 0x80);  -- Definir a variante

    -- Adicionar o restante dos bytes aleatórios
    uuid_v7 := set_byte(uuid_v7, 7, get_byte(random_part, 1));
    uuid_v7 := set_byte(uuid_v7, 9, get_byte(random_part, 3));
    uuid_v7 := set_byte(uuid_v7, 10, get_byte(random_part, 4));
    uuid_v7 := set_byte(uuid_v7, 11, get_byte(random_part, 5));
    uuid_v7 := set_byte(uuid_v7, 12, get_byte(random_part, 6));
    uuid_v7 := set_byte(uuid_v7, 13, get_byte(random_part, 7));
    uuid_v7 := set_byte(uuid_v7, 14, get_byte(random_part, 8));
    uuid_v7 := set_byte(uuid_v7, 15, get_byte(random_part, 9));

    -- Converter o BYTEA em uma string hexadecimal
    uuid_hex := encode(uuid_v7, 'hex');

    -- Converter a string hexadecimal em UUID no formato correto
    RETURN (
        substring(uuid_hex, 1, 8) || '-' ||
        substring(uuid_hex, 9, 4) || '-' ||
        substring(uuid_hex, 13, 4) || '-' ||
        substring(uuid_hex, 17, 4) || '-' ||
        substring(uuid_hex, 21, 12)
    )::uuid;
END;
$$;


ALTER FUNCTION public.generate_uuid_v7() OWNER TO postgres;

--
-- TOC entry 264 (class 1255 OID 24742)
-- Name: trg_no_duplicate_hour_minute(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trg_no_duplicate_hour_minute() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
begin
    if exists (
        select 1
        from sensor_data
        where date_trunc('minute', data_hora) = date_trunc('minute', new.data_hora)
    ) then
        raise exception 'Registro duplicado ignorado pela trigger';
    end if;

    return new;
end;
$$;


ALTER FUNCTION public.trg_no_duplicate_hour_minute() OWNER TO postgres;

--
-- TOC entry 262 (class 1255 OID 16423)
-- Name: uuid_generate_v4(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.uuid_generate_v4() RETURNS uuid
    LANGUAGE c STRICT PARALLEL SAFE
    AS '$libdir/uuid-ossp', 'uuid_generate_v4';


ALTER FUNCTION public.uuid_generate_v4() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 220 (class 1259 OID 24698)
-- Name: audit_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_log (
    id integer NOT NULL,
    table_name text,
    operation text,
    data jsonb,
    "timestamp" timestamp without time zone DEFAULT now()
);


ALTER TABLE public.audit_log OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 24705)
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.audit_log_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_log_id_seq OWNER TO postgres;

--
-- TOC entry 3516 (class 0 OID 0)
-- Dependencies: 221
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- TOC entry 222 (class 1259 OID 24706)
-- Name: sensor_data; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sensor_data (
    id bigint NOT NULL,
    temperatura_celsius double precision NOT NULL,
    temperatura_fahrenheit double precision NOT NULL,
    umidade double precision NOT NULL,
    data_hora timestamp without time zone NOT NULL,
    uuid uuid DEFAULT public.generate_uuid_v7() NOT NULL
);


ALTER TABLE public.sensor_data OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 24716)
-- Name: sensor_data_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sensor_data_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sensor_data_id_seq OWNER TO postgres;

--
-- TOC entry 3517 (class 0 OID 0)
-- Dependencies: 223
-- Name: sensor_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sensor_data_id_seq OWNED BY public.sensor_data.id;


--
-- TOC entry 224 (class 1259 OID 24717)
-- Name: vw_sensor_data_statistics; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.vw_sensor_data_statistics AS
 WITH tempmin AS (
         SELECT to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text) AS data,
            sensor_data.data_hora AS data_hora_min_temp,
            sensor_data.temperatura_celsius AS temperatura_minima,
            sensor_data.umidade AS umidade_minima,
            row_number() OVER (PARTITION BY (to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text)) ORDER BY sensor_data.temperatura_celsius, sensor_data.data_hora DESC) AS rn_temp
           FROM public.sensor_data
          WHERE ((sensor_data.temperatura_celsius > (0)::double precision) AND (sensor_data.umidade > (0)::double precision))
        ), tempmax AS (
         SELECT to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text) AS data,
            sensor_data.data_hora AS data_hora_max_temp,
            sensor_data.temperatura_celsius AS temperatura_maxima,
            sensor_data.umidade AS umidade_maxima,
            row_number() OVER (PARTITION BY (to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text)) ORDER BY sensor_data.temperatura_celsius DESC, sensor_data.data_hora DESC) AS rn_temp
           FROM public.sensor_data
          WHERE ((sensor_data.temperatura_celsius > (0)::double precision) AND (sensor_data.umidade > (0)::double precision))
        ), umidmin AS (
         SELECT to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text) AS data,
            sensor_data.data_hora AS data_hora_min_umid,
            sensor_data.umidade AS umidade_minima,
            row_number() OVER (PARTITION BY (to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text)) ORDER BY sensor_data.umidade, sensor_data.data_hora DESC) AS rn_umid
           FROM public.sensor_data
          WHERE ((sensor_data.temperatura_celsius > (0)::double precision) AND (sensor_data.umidade > (0)::double precision))
        ), umidmax AS (
         SELECT to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text) AS data,
            sensor_data.data_hora AS data_hora_max_umid,
            sensor_data.umidade AS umidade_maxima,
            row_number() OVER (PARTITION BY (to_char(sensor_data.data_hora, 'DD/MM/YYYY'::text)) ORDER BY sensor_data.umidade DESC, sensor_data.data_hora DESC) AS rn_umid
           FROM public.sensor_data
          WHERE ((sensor_data.temperatura_celsius > (0)::double precision) AND (sensor_data.umidade > (0)::double precision))
        )
 SELECT to_char(tm.data_hora_min_temp, 'DD/MM/YYYY HH24:MI:SS'::text) AS data_hora_temp_minima,
    (trunc((tm.temperatura_minima)::numeric, 2) || 'º'::text) AS temp_minima,
    to_char(tx.data_hora_max_temp, 'DD/MM/YYYY HH24:MI:SS'::text) AS data_hora_temp_maxima,
    (trunc((tx.temperatura_maxima)::numeric, 2) || 'º'::text) AS temp_maxima,
        CASE
            WHEN (tx.data_hora_max_temp < tm.data_hora_min_temp) THEN ((trunc((tm.temperatura_minima)::numeric, 2) - trunc((tx.temperatura_maxima)::numeric, 2)) || 'º'::text)
            ELSE (('+'::text || (trunc((tx.temperatura_maxima)::numeric, 2) - trunc((tm.temperatura_minima)::numeric, 2))) || 'º'::text)
        END AS variacao_temp,
    to_char(um.data_hora_min_umid, 'DD/MM/YYYY HH24:MI:SS'::text) AS data_hora_umidade_minima,
    (trunc((um.umidade_minima)::numeric, 2) || '%'::text) AS umidade_minima,
    to_char(ux.data_hora_max_umid, 'DD/MM/YYYY HH24:MI:SS'::text) AS data_hora_umidade_maxima,
    (trunc((ux.umidade_maxima)::numeric, 2) || '%'::text) AS umidade_maxima,
        CASE
            WHEN (ux.data_hora_max_umid < um.data_hora_min_umid) THEN ((trunc((um.umidade_minima)::numeric, 2) - trunc((ux.umidade_maxima)::numeric, 2)) || '%'::text)
            ELSE (('+'::text || (trunc((ux.umidade_maxima)::numeric, 2) - trunc((um.umidade_minima)::numeric, 2))) || '%'::text)
        END AS variacao_umidade
   FROM (((tempmin tm
     JOIN tempmax tx ON (((tm.data = tx.data) AND (tm.rn_temp = 1) AND (tx.rn_temp = 1))))
     JOIN umidmin um ON (((tm.data = um.data) AND (um.rn_umid = 1))))
     JOIN umidmax ux ON (((tm.data = ux.data) AND (ux.rn_umid = 1))))
  ORDER BY tm.data;


ALTER VIEW public.vw_sensor_data_statistics OWNER TO postgres;

--
-- TOC entry 3340 (class 2604 OID 24722)
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- TOC entry 3342 (class 2604 OID 24723)
-- Name: sensor_data id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_data ALTER COLUMN id SET DEFAULT nextval('public.sensor_data_id_seq'::regclass);


--
-- TOC entry 3345 (class 2606 OID 24725)
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- TOC entry 3357 (class 2606 OID 24727)
-- Name: sensor_data sensor_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_data
    ADD CONSTRAINT sensor_data_pkey PRIMARY KEY (id);


--
-- TOC entry 3359 (class 2606 OID 24729)
-- Name: sensor_data sensor_data_uuid_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sensor_data
    ADD CONSTRAINT sensor_data_uuid_key UNIQUE (uuid);


--
-- TOC entry 3346 (class 1259 OID 24730)
-- Name: idx_sensor_data_data_hora; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_data_hora ON public.sensor_data USING btree (data_hora);


--
-- TOC entry 3347 (class 1259 OID 24731)
-- Name: idx_sensor_data_date_temp; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_date_temp ON public.sensor_data USING btree (data_hora DESC, temperatura_celsius);


--
-- TOC entry 3348 (class 1259 OID 24732)
-- Name: idx_sensor_data_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_id ON public.sensor_data USING btree (id);


--
-- TOC entry 3349 (class 1259 OID 24734)
-- Name: idx_sensor_data_id_data_hora; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_id_data_hora ON public.sensor_data USING btree (id, data_hora);


--
-- TOC entry 3350 (class 1259 OID 24738)
-- Name: idx_sensor_data_mes; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_mes ON public.sensor_data USING btree (date_trunc('month'::text, data_hora));


--
-- TOC entry 3351 (class 1259 OID 24735)
-- Name: idx_sensor_data_uuid; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_uuid ON public.sensor_data USING btree (uuid);


--
-- TOC entry 3352 (class 1259 OID 24741)
-- Name: idx_sensor_data_valid; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_valid ON public.sensor_data USING btree (data_hora) WHERE ((temperatura_celsius > (0)::double precision) AND (umidade > (0)::double precision));


--
-- TOC entry 3353 (class 1259 OID 24736)
-- Name: idx_sensor_data_year_month; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_data_year_month ON public.sensor_data USING btree (EXTRACT(year FROM data_hora), EXTRACT(month FROM data_hora));


--
-- TOC entry 3354 (class 1259 OID 24739)
-- Name: idx_sensor_temp; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_temp ON public.sensor_data USING btree (temperatura_celsius, data_hora);


--
-- TOC entry 3355 (class 1259 OID 24740)
-- Name: idx_sensor_umid; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sensor_umid ON public.sensor_data USING btree (umidade, data_hora);


--
-- TOC entry 3360 (class 2620 OID 24744)
-- Name: sensor_data no_duplicate_hour_minute; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER no_duplicate_hour_minute BEFORE INSERT ON public.sensor_data FOR EACH ROW EXECUTE FUNCTION public.trg_no_duplicate_hour_minute();


--
-- TOC entry 3361 (class 2620 OID 24737)
-- Name: sensor_data sensor_data_audit_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER sensor_data_audit_trigger AFTER DELETE ON public.sensor_data FOR EACH ROW EXECUTE FUNCTION public.audit_trigger();


-- Completed on 2025-12-09 20:20:44

--
-- PostgreSQL database dump complete
--

\unrestrict 98SghTl7GhpfWVb1KDJfwNvCnJIobdOe4ua1bWEHhN1f0kaCkR4UsVP9nYb2LfI

