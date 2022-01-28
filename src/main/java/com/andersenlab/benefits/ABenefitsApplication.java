package com.andersenlab.benefits;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@OpenAPIDefinition
@SpringBootApplication
public class ABenefitsApplication{

    public static void main(String[] args) {
        SpringApplication.run(ABenefitsApplication.class, args);
    }

}
