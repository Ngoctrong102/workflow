package com.notificationplatform.controller;

import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TemplateResponse;
import com.notificationplatform.service.templatelibrary.TemplateLibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/template-library")
public class TemplateLibraryController {

    private final TemplateLibraryService templateLibraryService;

    public TemplateLibraryController(TemplateLibraryService templateLibraryService) {
        this.templateLibraryService = templateLibraryService;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<TemplateResponse>> listLibraryTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<TemplateResponse> responses = templateLibraryService.listLibraryTemplates(
                category, channel, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/public")
    public ResponseEntity<PagedResponse<TemplateResponse>> listPublicTemplates(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<TemplateResponse> responses = templateLibraryService.listPublicTemplates(
                category, channel, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TemplateResponse> getLibraryTemplate(@PathVariable String id) {
        TemplateResponse response = templateLibraryService.getLibraryTemplate(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/install")
    public ResponseEntity<TemplateResponse> installTemplate(
            @PathVariable String id,
            @RequestParam(required = false, defaultValue = "system") String userId) {
        TemplateResponse response = templateLibraryService.installTemplate(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = templateLibraryService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/templates/{id}/share")
    public ResponseEntity<TemplateResponse> shareTemplate(
            @PathVariable String id,
            @RequestParam boolean isPublic) {
        TemplateResponse response = templateLibraryService.shareTemplate(id, isPublic);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<TemplateResponse>> getPopularTemplates(
            @RequestParam(defaultValue = "10") int limit) {
        List<TemplateResponse> responses = templateLibraryService.getPopularTemplates(limit);
        return ResponseEntity.ok(responses);
    }
}

