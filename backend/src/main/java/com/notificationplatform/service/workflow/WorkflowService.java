package com.notificationplatform.service.workflow;

import com.notificationplatform.dto.request.CreateWorkflowRequest;
import com.notificationplatform.dto.request.ExecuteWorkflowRequest;
import com.notificationplatform.dto.request.UpdateWorkflowRequest;
import com.notificationplatform.dto.response.ExecutionResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.WorkflowResponse;

import java.util.List;

public interface WorkflowService {

    WorkflowResponse createWorkflow(CreateWorkflowRequest request);

    WorkflowResponse getWorkflowById(String id);

    PagedResponse<WorkflowResponse> listWorkflows(String status, String search, int limit, int offset);

    WorkflowResponse updateWorkflow(String id, UpdateWorkflowRequest request);

    void deleteWorkflow(String id);

    void validateWorkflow(com.notificationplatform.entity.Workflow workflow);

    ExecutionResponse executeWorkflow(String workflowId, ExecuteWorkflowRequest request);

    List<WorkflowResponse> getWorkflowVersions(String id);

    WorkflowResponse getWorkflowByVersion(String id, Integer version);

    WorkflowResponse activateWorkflow(String id);

    WorkflowResponse deactivateWorkflow(String id);

    WorkflowResponse pauseWorkflow(String id);

    WorkflowResponse resumeWorkflow(String id);

    WorkflowResponse rollbackWorkflow(String id, Integer version);
}

