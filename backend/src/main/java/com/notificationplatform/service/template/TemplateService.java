package com.notificationplatform.service.template;

import com.notificationplatform.dto.request.CreateTemplateRequest;
import com.notificationplatform.dto.request.RenderTemplateRequest;
import com.notificationplatform.dto.request.UpdateTemplateRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.RenderTemplateResponse;
import com.notificationplatform.dto.response.TemplateResponse;

public interface TemplateService {

    TemplateResponse createTemplate(CreateTemplateRequest request);

    TemplateResponse getTemplateById(String id);

    PagedResponse<TemplateResponse> listTemplates(String channel, String category, String status, 
                                                  String search, int limit, int offset);

    TemplateResponse updateTemplate(String id, UpdateTemplateRequest request);

    void deleteTemplate(String id);

    RenderTemplateResponse renderTemplate(RenderTemplateRequest request);

    RenderTemplateResponse renderTemplate(String templateId, java.util.Map<String, Object> variables);
}

