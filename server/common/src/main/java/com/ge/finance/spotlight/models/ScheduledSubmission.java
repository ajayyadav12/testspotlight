package com.ge.finance.spotlight.models;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="T_SCHEDULED_SUBMISSION")
public class ScheduledSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SCHEDULED_SUBMISSION_ID")
    @SequenceGenerator(name="S_SCHEDULED_SUBMISSION_ID", sequenceName = "S_SCHEDULED_SUBMISSION_ID", allocationSize = 1)
    private Long id;
    @Column(name="schedule_def_id")
    private Long scheduleDefinitionId;
    @OneToOne
    @JoinColumn(name="submission_id")
    private Submission submission;
    @ManyToOne
    @JoinColumn(name="process_id")
    private Process process;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="start_time")
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="end_time")
    private Date endTime;
    private int tolerance;
    private int notifications;
    @Column(name="acknowledgement_flag")
    private Boolean acknowledgementFlag = false;
    @Column(name="acknowledgement_note")
    private String acknowledgementNote;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="acknowledgement_date")
    private Date acknowledgementDate;
    @Column(name="ack_delayed_email_status")
    private Character ackDelayedEmailStatus = '0';
    @Column(name="disabled_flag")
    private Boolean disabled = false;
    @Column(name="disabled_note")
    private String disabledNote;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScheduleDefinitionId() {
        return scheduleDefinitionId;
    }

    public void setScheduleDefinitionId(Long scheduleDefinitionId) {
        this.scheduleDefinitionId = scheduleDefinitionId;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public int getNotifications() {
        return notifications;
    }

    public void setNotifications(int notifications) {
        this.notifications = notifications;
    }

	public Boolean getAcknowledgementFlag() {
		return acknowledgementFlag;
	}

	public void setAcknowledgementFlag(Boolean acknowledgementFlag) {
		this.acknowledgementFlag = acknowledgementFlag;
	}

	public String getAcknowledgementNote() {
		return acknowledgementNote;
	}

	public void setAcknowledgementNote(String acknowledgementNote) {
		this.acknowledgementNote = acknowledgementNote;
	}

	public Date getAcknowledgementDate() {
		return acknowledgementDate;
	}

	public void setAcknowledgementDate(Date acknowledgementDate) {
		this.acknowledgementDate = acknowledgementDate;
	}

    /**
     * A = Sent to app owner
     * B = Sent to app owner's manager     
     */
	public Character getAckDelayedEmailStatus() {
		return ackDelayedEmailStatus;
	}

    /**
     * A = Sent to app owner
     * B = Sent to app owner's manager     
     */
	public void setAckDelayedEmailStatus(Character ackDelayedEmailStatus) {
		this.ackDelayedEmailStatus = ackDelayedEmailStatus;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public void setDisabled(Boolean disabled) {
		this.disabled = disabled;
	}

	public String getDisabledNote() {
		return disabledNote;
	}

	public void setDisabledNote(String disabledNote) {
		this.disabledNote = disabledNote;
	}


}
