package com.notificationplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI notificationPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("No-Code Notification Platform API")
                        .description("RESTful API for managing workflows, templates, channels, triggers, notifications, and analytics")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("API Support")
                                .email("support@notificationplatform.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.notificationplatform.com")
                                .description("Production Server")
                ));
    }
}

