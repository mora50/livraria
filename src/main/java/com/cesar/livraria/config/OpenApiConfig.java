package com.cesar.livraria.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI livrariaOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Livraria API")
                                                .description("API REST para gerenciamento do catálogo de livros.")
                                                .version("v1")
                                                .contact(new Contact()
                                                                .name("César Augusto")
                                                                .url("https://github.com/cesaraugusto"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
        }
}
