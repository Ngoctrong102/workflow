package com.notificationplatform.config;

import org.springframework.context.annotation.Configuration;

/**
 * Engine configuration.
 * Node executors are now automatically registered via NodeExecutorRegistry
 * using Spring's List injection (Strategy Pattern).
 */
@Configuration
public class EngineConfig {
    // Node executors are now automatically registered via NodeExecutorRegistry
    // No manual registration needed
}

