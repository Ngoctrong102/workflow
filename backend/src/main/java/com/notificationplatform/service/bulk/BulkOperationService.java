package com.notificationplatform.service.bulk;

import com.notificationplatform.dto.response.BulkOperationResult;

import java.util.List;

/**
 * Service for bulk operations on workflows and templates
 */
public interface BulkOperationService {

    /**
     * Bulk delete workflows
     */
    BulkOperationResult bulkDeleteWorkflows(List<String> workflowIds);

    /**
     * Bulk update workflow status
     */
    BulkOperationResult bulkUpdateWorkflowStatus(List<String> workflowIds, String status);

    /**
     * Bulk delete templates
     */
    BulkOperationResult bulkDeleteTemplates(List<String> templateIds);

    /**
     * Bulk update template status
     */
    BulkOperationResult bulkUpdateTemplateStatus(List<String> templateIds, String status);
}

