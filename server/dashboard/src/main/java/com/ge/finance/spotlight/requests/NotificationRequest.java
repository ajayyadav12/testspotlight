package com.ge.finance.spotlight.requests;

public class NotificationRequest {

    private Long processStepId;
    private Long statusId;
    private Long distributionListId;
    private Long notificationTemplateId;

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

    public Long getDistributionListId() {
        return distributionListId;
    }

    public void setDistributionListId(Long distributionListId) {
        this.distributionListId = distributionListId;
    }

    public Long getNotificationTemplateId() {
        return notificationTemplateId;
    }

    public void setNotificationTemplateId(Long notificationTemplateId) {
        this.notificationTemplateId = notificationTemplateId;
    }

}
