package com.ge.finance.spotlight.models;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "T_SUBMISSION_STEP")
public class SubmissionStep {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SUBMISSION_STEP_ID")
    @SequenceGenerator(name = "S_SUBMISSION_STEP_ID", sequenceName = "S_SUBMISSION_STEP_ID", allocationSize = 1)
    private Long id;
    @Column(name = "submission_id")
    private Long submissionId;
    @ManyToOne
    @JoinColumn(name = "process_step_id")
    private ProcessStep processStep;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;    
    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;
    @Column(name = "notes")
    private String notes = "";
    @Column(name = "request_payload")
    private String requestPayload = "";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public ProcessStep getProcessStep() {
        return processStep;
    }

    public void setProcessStep(ProcessStep processStep) {
        this.processStep = processStep;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if (notes != "") {
            if (this.notes != null)
                this.notes += this.notes == "" ? notes : (" <br>" + notes);
            else
                this.notes = notes;
        }
    }

	public String getRequestPayload() {
		return requestPayload;
	}

	public void setRequestPayload(String requestPayload) {
        if (requestPayload != "") {
            this.requestPayload += this.requestPayload == "" ? requestPayload : (" \n " + requestPayload);
        }		
	}

}
