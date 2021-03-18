package com.ge.finance.spotlight.models;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

import com.ge.finance.spotlight.security.Constants;

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
    @JoinColumn(name = "created_by")
    private User createdBy;
    @Column(name = "escalation_type")
    private String escalationType = "";
    @Column(name = "submission_type")
    private String submissionType = "";
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "notificationId", cascade = { CascadeType.REMOVE })
    private Set<NotificationMobile> userMobiles;

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
        } else if (this.submissionType != null) {

            if (this.submissionType.equalsIgnoreCase(Constants.EDITED_SUBMISSION)) {
                type = NotificationTemplate.EDITED_SUBMISSION;
            } else if (this.submissionType.equalsIgnoreCase(Constants.ACKNOWLEDGED_SUBMISSION)) {
                type = NotificationTemplate.ACKNOWLEDGED_SUBMISSION;
            } else if (this.submissionType.equalsIgnoreCase(Constants.DISABLED_SUBMISSION)) {
                type = NotificationTemplate.DISABLED_SUBMISSION;
            } else if (this.submissionType.equalsIgnoreCase(Constants.LONG_RUNNING_SUBMISSION)) {
                type = NotificationTemplate.LONG_RUN_SUBMISSION;
            } else if (this.submissionType.equalsIgnoreCase(Constants.LONG_RUNNING_STEPS)) {
                type = NotificationTemplate.LONG_RUN_SUBMISSION_STEP_DELAYED;
            } else if (this.submissionType.equalsIgnoreCase(Constants.DELAYED_SUBMISSION)) {
                type = NotificationTemplate.DELAYED_SUBMISSION;
            } else if (this.submissionType.equalsIgnoreCase(Constants.DELAYED_ESCALATION)) {
                type = NotificationTemplate.ESCALATION_DELAYED;
            } else if (this.submissionType.equalsIgnoreCase(Constants.SUBMISSION_STEP_INCOMPLETE)) {
                type = NotificationTemplate.SUBMISSION_STEP_INCOMPLETE;
            } else {
                type = NotificationTemplate.REQUIRED_STEP_NOTIFICATION;
            }

        } else {
            if (this.status != null) {
                // Submission Level
                type = NotificationTemplate.BY_SUBMISSION_STATUS;
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

    public String getEscalationType() {
        return (this.escalationType == null) ? "" : this.escalationType;
    }

    public void setEscalationType(String escalationType) {
        this.escalationType = escalationType;
    }

    public String getSubmissionType() {
        return (this.submissionType == null) ? "" : this.submissionType;
    }

    public void setSubmissionType(String submissionType) {
        this.submissionType = submissionType;
    }

    public Set<NotificationMobile> getUserMobiles() {
        return this.userMobiles != null ? this.userMobiles : new HashSet<NotificationMobile>();
    }

    public void setUserMobiles(Set<NotificationMobile> userMobiles) {
        this.userMobiles = userMobiles;
    }

}
