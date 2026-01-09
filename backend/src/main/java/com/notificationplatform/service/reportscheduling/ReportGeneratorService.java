package com.notificationplatform.service.reportscheduling;

import com.notificationplatform.entity.Channel;
import com.notificationplatform.entity.ScheduledReport;
import com.notificationplatform.repository.ChannelRepository;
import com.notificationplatform.service.channel.email.SmtpEmailProvider;


import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
/**
 * Service for generating reports in various formats
 */
@Slf4j
@Service
public class ReportGeneratorService {

    private final SmtpEmailProvider emailProvider;
    private final ChannelRepository channelRepository;

    public ReportGeneratorService(SmtpEmailProvider emailProvider, ChannelRepository channelRepository) {
        this.emailProvider = emailProvider;
        this.channelRepository = channelRepository;
    }

    /**
     * Generate report file in specified format
     */
    public byte[] generateReport(Object reportData, String format) {
        switch (format.toLowerCase()) {
            case "csv":
                return generateCsvReport(reportData);
            case "json":
                return generateJsonReport(reportData);
            case "pdf":
            case "excel":
                // For now, return CSV as placeholder
                // In production, use libraries like Apache POI for Excel, iText for PDF
                log.warn("PDF/Excel format not fully implemented, generating CSV instead");
                return generateCsvReport(reportData);
            default:
                throw new IllegalArgumentException("Unsupported report format: " + format);
        }
    }

    /**
     * Send report via email
     */
    public void sendReport(ScheduledReport report, byte[] reportFile) {
        try {
            // Find email channel for sending reports
            Optional<Channel> emailChannel = channelRepository.findAllActive().stream()
                    .filter(c -> "email".equals(c.getType()) && "active".equals(c.getStatus()))
                    .findFirst();

            if (emailChannel.isEmpty()) {
                throw new IllegalStateException("No active email channel found for sending reports");
            }

            String subject = String.format("Scheduled Report: %s - %s", 
                    report.getName(), 
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE));

            String body = String.format(
                    "Please find attached the scheduled report: %s\n\n" +
                    "Report Type: %s\n" +
                    "Generated: %s\n",
                    report.getName(),
                    report.getReportType(),
                    LocalDate.now().format(DateTimeFormatter.ISO_DATE));

            // For now, we'll use a simple text email
            // In production, you would attach the file
            emailProvider.send(
                    emailChannel.get(),
                    report.getRecipients(),
                    null,
                    null,
                    subject,
                    body,
                    "text/plain"
            );

            log.info("Sent scheduled report via email: reportId={}, recipients={}", 
                       report.getId(), report.getRecipients());
        } catch (Exception e) {
            log.error("Error sending scheduled report: reportId={}", report.getId(), e);
            throw new RuntimeException("Error sending scheduled report: " + e.getMessage(), e);
        }
    }

    private byte[] generateCsvReport(Object reportData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(baos)) {

            // Simple CSV generation
            // In production, use a proper CSV library like OpenCSV
            if (reportData instanceof Map) {
                Map<String, Object> data = (Map<String, Object>) reportData;
                // Write headers
                writer.println(String.join(",", data.keySet()));
                // Write values
                writer.println(String.join(",", data.values().stream()
                        .map(v -> v != null ? v.toString() : "")
                        .toArray(String[]::new)));
            } else {
                writer.println("Report Data");
                writer.println(reportData.toString());
            }

            writer.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Error generating CSV report", e);
            throw new RuntimeException("Error generating CSV report: " + e.getMessage(), e);
        }
    }

    private byte[] generateJsonReport(Object reportData) {
        try {
            // In production, use Jackson ObjectMapper
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = 
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.writeValueAsBytes(reportData);
        } catch (Exception e) {
            log.error("Error generating JSON report", e);
            throw new RuntimeException("Error generating JSON report: " + e.getMessage(), e);
        }
    }
}

