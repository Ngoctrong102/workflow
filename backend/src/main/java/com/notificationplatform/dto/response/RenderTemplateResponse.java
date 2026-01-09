package com.notificationplatform.dto.response;

public class RenderTemplateResponse {

    private String renderedSubject;
    private String renderedBody;

    // Getters and Setters
    public String getRenderedSubject() {
        return renderedSubject;
    }

    public void setRenderedSubject(String renderedSubject) {
        this.renderedSubject = renderedSubject;
    }

    public String getRenderedBody() {
        return renderedBody;
    }

    public void setRenderedBody(String renderedBody) {
        this.renderedBody = renderedBody;
    }
}

