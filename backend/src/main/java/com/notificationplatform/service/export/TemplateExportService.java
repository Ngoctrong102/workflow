package com.notificationplatform.service.export;

import com.notificationplatform.dto.response.TemplateExportResponse;

import java.util.List;

/**
 * Service for exporting and importing templates
 */
public interface TemplateExportService {

    /**
     * Export template to JSON format
     */
    TemplateExportResponse exportTemplate(String templateId);

    /**
     * Export multiple templates to JSON format
     */
    TemplateExportResponse exportTemplates(List<String> templateIds);

    /**
     * Import template from JSON format
     */
    ImportResult importTemplate(String jsonContent, ImportOptions options);

    /**
     * Import multiple templates from JSON format
     */
    List<ImportResult> importTemplates(String jsonContent, ImportOptions options);

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
        private String templateId;
        private String templateName;
        private boolean success;
        private String message;
        private String action; // created, updated, skipped

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getTemplateName() {
            return templateName;
        }

        public void setTemplateName(String templateName) {
            this.templateName = templateName;
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

