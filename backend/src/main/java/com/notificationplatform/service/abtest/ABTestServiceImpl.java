package com.notificationplatform.service.abtest;

import com.notificationplatform.dto.request.CreateABTestRequest;
import com.notificationplatform.dto.request.UpdateABTestRequest;
import com.notificationplatform.dto.response.ABTestResponse;
import com.notificationplatform.dto.response.ABTestResultsResponse;
import com.notificationplatform.dto.response.ABTestVariantResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.ABTest;
import com.notificationplatform.entity.ABTestResult;
import com.notificationplatform.entity.ABTestVariant;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ABTestRepository;
import com.notificationplatform.repository.ABTestResultRepository;
import com.notificationplatform.repository.ABTestVariantRepository;
import com.notificationplatform.repository.WorkflowRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class ABTestServiceImpl implements ABTestService {

    private final ABTestRepository abTestRepository;
    private final ABTestVariantRepository variantRepository;
    private final ABTestResultRepository resultRepository;
    private final WorkflowRepository workflowRepository;
    private final VariantAssignmentService assignmentService;
    private final StatisticalAnalysisService analysisService;

    public ABTestServiceImpl(ABTestRepository abTestRepository,
                            ABTestVariantRepository variantRepository,
                            ABTestResultRepository resultRepository,
                            WorkflowRepository workflowRepository,
                            VariantAssignmentService assignmentService,
                            StatisticalAnalysisService analysisService) {
        this.abTestRepository = abTestRepository;
        this.variantRepository = variantRepository;
        this.resultRepository = resultRepository;
        this.workflowRepository = workflowRepository;
        this.assignmentService = assignmentService;
        this.analysisService = analysisService;
    }

    @Override
    public ABTestResponse createABTest(CreateABTestRequest request) {
        // Validate workflow exists
        workflowRepository.findByIdAndNotDeleted(request.getWorkflowId())
                .orElseThrow(() -> new ResourceNotFoundException("Workflow not found with id: " + request.getWorkflowId()));

        // Validate variants
        if (request.getVariants() == null || request.getVariants().size() < 2) {
            throw new IllegalArgumentException("At least 2 variants are required");
        }

        // Create A/B test
        ABTest abTest = new ABTest();
        abTest.setId(UUID.randomUUID().toString());
        abTest.setName(request.getName());
        abTest.setDescription(request.getDescription());
        abTest.setWorkflowId(request.getWorkflowId());
        abTest.setStatus("draft");
        abTest.setSuccessMetric(request.getSuccessMetric());
        abTest.setTrafficSplitType(request.getTrafficSplitType());
        abTest.setStartDate(request.getStartDate());
        abTest.setEndDate(request.getEndDate());
        abTest.setMinSampleSize(request.getMinSampleSize());
        abTest.setConfidenceLevel(request.getConfidenceLevel());
        abTest.setMetadata(request.getMetadata());

        // Normalize traffic percentages if equal split
        if ("equal".equals(request.getTrafficSplitType())) {
            BigDecimal equalPercentage = BigDecimal.valueOf(100)
                    .divide(BigDecimal.valueOf(request.getVariants().size()), 2, java.math.RoundingMode.HALF_UP);
            request.getVariants().forEach(v -> v.setTrafficPercentage(equalPercentage));
        }

        // Validate traffic percentages sum to 100
        BigDecimal totalPercentage = request.getVariants().stream()
                .map(v -> v.getTrafficPercentage() != null ? v.getTrafficPercentage() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPercentage.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Traffic percentages must sum to 100");
        }

        abTest = abTestRepository.save(abTest);

        // Create variants
        List<ABTestVariant> variants = new ArrayList<>();
        for (com.notificationplatform.dto.request.CreateABTestVariantRequest variantRequest : request.getVariants()) {
            ABTestVariant variant = new ABTestVariant();
            variant.setId(UUID.randomUUID().toString());
            variant.setAbTestId(abTest.getId());
            variant.setName(variantRequest.getName());
            variant.setLabel(variantRequest.getLabel());
            variant.setTemplateId(variantRequest.getTemplateId());
            variant.setChannelId(variantRequest.getChannelId());
            variant.setConfig(variantRequest.getConfig());
            variant.setTrafficPercentage(variantRequest.getTrafficPercentage());
            variants.add(variantRepository.save(variant));
        }

        abTest.setVariants(variants);

        log.info("Created A/B test: testId={}, variants={}", abTest.getId(), variants.size());

        return toResponse(abTest);
    }

    @Override
    @Transactional(readOnly = true)
    public ABTestResponse getABTestById(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));
        return toResponse(abTest);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ABTestResponse> listABTests(String workflowId, String status, String search, int limit, int offset) {
        // Validate pagination
        if (limit < 1) limit = 20;
        if (limit > 100) limit = 100;
        if (offset < 0) offset = 0;

        List<ABTest> abTests;

        if (workflowId != null && !workflowId.isEmpty()) {
            abTests = abTestRepository.findByWorkflowIdAndNotDeleted(workflowId);
        } else if (status != null && !status.isEmpty()) {
            abTests = abTestRepository.findByStatusAndNotDeleted(status);
        } else {
            abTests = abTestRepository.findAllActive();
        }

        // Filter by search if provided
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            abTests = abTests.stream()
                    .filter(t -> t.getName().toLowerCase().contains(searchLower) ||
                               (t.getDescription() != null && t.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        long total = abTests.size();

        // Apply pagination
        int fromIndex = Math.min(offset, abTests.size());
        int toIndex = Math.min(offset + limit, abTests.size());
        List<ABTest> pagedTests = abTests.subList(fromIndex, toIndex);

        List<ABTestResponse> responses = pagedTests.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(responses, total, limit, offset);
    }

    @Override
    public ABTestResponse updateABTest(String id, UpdateABTestRequest request) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        if (request.getName() != null) {
            abTest.setName(request.getName());
        }
        if (request.getDescription() != null) {
            abTest.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            abTest.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            abTest.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            abTest.setEndDate(request.getEndDate());
        }
        if (request.getMinSampleSize() != null) {
            abTest.setMinSampleSize(request.getMinSampleSize());
        }
        if (request.getConfidenceLevel() != null) {
            abTest.setConfidenceLevel(request.getConfidenceLevel());
        }
        if (request.getMetadata() != null) {
            abTest.setMetadata(request.getMetadata());
        }

        abTest = abTestRepository.save(abTest);

        log.info("Updated A/B test: testId={}", abTest.getId());

        return toResponse(abTest);
    }

    @Override
    public void deleteABTest(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        // Soft delete
        abTest.setDeletedAt(LocalDateTime.now());
        abTest.setStatus("archived");
        abTestRepository.save(abTest);

        log.info("Deleted A/B test: testId={}", abTest.getId());
    }

    @Override
    public ABTestResponse startABTest(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        if (!"draft".equals(abTest.getStatus()) && !"paused".equals(abTest.getStatus())) {
            throw new IllegalStateException("A/B test can only be started from draft or paused status");
        }

        abTest.setStatus("running");
        if (abTest.getStartDate() == null) {
            abTest.setStartDate(LocalDateTime.now());
        }
        abTest = abTestRepository.save(abTest);

        log.info("Started A/B test: testId={}", abTest.getId());

        return toResponse(abTest);
    }

    @Override
    public ABTestResponse pauseABTest(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        if (!"running".equals(abTest.getStatus())) {
            throw new IllegalStateException("A/B test can only be paused when running");
        }

        abTest.setStatus("paused");
        abTest = abTestRepository.save(abTest);

        log.info("Paused A/B test: testId={}", abTest.getId());

        return toResponse(abTest);
    }

    @Override
    public ABTestResponse stopABTest(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        if (!"running".equals(abTest.getStatus()) && !"paused".equals(abTest.getStatus())) {
            throw new IllegalStateException("A/B test can only be stopped when running or paused");
        }

        abTest.setStatus("completed");
        if (abTest.getEndDate() == null) {
            abTest.setEndDate(LocalDateTime.now());
        }
        abTest = abTestRepository.save(abTest);

        // Analyze and determine winner
        analyzeAndDetermineWinner(id);

        log.info("Stopped A/B test: testId={}", abTest.getId());

        return toResponse(abTest);
    }

    @Override
    public String assignVariant(String abTestId, String userId) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(abTestId)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + abTestId));

        if (!"running".equals(abTest.getStatus())) {
            throw new IllegalStateException("A/B test is not running");
        }

        // Check if user already assigned
        Optional<ABTestResult> existing = resultRepository.findByUserIdAndAbTestId(userId, abTestId);
        if (existing.isPresent()) {
            return existing.get().getVariantId();
        }

        // Assign variant
        String variantId = assignmentService.assignVariant(abTest, userId);

        // Record assignment
        ABTestResult result = new ABTestResult();
        result.setId(UUID.randomUUID().toString());
        result.setAbTestId(abTestId);
        result.setVariantId(variantId);
        result.setUserId(userId);
        result.setAssignedAt(LocalDateTime.now());
        resultRepository.save(result);

        log.debug("Assigned user {} to variant {} for A/B test {}", userId, variantId, abTestId);

        return variantId;
    }

    @Override
    public void recordEngagement(String abTestId, String variantId, String userId, String eventType) {
        ABTestResult result = resultRepository.findByUserIdAndAbTestId(userId, abTestId)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test result not found for user: " + userId));

        if (!result.getVariantId().equals(variantId)) {
            throw new IllegalArgumentException("Variant ID does not match assigned variant");
        }

        LocalDateTime now = LocalDateTime.now();

        switch (eventType.toLowerCase()) {
            case "open":
                if (result.getOpenedAt() == null) {
                    result.setOpenedAt(now);
                }
                break;
            case "click":
                if (result.getClickedAt() == null) {
                    result.setClickedAt(now);
                }
                break;
            case "convert":
            case "conversion":
                if (result.getConvertedAt() == null) {
                    result.setConvertedAt(now);
                }
                break;
            default:
                log.warn("Unknown engagement event type: {}", eventType);
        }

        resultRepository.save(result);

        log.debug("Recorded {} event for user {} in A/B test {}", eventType, userId, abTestId);
    }

    @Override
    @Transactional(readOnly = true)
    public ABTestResultsResponse getABTestResults(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        List<ABTestVariant> variants = variantRepository.findByAbTestIdAndNotDeleted(id);

        ABTestResultsResponse response = new ABTestResultsResponse();
        response.setAbTestId(abTest.getId());
        response.setAbTestName(abTest.getName());
        response.setSuccessMetric(abTest.getSuccessMetric());

        // Calculate results for each variant
        List<ABTestResultsResponse.VariantResult> variantResults = new ArrayList<>();
        for (ABTestVariant variant : variants) {
            ABTestResultsResponse.VariantResult variantResult = calculateVariantResult(abTest, variant);
            variantResults.add(variantResult);
        }
        response.setVariantResults(variantResults);

        // Statistical analysis
        if (variants.size() >= 2) {
            ABTestResultsResponse.StatisticalAnalysis analysis = new ABTestResultsResponse.StatisticalAnalysis();
            
            // Calculate p-value between first two variants
            BigDecimal pValue = analysisService.calculatePValue(
                    abTest.getId(), 
                    variants.get(0).getId(), 
                    variants.get(1).getId(), 
                    abTest.getSuccessMetric()
            );
            analysis.setPValue(pValue);
            analysis.setIsStatisticallySignificant(pValue.compareTo(new BigDecimal("0.05")) < 0);
            analysis.setSignificanceLevel(pValue.compareTo(new BigDecimal("0.05")) < 0 ? "significant" : "not significant");
            
            response.setStatisticalAnalysis(analysis);
        }

        // Determine winner
        String winnerId = abTest.getWinnerVariantId();
        if (winnerId == null && "completed".equals(abTest.getStatus())) {
            winnerId = analysisService.determineWinner(abTest, variants);
        }
        response.setWinnerVariantId(winnerId);

        // Generate recommendation
        final String finalWinnerId = winnerId; // Make effectively final for lambda
        if (finalWinnerId != null) {
            response.setRecommendation("Variant " + variants.stream()
                    .filter(v -> v.getId().equals(finalWinnerId))
                    .findFirst()
                    .map(ABTestVariant::getName)
                    .orElse("Unknown") + " is the winner based on " + abTest.getSuccessMetric());
        } else {
            response.setRecommendation("No clear winner yet. Continue testing or increase sample size.");
        }

        return response;
    }

    @Override
    public ABTestResponse analyzeAndDetermineWinner(String id) {
        ABTest abTest = abTestRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("A/B test not found with id: " + id));

        List<ABTestVariant> variants = variantRepository.findByAbTestIdAndNotDeleted(id);

        String winnerId = analysisService.determineWinner(abTest, variants);
        abTest.setWinnerVariantId(winnerId);
        abTest = abTestRepository.save(abTest);

        log.info("Determined winner for A/B test: testId={}, winnerId={}", abTest.getId(), winnerId);

        return toResponse(abTest);
    }

    private ABTestResultsResponse.VariantResult calculateVariantResult(ABTest abTest, ABTestVariant variant) {
        ABTestResultsResponse.VariantResult result = new ABTestResultsResponse.VariantResult();
        result.setVariantId(variant.getId());
        result.setVariantName(variant.getName());
        result.setVariantLabel(variant.getLabel());

        long totalAssignments = resultRepository.countByAbTestIdAndVariantId(abTest.getId(), variant.getId());
        long totalOpens = resultRepository.countOpensByAbTestIdAndVariantId(abTest.getId(), variant.getId());
        long totalClicks = resultRepository.countClicksByAbTestIdAndVariantId(abTest.getId(), variant.getId());
        long totalConversions = resultRepository.countConversionsByAbTestIdAndVariantId(abTest.getId(), variant.getId());

        result.setTotalAssignments(totalAssignments);
        result.setTotalOpens(totalOpens);
        result.setTotalClicks(totalClicks);
        result.setTotalConversions(totalConversions);

        // Calculate rates
        if (totalAssignments > 0) {
            result.setOpenRate(BigDecimal.valueOf(totalOpens)
                    .divide(BigDecimal.valueOf(totalAssignments), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
            result.setClickRate(BigDecimal.valueOf(totalClicks)
                    .divide(BigDecimal.valueOf(totalAssignments), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
            result.setConversionRate(BigDecimal.valueOf(totalConversions)
                    .divide(BigDecimal.valueOf(totalAssignments), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        } else {
            result.setOpenRate(BigDecimal.ZERO);
            result.setClickRate(BigDecimal.ZERO);
            result.setConversionRate(BigDecimal.ZERO);
        }

        return result;
    }

    private ABTestResponse toResponse(ABTest abTest) {
        ABTestResponse response = new ABTestResponse();
        response.setId(abTest.getId());
        response.setName(abTest.getName());
        response.setDescription(abTest.getDescription());
        response.setWorkflowId(abTest.getWorkflowId());
        response.setStatus(abTest.getStatus());
        response.setSuccessMetric(abTest.getSuccessMetric());
        response.setTrafficSplitType(abTest.getTrafficSplitType());
        response.setStartDate(abTest.getStartDate());
        response.setEndDate(abTest.getEndDate());
        response.setMinSampleSize(abTest.getMinSampleSize());
        response.setConfidenceLevel(abTest.getConfidenceLevel());
        response.setWinnerVariantId(abTest.getWinnerVariantId());
        response.setMetadata(abTest.getMetadata() != null ? 
                (Map<String, Object>) abTest.getMetadata() : null);
        response.setCreatedAt(abTest.getCreatedAt());
        response.setUpdatedAt(abTest.getUpdatedAt());

        // Load variants
        List<ABTestVariant> variants = variantRepository.findByAbTestIdAndNotDeleted(abTest.getId());
        List<ABTestVariantResponse> variantResponses = variants.stream()
                .map(this::toVariantResponse)
                .collect(Collectors.toList());
        response.setVariants(variantResponses);

        return response;
    }

    private ABTestVariantResponse toVariantResponse(ABTestVariant variant) {
        ABTestVariantResponse response = new ABTestVariantResponse();
        response.setId(variant.getId());
        response.setAbTestId(variant.getAbTestId());
        response.setName(variant.getName());
        response.setLabel(variant.getLabel());
        response.setTemplateId(variant.getTemplateId());
        response.setChannelId(variant.getChannelId());
        response.setConfig(variant.getConfig() != null ? 
                (Map<String, Object>) variant.getConfig() : null);
        response.setTrafficPercentage(variant.getTrafficPercentage());
        response.setCreatedAt(variant.getCreatedAt());
        response.setUpdatedAt(variant.getUpdatedAt());
        return response;
    }
}

