package com.notificationplatform.dto.mapper;

import com.notificationplatform.dto.request.CreateWorkflowReportRequest;
import com.notificationplatform.dto.request.UpdateWorkflowReportRequest;
import com.notificationplatform.dto.response.WorkflowReportHistoryResponse;
import com.notificationplatform.dto.response.WorkflowReportResponse;
import com.notificationplatform.entity.WorkflowReport;
import com.notificationplatform.entity.WorkflowReportHistory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WorkflowReportMapper {

    public WorkflowReport toEntity(CreateWorkflowReportRequest request) {
        WorkflowReport report = new WorkflowReport();
        report.setName(request.getName());
        report.setRecipients(request.getRecipients());
        report.setScheduleType(request.getScheduleType());
        report.setScheduleTime(request.getScheduleTime());
        report.setScheduleDay(request.getScheduleDay());
        report.setScheduleCron(request.getScheduleCron());
        report.setTimezone(request.getTimezone() != null ? request.getTimezone() : "UTC");
        report.setFormat(request.getFormat() != null ? request.getFormat() : "pdf");
        report.setSections(request.getSections());
        report.setStatus("inactive");
        return report;
    }

    public void updateEntity(WorkflowReport report, UpdateWorkflowReportRequest request) {
        if (request.getName() != null) {
            report.setName(request.getName());
        }
        if (request.getRecipients() != null) {
            report.setRecipients(request.getRecipients());
        }
        if (request.getScheduleType() != null) {
            report.setScheduleType(request.getScheduleType());
        }
        if (request.getScheduleTime() != null) {
            report.setScheduleTime(request.getScheduleTime());
        }
        if (request.getScheduleDay() != null) {
            report.setScheduleDay(request.getScheduleDay());
        }
        if (request.getScheduleCron() != null) {
            report.setScheduleCron(request.getScheduleCron());
        }
        if (request.getTimezone() != null) {
            report.setTimezone(request.getTimezone());
        }
        if (request.getFormat() != null) {
            report.setFormat(request.getFormat());
        }
        if (request.getSections() != null) {
            report.setSections(request.getSections());
        }
        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }
    }

    public WorkflowReportResponse toResponse(WorkflowReport report) {
        WorkflowReportResponse response = new WorkflowReportResponse();
        response.setId(report.getId());
        response.setWorkflowId(report.getWorkflow() != null ? report.getWorkflow().getId() : null);
        response.setName(report.getName());
        response.setRecipients(report.getRecipients());
        response.setScheduleType(report.getScheduleType());
        response.setScheduleTime(report.getScheduleTime());
        response.setScheduleDay(report.getScheduleDay());
        response.setScheduleCron(report.getScheduleCron());
        response.setTimezone(report.getTimezone());
        response.setFormat(report.getFormat());
        response.setSections(report.getSections());
        response.setStatus(report.getStatus());
        response.setLastGeneratedAt(report.getLastGeneratedAt());
        response.setNextGenerationAt(report.getNextGenerationAt());
        response.setLastGenerationStatus(report.getLastGenerationStatus());
        response.setLastGenerationError(report.getLastGenerationError());
        response.setGenerationCount(report.getGenerationCount());
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }

    public WorkflowReportHistoryResponse toHistoryResponse(WorkflowReportHistory history) {
        WorkflowReportHistoryResponse response = new WorkflowReportHistoryResponse();
        response.setId(history.getId());
        response.setWorkflowReportId(history.getWorkflowReport() != null ? history.getWorkflowReport().getId() : null);
        response.setWorkflowId(history.getWorkflow() != null ? history.getWorkflow().getId() : null);
        response.setReportPeriodStart(history.getReportPeriodStart());
        response.setReportPeriodEnd(history.getReportPeriodEnd());
        response.setFilePath(history.getFilePath());
        response.setFileSize(history.getFileSize());
        response.setFormat(history.getFormat());
        response.setRecipients(history.getRecipients());
        response.setDeliveryStatus(history.getDeliveryStatus());
        response.setGeneratedAt(history.getGeneratedAt());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }

    public List<WorkflowReportHistoryResponse> toHistoryResponseList(List<WorkflowReportHistory> histories) {
        List<WorkflowReportHistoryResponse> responses = new ArrayList<>();
        for (WorkflowReportHistory history : histories) {
            responses.add(toHistoryResponse(history));
        }
        return responses;
    }
}

