package com.notificationplatform.service.file;



import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
/**
 * Service for parsing different file formats
 */
@Slf4j
@Component
public class FileProcessor {

    /**
     * Parse CSV file
     */
    public List<Map<String, Object>> parseCsv(InputStream inputStream) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return rows;
            }
            
            String[] headers = parseCsvLine(headerLine);
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                String[] values = parseCsvLine(line);
                Map<String, Object> row = new HashMap<>();
                
                for (int i = 0; i < headers.length && i < values.length; i++) {
                    row.put(headers[i].trim(), values[i].trim());
                }
                
                rows.add(row);
            }
        }
        
        return rows;
    }

    /**
     * Parse JSON file (array or object)
     */
    public List<Map<String, Object>> parseJson(InputStream inputStream) throws IOException {
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        
        // Simple JSON parsing - for production, use Jackson or Gson
        // This is a basic implementation for MVP
        if (content.trim().startsWith("[")) {
            // JSON array
            return parseJsonArray(content);
        } else {
            // Single JSON object - wrap in list
            Map<String, Object> obj = parseJsonObject(content);
            return Collections.singletonList(obj);
        }
    }

    /**
     * Parse JSONL (JSON Lines) file
     */
    public List<Map<String, Object>> parseJsonl(InputStream inputStream) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                Map<String, Object> obj = parseJsonObject(line);
                rows.add(obj);
            }
        }
        
        return rows;
    }

    /**
     * Parse Excel file (basic implementation - for MVP)
     * Note: For production, use Apache POI library
     */
    public List<Map<String, Object>> parseExcel(InputStream inputStream, String format) throws IOException {
        // For MVP, we'll return empty list
        // In production, integrate with Apache POI
        log.warn("Excel parsing not fully implemented for MVP. Please use CSV or JSON format.");
        return new ArrayList<>();
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    private List<Map<String, Object>> parseJsonArray(String content) {
        // Basic JSON array parsing
        // For MVP, this is simplified - in production use Jackson
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Remove brackets
        content = content.trim();
        if (content.startsWith("[")) {
            content = content.substring(1);
        }
        if (content.endsWith("]")) {
            content = content.substring(0, content.length() - 1);
        }
        
        // Split by objects (simplified - assumes simple JSON)
        // This is a basic implementation
        String[] objects = content.split("\\},\\s*\\{");
        
        for (String objStr : objects) {
            objStr = objStr.trim();
            if (objStr.startsWith("{")) {
                objStr = objStr.substring(1);
            }
            if (objStr.endsWith("}")) {
                objStr = objStr.substring(0, objStr.length() - 1);
            }
            
            Map<String, Object> obj = parseJsonObject("{" + objStr + "}");
            result.add(obj);
        }
        
        return result;
    }

    private Map<String, Object> parseJsonObject(String content) {
        // Basic JSON object parsing
        // For MVP, this is simplified - in production use Jackson
        Map<String, Object> result = new HashMap<>();
        
        content = content.trim();
        if (content.startsWith("{")) {
            content = content.substring(1);
        }
        if (content.endsWith("}")) {
            content = content.substring(0, content.length() - 1);
        }
        
        // Parse key-value pairs
        String[] pairs = content.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                result.put(key, value);
            }
        }
        
        return result;
    }
}

