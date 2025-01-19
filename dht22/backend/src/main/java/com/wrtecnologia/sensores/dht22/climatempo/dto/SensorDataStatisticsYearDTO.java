package com.wrtecnologia.sensores.dht22.climatempo.dto;

public class SensorDataStatisticsYearDTO {
    private String mesAno;
    private String datahoraTemperaturaMinima;
    private String temperaturaMinima;
    private String datahoraTemperaturaMaxima;
    private String temperaturaMaxima;
    private String variacaoTemperatura;
    private String datahoraUmidadeMinima;
    private String umidadeMinima;
    private String datahoraUmidadeMaxima;
    private String umidadeMaxima;
    private String variacaoUmidade;

    // Construtor com todos os parâmetros necessários
    public SensorDataStatisticsYearDTO(String mesAno, String datahoraTemperaturaMinima, String temperaturaMinima, String datahoraTemperaturaMaxima,
                                       String temperaturaMaxima, String variacaoTemperatura, String datahoraUmidadeMinima,
                                       String umidadeMinima, String datahoraUmidadeMaxima, String umidadeMaxima,
                                       String variacaoUmidade) {
        this.mesAno = mesAno;
        this.datahoraTemperaturaMinima = datahoraTemperaturaMinima;
        this.temperaturaMinima = temperaturaMinima;
        this.datahoraTemperaturaMaxima = datahoraTemperaturaMaxima;
        this.temperaturaMaxima = temperaturaMaxima;
        this.variacaoTemperatura = variacaoTemperatura;
        this.datahoraUmidadeMinima = datahoraUmidadeMinima;
        this.umidadeMinima = umidadeMinima;
        this.datahoraUmidadeMaxima = datahoraUmidadeMaxima;
        this.umidadeMaxima = umidadeMaxima;
        this.variacaoUmidade = variacaoUmidade;
    }

    // Getters and Setters
    public String getMesAno() {
        return mesAno;
    }

    public void setMesAno(String mesAno) {
        this.mesAno = mesAno;
    }

    public String getDatahoraTemperaturaMinima() {
        return datahoraTemperaturaMinima;
    }

    public void setDatahoraTemperaturaMinima(String datahoraTemperaturaMinima) {
        this.datahoraTemperaturaMinima = datahoraTemperaturaMinima;
    }

    public String getTemperaturaMinima() {
        return temperaturaMinima;
    }

    public void setTemperaturaMinima(String temperaturaMinima) {
        this.temperaturaMinima = temperaturaMinima;
    }

    public String getDatahoraTemperaturaMaxima() {
        return datahoraTemperaturaMaxima;
    }

    public void setDatahoraTemperaturaMaxima(String datahoraTemperaturaMaxima) {
        this.datahoraTemperaturaMaxima = datahoraTemperaturaMaxima;
    }

    public String getTemperaturaMaxima() {
        return temperaturaMaxima;
    }

    public void setTemperaturaMaxima(String temperaturaMaxima) {
        this.temperaturaMaxima = temperaturaMaxima;
    }

    public String getVariacaoTemperatura() {
        return variacaoTemperatura;
    }

    public void setVariacaoTemperatura(String variacaoTemperatura) {
        this.variacaoTemperatura = variacaoTemperatura;
    }

    public String getDatahoraUmidadeMinima() {
        return datahoraUmidadeMinima;
    }

    public void setDatahoraUmidadeMinima(String datahoraUmidadeMinima) {
        this.datahoraUmidadeMinima = datahoraUmidadeMinima;
    }

    public String getUmidadeMinima() {
        return umidadeMinima;
    }

    public void setUmidadeMinima(String umidadeMinima) {
        this.umidadeMinima = umidadeMinima;
    }

    public String getDatahoraUmidadeMaxima() {
        return datahoraUmidadeMaxima;
    }

    public void setDatahoraUmidadeMaxima(String datahoraUmidadeMaxima) {
        this.datahoraUmidadeMaxima = datahoraUmidadeMaxima;
    }

    public String getUmidadeMaxima() {
        return umidadeMaxima;
    }

    public void setUmidadeMaxima(String umidadeMaxima) {
        this.umidadeMaxima = umidadeMaxima;
    }

    public String getVariacaoUmidade() {
        return variacaoUmidade;
    }

    public void setVariacaoUmidade(String variacaoUmidade) {
        this.variacaoUmidade = variacaoUmidade;
    }
}
