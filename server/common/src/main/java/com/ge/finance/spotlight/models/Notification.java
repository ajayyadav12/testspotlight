package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_NOTIFICATION_DEF")
@NamedQuery(name = "Notification.findByProcessId", query = "select n from Notification n where n.processId = :processId order by n.id")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_NOTIFICATION_ID")
    @SequenceGenerator(name = "S_NOTIFICATION_ID", sequenceName = "S_NOTIFICATION_ID", allocationSize = 1)
    private Long id;
    @Column(name = "process_id")
    private Long processId;
    @ManyToOne
    @JoinColumn(name = "process_step_id")
    private ProcessStep processStep;
    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;
    @Column(name = "additional_emails")
    private String additionalEmails = "";
    @Column(name = "text_msg")
    private Boolean enableTextMessaging = false;
    @ManyToOne
    @JoinColumn(name = "created_for")
    private User createdFor;
    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public ProcessStep getProcessStep() {
        return this.processStep;
    }

    public void setProcessStep(ProcessStep processStep) {
        this.processStep = processStep;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getNotificationTemplateID() {
        Long type = 0l;

        if (this.processStep != null) {
            // Step Level
            type = NotificationTemplate.BY_STEP_STATUS;
        } else {
            if (this.status != null) {
                // Submission Level
                type = NotificationTemplate.BY_SUBMISSION_STATUS;
            } else {
                // Delayed Submission
                type = NotificationTemplate.DELAYED_SUBMISSION;
            }
        }
        return type;
    }

    public String getAdditionalEmails() {
        return (this.additionalEmails == null) ? "" : this.additionalEmails;
    }

    public void setAdditionalEmails(String additionalEmails) {
        this.additionalEmails = additionalEmails;
    }

    public User getCreatedFor() {
        return this.createdFor;
    }

    public void setCreatedFor(User createdFor) {
        this.createdFor = createdFor;
    }

    public User getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean isEnableTextMessaging() {
        return this.enableTextMessaging;
    }

    public Boolean getEnableTextMessaging() {
        return this.enableTextMessaging;
    }

    public void setEnableTextMessaging(Boolean enableTextMessaging) {
        this.enableTextMessaging = enableTextMessaging;
    }

}
