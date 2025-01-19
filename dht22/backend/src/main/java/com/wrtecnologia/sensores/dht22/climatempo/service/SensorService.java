package com.wrtecnologia.sensores.dht22.climatempo.service;

import com.wrtecnologia.sensores.dht22.climatempo.dto.*;
import com.wrtecnologia.sensores.dht22.climatempo.mapper.SensorDataMapper;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import com.wrtecnologia.sensores.dht22.climatempo.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SensorService {

    private final SensorDataRepository sensorDataRepository;

    @Autowired
    public SensorService(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @Autowired
    private SensorDataMapper sensorDataMapper;  // Certifique-se de que esta linha está presente

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

    public List<SensorDataStatisticsDTO> getSensorDataStatisticsYear() {
        return sensorDataRepository.findSensorDataStatisticsYear();
    }

    public Optional<SensorDataDTO> getLastSensorData() {
        Optional<SensorData> sensorDataOpt = sensorDataRepository.findLatestSensorData();
        if (sensorDataOpt.isPresent()) {
            SensorData sensorData = sensorDataOpt.get();
            SensorDataDTO sensorDataDTO = new SensorDataDTO();
            sensorDataDTO.setId(sensorData.getId());
            sensorDataDTO.setTemperaturaCelsius(sensorData.getTemperaturaCelsius());
            sensorDataDTO.setTemperaturaFahrenheit(sensorData.getTemperaturaFahrenheit());
            sensorDataDTO.setUmidade(sensorData.getUmidade());
            sensorDataDTO.setDataHora(sensorData.getDataHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            sensorDataDTO.setUuid(sensorData.getUuid());
            return Optional.of(sensorDataDTO);
        } else {
            return Optional.empty();
        }
    }

    public List<SensorDataDTO> getSensorDataForCurrentDate() {
        // Busca a lista de entidades
        List<SensorData> sensorDataList = sensorDataRepository.findAllByCurrentDate();

        // Converte a lista de entidades para uma lista de DTOs
        return sensorDataList.stream()
                .map(sensorDataMapper::toDTO) // Converte cada entidade para DTO
                .collect(Collectors.toList()); // Coleta em uma lista de DTOs
    }

    public SensorDataCountDTO getCount() {
        long count = sensorDataRepository.countSensorData();
        return new SensorDataCountDTO(count);
    }

    public List<SensorDataHoraDTO> getSensorDataForToday() {
        List<Object[]> results = sensorDataRepository.findSensorDataForToday();

        // Convertendo os resultados em DTOs
        return results.stream().map(result -> {
            SensorDataHoraDTO dto = new SensorDataHoraDTO();
            dto.setHora((String) result[0]);
            dto.setTemperaturaCelsius((String) result[1]);
            dto.setUmidade((String) result[2]);
            return dto;
        }).collect(Collectors.toList());
    }

    public List<SensorDataCurrentDateTestDTO> getSensorDataAndCurrentDate() {
        List<Object[]> result = sensorDataRepository.findSensorDataAndCurrentDate();
        return result.stream()
                .map(sensorDataMapper::toSensorDataCurrentDateTestDTO) // Usa o mapper para converter cada resultado
                .collect(Collectors.toList());
    }
}
