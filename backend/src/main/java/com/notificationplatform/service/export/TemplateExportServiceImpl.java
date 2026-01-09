package com.notificationplatform.service.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.notificationplatform.dto.response.TemplateExportResponse;
import com.notificationplatform.entity.Template;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.TemplateRepository;
import com.notificationplatform.service.template.TemplateService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class TemplateExportServiceImpl implements TemplateExportService {

    private final TemplateRepository templateRepository;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    public TemplateExportServiceImpl(TemplateRepository templateRepository,
                                    TemplateService templateService,
                                    ObjectMapper objectMapper) {
        this.templateRepository = templateRepository;
        this.templateService = templateService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateExportResponse exportTemplate(String templateId) {
        Template template = templateRepository.findByIdAndNotDeleted(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        TemplateExportResponse response = new TemplateExportResponse();
        response.setExportedAt(LocalDateTime.now());

        TemplateExportResponse.TemplateExport export = toExport(template);
        response.setTemplates(List.of(export));

        log.info("Exported template: templateId={}, name={}", templateId, template.getName());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateExportResponse exportTemplates(List<String> templateIds) {
        List<Template> templates = templateIds.stream()
                .map(id -> templateRepository.findByIdAndNotDeleted(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id)))
                .collect(Collectors.toList());

        TemplateExportResponse response = new TemplateExportResponse();
        response.setExportedAt(LocalDateTime.now());

        List<TemplateExportResponse.TemplateExport> exports = templates.stream()
                .map(this::toExport)
                .collect(Collectors.toList());

        response.setTemplates(exports);

        log.info("Exported {} templates", templates.size());

        return response;
    }

    @Override
    public ImportResult importTemplate(String jsonContent, ImportOptions options) {
        try {
            TemplateExportResponse exportResponse = objectMapper.readValue(jsonContent, TemplateExportResponse.class);

            if (exportResponse.getTemplates() == null || exportResponse.getTemplates().isEmpty()) {
                ImportResult result = new ImportResult();
                result.setSuccess(false);
                result.setMessage("No templates found in export");
                return result;
            }

            // Import first template
            return importTemplateData(exportResponse.getTemplates().get(0), options);

        } catch (Exception e) {
            log.error("Error importing template", e);
            ImportResult result = new ImportResult();
            result.setSuccess(false);
            result.setMessage("Error parsing JSON: " + e.getMessage());
            return result;
        }
    }

    @Override
    public List<ImportResult> importTemplates(String jsonContent, ImportOptions options) {
        List<ImportResult> results = new ArrayList<>();

        try {
            TemplateExportResponse exportResponse = objectMapper.readValue(jsonContent, TemplateExportResponse.class);

            if (exportResponse.getTemplates() == null || exportResponse.getTemplates().isEmpty()) {
                ImportResult result = new ImportResult();
                result.setSuccess(false);
                result.setMessage("No templates found in export");
                results.add(result);
                return results;
            }

            for (TemplateExportResponse.TemplateExport export : exportResponse.getTemplates()) {
                results.add(importTemplateData(export, options));
            }

        } catch (Exception e) {
            log.error("Error importing templates", e);
            ImportResult result = new ImportResult();
            result.setSuccess(false);
            result.setMessage("Error parsing JSON: " + e.getMessage());
            results.add(result);
        }

        return results;
    }

    private ImportResult importTemplateData(TemplateExportResponse.TemplateExport export, ImportOptions options) {
        ImportResult result = new ImportResult();
        result.setTemplateName(export.getName());

        try {
            // Validate if not skipping
            if (!options.isSkipValidation()) {
                validateTemplateExport(export);
            }

            // Check for existing template with same name and channel
            Optional<Template> existing = templateRepository.findAllActive().stream()
                    .filter(t -> export.getName().equals(t.getName()) && 
                               export.getChannel().equals(t.getChannel()))
                    .findFirst();

            if (existing.isPresent()) {
                // Handle conflict
                String resolution = options.getConflictResolution();
                if ("skip".equals(resolution)) {
                    result.setSuccess(false);
                    result.setAction("skipped");
                    result.setMessage("Template with name '" + export.getName() + "' and channel '" + export.getChannel() + "' already exists");
                    return result;
                } else if ("overwrite".equals(resolution) || options.isOverwriteExisting()) {
                    // Update existing template
                    Template template = existing.get();
                    template.setDescription(export.getDescription());
                    template.setBody(export.getContent()); // Template uses 'body' field
                    template.setCategory(export.getCategory());
                    template.setVersion(template.getVersion() + 1);
                    template = templateRepository.save(template);

                    result.setSuccess(true);
                    result.setTemplateId(template.getId());
                    result.setAction("updated");
                    result.setMessage("Template updated successfully");

                    log.info("Imported template (updated): templateId={}, name={}", template.getId(), template.getName());
                    return result;
                } else if ("rename".equals(resolution)) {
                    // Create with new name
                    export.setName(export.getName() + " (Imported)");
                }
            }

            // Create new template
            com.notificationplatform.dto.request.CreateTemplateRequest createRequest =
                    new com.notificationplatform.dto.request.CreateTemplateRequest();
            createRequest.setName(export.getName());
            createRequest.setDescription(export.getDescription());
            createRequest.setChannel(export.getChannel());
            createRequest.setCategory(export.getCategory());
            // Set content - use body field for template content
            createRequest.setBody(export.getContent() != null ? export.getContent() : "");

            com.notificationplatform.dto.response.TemplateResponse templateResponse =
                    templateService.createTemplate(createRequest);

            result.setSuccess(true);
            result.setTemplateId(templateResponse.getId());
            result.setAction("created");
            result.setMessage("Template imported successfully");

            log.info("Imported template (created): templateId={}, name={}", templateResponse.getId(), export.getName());

        } catch (Exception e) {
            log.error("Error importing template: {}", export.getName(), e);
            result.setSuccess(false);
            result.setMessage("Error importing template: " + e.getMessage());
        }

        return result;
    }

    private void validateTemplateExport(TemplateExportResponse.TemplateExport export) {
        if (export.getName() == null || export.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }

        if (export.getChannel() == null || export.getChannel().trim().isEmpty()) {
            throw new IllegalArgumentException("Template channel is required");
        }

        if (export.getContent() == null || export.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Template content is required");
        }
    }

    private TemplateExportResponse.TemplateExport toExport(Template template) {
        TemplateExportResponse.TemplateExport export = new TemplateExportResponse.TemplateExport();
        export.setId(template.getId());
        export.setName(template.getName());
        export.setDescription(template.getDescription());
        export.setChannel(template.getChannel());
        export.setCategory(template.getCategory());
        export.setContent(template.getBody() != null ? template.getBody() : ""); // Template uses 'body' field
        export.setContentType("text/plain"); // Default content type
        export.setStatus(template.getStatus());
        export.setVersion(template.getVersion());
        export.setCreatedAt(template.getCreatedAt());
        export.setUpdatedAt(template.getUpdatedAt());
        return export;
    }
}

