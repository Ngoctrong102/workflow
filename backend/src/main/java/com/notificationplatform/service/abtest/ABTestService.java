package com.notificationplatform.service.abtest;

import com.notificationplatform.dto.request.CreateABTestRequest;
import com.notificationplatform.dto.request.UpdateABTestRequest;
import com.notificationplatform.dto.response.ABTestResponse;
import com.notificationplatform.dto.response.ABTestResultsResponse;
import com.notificationplatform.dto.response.PagedResponse;

public interface ABTestService {

    /**
     * Create a new A/B test
     */
    ABTestResponse createABTest(CreateABTestRequest request);

    /**
     * Get A/B test by ID
     */
    ABTestResponse getABTestById(String id);

    /**
     * List A/B tests with pagination
     */
    PagedResponse<ABTestResponse> listABTests(String workflowId, String status, String search, int limit, int offset);

    /**
     * Update A/B test
     */
    ABTestResponse updateABTest(String id, UpdateABTestRequest request);

    /**
     * Delete A/B test (soft delete)
     */
    void deleteABTest(String id);

    /**
     * Start A/B test
     */
    ABTestResponse startABTest(String id);

    /**
     * Pause A/B test
     */
    ABTestResponse pauseABTest(String id);

    /**
     * Stop A/B test
     */
    ABTestResponse stopABTest(String id);

    /**
     * Assign user to a variant
     */
    String assignVariant(String abTestId, String userId);

    /**
     * Record engagement event (open, click, conversion)
     */
    void recordEngagement(String abTestId, String variantId, String userId, String eventType);

    /**
     * Get A/B test results
     */
    ABTestResultsResponse getABTestResults(String id);

    /**
     * Analyze A/B test results and determine winner
     */
    ABTestResponse analyzeAndDetermineWinner(String id);
}

