package com.notificationplatform.service.dashboard;

import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowChannelPerformanceDTO;
import com.notificationplatform.dto.response.WorkflowDashboardDTO;
import com.notificationplatform.dto.response.WorkflowErrorAnalysisDTO;
import com.notificationplatform.dto.response.WorkflowExecutionTrendDTO;
import com.notificationplatform.dto.response.WorkflowNodePerformanceDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkflowDashboardService {

    WorkflowDashboardDTO getDashboardOverview(String workflowId, LocalDateTime startDate, 
                                             LocalDateTime endDate, String timezone);

    List<WorkflowExecutionTrendDTO> getExecutionTrends(String workflowId, LocalDateTime startDate,
                                                       LocalDateTime endDate, String granularity);

    List<WorkflowNodePerformanceDTO> getNodePerformance(String workflowId, LocalDateTime startDate,
                                                        LocalDateTime endDate);

    List<WorkflowChannelPerformanceDTO> getChannelPerformance(String workflowId, LocalDateTime startDate,
                                                              LocalDateTime endDate);

    PagedResponse<com.notificationplatform.dto.response.ExecutionStatusResponse> getExecutionHistory(
            String workflowId, String status, LocalDateTime startDate, LocalDateTime endDate,
            String triggerType, int limit, int offset);

    PagedResponse<WorkflowErrorAnalysisDTO> getErrorAnalysis(String workflowId, LocalDateTime startDate,
                                                            LocalDateTime endDate, String errorType,
                                                            int limit, int offset);
}

