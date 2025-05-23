package com.example.daehyunbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DaehyunBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DaehyunBackendApplication.class, args);
    }

}