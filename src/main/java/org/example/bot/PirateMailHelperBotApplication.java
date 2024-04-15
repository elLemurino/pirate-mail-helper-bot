package org.example.bot;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PirateMailHelperBotApplication {

    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(PirateMailHelperBotApplication.class, args);
    }
}
