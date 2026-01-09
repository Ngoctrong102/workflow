package com.notificationplatform.service.execution;

import com.notificationplatform.dto.request.CancelExecutionRequest;
import com.notificationplatform.dto.request.RetryExecutionRequest;
import com.notificationplatform.dto.request.VisualizeStepRequest;
import com.notificationplatform.dto.response.ExecutionDetailResponse;
import com.notificationplatform.dto.response.ExecutionStatusResponse;
import com.notificationplatform.dto.response.ExecutionVisualizationResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.VisualizeStepResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ExecutionService {

    ExecutionStatusResponse getExecutionStatus(String executionId);

    ExecutionDetailResponse getExecutionDetail(String executionId);

    PagedResponse<ExecutionStatusResponse> listExecutions(String workflowId, String status, 
                                                           LocalDateTime startDate, LocalDateTime endDate,
                                                           String search,
                                                           int limit, int offset);

    List<ExecutionDetailResponse.ExecutionLog> getExecutionLogs(String executionId, String nodeId, String level);

    Map<String, Object> getExecutionContext(String executionId);

    void cancelExecution(String executionId, CancelExecutionRequest request);

    ExecutionStatusResponse retryExecution(String executionId, RetryExecutionRequest request);

    // Visualization methods
    ExecutionVisualizationResponse getExecutionVisualization(String executionId);

    VisualizeStepResponse executeVisualizationStep(String executionId, VisualizeStepRequest request);

    ExecutionVisualizationResponse getExecutionStateAtStep(String executionId, Integer stepNumber);

    void resetVisualization(String executionId);

    Map<String, Object> getVisualizationContext(String executionId);
}

