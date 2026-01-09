package com.notificationplatform.service.visualization;

import com.notificationplatform.dto.request.VisualizeStepRequest;
import com.notificationplatform.dto.response.ExecutionVisualizationResponse;
import com.notificationplatform.dto.response.VisualizeStepResponse;

import java.util.Map;

/**
 * Service for execution visualization and step-by-step replay.
 * 
 * See: @import(features/execution-visualization.md)
 */
public interface ExecutionVisualizationService {

    /**
     * Load execution data for visualization.
     * 
     * @param executionId Execution ID
     * @return Execution visualization data
     */
    ExecutionVisualizationResponse loadExecutionForVisualization(String executionId);

    /**
     * Get execution state at specific step.
     * 
     * @param executionId Execution ID
     * @param stepNumber Step number
     * @return Execution state at step
     */
    ExecutionVisualizationResponse getExecutionStateAtStep(String executionId, Integer stepNumber);

    /**
     * Execute next step in visualization.
     * 
     * @param executionId Execution ID
     * @param request Step request with direction
     * @return Step execution result
     */
    VisualizeStepResponse executeNextStep(String executionId, VisualizeStepRequest request);

    /**
     * Reset visualization to initial state.
     * 
     * @param executionId Execution ID
     */
    void resetVisualization(String executionId);

    /**
     * Get current execution context.
     * 
     * @param executionId Execution ID
     * @return Current context
     */
    Map<String, Object> getCurrentContext(String executionId);
}

