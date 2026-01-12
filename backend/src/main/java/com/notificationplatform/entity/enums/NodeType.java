package com.notificationplatform.entity.enums;

/**
 * Enum representing node types in workflow definitions.
 * 
 * According to documentation, there are 3 main node categories:
 * - TRIGGER: Entry points that start workflow execution (api-call, scheduler, event subtypes)
 * - LOGIC: Control flow and conditional logic (condition, switch, loop, delay, merge, wait-events subtypes)
 * - ACTION: Operations and side effects (api-call, publish-event, function, custom-action subtypes)
 * 
 * Subtypes (e.g., api-call, scheduler, condition, send-email) are stored in node.data.config.subtype
 * or node.data.config.registryId, not as separate enum values.
 * 
 * See: @import(features/node-types.md)
 */
public enum NodeType {
    TRIGGER,
    LOGIC,
    ACTION;
}

