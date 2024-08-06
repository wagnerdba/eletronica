package com.wrtecnologia.sensores.dht22.climatempo.controller;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import com.wrtecnologia.sensores.dht22.climatempo.repository.SensorDataRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/dht22")
public class SensorController {

    private SensorDataRepository sensorDataRepository;

    public SensorController(SensorDataRepository sensorDataRepository) {
        this.sensorDataRepository = sensorDataRepository;
    }

    @PostMapping("/post_data")
    public ResponseEntity<String> receivePostSensorData(@RequestBody SensorDataDTO sensorDataDTO) {
        try {
            // Log de dataHora
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            System.out.print(now.format(formatter));

            // Log do JSON recebido
            System.out.println(" -> Requisição Recebida: " + sensorDataDTO.toString());

            // Verificar se o campo dataHora é nulo ou vazio
            if (sensorDataDTO.getDataHora() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Campo 'data_hora' é obrigatório e não pode ser nulo.");
            }

            // Criar e configurar o objeto SensorData
            SensorData sensorData = new SensorData();
            sensorData.setTemperaturaCelsius(sensorDataDTO.getTemperaturaCelsius());
            sensorData.setTemperaturaFahrenheit(sensorDataDTO.getTemperaturaFahrenheit());
            sensorData.setUmidade(sensorDataDTO.getUmidade());
            sensorData.setDataHora(sensorDataDTO.getDataHoraAsLocalDateTime()); // Converter a string de data para LocalDateTime

            // Salvar no banco de dados
            sensorDataRepository.save(sensorData);

            // Retornar o ID e UUID gerados pelo banco de dados
            return ResponseEntity.status(HttpStatus.CREATED).body("/api/dht22/post_data - Sucesso - id: " + sensorData.getId() + ", uuid: " + sensorData.getUuid());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("POST - Falha de validação: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("POST - Falha ao processar dados: " + e.getMessage());
        }
    }

    // Novo endpoint GET para consulta
    @GetMapping("/consulta")
    public ResponseEntity<String> consultarDados() {
        // Retorna uma string qualquer como resposta
        return ResponseEntity.ok("Retorno do endpoint de teste da aplicação do sensor DHT22");
    }

    @GetMapping("/all")
    public ResponseEntity<List<SensorDataDTO>> getAllSensorData(
            @RequestParam(value = "order", defaultValue = "A") String order) {

        List<SensorData> sensorDataList;

        if ("D".equalsIgnoreCase(order)) {
            // Busca dados em ordem decrescente
            sensorDataList = sensorDataRepository.findAllByOrderByDataHoraDesc();
        } else {
            // Busca dados em ordem crescente
            sensorDataList = sensorDataRepository.findAllByOrderByDataHoraAsc();
        }

        // Mapeia dados do sensor para DTO
        List<SensorDataDTO> sensorDataDTOList = sensorDataList.stream()
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
                .toList();

        return ResponseEntity.ok(sensorDataDTOList);
    }

}
