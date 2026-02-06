package com.wrtecnologia.sensores.dht22.climatempo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class SensorDataDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("temperatura_celsius")
    private double temperaturaCelsius;

    @JsonProperty("temperatura_fahrenheit")
    private double temperaturaFahrenheit;

    @JsonProperty("umidade")
    private double umidade;

    @JsonProperty("data_hora")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String dataHora;

    @JsonProperty("uuid")
    private UUID uuid; // Adiciona o campo UUID

    @JsonProperty("uptime")
    private String uptime;

    @JsonProperty("fallback")
    private boolean fallback;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getTemperaturaCelsius() {
        return temperaturaCelsius;
    }

    public void setTemperaturaCelsius(double temperaturaCelsius) {
        this.temperaturaCelsius = temperaturaCelsius;
    }

    public double getTemperaturaFahrenheit() {
        return temperaturaFahrenheit;
    }

    public void setTemperaturaFahrenheit(double temperaturaFahrenheit) {
        this.temperaturaFahrenheit = temperaturaFahrenheit;
    }

    public double getUmidade() {
        return umidade;
    }

    public void setUmidade(double umidade) {
        this.umidade = umidade;
    }

    public String getDataHora() {
        return dataHora;
    }

    public void setDataHora(String dataHora) {
        this.dataHora = dataHora;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }

    public boolean isFallback() {
        return fallback;
    }

    public void setFallback(boolean fallback) {
        this.fallback = fallback;
    }

    // Adicione @JsonIgnore ao campo ou método que deseja ocultar
    @JsonIgnore
    public LocalDateTime getDataHoraAsLocalDateTime() {
        if (this.dataHora == null || this.dataHora.isEmpty()) {
            throw new IllegalArgumentException("Campo 'data_hora' não pode ser nulo ou vazio");
        }
        try {
            return LocalDateTime.parse(this.dataHora, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato de data inválido para 'data_hora': " + this.dataHora);
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "SensorDataDTO{" +
                "temperaturaCelsius=" + temperaturaCelsius +
                ", temperaturaFahrenheit=" + temperaturaFahrenheit +
                ", umidade=" + umidade +
                ", dataHora='" + dataHora + '\'' +
                '}';
    }
}
