package com.notificationplatform.service.workflowreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class ReportGenerator {

    private final ObjectMapper objectMapper;

    public ReportGenerator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Generate CSV report from query results
     */
    public byte[] generateCsvReport(List<Map<String, Object>> results, 
                                    ReportMetadata metadata) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {

            // Write metadata header if provided
            if (metadata != null) {
                writeCsvMetadataHeader(writer, metadata);
                writer.println();
            }

            if (results.isEmpty()) {
                writer.println("No data available");
                writer.flush();
                return baos.toByteArray();
            }

            // Get column names from first row
            Set<String> columns = new LinkedHashSet<>();
            for (Map<String, Object> row : results) {
                columns.addAll(row.keySet());
            }

            // Write CSV header
            writer.println(String.join(",", escapeCsvValues(columns)));

            // Write data rows
            for (Map<String, Object> row : results) {
                List<String> values = new ArrayList<>();
                for (String column : columns) {
                    Object value = row.get(column);
                    values.add(escapeCsvValue(value != null ? value.toString() : ""));
                }
                writer.println(String.join(",", values));
            }

            writer.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Error generating CSV report: " + e.getMessage(), e);
        }
    }

    /**
     * Generate Excel report from query results (CSV format for now)
     */
    public byte[] generateExcelReport(List<Map<String, Object>> results, 
                                      ReportMetadata metadata) {
        // For now, generate CSV format
        // In production, use Apache POI to generate actual Excel files
        log.warn("Excel format not fully implemented, generating CSV instead");
        return generateCsvReport(results, metadata);
    }

    /**
     * Generate JSON report from query results
     */
    public byte[] generateJsonReport(List<Map<String, Object>> results, 
                                     ReportMetadata metadata) {
        try {
            Map<String, Object> reportData = new LinkedHashMap<>();

            // Add metadata if provided
            if (metadata != null) {
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("workflow_id", metadata.getWorkflowId());
                meta.put("workflow_name", metadata.getWorkflowName());
                meta.put("period_start", metadata.getPeriodStart());
                meta.put("period_end", metadata.getPeriodEnd());
                meta.put("generated_at", metadata.getGeneratedAt());
                reportData.put("metadata", meta);
            }

            reportData.put("data", results);
            reportData.put("row_count", results.size());

            return objectMapper.writeValueAsBytes(reportData);
        } catch (Exception e) {
            log.error("Error generating JSON report", e);
            throw new RuntimeException("Error generating JSON report: " + e.getMessage(), e);
        }
    }

    /**
     * Write CSV metadata header
     */
    private void writeCsvMetadataHeader(PrintWriter writer, ReportMetadata metadata) {
        writer.println("# Workflow Report");
        if (metadata.getWorkflowName() != null) {
            writer.println("# Workflow: " + metadata.getWorkflowName());
        }
        if (metadata.getWorkflowId() != null) {
            writer.println("# Workflow ID: " + metadata.getWorkflowId());
        }
        if (metadata.getPeriodStart() != null && metadata.getPeriodEnd() != null) {
            writer.println("# Period: " + 
                    metadata.getPeriodStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + 
                    " to " + 
                    metadata.getPeriodEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        if (metadata.getGeneratedAt() != null) {
            writer.println("# Generated: " + 
                    metadata.getGeneratedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    /**
     * Escape CSV values
     */
    private List<String> escapeCsvValues(Collection<String> values) {
        List<String> escaped = new ArrayList<>();
        for (String value : values) {
            escaped.add(escapeCsvValue(value));
        }
        return escaped;
    }

    /**
     * Escape CSV value
     */
    private String escapeCsvValue(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public static class ReportMetadata {
        private String workflowId;
        private String workflowName;
        private LocalDateTime periodStart;
        private LocalDateTime periodEnd;
        private LocalDateTime generatedAt;

        public ReportMetadata() {
        }

        public ReportMetadata(String workflowId, String workflowName, 
                             LocalDateTime periodStart, LocalDateTime periodEnd) {
            this.workflowId = workflowId;
            this.workflowName = workflowName;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
            this.generatedAt = LocalDateTime.now();
        }

        // Getters and Setters
        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }

        public String getWorkflowName() {
            return workflowName;
        }

        public void setWorkflowName(String workflowName) {
            this.workflowName = workflowName;
        }

        public LocalDateTime getPeriodStart() {
            return periodStart;
        }

        public void setPeriodStart(LocalDateTime periodStart) {
            this.periodStart = periodStart;
        }

        public LocalDateTime getPeriodEnd() {
            return periodEnd;
        }

        public void setPeriodEnd(LocalDateTime periodEnd) {
            this.periodEnd = periodEnd;
        }

        public LocalDateTime getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
        }
    }
}

