package com.notificationplatform.entity;

/**
 * Enum representing the types of fields that can be defined in an object type.
 */
public enum FieldType {
    STRING("string"),
    NUMBER("number"),
    BOOLEAN("boolean"),
    DATE("date"),
    DATETIME("datetime"),
    EMAIL("email"),
    PHONE("phone"),
    URL("url"),
    JSON("json"),
    ARRAY("array"),
    OBJECT("object");

    private final String value;

    FieldType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get FieldType from string value.
     * @param value The string value
     * @return FieldType enum or null if not found
     */
    public static FieldType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (FieldType type : FieldType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}

