package com.ge.finance.spotlight.dto;

import java.util.Date;

/**
 * ProcessSummaryDTO
 */

public class ScheduleSummaryDTO {

    private Long id;
    private String submissionLevel;
    private String processName;
    private String settings;
    private Long processId;
    private Date startDate;
    private Date endDate;    
    private Long rangeLength;;
    private char recurrencePattern;
    private String additionalEmails;

    public String getProcessName() {
        return this.processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public char getRecurrencePattern() {
        return this.recurrencePattern;
    }

    public void setRecurrencePattern(char recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    public Long getProcessId() {
        return this.processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String getSettings() {
        return this.settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
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

    public Long getRangeLength() {
        return this.rangeLength;
    }

    public void setRangeLength(Long rangeLength) {
        this.rangeLength = rangeLength;
    }

    public String getSubmissionLevel() {
        return this.submissionLevel;
    }

    public void setSubmissionLevel(String submissionLevel) {
        this.submissionLevel = submissionLevel;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getAdditionalEmails() {
		return additionalEmails;
	}

	public void setAdditionalEmails(String additionalEmails) {
		this.additionalEmails = additionalEmails;
	}

}