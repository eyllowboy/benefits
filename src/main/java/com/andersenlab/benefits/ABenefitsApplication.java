package com.andersenlab.benefits;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "A-Benefits API",
        version = "1.0",
        description = "Information about benefits for employees",
        contact = @Contact(name = "Address of project in wiki",
                url = "https://wiki.andersenlab.com/pages/viewpage.action?pageId=187378072")))
@SecurityScheme(name = "benefits",
        scheme = "basic",
        in = SecuritySchemeIn.HEADER,
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode =
        @OAuthFlow(authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
                tokenUrl = "${springdoc.oAuthFlow.tokenUrl}"
        ))
)
public class ABenefitsApplication {

    public static void main(final String[] args) {
        SpringApplication.run(ABenefitsApplication.class, args);
    }



}
