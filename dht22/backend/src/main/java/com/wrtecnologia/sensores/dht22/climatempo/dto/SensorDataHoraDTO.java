package com.wrtecnologia.sensores.dht22.climatempo.dto;

public class SensorDataHoraDTO {
    private String hora;
    private String temperaturaCelsius;
    private String umidade;

    // Getters e Setters
    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getTemperaturaCelsius() {
        return temperaturaCelsius;
    }

    public void setTemperaturaCelsius(String temperaturaCelsius) {
        this.temperaturaCelsius = temperaturaCelsius;
    }

    public String getUmidade() {
        return umidade;
    }

    public void setUmidade(String umidade) {
        this.umidade = umidade;
    }
}

