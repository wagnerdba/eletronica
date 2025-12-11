package com.wrtecnologia.sensores.dht22.climatempo.service.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import com.wrtecnologia.sensores.dht22.climatempo.service.SensorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class Esp32CollectorService {

    private final SensorService sensorService;

    @Value("${esp32.api.url}")
    private String url;

    public Esp32CollectorService(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    // @Scheduled(fixedRate = 60000)
    @Scheduled(cron = "5 * * * * *")  // Executa no segundo 05 de cada minuto
    public void executarColetaAutomatica() {
        int maxTentativas = 6;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {

                System.out.println(
                        "Job executado. Tentativa " + tentativa +
                                " em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );

                // 🟦 1. Conectar ao ESP32
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("Falha ao acessar ESP32 -> HTTP: " + responseCode);
                    throw new RuntimeException("Falha HTTP: " + responseCode);
                }

                // 🟦 2. Ler JSON do ESP32
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder jsonBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }

                reader.close();
                connection.disconnect();

                String json = jsonBuilder.toString();
                System.out.println("[ESP32] GET json.: " + json);

                // 🟦 3. Converter JSON para DTO
                ObjectMapper mapper = new ObjectMapper();
                SensorDataDTO dto = mapper.readValue(json, SensorDataDTO.class);

                // 🟦 4. SALVAR no banco
                SensorData saved = sensorService.saveSensorData(dto);
                System.out.println("[BANCO] POST id..: " +
                        saved.getId() + ", uuid: " + saved.getUuid());

                // sucesso → parar tentativas
                break;

            } catch (Exception e) {

                // 🟥 Trigger impedindo duplicata → parar na hora
                if (e.getMessage() != null && e.getMessage().contains("duplicado")) {
                    System.out.println("Registro duplicado ignorado pela trigger");
                    break;
                }

                if (tentativa < maxTentativas) {
                    System.out.println("Tentativa falhou, nova tentativa em 3s...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                } else {
                    System.out.println("Falha após " + maxTentativas + " tentativas.");
                }
            }
        }
    }
}
