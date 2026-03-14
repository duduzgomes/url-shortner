package com.eduardogomes.shortner_url.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("URL Shortener API")
                .description("API para encurtar e redirecionar URLs")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Eduardo Gomes")
                    .email("carlosgomesduduz@gmail.com")
                )
            )
            .addServersItem(new Server()
                .url("http://localhost:80")
                .description("Load Balancer")
            );
    }
}
