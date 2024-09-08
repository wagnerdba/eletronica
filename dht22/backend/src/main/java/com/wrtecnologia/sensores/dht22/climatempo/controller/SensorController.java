package com.wrtecnologia.sensores.dht22.climatempo.controller;

import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataCountDTO;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import com.wrtecnologia.sensores.dht22.climatempo.service.SensorService;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/dht22")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
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

            // Salvar dados do sensor usando o serviço
            SensorData sensorData = sensorService.saveSensorData(sensorDataDTO);

            // Retornar o ID e UUID gerados pelo banco de dados
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("/api/dht22/post_data - Sucesso - id: " + sensorData.getId() + ", uuid: " + sensorData.getUuid());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("POST - Falha de validação: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("POST - Falha ao processar dados: " + e.getMessage());
        }
    }

    // Novo endpoint GET para consulta
    @GetMapping("/test")
    public ResponseEntity<String> consultarTest() {
        // Retorna uma string qualquer como resposta
        return ResponseEntity.ok("Retorno do endpoint de teste da aplicação do sensor DHT22");
    }

    @GetMapping("/all")
    public ResponseEntity<List<SensorDataDTO>> getAllSensorData(
            @RequestParam(value = "order", defaultValue = "A") String order) {

        // Obter todos os dados do sensor usando o serviço
        List<SensorDataDTO> sensorDataDTOList = sensorService.getAllSensorData(order);

        return ResponseEntity.ok(sensorDataDTOList);
    }

    @GetMapping("/statistics")
    public List<SensorDataStatisticsDTO> getSensorDataStatistics() {
        return sensorService.getSensorDataStatistics();
    }

    // @CrossOrigin(origins = "http://localhost:3000") // Para testes locais
    @GetMapping("/last")
    public ResponseEntity<SensorDataDTO> getLastSensorData() {
        Optional<SensorDataDTO> sensorDataDTO = sensorService.getLastSensorData();
        return sensorDataDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/today")
    public ResponseEntity<List<SensorDataDTO>> getSensorDataForToday() {
        List<SensorDataDTO> sensorDataList = sensorService.getSensorDataForCurrentDate();
        return ResponseEntity.ok(sensorDataList);
    }

    @GetMapping("/count")
    public SensorDataCountDTO getCount() {
        return sensorService.getCount();
    }
}
