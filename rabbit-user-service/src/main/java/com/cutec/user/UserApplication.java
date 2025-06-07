package com.cutec.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.cutec")
public class UserApplication {

    // http://192.168.31.22:8080/swagger-ui/index.html#/
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
