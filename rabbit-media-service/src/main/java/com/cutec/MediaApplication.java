package com.cutec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class MediaApplication {

    // http://192.168.31.22:8080/swagger-ui/index.html#/
    public static void main(String[] args) {
        SpringApplication.run(MediaApplication.class, args);
    }

}
