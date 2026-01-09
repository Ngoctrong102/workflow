package com.notificationplatform.controller;

import com.notificationplatform.dto.request.CreateABTestRequest;
import com.notificationplatform.dto.request.UpdateABTestRequest;
import com.notificationplatform.dto.response.ABTestResponse;
import com.notificationplatform.dto.response.ABTestResultsResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.service.abtest.ABTestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ab-tests")
@Tag(name = "A/B Testing", description = "A/B testing APIs - Test different workflow variants and measure performance")
public class ABTestController {

    private final ABTestService abTestService;

    public ABTestController(ABTestService abTestService) {
        this.abTestService = abTestService;
    }

    @PostMapping
    @Operation(summary = "Create A/B test", description = "Create a new A/B test with variants and traffic split configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "A/B test created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Workflow not found")
    })
    public ResponseEntity<ABTestResponse> createABTest(@Valid @RequestBody CreateABTestRequest request) {
        ABTestResponse response = abTestService.createABTest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get A/B test", description = "Get A/B test configuration by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B test retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResponse> getABTest(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        ABTestResponse response = abTestService.getABTestById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List A/B tests", description = "Get paginated list of A/B tests with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B tests retrieved successfully")
    })
    public ResponseEntity<PagedResponse<ABTestResponse>> listABTests(
            @Parameter(description = "Filter by workflow ID") @RequestParam(required = false) String workflowId,
            @Parameter(description = "Filter by status (draft, running, paused, completed, archived)") 
            @RequestParam(required = false) String status,
            @Parameter(description = "Search by name or description") @RequestParam(required = false) String search,
            @Parameter(description = "Number of results (default: 20)") @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "Pagination offset (default: 0)") @RequestParam(defaultValue = "0") int offset) {
        PagedResponse<ABTestResponse> responses = abTestService.listABTests(workflowId, status, search, limit, offset);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update A/B test", description = "Update A/B test configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B test updated successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResponse> updateABTest(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id,
            @Valid @RequestBody UpdateABTestRequest request) {
        ABTestResponse response = abTestService.updateABTest(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete A/B test", description = "Delete (soft delete) an A/B test")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "A/B test deleted successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<Void> deleteABTest(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        abTestService.deleteABTest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start A/B test", description = "Start an A/B test (change status to running)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B test started successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResponse> startABTest(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        ABTestResponse response = abTestService.startABTest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause A/B test", description = "Pause an A/B test (change status to paused)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B test paused successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResponse> pauseABTest(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        ABTestResponse response = abTestService.pauseABTest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "Stop A/B test", description = "Stop an A/B test (change status to completed and analyze results)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B test stopped successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResponse> stopABTest(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        ABTestResponse response = abTestService.stopABTest(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign variant to user", description = "Assign a user to a variant based on traffic split")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Variant assigned successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found"),
            @ApiResponse(responseCode = "400", description = "A/B test is not running")
    })
    public ResponseEntity<String> assignVariant(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id,
            @Parameter(description = "User ID", required = true) @RequestParam String userId) {
        String variantId = abTestService.assignVariant(id, userId);
        return ResponseEntity.ok(variantId);
    }

    @PostMapping("/{id}/engagement")
    @Operation(summary = "Record engagement event", description = "Record an engagement event (open, click, conversion) for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Engagement event recorded successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test result not found")
    })
    public ResponseEntity<Void> recordEngagement(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id,
            @Parameter(description = "Variant ID", required = true) @RequestParam String variantId,
            @Parameter(description = "User ID", required = true) @RequestParam String userId,
            @Parameter(description = "Event type (open, click, conversion)", required = true) 
            @RequestParam String eventType) {
        abTestService.recordEngagement(id, variantId, userId, eventType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/results")
    @Operation(summary = "Get A/B test results", description = "Get aggregated results and statistical analysis for an A/B test")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A/B test results retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResultsResponse> getABTestResults(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        ABTestResultsResponse response = abTestService.getABTestResults(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/analyze")
    @Operation(summary = "Analyze and determine winner", description = "Analyze A/B test results and determine the winning variant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analysis completed successfully"),
            @ApiResponse(responseCode = "404", description = "A/B test not found")
    })
    public ResponseEntity<ABTestResponse> analyzeAndDetermineWinner(
            @Parameter(description = "A/B test ID", required = true) @PathVariable String id) {
        ABTestResponse response = abTestService.analyzeAndDetermineWinner(id);
        return ResponseEntity.ok(response);
    }
}

