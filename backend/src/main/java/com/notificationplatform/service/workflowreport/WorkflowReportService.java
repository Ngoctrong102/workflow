package com.notificationplatform.service.workflowreport;

import com.notificationplatform.dto.request.CreateWorkflowReportRequest;
import com.notificationplatform.dto.request.UpdateWorkflowReportRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.QueryValidationResponse;
import com.notificationplatform.dto.response.WorkflowReportHistoryResponse;
import com.notificationplatform.dto.response.WorkflowReportPreviewResponse;
import com.notificationplatform.dto.response.WorkflowReportResponse;
import org.springframework.core.io.Resource;

public interface WorkflowReportService {

    WorkflowReportResponse createWorkflowReport(String workflowId, CreateWorkflowReportRequest request);

    WorkflowReportResponse getWorkflowReport(String workflowId);

    WorkflowReportResponse updateWorkflowReport(String workflowId, UpdateWorkflowReportRequest request);

    void deleteWorkflowReport(String workflowId);

    QueryValidationResponse validateQuery(String workflowId, String analystQuery);

    WorkflowReportHistoryResponse generateReport(String workflowId);

    WorkflowReportPreviewResponse previewReport(String workflowId);

    PagedResponse<WorkflowReportHistoryResponse> getReportHistory(String workflowId, int limit, int offset);

    Resource downloadReport(String workflowId, String reportId);

    WorkflowReportResponse updateReportStatus(String workflowId, String status);
}

