package com.wrtecnologia.sensores.dht22.climatempo;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.ZoneId;
import java.util.TimeZone;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class ClimatempoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClimatempoApplication.class, args);
    }

    @PostConstruct
    public void init() {

        // Setting Spring Boot SetTimeZone
        log.info(String.valueOf(ZoneId.systemDefault()));
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        log.info(String.valueOf(ZoneId.systemDefault()));

    }
}