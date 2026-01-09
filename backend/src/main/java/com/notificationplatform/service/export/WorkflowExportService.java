package com.notificationplatform.service.export;

import com.notificationplatform.dto.response.WorkflowExportResponse;

import java.util.List;

/**
 * Service for exporting and importing workflows
 */
public interface WorkflowExportService {

    /**
     * Export workflow to JSON format
     */
    WorkflowExportResponse exportWorkflow(String workflowId);

    /**
     * Export multiple workflows to JSON format
     */
    WorkflowExportResponse exportWorkflows(List<String> workflowIds);

    /**
     * Import workflow from JSON format
     */
    ImportResult importWorkflow(String jsonContent, ImportOptions options);

    /**
     * Import multiple workflows from JSON format
     */
    List<ImportResult> importWorkflows(String jsonContent, ImportOptions options);

    class ImportOptions {
        private boolean overwriteExisting = false;
        private boolean skipValidation = false;
        private String conflictResolution = "skip"; // skip, overwrite, rename

        public boolean isOverwriteExisting() {
            return overwriteExisting;
        }

        public void setOverwriteExisting(boolean overwriteExisting) {
            this.overwriteExisting = overwriteExisting;
        }

        public boolean isSkipValidation() {
            return skipValidation;
        }

        public void setSkipValidation(boolean skipValidation) {
            this.skipValidation = skipValidation;
        }

        public String getConflictResolution() {
            return conflictResolution;
        }

        public void setConflictResolution(String conflictResolution) {
            this.conflictResolution = conflictResolution;
        }
    }

    class ImportResult {
        private String workflowId;
        private String workflowName;
        private boolean success;
        private String message;
        private String action; // created, updated, skipped

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }

        public String getWorkflowName() {
            return workflowName;
        }

        public void setWorkflowName(String workflowName) {
            this.workflowName = workflowName;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}

