package com.wrtecnologia.sensores.dht22.climatempo.repository.impl;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsDTO;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsYearDTO;
import com.wrtecnologia.sensores.dht22.climatempo.repository.SensorDataRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SensorDataRepositoryCustomImpl implements SensorDataRepositoryCustom {

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<SensorDataStatisticsDTO> findSensorDataStatistics() {
        String sql = """
                WITH TempMin AS (
                                    SELECT
                                        TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                                        data_hora AS data_hora_min_temp,
                                        temperatura_celsius AS temperatura_minima,
                                        umidade AS umidade_minima,
                                        ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY temperatura_celsius ASC, data_hora DESC) AS rn_temp
                                    FROM
                                        sensor_data
                                   WHERE temperatura_celsius > 0 AND umidade > 0
                                     AND ((EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE))
                                         OR (EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                ),
                                TempMax AS (
                                    SELECT
                                        TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                                        data_hora AS data_hora_max_temp,
                                        temperatura_celsius AS temperatura_maxima,
                                        umidade AS umidade_maxima,
                                        ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY temperatura_celsius DESC, data_hora DESC) AS rn_temp
                                    FROM
                                        sensor_data
                                   WHERE temperatura_celsius > 0 AND umidade > 0
                                     AND ((EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE))
                                         OR (EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                ),
                                UmidMin AS (
                                    SELECT
                                        TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                                        data_hora AS data_hora_min_umid,
                                        umidade AS umidade_minima,
                                        ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY umidade ASC, data_hora DESC) AS rn_umid
                                    FROM
                                        sensor_data
                                   WHERE temperatura_celsius > 0 AND umidade > 0
                 					AND ((EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE))
                                         OR (EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                ),
                                UmidMax AS (
                                    SELECT
                                        TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                                        data_hora AS data_hora_max_umid,
                                        umidade AS umidade_maxima,
                                        ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY umidade DESC, data_hora DESC) AS rn_umid
                                    FROM
                                        sensor_data
                                   WHERE temperatura_celsius > 0 AND umidade > 0
                					 AND ((EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE)
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE))
                                         OR (EXTRACT(MONTH FROM data_hora) = EXTRACT(MONTH FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                            AND EXTRACT(YEAR FROM data_hora) = EXTRACT(YEAR FROM CURRENT_DATE - INTERVAL '1 MONTH'))
                                )
                                SELECT
                                    -- Data e hora da temperatura mínima
                                    TO_CHAR(tm.data_hora_min_temp, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_temp_minima,
                
                                    -- Temperatura mínima
                                    TRUNC(CAST(tm.temperatura_minima AS NUMERIC), 2) || 'º' AS temp_minima,
                
                                    -- Data e hora da temperatura máxima
                                    TO_CHAR(tx.data_hora_max_temp, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_temp_maxima,
                
                                    -- Temperatura máxima
                                    TRUNC(CAST(tx.temperatura_maxima AS NUMERIC), 2) || 'º' AS temp_maxima,
                
                                    -- Variação de temperatura com sinal
                                    (CASE
                                        WHEN tx.data_hora_max_temp < tm.data_hora_min_temp
                                        THEN TRUNC(CAST(tm.temperatura_minima AS NUMERIC), 2) - TRUNC(CAST(tx.temperatura_maxima AS NUMERIC), 2) || 'º'
                                        ELSE '+' || (TRUNC(CAST(tx.temperatura_maxima AS NUMERIC), 2) - TRUNC(CAST(tm.temperatura_minima AS NUMERIC), 2)) || 'º'
                                    END) AS variacao_temp,
                
                                    -- Data e hora da umidade mínima
                                    TO_CHAR(um.data_hora_min_umid, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_umidade_minima,
                
                                    -- Umidade mínima
                                    TRUNC(CAST(um.umidade_minima AS NUMERIC), 2) || '%' AS umidade_minima,
                
                                    -- Data e hora da umidade máxima
                                    TO_CHAR(ux.data_hora_max_umid, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_umidade_maxima,
                
                                    -- Umidade máxima
                                    TRUNC(CAST(ux.umidade_maxima AS NUMERIC), 2) || '%' AS umidade_maxima,
                
                                    -- Variação de umidade com sinal
                                    (CASE
                                        WHEN ux.data_hora_max_umid < um.data_hora_min_umid
                                        THEN TRUNC(CAST(um.umidade_minima AS NUMERIC), 2) - TRUNC(CAST(ux.umidade_maxima AS NUMERIC), 2) || '%'
                                        ELSE '+' || (TRUNC(CAST(ux.umidade_maxima AS NUMERIC), 2) - TRUNC(CAST(um.umidade_minima AS NUMERIC), 2)) || '%'
                                    END) AS variacao_umidade
                                FROM
                                    TempMin tm
                                JOIN
                                    TempMax tx ON tm.data = tx.data AND tm.rn_temp = 1 AND tx.rn_temp = 1
                                JOIN
                                    UmidMin um ON tm.data = um.data AND um.rn_umid = 1
                                JOIN
                                    UmidMax ux ON tm.data = ux.data AND ux.rn_umid = 1
                                ORDER BY
                                    -- tm.data ASC;
                                    TO_DATE(tm.data, 'DD/MM/YYYY') ASC;
                
                
                """;

        Query query = entityManager.createNativeQuery(sql);

        List<Object[]> results = query.getResultList();
        List<SensorDataStatisticsDTO> dtos = new ArrayList<>();

        for (Object[] result : results) {
            SensorDataStatisticsDTO dto = new SensorDataStatisticsDTO(
                    (String) result[0],
                    (String) result[1],
                    (String) result[2],
                    (String) result[3],
                    (String) result[4],
                    (String) result[5],
                    (String) result[6],
                    (String) result[7],
                    (String) result[8],
                    (String) result[9]
            );
            dtos.add(dto);
        }

        return dtos;
    }

    @Override
    public List<SensorDataStatisticsYearDTO> findSensorDataStatisticsYear() {
        String sql = """
                WITH TempMin AS (
                    SELECT
                        date_trunc('month', data_hora) AS mes,
                        data_hora AS data_hora_temperatura_minima,
                        temperatura_celsius AS temperatura_minima,
                        umidade AS umidade_minima,
                        ROW_NUMBER() OVER (
                            PARTITION BY date_trunc('month', data_hora)
                            ORDER BY temperatura_celsius ASC, data_hora DESC
                        ) AS rn_temp
                    FROM sensor_data
                    WHERE temperatura_celsius > 0 AND umidade > 0
                ),
                TempMax AS (
                    SELECT
                        date_trunc('month', data_hora) AS mes,
                        data_hora AS data_hora_max_temp,
                        temperatura_celsius AS temperatura_maxima,
                        umidade AS umidade_maxima,
                        ROW_NUMBER() OVER (
                            PARTITION BY date_trunc('month', data_hora)
                            ORDER BY temperatura_celsius DESC, data_hora DESC
                        ) AS rn_temp
                    FROM sensor_data
                    WHERE temperatura_celsius > 0 AND umidade > 0
                ),
                UmidMin AS (
                    SELECT
                        date_trunc('month', data_hora) AS mes,
                        data_hora AS data_hora_min_umid,
                        umidade AS umidade_minima,
                        ROW_NUMBER() OVER (
                            PARTITION BY date_trunc('month', data_hora)
                            ORDER BY umidade ASC, data_hora DESC
                        ) AS rn_umid
                    FROM sensor_data
                    WHERE temperatura_celsius > 0 AND umidade > 0
                ),
                UmidMax AS (
                    SELECT
                        date_trunc('month', data_hora) AS mes,
                        data_hora AS data_hora_max_umid,
                        umidade AS umidade_maxima,
                        ROW_NUMBER() OVER (
                            PARTITION BY date_trunc('month', data_hora)
                            ORDER BY umidade DESC, data_hora DESC
                        ) AS rn_umid
                    FROM sensor_data
                    WHERE temperatura_celsius > 0 AND umidade > 0
                )
                SELECT
                    -- Mês em MM/YYYY
                    TO_CHAR(tm.mes, 'MM/YYYY') AS mes_ano,
                
                    -- Data da temp mínima
                    TO_CHAR(tm.data_hora_temperatura_minima, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_temperatura_minima,
                    TRUNC(tm.temperatura_minima::numeric, 2) || 'º' AS temperatura_minima,
                
                    -- Data da temp máxima
                    TO_CHAR(tx.data_hora_max_temp, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_temperatura_maxima,
                    TRUNC(tx.temperatura_maxima::numeric, 2) || 'º' AS temperatura_maxima,
                
                    -- Variação de temperatura
                    CASE
                        WHEN tx.data_hora_max_temp < tm.data_hora_temperatura_minima
                            THEN TRUNC(tm.temperatura_minima::numeric, 2) -
                                 TRUNC(tx.temperatura_maxima::numeric, 2) || 'º'
                        ELSE
                            '+' || (TRUNC(tx.temperatura_maxima::numeric, 2) -
                                    TRUNC(tm.temperatura_minima::numeric, 2)) || 'º'
                    END AS variacao_temperatura,
                
                    -- Data da umidade mínima
                    TO_CHAR(um.data_hora_min_umid, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_umidade_minima,
                    TRUNC(um.umidade_minima::numeric, 2) || '%' AS umidade_minima,
                
                    -- Data da umidade máxima
                    TO_CHAR(ux.data_hora_max_umid, 'DD/MM/YYYY HH24:MI:SS') AS data_hora_umidade_maxima,
                    TRUNC(ux.umidade_maxima::numeric, 2) || '%' AS umidade_maxima,
                
                    -- Variação de umidade
                    CASE
                        WHEN ux.data_hora_max_umid < um.data_hora_min_umid
                            THEN TRUNC(um.umidade_minima::numeric, 2) -
                                 TRUNC(ux.umidade_maxima::numeric, 2) || '%'
                        ELSE
                            '+' || (TRUNC(ux.umidade_maxima::numeric, 2) -
                                    TRUNC(um.umidade_minima::numeric, 2)) || '%'
                    END AS variacao_umidade
                
                FROM TempMin tm
                JOIN TempMax tx ON tm.mes = tx.mes AND tm.rn_temp = 1 AND tx.rn_temp = 1
                JOIN UmidMin um ON tm.mes = um.mes AND um.rn_umid = 1
                JOIN UmidMax ux ON tm.mes = ux.mes AND ux.rn_umid = 1
                
                ORDER BY
                    EXTRACT(YEAR FROM tm.mes),
                    EXTRACT(MONTH FROM tm.mes);
                """;

        Query query = entityManager.createNativeQuery(sql);

        List<Object[]> results = query.getResultList();
        List<SensorDataStatisticsYearDTO> dtos = new ArrayList<>();

        for (Object[] result : results) {
            SensorDataStatisticsYearDTO dto = new SensorDataStatisticsYearDTO(
                    (String) result[0],
                    (String) result[1],
                    (String) result[2],
                    (String) result[3],
                    (String) result[4],
                    (String) result[5],
                    (String) result[6],
                    (String) result[7],
                    (String) result[8],
                    (String) result[9],
                    (String) result[10]
            );
            dtos.add(dto);
        }
        return dtos;
    }
}
