package com.notificationplatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an object type definition.
 * Object types define the structure and fields for custom objects in the system.
 */
@Entity
@Table(name = "object_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ObjectType {

    @Id
    @Column(name = "id", length = 255)
    @NotBlank
    @Size(max = 255)
    private String id;

    @Column(name = "name", nullable = false, unique = true, length = 255)
    @NotBlank
    @Size(max = 255)
    private String name;

    @Column(name = "display_name", length = 255)
    @Size(max = 255)
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "fields", nullable = false, columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    @NotNull
    private List<FieldDefinition> fields = new ArrayList<>(); // Array of field definitions

    @Column(name = "tags", columnDefinition = "TEXT[]")
    private List<String> tags = new ArrayList<>();

    @Column(name = "version", nullable = false)
    @NotNull
    private Integer version = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Custom setter for fields to handle null
    public void setFields(List<FieldDefinition> fields) {
        this.fields = fields != null ? fields : new ArrayList<>();
    }

    // Custom setter for tags to handle null
    public void setTags(List<String> tags) {
        this.tags = tags != null ? tags : new ArrayList<>();
    }
}

