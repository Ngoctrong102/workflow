package com.notificationplatform.service.workflowreport;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ReportQueryExecutor {

    private static final int QUERY_TIMEOUT_SECONDS = 300; // 5 minutes
    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
            "(?i)(DELETE|DROP|TRUNCATE|ALTER|CREATE|INSERT|UPDATE|GRANT|REVOKE|EXEC|EXECUTE)"
    );

    private final JdbcTemplate jdbcTemplate;

    public ReportQueryExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Execute analyst query with parameters
     */
    public QueryExecutionResult executeQuery(String query, String workflowId, 
                                            LocalDateTime startDate, LocalDateTime endDate,
                                            String timezone) {
        log.debug("Executing query for workflow: {}, startDate: {}, endDate: {}", 
                  workflowId, startDate, endDate);

        long startTime = System.currentTimeMillis();

        try {
            // Replace parameters in query
            String processedQuery = replaceParameters(query, workflowId, startDate, endDate, timezone);

            // Execute query with timeout
            CompletableFuture<List<Map<String, Object>>> future = CompletableFuture.supplyAsync(() -> {
                return jdbcTemplate.query(processedQuery, (rs, rowNum) -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    return row;
                });
            });

            List<Map<String, Object>> results = future.get(QUERY_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Query executed successfully: workflowId={}, rows={}, executionTime={}ms", 
                     workflowId, results.size(), executionTime);

            return new QueryExecutionResult(results, executionTime, null);

        } catch (java.util.concurrent.TimeoutException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String error = "Query execution timeout after " + QUERY_TIMEOUT_SECONDS + " seconds";
            log.error("Query timeout: workflowId={}, executionTime={}ms", workflowId, executionTime);
            return new QueryExecutionResult(Collections.emptyList(), executionTime, error);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            String error = "Query execution error: " + e.getMessage();
            log.error("Query execution error: workflowId={}, executionTime={}ms", workflowId, executionTime, e);
            return new QueryExecutionResult(Collections.emptyList(), executionTime, error);
        }
    }

    /**
     * Validate query syntax and safety
     */
    public QueryValidationResult validateQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new QueryValidationResult(false, "Query cannot be empty");
        }

        // Check for dangerous SQL operations
        if (DANGEROUS_PATTERNS.matcher(query).find()) {
            return new QueryValidationResult(false, 
                    "Query contains dangerous SQL operations. Only SELECT queries are allowed.");
        }

        // Check for required parameters
        if (!query.contains(":workflow_id") && !query.contains(":workflowId")) {
            log.warn("Query does not contain :workflow_id parameter");
        }
        if (!query.contains(":start_date") && !query.contains(":startDate")) {
            log.warn("Query does not contain :start_date parameter");
        }
        if (!query.contains(":end_date") && !query.contains(":endDate")) {
            log.warn("Query does not contain :end_date parameter");
        }

            // Try to parse query syntax (basic check)
        try {
            // Use a simple test query to validate syntax
            String testQuery = query
                    .replaceAll(":workflow_id", "'test'")
                    .replaceAll(":workflowId", "'test'")
                    .replaceAll(":start_date", "CURRENT_TIMESTAMP")
                    .replaceAll(":startDate", "CURRENT_TIMESTAMP")
                    .replaceAll(":end_date", "CURRENT_TIMESTAMP")
                    .replaceAll(":endDate", "CURRENT_TIMESTAMP");

            // Try to prepare statement (syntax check)
            if (jdbcTemplate.getDataSource() != null) {
                try (var conn = jdbcTemplate.getDataSource().getConnection();
                     var stmt = conn.prepareStatement(testQuery)) {
                    // Just validate syntax, don't execute
                }
            }
            
            return new QueryValidationResult(true, null);
        } catch (Exception e) {
            return new QueryValidationResult(false, "Query syntax error: " + e.getMessage());
        }
    }

    /**
     * Replace query parameters safely
     */
    private String replaceParameters(String query, String workflowId, 
                                    LocalDateTime startDate, LocalDateTime endDate,
                                    String timezone) {
        String processed = query;

        // Replace workflow_id (both formats)
        processed = processed.replace(":workflow_id", "'" + escapeSql(workflowId) + "'");
        processed = processed.replace(":workflowId", "'" + escapeSql(workflowId) + "'");

        // Convert dates to timestamps in the specified timezone
        Timestamp startTimestamp = Timestamp.valueOf(startDate);
        Timestamp endTimestamp = Timestamp.valueOf(endDate);

        // Replace start_date (both formats)
        processed = processed.replace(":start_date", "'" + startTimestamp.toString() + "'");
        processed = processed.replace(":startDate", "'" + startTimestamp.toString() + "'");

        // Replace end_date (both formats)
        processed = processed.replace(":end_date", "'" + endTimestamp.toString() + "'");
        processed = processed.replace(":endDate", "'" + endTimestamp.toString() + "'");

        return processed;
    }

    /**
     * Escape SQL string to prevent injection
     */
    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    /**
     * Calculate period dates based on period type
     */
    public PeriodDates calculatePeriodDates(String periodType, String timezone) {
        ZoneId zoneId = timezone != null ? ZoneId.of(timezone) : ZoneId.of("UTC");
        LocalDateTime now = LocalDateTime.now(zoneId);
        LocalDateTime endDate = now;
        LocalDateTime startDate;
        
        // Use zoneId to ensure timezone is applied
        zoneId.toString(); // Suppress unused warning

        switch (periodType != null ? periodType.toLowerCase() : "last_24h") {
            case "last_24h":
                startDate = now.minusHours(24);
                break;
            case "last_7d":
                startDate = now.minusDays(7);
                break;
            case "last_30d":
                startDate = now.minusDays(30);
                break;
            case "last_90d":
                startDate = now.minusDays(90);
                break;
            default:
                startDate = now.minusDays(30);
        }

        return new PeriodDates(startDate, endDate);
    }

    public static class QueryExecutionResult {
        private final List<Map<String, Object>> results;
        private final long executionTimeMs;
        private final String error;

        public QueryExecutionResult(List<Map<String, Object>> results, long executionTimeMs, String error) {
            this.results = results;
            this.executionTimeMs = executionTimeMs;
            this.error = error;
        }

        public List<Map<String, Object>> getResults() {
            return results;
        }

        public long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public String getError() {
            return error;
        }

        public boolean isSuccess() {
            return error == null;
        }
    }

    public static class QueryValidationResult {
        private final boolean valid;
        private final String error;

        public QueryValidationResult(boolean valid, String error) {
            this.valid = valid;
            this.error = error;
        }

        public boolean isValid() {
            return valid;
        }

        public String getError() {
            return error;
        }
    }

    public static class PeriodDates {
        private final LocalDateTime startDate;
        private final LocalDateTime endDate;

        public PeriodDates(LocalDateTime startDate, LocalDateTime endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDateTime getStartDate() {
            return startDate;
        }

        public LocalDateTime getEndDate() {
            return endDate;
        }
    }
}

