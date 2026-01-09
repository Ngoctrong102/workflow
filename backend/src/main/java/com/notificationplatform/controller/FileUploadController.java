package com.notificationplatform.controller;

import com.notificationplatform.dto.response.FileUploadResponse;
import com.notificationplatform.entity.FileUpload;
import com.notificationplatform.entity.Trigger;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.file.FileUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Controller for file upload endpoints
 */
@RestController
@RequestMapping("/triggers/file")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final TriggerRepository triggerRepository;

    public FileUploadController(FileUploadService fileUploadService,
                               TriggerRepository triggerRepository) {
        this.fileUploadService = fileUploadService;
        this.triggerRepository = triggerRepository;
    }

    @PostMapping("/{triggerId}/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @PathVariable String triggerId,
            @RequestParam("file") MultipartFile file) {
        
        // Find trigger
        Trigger trigger = triggerRepository.findByIdAndNotDeleted(triggerId)
                .orElseThrow(() -> new ResourceNotFoundException("Trigger not found with id: " + triggerId));

        if (trigger.getTriggerType() == null || !"file-trigger".equals(trigger.getTriggerType().getValue())) {
            return ResponseEntity.badRequest().build();
        }

        if (!"active".equals(trigger.getStatus())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            FileUpload fileUpload = fileUploadService.processFile(trigger, file);
            FileUploadResponse response = toResponse(fileUpload);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{triggerId}/uploads/{fileId}")
    public ResponseEntity<FileUploadResponse> getFileUploadStatus(
            @PathVariable String triggerId,
            @PathVariable String fileId) {
        
        // Verify trigger exists
        if (!triggerRepository.findByIdAndNotDeleted(triggerId).isPresent()) {
            throw new ResourceNotFoundException("Trigger not found with id: " + triggerId);
        }

        // Find file upload
        FileUpload fileUpload = fileUploadService.getFileUpload(fileId);
        if (fileUpload == null || !fileUpload.getTrigger().getId().equals(triggerId)) {
            throw new ResourceNotFoundException("File upload not found with id: " + fileId);
        }

        FileUploadResponse response = toResponse(fileUpload);
        return ResponseEntity.ok(response);
    }

    private FileUploadResponse toResponse(FileUpload fileUpload) {
        FileUploadResponse response = new FileUploadResponse();
        response.setId(fileUpload.getId());
        response.setFileName(fileUpload.getFilename());
        response.setFileSize(fileUpload.getFileSize());
        response.setStatus(fileUpload.getStatus());
        response.setProcessedRows(fileUpload.getRowsProcessed());
        response.setErrors(fileUpload.getRowsTotal() != null && fileUpload.getRowsProcessed() != null ? 
                fileUpload.getRowsTotal() - fileUpload.getRowsProcessed() : 0);
        response.setUploadedAt(fileUpload.getUploadedAt());
        response.setCompletedAt(fileUpload.getCompletedAt());
        // Set trigger_id if available
        if (fileUpload.getTrigger() != null) {
            response.setTriggerId(fileUpload.getTrigger().getId());
        }
        return response;
    }
}

