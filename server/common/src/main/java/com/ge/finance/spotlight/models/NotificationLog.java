package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "T_NOTIFICATION_LOG")
@NamedQuery(name = "NotificationLog.findAll", query = "select n from NotificationLog n order by n.id DESC")
public class NotificationLog {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_NOTIFICATION_LOG_ID")
	@SequenceGenerator(name = "S_NOTIFICATION_LOG_ID", sequenceName = "S_NOTIFICATION_LOG_ID", allocationSize = 1)
	private Long id;
	@Column(name = "notification_def_id")
	private Long notificationDefId;
	@Column(name = "submission_id")
	private Long submissionId;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "sendtime")
	private Date sendTime;
	@Column(name = "to_emails")
	private String toEmails;
	@Column(name = "notification_template_id")
	private Long notificationTemplate;
	@Column(name = "scheduled_submission_id")
	private Long scheduledSubmissionId;
	@Column(name = "process_id")
	private Long processId;
	@Column(name = "process_step_id")
	private Long processStepId;
	@Column(name = "body")
	private String body;
	@Column(name = "subject")
	private String subject;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getNotificationDefId() {
		return notificationDefId;
	}

	public void setNotificationDefId(Long notificationDefId) {
		this.notificationDefId = notificationDefId;
	}

	public Long getSubmissionId() {
		return submissionId;
	}

	public void setSubmissionId(Long submissionId) {
		this.submissionId = submissionId;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}

	public String getToEmails() {
		return toEmails;
	}

	public void setToEmails(String toEmails) {
		this.toEmails = toEmails;
	}

	public Long getNotificationTemplate() {
		return notificationTemplate;
	}

	public void setNotificationTemplate(Long notificationTemplate) {
		this.notificationTemplate = notificationTemplate;
	}

	public Long getScheduledSubmissionId() {
		return scheduledSubmissionId;
	}

	public void setScheduledSubmissionId(Long scheduledSubmissionId) {
		this.scheduledSubmissionId = scheduledSubmissionId;
	}

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public Long getProcessStepId() {
		return processStepId;
	}

	public void setProcessStepId(Long processStepId) {
		this.processStepId = processStepId;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
}
