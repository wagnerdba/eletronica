package com.wrtecnologia.sensores.dht22.climatempo.repository.impl;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsDTO;
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
                    ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY temperatura_celsius ASC, data_hora ASC) AS rn_temp
                FROM 
                    sensor_data
            ),
            TempMax AS (
                SELECT 
                    TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                    data_hora AS data_hora_max_temp,
                    temperatura_celsius AS temperatura_maxima,
                    umidade AS umidade_maxima,
                    ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY temperatura_celsius DESC, data_hora ASC) AS rn_temp
                FROM 
                    sensor_data
            ),
            UmidMin AS (
                SELECT 
                    TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                    data_hora AS data_hora_min_umid,
                    umidade AS umidade_minima,
                    ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY umidade ASC, data_hora ASC) AS rn_umid
                FROM 
                    sensor_data
            ),
            UmidMax AS (
                SELECT 
                    TO_CHAR(data_hora, 'DD/MM/YYYY') AS data,
                    data_hora AS data_hora_max_umid,
                    umidade AS umidade_maxima,
                    ROW_NUMBER() OVER (PARTITION BY TO_CHAR(data_hora, 'DD/MM/YYYY') ORDER BY umidade DESC, data_hora ASC) AS rn_umid
                FROM 
                    sensor_data
            )
            SELECT 
                TO_CHAR(tm.data_hora_min_temp, 'DD/MM/YYYY HH24:MI:SS') AS datahora_temperatura_minima,
                TRUNC(CAST(tm.temperatura_minima AS NUMERIC), 2) || 'º' AS temperatura_minima,
                TO_CHAR(tx.data_hora_max_temp, 'DD/MM/YYYY HH24:MI:SS') AS datahora_temperatura_maxima,
                TRUNC(CAST(tx.temperatura_maxima AS NUMERIC), 2) || 'º' AS temperatura_maxima,
                (CASE 
                    WHEN tx.data_hora_max_temp < tm.data_hora_min_temp 
                    THEN TRUNC(CAST(tm.temperatura_minima AS NUMERIC), 2) - TRUNC(CAST(tx.temperatura_maxima AS NUMERIC), 2) || 'º'
                    ELSE '+' || (TRUNC(CAST(tx.temperatura_maxima AS NUMERIC), 2) - TRUNC(CAST(tm.temperatura_minima AS NUMERIC), 2)) || 'º'
                END) AS variacao_temperatura,
                TO_CHAR(um.data_hora_min_umid, 'DD/MM/YYYY HH24:MI:SS') AS datahora_umidade_minima,
                TRUNC(CAST(um.umidade_minima AS NUMERIC), 2) || '%' AS umidade_minima,
                TO_CHAR(ux.data_hora_max_umid, 'DD/MM/YYYY HH24:MI:SS') AS datahora_umidade_maxima,
                TRUNC(CAST(ux.umidade_maxima AS NUMERIC), 2) || '%' AS umidade_maxima,
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
                tm.data ASC;
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
}
