package com.notificationplatform.service.registry;

import com.notificationplatform.dto.response.TriggerDefinition;
import com.notificationplatform.entity.enums.TriggerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Trigger Registry Service.
 * Trigger definitions are hardcoded in code (not stored in database).
 */
@Service
@Slf4j
public class TriggerRegistryServiceImpl implements TriggerRegistryService {

    private final Map<String, TriggerDefinition> triggerDefinitions = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing Trigger Registry with default trigger definitions");
        
        // API Call Trigger
        TriggerDefinition apiTrigger = TriggerDefinition.builder()
                .id("api-trigger-standard")
                .name("API Call Trigger")
                .type(TriggerType.API_CALL)
                .description("Receives HTTP request to start workflow")
                .configTemplate(createApiTriggerConfigTemplate())
                .metadata(createApiTriggerMetadata())
                .build();
        triggerDefinitions.put(apiTrigger.getId(), apiTrigger);

        // Scheduler Trigger
        TriggerDefinition schedulerTrigger = TriggerDefinition.builder()
                .id("scheduler-trigger-standard")
                .name("Scheduler Trigger")
                .type(TriggerType.SCHEDULER)
                .description("Cron-based scheduled execution")
                .configTemplate(createSchedulerTriggerConfigTemplate())
                .metadata(createSchedulerTriggerMetadata())
                .build();
        triggerDefinitions.put(schedulerTrigger.getId(), schedulerTrigger);

        // Kafka Event Trigger
        TriggerDefinition kafkaEventTrigger = TriggerDefinition.builder()
                .id("kafka-event-trigger-standard")
                .name("Kafka Event Trigger")
                .type(TriggerType.EVENT)
                .description("Listens to Kafka topic events")
                .configTemplate(createKafkaEventTriggerConfigTemplate())
                .metadata(createKafkaEventTriggerMetadata())
                .build();
        triggerDefinitions.put(kafkaEventTrigger.getId(), kafkaEventTrigger);

        log.info("Trigger Registry initialized with {} trigger definitions", triggerDefinitions.size());
    }

    @Override
    public List<TriggerDefinition> getAllTriggers() {
        log.debug("Getting all trigger definitions from registry");
        return new ArrayList<>(triggerDefinitions.values());
    }

    @Override
    public Optional<TriggerDefinition> getTriggerById(String id) {
        log.debug("Getting trigger definition by ID: {}", id);
        return Optional.ofNullable(triggerDefinitions.get(id));
    }

    @Override
    public List<TriggerDefinition> getTriggerByType(TriggerType type) {
        log.debug("Getting trigger definitions by type: {}", type);
        return triggerDefinitions.values().stream()
                .filter(trigger -> trigger.getType() == type)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getConfigTemplate(String id) {
        log.debug("Getting config template for trigger: {}", id);
        TriggerDefinition trigger = triggerDefinitions.get(id);
        if (trigger == null) {
            return Collections.emptyMap();
        }
        return trigger.getConfigTemplate();
    }

    // Helper methods to create config templates

    private Map<String, Object> createApiTriggerConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("endpointPath", "/api/v1/trigger/{workflowId}");
        template.put("httpMethod", "POST");
        
        Map<String, Object> authentication = new HashMap<>();
        authentication.put("type", "api-key|bearer-token|none");
        template.put("authentication", authentication);
        
        Map<String, Object> requestSchema = new HashMap<>();
        requestSchema.put("fields", Collections.emptyList());
        template.put("requestSchema", requestSchema);
        
        return template;
    }

    private Map<String, Object> createSchedulerTriggerConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("cronExpression", "0 9 * * *");
        template.put("timezone", "UTC");
        template.put("startDate", null);
        template.put("endDate", null);
        template.put("repeat", true);
        template.put("data", Collections.emptyMap());
        return template;
    }

    private Map<String, Object> createKafkaEventTriggerConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        
        Map<String, Object> kafka = new HashMap<>();
        kafka.put("brokers", Arrays.asList("localhost:9092"));
        kafka.put("topic", "");
        kafka.put("consumerGroup", "workflow-consumer-group");
        kafka.put("offset", "latest");
        template.put("kafka", kafka);
        
        template.put("schemas", Collections.emptyList());
        
        Map<String, Object> kafkaConnect = new HashMap<>();
        kafkaConnect.put("enabled", false);
        kafkaConnect.put("schemaRegistryUrl", null);
        kafkaConnect.put("subject", null);
        template.put("kafkaConnect", kafkaConnect);
        
        return template;
    }

    private Map<String, Object> createApiTriggerMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("icon", "api-trigger");
        metadata.put("color", "#0ea5e9");
        metadata.put("version", "1.0.0");
        return metadata;
    }

    private Map<String, Object> createSchedulerTriggerMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("icon", "schedule-trigger");
        metadata.put("color", "#0ea5e9");
        metadata.put("version", "1.0.0");
        return metadata;
    }

    private Map<String, Object> createKafkaEventTriggerMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("icon", "event-trigger");
        metadata.put("color", "#0ea5e9");
        metadata.put("version", "1.0.0");
        return metadata;
    }
}

