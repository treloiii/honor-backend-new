package com.trelloiii.honor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class HonorBackNewApplication {

    public static void main(String[] args) {
        SpringApplication.run(HonorBackNewApplication.class, args);
    }

}
