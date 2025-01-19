package com.wrtecnologia.sensores.dht22.climatempo.repository;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsDTO;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsYearDTO;

import java.util.List;

public interface SensorDataRepositoryCustom {
    List<SensorDataStatisticsDTO> findSensorDataStatistics();
    List<SensorDataStatisticsYearDTO> findSensorDataStatisticsYear();
}

