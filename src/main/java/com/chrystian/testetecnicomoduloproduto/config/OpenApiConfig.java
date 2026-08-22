package com.chrystian.testetecnicomoduloproduto.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gerenciamento de Produtos")
                        .description("Sistema de gerenciamento de produtos com recursos de CRUD, " +
                                "venda e reposição de estoque")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Chrystian Miguel Dos santos")
                                .email("chrystianniguel@hotmail.com")));
    }
}

