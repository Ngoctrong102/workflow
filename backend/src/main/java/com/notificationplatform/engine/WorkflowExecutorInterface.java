package com.notificationplatform.engine;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.Workflow;

import java.util.Map;

/**
 * Interface for workflow execution engine.
 * 
 * See: @import(features/workflow-execution-state.md)
 */
public interface WorkflowExecutorInterface {

    /**
     * Execute a workflow.
     * 
     * @param workflow Workflow to execute
     * @param triggerData Data from trigger
     * @param triggerId Trigger ID that started execution
     * @return Execution entity
     */
    Execution executeWorkflow(Workflow workflow, Map<String, Object> triggerData, String triggerId);

    /**
     * Resume workflow execution from a paused state.
     * 
     * @param executionId Execution ID
     * @param nodeId Node ID to resume from
     * @param aggregatedData Aggregated data from events
     */
    void resumeExecution(String executionId, String nodeId, Map<String, Object> aggregatedData);

    /**
     * Continue execution from a specific node.
     * 
     * @param executionId Execution ID
     * @param nodeId Node ID to continue from
     */
    void continueExecution(String executionId, String nodeId);
}

