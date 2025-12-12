package com.wrtecnologia.sensores.dht22.climatempo.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sensor_data")
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", unique = true, updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "temperatura_celsius")
    private double temperaturaCelsius;

    @Column(name = "temperatura_fahrenheit")
    private double temperaturaFahrenheit;

    @Column(name = "umidade")
    private double umidade;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    @Column(name = "uptime", length = 8)
    private String uptime;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getUptime() {
        return uptime;
    }

    public void setUptime(String uptime) {
        this.uptime = uptime;
    }


    @Override
    public String toString() {
        return "SensorData{" +
                "temperaturaCelsius=" + temperaturaCelsius +
                ", temperaturaFahrenheit=" + temperaturaFahrenheit +
                ", umidade=" + umidade +
                ", dataHora=" + dataHora +
                '}';
    }
}
