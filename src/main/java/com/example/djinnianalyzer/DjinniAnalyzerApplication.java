package com.example.djinnianalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DjinniAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DjinniAnalyzerApplication.class, args);
    }
}