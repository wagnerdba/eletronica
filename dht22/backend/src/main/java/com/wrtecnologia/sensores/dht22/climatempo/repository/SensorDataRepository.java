package com.wrtecnologia.sensores.dht22.climatempo.repository;

import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorDataRepository extends JpaRepository<SensorData, Long>, SensorDataRepositoryCustom  {
    List<SensorData> findAllByOrderByDataHoraDesc();

    List<SensorData> findAllByOrderByDataHoraAsc();

    @Query("SELECT sd FROM SensorData sd ORDER BY sd.id DESC LIMIT 1")
    Optional<SensorData> findLatestSensorData();

    /*
    @Query(value = "SELECT * FROM sensor_data WHERE data_hora >= DATE_TRUNC('day', CURRENT_DATE) AND data_hora < DATE_TRUNC('day', CURRENT_DATE) + INTERVAL '1 day' AND EXTRACT(MINUTE FROM data_hora) % 1 = 0 ORDER BY data_hora", nativeQuery = true)
    List<SensorData> findAllByCurrentDate();
    */


    @Query(value = "SELECT * FROM sensor_data WHERE DATE(data_hora) = CURRENT_DATE ORDER BY data_hora", nativeQuery = true)
    List<SensorData> findAllByCurrentDate();
    

   /*
    @Query("SELECT s FROM SensorData s WHERE FUNCTION('DATE', s.dataHora) = CURRENT_DATE ORDER BY s.dataHora DESC LIMIT 720")
    List<SensorData> findAllByCurrentDate();*/

    @Query("SELECT COUNT(s.id) FROM SensorData s")
    long countSensorData();

    @Query(value = """
            WITH MaxValues AS (
                SELECT
                    DATE_TRUNC('hour', data_hora) AS hour_start,
                    MAX(temperatura_celsius::numeric) AS max_temperatura_celsius,
                    MAX(umidade::numeric) AS max_umidade
                FROM
                    sensor_data
                WHERE
                    data_hora::date = CURRENT_DATE
                GROUP BY
                    DATE_TRUNC('hour', data_hora)
            )
            SELECT DISTINCT ON (hour_start)
                TO_CHAR(hour_start, 'HH24:MI') AS hora,
                TO_CHAR(TRUNC(max_temperatura_celsius, 2), 'FM999990.00') AS temperatura_celsius,
                TO_CHAR(TRUNC(max_umidade, 2), 'FM999990.00') AS umidade
            FROM
                MaxValues
            ORDER BY
                hour_start
            """, nativeQuery = true)
    List<Object[]> findSensorDataForToday();
}
