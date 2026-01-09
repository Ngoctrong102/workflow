package com.notificationplatform.service.file;

import com.notificationplatform.entity.Execution;
import com.notificationplatform.entity.FileUpload;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.engine.WorkflowExecutor;
import com.notificationplatform.repository.FileUploadRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
/**
 * Service for handling file uploads and processing
 */
@Slf4j
@Service
@Transactional
public class FileUploadService {

    private final FileUploadRepository fileUploadRepository;
    private final FileProcessor fileProcessor;
    private final WorkflowExecutor workflowExecutor;

    public FileUploadService(FileUploadRepository fileUploadRepository,
                            FileProcessor fileProcessor,
                            WorkflowExecutor workflowExecutor) {
        this.fileUploadRepository = fileUploadRepository;
        this.fileProcessor = fileProcessor;
        this.workflowExecutor = workflowExecutor;
    }

    /**
     * Process uploaded file and trigger workflows
     */
    public FileUpload processFile(Trigger trigger, MultipartFile file) throws IOException {
        log.info("Processing file upload: triggerId={}, filename={}", trigger.getId(), file.getOriginalFilename());

        // Create file upload record
        FileUpload fileUpload = new FileUpload();
        fileUpload.setId(UUID.randomUUID().toString());
        fileUpload.setTrigger(trigger);
        fileUpload.setFilename(file.getOriginalFilename());
        fileUpload.setFileSize(file.getSize());
        fileUpload.setFileType(getFileType(file.getOriginalFilename()));
        fileUpload.setFilePath(""); // Will be set if file storage is implemented
        fileUpload.setStatus("processing");
        fileUpload = fileUploadRepository.save(fileUpload);

        try {
            // Validate file
            validateFile(trigger, file);

            // Parse file based on format
            List<Map<String, Object>> rows = parseFile(file);

            // Get workflow
            Workflow workflow = trigger.getWorkflow();
            if (workflow == null) {
                throw new IllegalStateException("Trigger has no associated workflow");
            }

            // Get data mapping
            Map<String, Object> config = trigger.getConfig() != null ? trigger.getConfig() : new HashMap<>();
            Map<String, String> dataMapping = (Map<String, String>) config.get("dataMapping");
            String processingMode = (String) config.getOrDefault("processingMode", "batch");

            // Process rows
            int processedCount = 0;
            int errorCount = 0;
            List<String> errors = new ArrayList<>();

            if ("batch".equals(processingMode)) {
                // Process each row separately
                for (Map<String, Object> row : rows) {
                    try {
                        // Map data
                        Map<String, Object> mappedData = mapData(row, dataMapping);

                        // Execute workflow
                        Execution execution = workflowExecutor.execute(workflow, mappedData, trigger.getId());
                        processedCount++;
                    } catch (Exception e) {
                        errorCount++;
                        errors.add("Row " + processedCount + ": " + e.getMessage());
                        log.error("Error processing row", e);
                    }
                }
            } else {
                // Aggregate mode - process all rows together
                try {
                    Map<String, Object> aggregatedData = new HashMap<>();
                    aggregatedData.put("rows", rows);
                    aggregatedData.put("count", rows.size());
                    
                    if (dataMapping != null) {
                        aggregatedData.putAll(dataMapping);
                    }

                    Execution execution = workflowExecutor.execute(workflow, aggregatedData, trigger.getId());
                    processedCount = rows.size();
                } catch (Exception e) {
                    errorCount = rows.size();
                    errors.add("Aggregate processing error: " + e.getMessage());
                    log.error("Error processing aggregate", e);
                }
            }

            // Update file upload status
            fileUpload.setStatus("completed");
            fileUpload.setRowsProcessed(processedCount);
            fileUpload.setRowsTotal(rows.size());
            fileUpload.setError(errors.isEmpty() ? null : String.join("; ", errors));
            fileUpload.setCompletedAt(LocalDateTime.now());
            fileUpload = fileUploadRepository.save(fileUpload);

            log.info("File processing completed: fileUploadId={}, processed={}, errors={}", 
                       fileUpload.getId(), processedCount, errorCount);

        } catch (Exception e) {
            log.error("Error processing file: fileUploadId={}", fileUpload.getId(), e);
            fileUpload.setStatus("failed");
            fileUpload.setError(e.getMessage());
            fileUpload.setCompletedAt(LocalDateTime.now());
            fileUpload = fileUploadRepository.save(fileUpload);
        }

        return fileUpload;
    }

    private void validateFile(Trigger trigger, MultipartFile file) {
        // Get validation rules from trigger config
        Map<String, Object> config = trigger.getConfig() != null ? (Map<String, Object>) trigger.getConfig() : new HashMap<>();
        
        List<String> allowedFormats = (List<String>) config.get("fileFormats");
        Long maxFileSize = config.containsKey("maxFileSize") ? 
            ((Number) config.get("maxFileSize")).longValue() : 10485760L; // 10MB default

        // Validate file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize);
        }

        // Validate file format
        if (allowedFormats != null && !allowedFormats.isEmpty()) {
            String fileName = file.getOriginalFilename();
            String extension = fileName != null && fileName.contains(".") ? 
                fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";
            
            if (!allowedFormats.contains(extension)) {
                throw new IllegalArgumentException("File format not allowed. Allowed formats: " + allowedFormats);
            }
        }
    }

    private List<Map<String, Object>> parseFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        String extension = fileName != null && fileName.contains(".") ? 
            fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase() : "";

        try (InputStream inputStream = file.getInputStream()) {
            switch (extension) {
                case "csv":
                    return fileProcessor.parseCsv(inputStream);
                case "json":
                    return fileProcessor.parseJson(inputStream);
                case "jsonl":
                    return fileProcessor.parseJsonl(inputStream);
                case "xlsx":
                case "xls":
                    return fileProcessor.parseExcel(inputStream, extension);
                default:
                    throw new IllegalArgumentException("Unsupported file format: " + extension);
            }
        }
    }

    private Map<String, Object> mapData(Map<String, Object> row, Map<String, String> dataMapping) {
        if (dataMapping == null || dataMapping.isEmpty()) {
            return row; // No mapping, return as-is
        }

        Map<String, Object> mapped = new HashMap<>();
        for (Map.Entry<String, String> entry : dataMapping.entrySet()) {
            String targetKey = entry.getKey();
            String sourceKey = entry.getValue();
            
            Object value = row.get(sourceKey);
            if (value != null) {
                mapped.put(targetKey, value);
            }
        }
        
        // Also include original row data
        mapped.put("_original", row);
        
        return mapped;
    }

    /**
     * Get file upload by ID
     */
    @Transactional(readOnly = true)
    public FileUpload getFileUpload(String fileId) {
        return fileUploadRepository.findById(fileId).orElse(null);
    }

    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "csv":
                return "csv";
            case "json":
                return "json";
            case "jsonl":
                return "jsonl";
            case "xlsx":
            case "xls":
                return "excel";
            default:
                return extension;
        }
    }
}

