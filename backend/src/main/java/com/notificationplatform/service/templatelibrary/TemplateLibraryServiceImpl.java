package com.notificationplatform.service.templatelibrary;

import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TemplateResponse;
import com.notificationplatform.entity.Template;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.TemplateRepository;
import com.notificationplatform.service.template.TemplateService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class TemplateLibraryServiceImpl implements TemplateLibraryService {

    private final TemplateRepository templateRepository;
    private final TemplateService templateService;

    public TemplateLibraryServiceImpl(TemplateRepository templateRepository,
                                     TemplateService templateService) {
        this.templateRepository = templateRepository;
        this.templateService = templateService;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TemplateResponse> listLibraryTemplates(String category, String channel, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Template> templates = templateRepository.findAllActive().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsLibrary()))
                .collect(Collectors.toList());

        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            templates = templates.stream()
                    .filter(t -> category.equals(t.getLibraryCategory()))
                    .collect(Collectors.toList());
        }

        // Filter by channel if provided
        if (channel != null && !channel.isEmpty()) {
            templates = templates.stream()
                    .filter(t -> channel.equals(t.getChannel()))
                    .collect(Collectors.toList());
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            templates = templates.stream()
                    .filter(t -> t.getName().toLowerCase().contains(searchLower) ||
                               (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchLower)) ||
                               (t.getLibraryCategory() != null && t.getLibraryCategory().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Sort by install count (popular first)
        templates.sort((a, b) -> {
            int countA = a.getInstallCount() != null ? a.getInstallCount() : 0;
            int countB = b.getInstallCount() != null ? b.getInstallCount() : 0;
            return Integer.compare(countB, countA);
        });

        long total = templates.size();

        // Apply pagination
        int fromIndex = Math.min(offset, templates.size());
        int toIndex = Math.min(offset + limit, templates.size());
        List<Template> pagedTemplates = templates.subList(fromIndex, toIndex);

        List<TemplateResponse> responses = pagedTemplates.stream()
                .map(t -> {
                    TemplateResponse response = templateService.getTemplateById(t.getId());
                    return response;
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TemplateResponse> listPublicTemplates(String category, String channel, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<Template> templates = templateRepository.findAllActive().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsPublic()))
                .collect(Collectors.toList());

        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            templates = templates.stream()
                    .filter(t -> category.equals(t.getCategory()) || category.equals(t.getLibraryCategory()))
                    .collect(Collectors.toList());
        }

        // Filter by channel if provided
        if (channel != null && !channel.isEmpty()) {
            templates = templates.stream()
                    .filter(t -> channel.equals(t.getChannel()))
                    .collect(Collectors.toList());
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            templates = templates.stream()
                    .filter(t -> t.getName().toLowerCase().contains(searchLower) ||
                               (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        long total = templates.size();

        // Apply pagination
        int fromIndex = Math.min(offset, templates.size());
        int toIndex = Math.min(offset + limit, templates.size());
        List<Template> pagedTemplates = templates.subList(fromIndex, toIndex);

        List<TemplateResponse> responses = pagedTemplates.stream()
                .map(t -> {
                    TemplateResponse response = templateService.getTemplateById(t.getId());
                    return response;
                })
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public TemplateResponse installTemplate(String libraryTemplateId, String userId) {
        Template libraryTemplate = templateRepository.findByIdAndNotDeleted(libraryTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException("Library template not found with id: " + libraryTemplateId));

        if (!Boolean.TRUE.equals(libraryTemplate.getIsLibrary())) {
            throw new IllegalArgumentException("Template is not a library template");
        }

        // Create copy of template for user
        com.notificationplatform.dto.request.CreateTemplateRequest createRequest =
                new com.notificationplatform.dto.request.CreateTemplateRequest();
        createRequest.setName(libraryTemplate.getName() + " (Installed)");
        createRequest.setDescription(libraryTemplate.getDescription());
        createRequest.setChannel(libraryTemplate.getChannel());
        createRequest.setCategory(libraryTemplate.getCategory());
        createRequest.setBody(libraryTemplate.getBody());
        if (libraryTemplate.getSubject() != null) {
            createRequest.setSubject(libraryTemplate.getSubject());
        }

        TemplateResponse installedTemplate = templateService.createTemplate(createRequest);

        // Update install count
        libraryTemplate.setInstallCount((libraryTemplate.getInstallCount() != null ? libraryTemplate.getInstallCount() : 0) + 1);
        templateRepository.save(libraryTemplate);

        log.info("Installed template: libraryTemplateId={}, userId={}, installedTemplateId={}",
                   libraryTemplateId, userId, installedTemplate.getId());

        return installedTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse getLibraryTemplate(String id) {
        Template template = templateRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        if (!Boolean.TRUE.equals(template.getIsLibrary())) {
            throw new IllegalArgumentException("Template is not a library template");
        }

        return templateService.getTemplateById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        Set<String> categories = new HashSet<>();
        
        // Get categories from library templates
        templateRepository.findAllActive().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsLibrary()))
                .forEach(t -> {
                    if (t.getLibraryCategory() != null) {
                        categories.add(t.getLibraryCategory());
                    }
                    if (t.getCategory() != null) {
                        categories.add(t.getCategory());
                    }
                });

        return new ArrayList<>(categories).stream().sorted().collect(Collectors.toList());
    }

    @Override
    public TemplateResponse shareTemplate(String templateId, boolean isPublic) {
        Template template = templateRepository.findByIdAndNotDeleted(templateId)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + templateId));

        template.setIsPublic(isPublic);
        template = templateRepository.save(template);

        log.info("Updated template sharing: templateId={}, isPublic={}", templateId, isPublic);

        return templateService.getTemplateById(templateId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateResponse> getPopularTemplates(int limit) {
        List<Template> templates = templateRepository.findAllActive().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsLibrary()))
                .sorted((a, b) -> {
                    int countA = a.getInstallCount() != null ? a.getInstallCount() : 0;
                    int countB = b.getInstallCount() != null ? b.getInstallCount() : 0;
                    return Integer.compare(countB, countA);
                })
                .limit(limit)
                .collect(Collectors.toList());

        return templates.stream()
                .map(t -> templateService.getTemplateById(t.getId()))
                .collect(Collectors.toList());
    }
}

