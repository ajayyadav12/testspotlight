package com.ge.finance.spotlight.requests;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

public class SubmissionStepRequest {

    private Long submissionId;
    private String altSubmissionId;
    @NotEmpty
    private String processStepName;
    private Date time;
    private String status;
    private boolean adHoc = false;
    private String period;
    private Integer records;
    private Integer warnings;
    private Integer errors;
    private String submissionNotes = "";
    private String stepNotes = "";
    @Length(max = 2, message = "Bad Format. Values allowed: PO, CO, RE, HC, AV, CA, OG")
    private String bu = "";
    @JsonIgnore
    private Date systemTime = new Date();
    private Date startTime;
    private Date endTime;
    private String secret;
    private String reportFile;
    private String dataFile;

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public String getProcessStepName() {
        return processStepName;
    }

    public void setProcessStepName(String processStepName) {
        this.processStepName = processStepName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean getAdHoc() {
        return adHoc;
    }

    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Integer getRecords() {
        return records;
    }

    public void setRecords(Integer records) {
        this.records = records;
    }

    public Integer getWarnings() {
        return warnings;
    }

    public void setWarnings(Integer warnings) {
        this.warnings = warnings;
    }

    public Integer getErrors() {
        return errors;
    }

    public void setErrors(Integer errors) {
        this.errors = errors;
    }

    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    public String getStepNotes() {
        return stepNotes;
    }

    public void setStepNotes(String stepNotes) {
        this.stepNotes = stepNotes;
    }

    public String getAltSubmissionId() {
        return altSubmissionId;
    }

    public void setAltSubmissionId(String altSubmissionId) {
        this.altSubmissionId = altSubmissionId;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

    public Date getStartTime() {
        return startTime != null ? startTime : systemTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime != null ? endTime : systemTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public boolean isAdHoc() {
        return this.adHoc;
    }

    public Date getSystemTime() {
        return this.systemTime;
    }

    public void setSystemTime(Date systemTime) {
        this.systemTime = systemTime;
    }

    public String getReportFile() {
        return this.reportFile;
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public String getDataFile() {
        return this.dataFile;
    }

    public void setDataFile(String dataFile) {
        this.dataFile = dataFile;
    }

}
