package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.request.CreateTemplateRequest;
import com.notificationplatform.dto.request.UpdateTemplateRequest;
import com.notificationplatform.dto.response.TemplateResponse;
import com.notificationplatform.entity.Template;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class TemplateMapper {

    public Template toEntity(CreateTemplateRequest request) {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName(request.getName());
        template.setChannel(request.getChannel());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setVariables(request.getVariables());
        template.setCategory(request.getCategory());
        template.setTags(request.getTags() != null ? request.getTags() : new ArrayList<>());
        template.setVersion(1);
        return template;
    }

    public void updateEntity(Template template, UpdateTemplateRequest request) {
        if (request.getName() != null) {
            template.setName(request.getName());
        }
        if (request.getChannel() != null) {
            template.setChannel(request.getChannel());
        }
        if (request.getSubject() != null) {
            template.setSubject(request.getSubject());
        }
        if (request.getBody() != null) {
            template.setBody(request.getBody());
        }
        if (request.getVariables() != null) {
            template.setVariables(request.getVariables());
        }
        if (request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }
        if (request.getTags() != null) {
            template.setTags(request.getTags());
        }
        // Increment version on update
        template.setVersion(template.getVersion() + 1);
    }

    public TemplateResponse toResponse(Template template) {
        TemplateResponse response = new TemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setChannel(template.getChannel());
        response.setSubject(template.getSubject());
        response.setBody(template.getBody());
        List<Map<String, Object>> variables = template.getVariables() != null ? (List<Map<String, Object>>) template.getVariables() : null;
        response.setVariables(variables);
        response.setCategory(template.getCategory());
        response.setTags(template.getTags());
        response.setVersion(template.getVersion());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }

    public List<TemplateResponse> toResponseList(List<Template> templates) {
        List<TemplateResponse> responses = new ArrayList<>();
        for (Template template : templates) {
            responses.add(toResponse(template));
        }
        return responses;
    }
}

