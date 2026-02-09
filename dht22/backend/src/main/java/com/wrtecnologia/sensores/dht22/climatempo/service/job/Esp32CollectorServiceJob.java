/*
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
import java.util.Optional;

@Service
public class Esp32CollectorServiceJob {

    private final SensorService sensorService;

    @Value("${esp32.api.url}")
    private String url;

    public Esp32CollectorServiceJob(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @Scheduled(cron = "5 * * * * *")  // Executa no segundo 05 de cada minuto
    public void executarColetaAutomatica() {

        int maxTentativas = 4;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {

                final LocalDateTime jobStartTime = LocalDateTime.now().withNano(0);

                System.out.println("[⚡ JOB *] Execução " + tentativa + " em " + jobStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                SensorDataDTO dto;

                // 🔁 Tentativas 1 a 4 → ESP32
                if (tentativa < maxTentativas) {

                    // 🟦 1. Conectar ao ESP32
                    URL requestUrl = new URL(url);
                    HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);

                    int responseCode = connection.getResponseCode();
                    if (responseCode != 200) {
                        throw new RuntimeException("Falha HTTP: " + responseCode);
                    }

                    // 🟦 2. Ler JSON
                    BufferedReader reader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    String json = jsonBuilder.toString();
                    System.out.println("[🔍 ESP32] GET json.: " + json);

                    // 🟦 3. Converter JSON → DTO
                    ObjectMapper mapper = new ObjectMapper();
                    dto = mapper.readValue(json, SensorDataDTO.class);

                }
                // 🟨 Tentativa 4 → FALLBACK (SEM REDE COM ESP32)
                else {

                    Optional<SensorDataDTO> lastOpt = sensorService.getLastSensorData();

                    if (!lastOpt.isPresent()) {
                        throw new IllegalStateException("Não existe registro anterior para fallback."
                        );
                    }

                    SensorDataDTO last = lastOpt.get();

                    dto = new SensorDataDTO();
                    dto.setTemperaturaCelsius(last.getTemperaturaCelsius());
                    dto.setTemperaturaFahrenheit(last.getTemperaturaFahrenheit());
                    dto.setUmidade(last.getUmidade());
                    dto.setDataHora(jobStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    dto.setFallback(true);
                    dto.setUptime("0");
                    dto.setSensorIp("0.0.0.0");

                    System.out.println("[🟡 FALLBACK] Falha na comunicação com o ESP32: Fallback executado.");
                }

                // 🟦 4. Salvar no banco (fluxo normal)
                SensorData saved = sensorService.saveSensorData(dto);
                System.out.println("[💾 BANCO] POST id..: " + saved.getId() + ", uuid: " + saved.getUuid());

                // sucesso → encerra o job do minuto
                break;

            } catch (Exception e) {

                String msg = e.getMessage();

                // 🟥 Registro duplicado
                if (msg != null && msg.contains("ux_sensor_data_day_hour_minute")) {
                    System.out.println("🟡 Registro duplicado. Job concluído.");
                    break;
                }

                // 🟡 Falhas de rede
                if (e instanceof java.net.SocketTimeoutException ||
                        e instanceof java.net.ConnectException ||
                        (msg != null && msg.contains("No route to host"))) {

                    System.out.println("🔴 Erro de rede: Não foi possível alcançar o host (ESP32).");
                }

                // 🔁 Retry
                if (tentativa < maxTentativas) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                } else {
                    System.out.println("🔴 Falha após " + maxTentativas + " tentativas.");
                }
            }
        }
    }
}
*/

package com.wrtecnologia.sensores.dht22.climatempo.service.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataDTO;
import com.wrtecnologia.sensores.dht22.climatempo.model.SensorData;
import com.wrtecnologia.sensores.dht22.climatempo.service.SensorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class Esp32CollectorServiceJob {

    private static final int MAX_TENTATIVAS = 4;
    private static final int RETRY_DELAY_MS = 3000;

    private static final DateTimeFormatter DT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SensorService sensorService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${esp32.api.url}")
    private String url;

    public Esp32CollectorServiceJob(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @Scheduled(cron = "5 * * * * *") // Executa no segundo 05 de cada minuto
    public void executarColetaAutomatica() {

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {

                final LocalDateTime jobStartTime = LocalDateTime.now().withNano(0);

                System.out.println("[⚡ JOB *] Execução " + tentativa + " em "
                        + jobStartTime.format(DT_FORMAT));

                SensorDataDTO dto;

                // 🔁 Tentativas 1 a 3 → ESP32
                if (tentativa < MAX_TENTATIVAS) {
                    dto = obterDadosDoEsp32();
                }
                // 🟨 Tentativa 4 → FALLBACK
                else {
                    dto = executarFallback(jobStartTime);
                }

                // 🟦 Salvar no banco
                SensorData saved = sensorService.saveSensorData(dto);
                System.out.println("[💾 BANCO] POST id..: "
                        + saved.getId() + ", uuid: " + saved.getUuid());

                break; // sucesso

            } catch (Exception e) {

                String msg = e.getMessage();

                // 🟥 Registro duplicado
                if (msg != null && msg.contains("ux_sensor_data_day_hour_minute")) {
                    System.out.println("🟡 Registro duplicado. Job concluído.");
                    break;
                }

                // 🟡 Falhas de rede / HTTP
                boolean erroRede =
                        e instanceof RestClientException ||
                                (msg != null && msg.contains("No route to host"));

                if (erroRede) {
                    System.out.println("🔴 Erro de rede: Não foi possível alcançar o host (ESP32).");
                }

                // 🔁 Retry
                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("🔴 Falha após " + MAX_TENTATIVAS + " tentativas.");
                }
            }
        }
    }

    /**
     * Chamada HTTP ao ESP32 usando RestTemplate.
     *
     * Mantém o mesmo comportamento:
     * - erro HTTP ≠ 200 → exceção
     * - erro de rede → exceção
     */
    private SensorDataDTO obterDadosDoEsp32() throws Exception {

        ResponseEntity<String> response =
                restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Falha HTTP: " + response.getStatusCode());
        }

        String json = response.getBody();

        System.out.println("[🔍 ESP32] GET json.: " + json);

        return MAPPER.readValue(json, SensorDataDTO.class);
    }

    /**
     * Fallback usando último registro persistido.
     */
    private SensorDataDTO executarFallback(LocalDateTime jobStartTime) {

        SensorDataDTO last = sensorService.getLastSensorData()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Não existe registro anterior para fallback."
                        )
                );

        SensorDataDTO dto = new SensorDataDTO();
        dto.setTemperaturaCelsius(last.getTemperaturaCelsius());
        dto.setTemperaturaFahrenheit(last.getTemperaturaFahrenheit());
        dto.setUmidade(last.getUmidade());
        dto.setDataHora(jobStartTime.format(DT_FORMAT));
        dto.setFallback(true);
        dto.setUptime("0");
        dto.setSensorIp("0.0.0.0");

        System.out.println("[🟡 FALLBACK] Falha na comunicação com o ESP32: Fallback executado.");

        return dto;
    }
}
