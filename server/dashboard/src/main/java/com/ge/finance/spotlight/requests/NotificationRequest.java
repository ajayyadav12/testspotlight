package com.ge.finance.spotlight.requests;

public class NotificationRequest {

    private Long processStepId;
    private Long statusId;
    private Boolean enableTextMessaging;
    private String escalationType;
    private String submissionType;

    public Long getProcessStepId() {
        return processStepId;
    }

    public void setProcessStepId(Long processStepId) {
        this.processStepId = processStepId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Boolean getEnableTextMessaging() {
        return enableTextMessaging;
    }

    public void setEnableTextMessaging(Boolean enableTextMessaging) {
        this.enableTextMessaging = enableTextMessaging;
    }

    public String getEscalationType() {
        return escalationType;
    }

    public void setEscalationType(String escalationType) {
        this.escalationType = escalationType;
    }

    public String getSubmissionType() {
        return submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

}
