package com.wrtecnologia.sensores.dht22.climatempo.repository;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
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

}
