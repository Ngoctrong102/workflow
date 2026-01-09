package com.notificationplatform.service.templatelibrary;

import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TemplateResponse;

import java.util.List;

/**
 * Service for managing template library and pre-built templates
 */
public interface TemplateLibraryService {

    /**
     * Get library templates with pagination
     */
    PagedResponse<TemplateResponse> listLibraryTemplates(String category, String channel, String search, int limit, int offset);

    /**
     * Get public templates
     */
    PagedResponse<TemplateResponse> listPublicTemplates(String category, String channel, String search, int limit, int offset);

    /**
     * Install template from library to user's templates
     */
    TemplateResponse installTemplate(String libraryTemplateId, String userId);

    /**
     * Get template by ID from library
     */
    TemplateResponse getLibraryTemplate(String id);

    /**
     * Get template categories
     */
    List<String> getCategories();

    /**
     * Share template publicly
     */
    TemplateResponse shareTemplate(String templateId, boolean isPublic);

    /**
     * Get popular templates
     */
    List<TemplateResponse> getPopularTemplates(int limit);
}

