package com.notificationplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.dto.response.BulkOperationResult;
import com.notificationplatform.dto.response.TemplateExportResponse;
import com.notificationplatform.dto.response.WorkflowExportResponse;
import com.notificationplatform.service.bulk.BulkOperationService;
import com.notificationplatform.service.export.TemplateExportService;
import com.notificationplatform.service.export.WorkflowExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/export-import")
public class ExportImportController {

    private final WorkflowExportService workflowExportService;
    private final TemplateExportService templateExportService;
    private final BulkOperationService bulkOperationService;
    private final ObjectMapper objectMapper;

    public ExportImportController(WorkflowExportService workflowExportService,
                                 TemplateExportService templateExportService,
                                 BulkOperationService bulkOperationService,
                                 ObjectMapper objectMapper) {
        this.workflowExportService = workflowExportService;
        this.templateExportService = templateExportService;
        this.bulkOperationService = bulkOperationService;
        this.objectMapper = objectMapper;
    }

    // Workflow Export
    @GetMapping("/workflows/{id}/export")
    public ResponseEntity<String> exportWorkflow(@PathVariable String id) throws IOException {
        WorkflowExportResponse response = workflowExportService.exportWorkflow(id);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "workflow-" + id + ".json");

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    @PostMapping("/workflows/export")
    public ResponseEntity<String> exportWorkflows(@RequestBody List<String> workflowIds) throws IOException {
        WorkflowExportResponse response = workflowExportService.exportWorkflows(workflowIds);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "workflows-export.json");

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    // Workflow Import
    @PostMapping("/workflows/import")
    public ResponseEntity<List<WorkflowExportService.ImportResult>> importWorkflows(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") boolean overwriteExisting,
            @RequestParam(required = false, defaultValue = "skip") String conflictResolution) throws IOException {
        String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);

        WorkflowExportService.ImportOptions options = new WorkflowExportService.ImportOptions();
        options.setOverwriteExisting(overwriteExisting);
        options.setConflictResolution(conflictResolution);

        List<WorkflowExportService.ImportResult> results = workflowExportService.importWorkflows(jsonContent, options);
        return ResponseEntity.ok(results);
    }

    // Template Export
    @GetMapping("/templates/{id}/export")
    public ResponseEntity<String> exportTemplate(@PathVariable String id) throws IOException {
        TemplateExportResponse response = templateExportService.exportTemplate(id);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "template-" + id + ".json");

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    @PostMapping("/templates/export")
    public ResponseEntity<String> exportTemplates(@RequestBody List<String> templateIds) throws IOException {
        TemplateExportResponse response = templateExportService.exportTemplates(templateIds);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "templates-export.json");

        return new ResponseEntity<>(json, headers, HttpStatus.OK);
    }

    // Template Import
    @PostMapping("/templates/import")
    public ResponseEntity<List<TemplateExportService.ImportResult>> importTemplates(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "false") boolean overwriteExisting,
            @RequestParam(required = false, defaultValue = "skip") String conflictResolution) throws IOException {
        String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);

        TemplateExportService.ImportOptions options = new TemplateExportService.ImportOptions();
        options.setOverwriteExisting(overwriteExisting);
        options.setConflictResolution(conflictResolution);

        List<TemplateExportService.ImportResult> results = templateExportService.importTemplates(jsonContent, options);
        return ResponseEntity.ok(results);
    }

    // Bulk Operations
    @PostMapping("/workflows/bulk-delete")
    public ResponseEntity<BulkOperationResult> bulkDeleteWorkflows(@RequestBody List<String> workflowIds) {
        BulkOperationResult result = bulkOperationService.bulkDeleteWorkflows(workflowIds);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/workflows/bulk-update-status")
    public ResponseEntity<BulkOperationResult> bulkUpdateWorkflowStatus(
            @RequestBody List<String> workflowIds,
            @RequestParam String status) {
        BulkOperationResult result = bulkOperationService.bulkUpdateWorkflowStatus(workflowIds, status);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/templates/bulk-delete")
    public ResponseEntity<BulkOperationResult> bulkDeleteTemplates(@RequestBody List<String> templateIds) {
        BulkOperationResult result = bulkOperationService.bulkDeleteTemplates(templateIds);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/templates/bulk-update-status")
    public ResponseEntity<BulkOperationResult> bulkUpdateTemplateStatus(
            @RequestBody List<String> templateIds,
            @RequestParam String status) {
        BulkOperationResult result = bulkOperationService.bulkUpdateTemplateStatus(templateIds, status);
        return ResponseEntity.ok(result);
    }
}

