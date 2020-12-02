package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "T_SCHEDULE_REPORT")
public class ScheduleReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SCHEDULEREPORT_ID")
    @SequenceGenerator(name = "S_SCHEDULEREPORT_ID", sequenceName = "S_SCHEDULEREPORT_ID", allocationSize = 1)    
    private Long id;
    // @ManyToOne
    // @JoinColumn(name = "PROCESS_ID")
    @Column(name = "PROCESS_ID")
    private Long processId;
    @Column(name = "RECURRANCE_PATTERN")
    private char recurrencePattern;
    @Column(name = "SETTINGS")
    private String settings;            
    @Column(name = "RANGE_LENGTH")
    private Long rangeLength;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SCHEDULE_START_TIME")
    private Date startDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "SCHEDULE_END_TIME")
    private Date endDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "additional_emails")
    private String additionalEmails;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public char getRecurrencePattern() {
        return this.recurrencePattern;
    }

    public void setRecurrencePattern(char recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public String getSettings() {
        return this.settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

    public Long getProcessId() {
        return this.processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getRangeLength() {
        return this.rangeLength;
    }

    public void setRangeLength(Long rangeLength) {
        this.rangeLength = rangeLength;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

	public String getAdditionalEmails() {
		return additionalEmails;
	}

	public void setAdditionalEmails(String additionalEmails) {
		this.additionalEmails = additionalEmails;
	}

}