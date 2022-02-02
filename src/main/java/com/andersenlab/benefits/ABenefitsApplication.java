package com.andersenlab.benefits;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "A-Benefits API",
        version = "1.0",
        description = "Information about benefits for employees",
        contact = @Contact(name = "Address of project in wiki",
                url = "https://wiki.andersenlab.com/pages/viewpage.action?pageId=187378072")))
public class ABenefitsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ABenefitsApplication.class, args);
    }

}
