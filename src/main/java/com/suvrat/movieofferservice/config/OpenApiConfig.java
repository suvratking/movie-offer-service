package com.suvrat.movieofferservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI movieOfferOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Movie Offer Service API")
                .description("APIs for creating, evaluating, and applying movie booking offers")
                .version("v1")
                .contact(new Contact().name("Movie Offer Team").email("support@movie-offer.local"))
        );
    }
}
