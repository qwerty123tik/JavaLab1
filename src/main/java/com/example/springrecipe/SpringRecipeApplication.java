package com.example.springrecipe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringRecipeApplication {

    public static void main(String[] args) {

        SpringApplication.run(SpringRecipeApplication.class, args);
    }

}

