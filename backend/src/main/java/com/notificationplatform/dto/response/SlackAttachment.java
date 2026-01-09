package com.notificationplatform.dto.response;

import java.util.List;
import java.util.Map;

public class SlackAttachment {

    private String color;
    private String title;
    private String text;
    private String fallback;
    private List<SlackField> fields;
    private Map<String, Object> actions;

    // Getters and Setters
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public List<SlackField> getFields() {
        return fields;
    }

    public void setFields(List<SlackField> fields) {
        this.fields = fields;
    }

    public Map<String, Object> getActions() {
        return actions;
    }

    public void setActions(Map<String, Object> actions) {
        this.actions = actions;
    }

    public static class SlackField {
        private String title;
        private String value;
        private boolean shortField;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isShortField() {
            return shortField;
        }

        public void setShortField(boolean shortField) {
            this.shortField = shortField;
        }
    }
}

