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
}
