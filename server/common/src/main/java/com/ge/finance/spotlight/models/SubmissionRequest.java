package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "T_SUBMISSION_REQUEST")
public class SubmissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SUBMISSION_REQUEST_ID")
    @SequenceGenerator(name = "S_SUBMISSION_REQUEST_ID", sequenceName = "S_SUBMISSION_REQUEST_ID", allocationSize = 1)
    private Long id;

    @Column(name = "request")
    private String request;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "state")
    private String state;

    @Column(name = "process_id")
    private Long processId;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequest() {
        return this.request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Long getProcessId() {
        return this.processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

}
