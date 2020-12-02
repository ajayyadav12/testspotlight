package com.ge.finance.spotlight.models;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "T_SUBMISSION")
public class ChildSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SUBMISSION_ID")
    @SequenceGenerator(name = "S_SUBMISSION_ID", sequenceName = "S_SUBMISSION_ID", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time")
    private Date updateTime;
    private int warnings;
    private int records;
    @Column(name = "fatal_errors")
    private int errors;
    private String period;
    @Column(name = "period_sequence")
    private int periodSequence;
    @Column(name = "ad_hoc_flag")
    private char adHoc;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "submissionId")
    private Set<SubmissionStep> steps;
    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;
    @Column(name = "submission_parent_id")
    private Long submissionParentId;
    @Column(name = "notes")
    private String notes = "";
    @Column(name = "alt_id")
    private String altId;
    @Column(name = "bu")
    private String bu;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getWarnings() {
        return warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public int getRecords() {
        return records;
    }

    public void setRecords(int records) {
        this.records = records;
    }

    public int getErrors() {
        return errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public int getPeriodSequence() {
        return periodSequence;
    }

    public void setPeriodSequence(int periodSequence) {
        this.periodSequence = periodSequence;
    }

    public boolean getAdHoc() {
        return adHoc == 'Y';
    }

    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc ? 'Y' : 'N';
    }

    public Set<SubmissionStep> getSteps() {
        return steps;
    }

    public void setSteps(Set<SubmissionStep> steps) {
        this.steps = steps;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return the submissionParentId
     */
    public Long getSubmissionParentId() {
        return submissionParentId;
    }

    /**
     * @param submissionParentId the submissionParentId to set
     */
    public void setSubmissionParentId(Long submissionParentId) {
        this.submissionParentId = submissionParentId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        if (notes != "") {
            this.notes += this.notes == "" ? notes : (" \n " + notes);
        }
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

}
