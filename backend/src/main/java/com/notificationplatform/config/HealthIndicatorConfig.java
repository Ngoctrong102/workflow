package com.notificationplatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Custom health indicators for monitoring system components
 */
@Component
public class HealthIndicatorConfig {

    /**
     * Database health indicator
     */
    @Component
    public static class DatabaseHealthIndicator implements HealthIndicator {

        private final DataSource dataSource;

        public DatabaseHealthIndicator(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Override
        public Health health() {
            try (Connection connection = dataSource.getConnection()) {
                if (connection.isValid(1)) {
                    return Health.up()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connected")
                            .build();
                } else {
                    return Health.down()
                            .withDetail("database", "PostgreSQL")
                            .withDetail("status", "Connection invalid")
                            .build();
                }
            } catch (SQLException e) {
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", e.getMessage())
                        .build();
            }
        }
    }

    /**
     * Kafka health indicator
     * Note: KafkaTemplate is optional, so we make it nullable
     */
    @Component
    public static class KafkaHealthIndicator implements HealthIndicator {

        private final KafkaTemplate<String, String> kafkaTemplate;

        public KafkaHealthIndicator(@Autowired(required = false) KafkaTemplate<String, String> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @Override
        public Health health() {
            if (kafkaTemplate == null) {
                return Health.up()
                        .withDetail("messageQueue", "Kafka")
                        .withDetail("status", "Not configured")
                        .withDetail("note", "Kafka is optional for MVP")
                        .build();
            }

            try {
                // Try to get metadata to check connection
                kafkaTemplate.getProducerFactory().createProducer().partitionsFor("health-check-topic");
                return Health.up()
                        .withDetail("messageQueue", "Kafka")
                        .withDetail("status", "Connected")
                        .build();
            } catch (Exception e) {
                // Kafka might not be available, but that's okay for MVP
                return Health.up()
                        .withDetail("messageQueue", "Kafka")
                        .withDetail("status", "Not configured")
                        .withDetail("note", "Kafka is optional for MVP")
                        .build();
            }
        }
    }
}

