package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateTemplateRequest;
import com.notificationplatform.dto.request.RenderTemplateRequest;
import com.notificationplatform.dto.request.UpdateTemplateRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.RenderTemplateResponse;
import com.notificationplatform.dto.response.TemplateResponse;
import com.notificationplatform.service.template.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/templates")
@Tag(name = "Templates", description = "Template management APIs - Create and manage notification templates")
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    @Operation(summary = "Create template", description = "Create a new notification template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Template created successfully",
                    content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
            @ApiResponse(responseCode = "422", description = "Validation error")
    })
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        TemplateResponse response = templateService.createTemplate(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template", description = "Get template by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template found",
                    content = @Content(schema = @Schema(implementation = TemplateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found")
    })
    public ResponseEntity<TemplateResponse> getTemplate(
            @Parameter(description = "Template ID", required = true) @PathVariable String id) {
        TemplateResponse response = templateService.getTemplateById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TemplateResponse>> listTemplates(
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<TemplateResponse> response = templateService.listTemplates(channel, category, status, search, limit, offset);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TemplateResponse> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody UpdateTemplateRequest request) {
        TemplateResponse response = templateService.updateTemplate(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/render")
    public ResponseEntity<RenderTemplateResponse> renderTemplate(@Valid @RequestBody RenderTemplateRequest request) {
        RenderTemplateResponse response = templateService.renderTemplate(request);
        return ResponseEntity.ok(response);
    }
}

