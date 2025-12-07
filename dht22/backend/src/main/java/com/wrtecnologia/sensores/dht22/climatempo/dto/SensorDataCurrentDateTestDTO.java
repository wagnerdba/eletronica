package com.wrtecnologia.sensores.dht22.climatempo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorDataCurrentDateTestDTO {
    private String tableDate;
    private String currentDate;
    private String currentTime;

}
