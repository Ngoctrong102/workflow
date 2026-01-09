package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateABTestRequest;
import com.notificationplatform.dto.request.UpdateABTestRequest;
import com.notificationplatform.dto.response.ABTestResponse;
import com.notificationplatform.dto.response.ABTestResultsResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.abtest.ABTestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ab-tests")
public class ABTestController {

    private final ABTestService abTestService;

    public ABTestController(ABTestService abTestService) {
        this.abTestService = abTestService;
    }

    @PostMapping
    public ResponseEntity<ABTestResponse> createABTest(@Valid @RequestBody CreateABTestRequest request) {
        ABTestResponse response = abTestService.createABTest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ABTestResponse> getABTest(@PathVariable String id) {
        ABTestResponse response = abTestService.getABTestById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ABTestResponse>> listABTests(
            @RequestParam(required = false) String workflowId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ABTestResponse> responses = abTestService.listABTests(workflowId, status, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ABTestResponse> updateABTest(
            @PathVariable String id,
            @Valid @RequestBody UpdateABTestRequest request) {
        ABTestResponse response = abTestService.updateABTest(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteABTest(@PathVariable String id) {
        abTestService.deleteABTest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ABTestResponse> startABTest(@PathVariable String id) {
        ABTestResponse response = abTestService.startABTest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    public ResponseEntity<ABTestResponse> pauseABTest(@PathVariable String id) {
        ABTestResponse response = abTestService.pauseABTest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<ABTestResponse> stopABTest(@PathVariable String id) {
        ABTestResponse response = abTestService.stopABTest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<String> assignVariant(
            @PathVariable String id,
            @RequestParam String userId) {
        String variantId = abTestService.assignVariant(id, userId);
        return ResponseEntity.ok(variantId);
    }

    @PostMapping("/{id}/engagement")
    public ResponseEntity<Void> recordEngagement(
            @PathVariable String id,
            @RequestParam String variantId,
            @RequestParam String userId,
            @RequestParam String eventType) {
        abTestService.recordEngagement(id, variantId, userId, eventType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<ABTestResultsResponse> getABTestResults(@PathVariable String id) {
        ABTestResultsResponse response = abTestService.getABTestResults(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ABTestResponse> analyzeAndDetermineWinner(@PathVariable String id) {
        ABTestResponse response = abTestService.analyzeAndDetermineWinner(id);
        return ResponseEntity.ok(response);
    }
}

