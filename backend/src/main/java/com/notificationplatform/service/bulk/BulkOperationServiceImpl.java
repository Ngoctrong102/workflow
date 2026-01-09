package com.notificationplatform.service.bulk;

import com.notificationplatform.dto.response.BulkOperationResult;
import com.notificationplatform.entity.Template;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.repository.TemplateRepository;
import com.notificationplatform.repository.WorkflowRepository;
import com.notificationplatform.service.template.TemplateService;
import com.notificationplatform.service.workflow.WorkflowService;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@Transactional
public class BulkOperationServiceImpl implements BulkOperationService {

    private final WorkflowRepository workflowRepository;
    private final TemplateRepository templateRepository;
    private final WorkflowService workflowService;
    private final TemplateService templateService;

    public BulkOperationServiceImpl(WorkflowRepository workflowRepository,
                                    TemplateRepository templateRepository,
                                    WorkflowService workflowService,
                                    TemplateService templateService) {
        this.workflowRepository = workflowRepository;
        this.templateRepository = templateRepository;
        this.workflowService = workflowService;
        this.templateService = templateService;
    }

    @Override
    public BulkOperationResult bulkDeleteWorkflows(List<String> workflowIds) {
        BulkOperationResult result = new BulkOperationResult();
        result.setTotal(workflowIds.size());
        result.setSuccess(0);
        result.setFailed(0);

        List<BulkOperationResult.OperationResult> results = new ArrayList<>();

        for (String workflowId : workflowIds) {
            BulkOperationResult.OperationResult operationResult = new BulkOperationResult.OperationResult();
            operationResult.setId(workflowId);

            try {
                Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                        .orElse(null);

                if (workflow == null) {
                    operationResult.setSuccess(false);
                    operationResult.setError("Workflow not found");
                    result.setFailed(result.getFailed() + 1);
                } else {
                    operationResult.setName(workflow.getName());
                    workflowService.deleteWorkflow(workflowId);
                    operationResult.setSuccess(true);
                    operationResult.setMessage("Workflow deleted successfully");
                    result.setSuccess(result.getSuccess() + 1);
                }
            } catch (Exception e) {
                log.error("Error deleting workflow: {}", workflowId, e);
                operationResult.setSuccess(false);
                operationResult.setError(e.getMessage());
                result.setFailed(result.getFailed() + 1);
            }

            results.add(operationResult);
        }

        result.setResults(results);
        result.setSummary(createSummary(result));

        log.info("Bulk delete workflows: total={}, success={}, failed={}",
                   result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    @Override
    public BulkOperationResult bulkUpdateWorkflowStatus(List<String> workflowIds, String status) {
        BulkOperationResult result = new BulkOperationResult();
        result.setTotal(workflowIds.size());
        result.setSuccess(0);
        result.setFailed(0);

        List<BulkOperationResult.OperationResult> results = new ArrayList<>();

        for (String workflowId : workflowIds) {
            BulkOperationResult.OperationResult operationResult = new BulkOperationResult.OperationResult();
            operationResult.setId(workflowId);

            try {
                Workflow workflow = workflowRepository.findByIdAndNotDeleted(workflowId)
                        .orElse(null);

                if (workflow == null) {
                    operationResult.setSuccess(false);
                    operationResult.setError("Workflow not found");
                    result.setFailed(result.getFailed() + 1);
                } else {
                    operationResult.setName(workflow.getName());
                    WorkflowStatus workflowStatus = WorkflowStatus.fromValue(status);
                    if (workflowStatus == null) {
                        operationResult.setSuccess(false);
                        operationResult.setError("Invalid status: " + status);
                        result.setFailed(result.getFailed() + 1);
                    } else {
                        workflow.setStatus(workflowStatus);
                        workflowRepository.save(workflow);
                        operationResult.setSuccess(true);
                        operationResult.setMessage("Workflow status updated to " + status);
                        result.setSuccess(result.getSuccess() + 1);
                    }
                    operationResult.setSuccess(true);
                    operationResult.setMessage("Workflow status updated to " + status);
                    result.setSuccess(result.getSuccess() + 1);
                }
            } catch (Exception e) {
                log.error("Error updating workflow status: {}", workflowId, e);
                operationResult.setSuccess(false);
                operationResult.setError(e.getMessage());
                result.setFailed(result.getFailed() + 1);
            }

            results.add(operationResult);
        }

        result.setResults(results);
        result.setSummary(createSummary(result));

        log.info("Bulk update workflow status: total={}, success={}, failed={}, status={}",
                   result.getTotal(), result.getSuccess(), result.getFailed(), status);

        return result;
    }

    @Override
    public BulkOperationResult bulkDeleteTemplates(List<String> templateIds) {
        BulkOperationResult result = new BulkOperationResult();
        result.setTotal(templateIds.size());
        result.setSuccess(0);
        result.setFailed(0);

        List<BulkOperationResult.OperationResult> results = new ArrayList<>();

        for (String templateId : templateIds) {
            BulkOperationResult.OperationResult operationResult = new BulkOperationResult.OperationResult();
            operationResult.setId(templateId);

            try {
                Template template = templateRepository.findByIdAndNotDeleted(templateId)
                        .orElse(null);

                if (template == null) {
                    operationResult.setSuccess(false);
                    operationResult.setError("Template not found");
                    result.setFailed(result.getFailed() + 1);
                } else {
                    operationResult.setName(template.getName());
                    templateService.deleteTemplate(templateId);
                    operationResult.setSuccess(true);
                    operationResult.setMessage("Template deleted successfully");
                    result.setSuccess(result.getSuccess() + 1);
                }
            } catch (Exception e) {
                log.error("Error deleting template: {}", templateId, e);
                operationResult.setSuccess(false);
                operationResult.setError(e.getMessage());
                result.setFailed(result.getFailed() + 1);
            }

            results.add(operationResult);
        }

        result.setResults(results);
        result.setSummary(createSummary(result));

        log.info("Bulk delete templates: total={}, success={}, failed={}",
                   result.getTotal(), result.getSuccess(), result.getFailed());

        return result;
    }

    @Override
    public BulkOperationResult bulkUpdateTemplateStatus(List<String> templateIds, String status) {
        BulkOperationResult result = new BulkOperationResult();
        result.setTotal(templateIds.size());
        result.setSuccess(0);
        result.setFailed(0);

        List<BulkOperationResult.OperationResult> results = new ArrayList<>();

        for (String templateId : templateIds) {
            BulkOperationResult.OperationResult operationResult = new BulkOperationResult.OperationResult();
            operationResult.setId(templateId);

            try {
                Template template = templateRepository.findByIdAndNotDeleted(templateId)
                        .orElse(null);

                if (template == null) {
                    operationResult.setSuccess(false);
                    operationResult.setError("Template not found");
                    result.setFailed(result.getFailed() + 1);
                } else {
                    operationResult.setName(template.getName());
                    template.setStatus(status);
                    templateRepository.save(template);
                    operationResult.setSuccess(true);
                    operationResult.setMessage("Template status updated to " + status);
                    result.setSuccess(result.getSuccess() + 1);
                }
            } catch (Exception e) {
                log.error("Error updating template status: {}", templateId, e);
                operationResult.setSuccess(false);
                operationResult.setError(e.getMessage());
                result.setFailed(result.getFailed() + 1);
            }

            results.add(operationResult);
        }

        result.setResults(results);
        result.setSummary(createSummary(result));

        log.info("Bulk update template status: total={}, success={}, failed={}, status={}",
                   result.getTotal(), result.getSuccess(), result.getFailed(), status);

        return result;
    }

    private Map<String, Object> createSummary(BulkOperationResult result) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total", result.getTotal());
        summary.put("success", result.getSuccess());
        summary.put("failed", result.getFailed());
        summary.put("successRate", result.getTotal() > 0 ?
                (double) result.getSuccess() / result.getTotal() * 100 : 0.0);
        return summary;
    }
}

