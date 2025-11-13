package com.netly.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NetlyApplication {
    public static void main(String[] args) {
        SpringApplication.run(NetlyApplication.class, args);
    }
}
