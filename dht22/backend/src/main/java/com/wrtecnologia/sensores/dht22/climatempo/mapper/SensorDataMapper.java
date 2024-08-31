package com.wrtecnologia.sensores.dht22.climatempo.mapper;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class SensorDataMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SensorDataDTO toDTO(SensorData entity) {
        SensorDataDTO dto = new SensorDataDTO();
        dto.setId(entity.getId());
        dto.setTemperaturaCelsius(entity.getTemperaturaCelsius());
        dto.setTemperaturaFahrenheit(entity.getTemperaturaFahrenheit());
        dto.setUmidade(entity.getUmidade());
        dto.setDataHora(entity.getDataHora().format(formatter));  // Converte LocalDateTime para String
        dto.setUuid(entity.getUuid());
        return dto;
    }
}