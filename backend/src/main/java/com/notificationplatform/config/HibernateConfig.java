package com.notificationplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.hibernate.type.format.FormatMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Hibernate configuration for JSON serialization
 * This ensures Hibernate uses Jackson ObjectMapper for JSON fields
 */
@Configuration
public class HibernateConfig {

    @Bean
    public FormatMapper jsonFormatMapper(ObjectMapper objectMapper) {
        return new JacksonJsonFormatMapper(objectMapper);
    }
}

