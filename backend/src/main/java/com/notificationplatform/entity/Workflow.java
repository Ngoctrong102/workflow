package com.notificationplatform.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.constants.ApplicationConstants;
import com.notificationplatform.entity.enums.WorkflowStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "workflows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"executions"})
@EqualsAndHashCode(exclude = {"executions"})
@Slf4j
public class Workflow {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "definition", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private Map<String, Object> definition; // JSON structure for workflow nodes and edges

    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    @NotNull
    private WorkflowStatus status = WorkflowStatus.DRAFT;

    @Column(name = "version", nullable = false)
    @NotNull
    private Integer version = ApplicationConstants.Defaults.WORKFLOW_VERSION;

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private List<String> tags = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "workflow")
    private List<Execution> executions = new ArrayList<>();

    @OneToOne(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private WorkflowReport workflowReport;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Custom setter for definition to handle different input types
    public void setDefinition(Object definition) {
        if (definition == null) {
            this.definition = null;
            return;
        }
        
        // If it's already a Map, use it directly
        if (definition instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) definition;
            this.definition = map;
            return;
        }
        
        // If it's a String (JSON), parse it
        if (definition instanceof String) {
            try {
                this.definition = objectMapper.readValue((String) definition, Map.class);
            } catch (JsonProcessingException e) {
                log.error("Error parsing definition JSON string", e);
                throw new IllegalArgumentException("Invalid JSON definition: " + e.getMessage(), e);
            }
            return;
        }
        
        // Otherwise, convert to Map using ObjectMapper
        try {
            this.definition = objectMapper.convertValue(definition, Map.class);
        } catch (Exception e) {
            log.error("Error converting definition to Map", e);
            throw new IllegalArgumentException("Cannot convert definition to Map: " + e.getMessage(), e);
        }
    }
}

