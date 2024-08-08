package com.wrtecnologia.sensores.dht22.climatempo.service;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import com.wrtecnologia.sensores.dht22.climatempo.repository.SensorDataRepository;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SensorService {

    private final SensorDataRepository sensorDataRepository;

    @Autowired
    public SensorService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    public SensorData saveSensorData(SensorDataDTO sensorDataDTO) {
        // Verificar se o campo dataHora é nulo ou vazio
        if (sensorDataDTO.getDataHora() == null) {
            throw new IllegalArgumentException("Campo 'data_hora' é obrigatório e não pode ser nulo.");
        }

        // Criar e configurar o objeto SensorData
        SensorData sensorData = new SensorData();
        sensorData.setTemperaturaCelsius(sensorDataDTO.getTemperaturaCelsius());
        sensorData.setTemperaturaFahrenheit(sensorDataDTO.getTemperaturaFahrenheit());
        sensorData.setUmidade(sensorDataDTO.getUmidade());
        sensorData.setDataHora(sensorDataDTO.getDataHoraAsLocalDateTime());

        // Salvar no banco de dados
        return sensorDataRepository.save(sensorData);
    }

    public List<SensorDataDTO> getAllSensorData(String order) {
        List<SensorData> sensorDataList;

        if ("D".equalsIgnoreCase(order)) {
            // Busca dados em ordem decrescente
            sensorDataList = sensorDataRepository.findAllByOrderByDataHoraDesc();
        } else {
            // Busca dados em ordem crescente
            sensorDataList = sensorDataRepository.findAllByOrderByDataHoraAsc();
        }

        // Mapeia dados do sensor para DTO
        return sensorDataList.stream()
                .map(sensorData -> {
                    SensorDataDTO dto = new SensorDataDTO();
                    dto.setId(sensorData.getId());
                    dto.setTemperaturaCelsius(sensorData.getTemperaturaCelsius());
                    dto.setTemperaturaFahrenheit(sensorData.getTemperaturaFahrenheit());
                    dto.setUmidade(sensorData.getUmidade());
                    dto.setDataHora(sensorData.getDataHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    dto.setUuid(sensorData.getUuid());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<SensorDataStatisticsDTO> getSensorDataStatistics() {
        return sensorDataRepository.findSensorDataStatistics();
    }

}
