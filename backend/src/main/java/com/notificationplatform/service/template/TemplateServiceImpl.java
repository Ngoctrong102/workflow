package com.notificationplatform.service.template;

import com.notificationplatform.dto.mapper.TemplateMapper;
import com.notificationplatform.dto.request.CreateTemplateRequest;
import com.notificationplatform.dto.request.RenderTemplateRequest;
import com.notificationplatform.dto.request.UpdateTemplateRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.RenderTemplateResponse;
import com.notificationplatform.dto.response.TemplateResponse;
import com.notificationplatform.entity.Template;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.TemplateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;
    private final TemplateRenderer templateRenderer;
    private final TemplateValidator templateValidator;

    public TemplateServiceImpl(TemplateRepository templateRepository,
                              TemplateMapper templateMapper,
                              TemplateRenderer templateRenderer,
                              TemplateValidator templateValidator) {
        this.templateRepository = templateRepository;
        this.templateMapper = templateMapper;
        this.templateRenderer = templateRenderer;
        this.templateValidator = templateValidator;
    }

    @Override
    public TemplateResponse createTemplate(CreateTemplateRequest request) {
        // Validate template
        templateValidator.validateCreateRequest(request);

        // Create entity
        Template template = templateMapper.toEntity(request);
        
        // Validate template content
        templateValidator.validateTemplateContent(template);

        // Save
        Template saved = templateRepository.save(template);
        return templateMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "templates", key = "#id")
    public TemplateResponse getTemplateById(String id) {
        Template template = templateRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
        return templateMapper.toResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TemplateResponse> listTemplates(String channel, String category, String status, 
                                                         String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Template> templates;
        long total;

        // Build query based on filters
        if (channel != null && !channel.isEmpty()) {
            templates = templateRepository.findByChannel(channel);
            total = templateRepository.countByChannel(channel);
        } else {
            templates = templateRepository.findAllActive();
            total = templateRepository.count();
        }

        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            templates = templates.stream()
                    .filter(t -> category.equals(t.getCategory()))
                    .collect(Collectors.toList());
            total = templates.size();
        }

        // Apply pagination
        int fromIndex = Math.min(offset, templates.size());
        int toIndex = Math.min(offset + limit, templates.size());
        List<Template> pagedTemplates = templates.subList(fromIndex, toIndex);

        List<TemplateResponse> responses = templateMapper.toResponseList(pagedTemplates);
        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "templates", key = "#id")
    public TemplateResponse updateTemplate(String id, UpdateTemplateRequest request) {
        Template template = templateRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        // Validate update request
        templateValidator.validateUpdateRequest(template, request);

        // Update entity
        templateMapper.updateEntity(template, request);

        // Validate updated content
        templateValidator.validateTemplateContent(template);

        // Save
        Template saved = templateRepository.save(template);
        return templateMapper.toResponse(saved);
    }

    @Override
    @org.springframework.cache.annotation.CacheEvict(value = "templates", key = "#id")
    public void deleteTemplate(String id) {
        Template template = templateRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        // Soft delete
        template.setDeletedAt(LocalDateTime.now());
        templateRepository.save(template);
    }

    @Override
    @Transactional(readOnly = true)
    public RenderTemplateResponse renderTemplate(RenderTemplateRequest request) {
        return renderTemplate(request.getTemplateId(), request.getVariables());
    }

    @Override
    @Transactional(readOnly = true)
    public RenderTemplateResponse renderTemplate(String templateId, Map<String, Object> variables) {
        Template template = templateRepository.findByIdAndNotDeleted(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        RenderTemplateResponse response = new RenderTemplateResponse();

        // Render subject if present
        if (template.getSubject() != null && !template.getSubject().isEmpty()) {
            String renderedSubject = templateRenderer.render(template.getSubject(), variables);
            response.setRenderedSubject(renderedSubject);
        }

        // Render body
        String renderedBody = templateRenderer.render(template.getBody(), variables);
        response.setRenderedBody(renderedBody);

        return response;
    }
}

