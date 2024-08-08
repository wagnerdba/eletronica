package com.wrtecnologia.sensores.dht22.climatempo.config;

import jakarta.persistence.ConstructorResult;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.SqlResultSetMapping;
import com.wrtecnologia.sensores.dht22.climatempo.dto.SensorDataStatisticsDTO;

@SqlResultSetMapping(
        name = "SensorDataStatisticsMapping",
        classes = {
                @ConstructorResult(
                        targetClass = SensorDataStatisticsDTO.class,
                        columns = {
                                @ColumnResult(name = "datahora_temperatura_minima", type = String.class),
                                @ColumnResult(name = "temperatura_minima", type = String.class),
                                @ColumnResult(name = "datahora_temperatura_maxima", type = String.class),
                                @ColumnResult(name = "temperatura_maxima", type = String.class),
                                @ColumnResult(name = "variacao_temperatura", type = String.class),
                                @ColumnResult(name = "datahora_umidade_minima", type = String.class),
                                @ColumnResult(name = "umidade_minima", type = String.class),
                                @ColumnResult(name = "datahora_umidade_maxima", type = String.class),
                                @ColumnResult(name = "umidade_maxima", type = String.class),
                                @ColumnResult(name = "variacao_umidade", type = String.class)
                        }
                )
        }
)
public class SensorDataStatisticsMapping {
    // Esta classe é usada apenas para definir o mapeamento
}