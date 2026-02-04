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

    /*
    // @Scheduled(fixedRate = 60000)
    @Scheduled(cron = "5 * * * * *")  // Executa no segundo 05 de cada minuto
    public void executarColetaAutomatica() {
        int maxTentativas = 6;

        for (int tentativa = 1; tentativa <= maxTentativas; tentativa++) {
            try {

                System.out.println(
                        "[⚡ JOB *] Execução " + tentativa +
                                " em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );

                // 🟦 1. Conectar ao ESP32
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(8000);
                connection.setReadTimeout(8000);

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.out.println("\uD83D\uDD34 Falha ao acessar ESP32 -> HTTP: " + responseCode);
                    throw new RuntimeException("\uD83D\uDD34 Falha HTTP: " + responseCode);
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
                System.out.println("[\uD83D\uDD0D ESP32] GET json.: " + json);

                // 🟦 3. Converter JSON para DTO
                ObjectMapper mapper = new ObjectMapper();
                SensorDataDTO dto = mapper.readValue(json, SensorDataDTO.class);

                // 🟦 4. SALVAR no banco
                SensorData saved = sensorService.saveSensorData(dto);
                System.out.println("[\uD83D\uDCBE BANCO] POST id..: " +
                        saved.getId() + ", uuid: " + saved.getUuid());

                // sucesso → parar tentativas
                break;

            } catch (Exception e) {

                String msg = e.getMessage();

                // 🟥 Violação de índice único (duplicata por minuto)
                if (msg != null && msg.contains("ux_sensor_data_day_hour_minute")) {
                    System.out.println("\uD83D\uDFE1 Registro duplicado. Job concluído.");
                    break;
                }

                // 🟡 Falhas normais de rede
                if (e instanceof java.net.SocketTimeoutException ||
                        e instanceof java.net.ConnectException) {

                    System.out.println("🔴 ESP32 indisponível temporariamente: " + e.getMessage());
                }

                // 🟢 No route to host
                else if (msg != null && msg.contains("No route to host")) {
                    System.out.println("🔴 Erro de rede: Não foi possível alcançar o host.");
                }

                // 🔁 Retry
                else if (tentativa < maxTentativas) {
                    System.out.println("🔴 Falha - Será feita uma nova tentativa...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ignored) {}
                } else {
                    System.out.println("🔴 Falha após " + maxTentativas + " tentativas.");
                }
            }
        }
    }
     */

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

                    System.out.println("🟡 Erro de rede: Gravando último registro do banco. Falha de rede com o ESP32.");

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
                    // dataHora, uuid, uptime → gerados no save normal
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
