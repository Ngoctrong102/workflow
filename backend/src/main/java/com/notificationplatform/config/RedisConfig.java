package com.notificationplatform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration for caching and distributed locks
 */
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    /**
     * Create GenericJackson2JsonRedisSerializer with Java 8 date/time support.
     * Note: Cache is currently disabled, so this is not actively used.
     * When cache is re-enabled, we'll need to create a separate ObjectMapper for Redis.
     */
    @Bean
    public GenericJackson2JsonRedisSerializer redisJsonSerializer() {
        // Create a simple ObjectMapper without typing for now (cache is disabled)
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setDatabase(redisDatabase);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer redisJsonSerializer) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(redisJsonSerializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(redisJsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            GenericJackson2JsonRedisSerializer redisJsonSerializer) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisJsonSerializer))
                .disableCachingNullValues();

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withCacheConfiguration("workflows", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("templates", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("channels", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("triggers", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("workflowDashboard", config.entryTtl(Duration.ofMinutes(1)))
                .withCacheConfiguration("workflowTrends", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("workflowNodePerformance", config.entryTtl(Duration.ofMinutes(5)))
                .withCacheConfiguration("workflowChannelPerformance", config.entryTtl(Duration.ofMinutes(5)))
                .transactionAware()
                .build();
    }
}

