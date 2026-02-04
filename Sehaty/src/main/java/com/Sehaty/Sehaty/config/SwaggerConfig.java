package com.Sehaty.Sehaty.config;

import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 * Sets up the API documentation metadata.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI bean with API information.
     *
     * @return OpenAPI instance with title, description, version, and contact info.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sehaty API Documentation")
                        .description("API documentation for Sehaty system")
                        .version("1.0")
                        .contact(new Contact()
                                .name("Bassam Hassan")
                                .email("bassamhassan191@gmail.com")));
    }
}
