package com.ge.finance.spotlight.dto;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ge.finance.spotlight.models.ScheduleDefinition;
import com.ge.finance.spotlight.models.Submission;

import java.util.Date;

@Entity
@Table(name="T_SCHEDULED_SUBMISSION")
public class ScheduledSubmissionDTO {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SCHEDULED_SUBMISSION_ID")
    @SequenceGenerator(name="S_SCHEDULED_SUBMISSION_ID", sequenceName = "S_SCHEDULED_SUBMISSION_ID", allocationSize = 1)
    private Long id;   
    @OneToOne     
    @JoinColumn(name="submission_id")
    @JsonIgnore
    private Submission submission;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="start_time")
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="end_time")
    private Date endTime;    
    @Column(name = "acknowledgement_note")
    private String acknowledgementNote;
    @Column(name = "disabled_note")
    private String disabledNote;
    @OneToOne     
    @JoinColumn(name="schedule_def_id")
    private ScheduleDefinition scheduleDefinition;
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

	public Submission getSubmission() {
		return submission;
	}

	public void setSubmission(Submission submission) {
		this.submission = submission;
	}

	public String getAcknowledgementNote() {
		return acknowledgementNote;
	}

	public void setAcknowledgementNote(String acknowledgementNote) {
		this.acknowledgementNote = acknowledgementNote;
	}

	public String getDisabledNote() {
		return disabledNote;
	}

	public void setDisabledNote(String disabledNote) {
		this.disabledNote = disabledNote;
	}

	public ScheduleDefinition getScheduleDefinition() {
		return scheduleDefinition;
	}

	public void setScheduleDefinition(ScheduleDefinition scheduleDefinition) {
		this.scheduleDefinition = scheduleDefinition;
	}
   

}
