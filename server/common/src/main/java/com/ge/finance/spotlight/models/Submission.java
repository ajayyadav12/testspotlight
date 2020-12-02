package com.ge.finance.spotlight.models;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "T_SUBMISSION")
public class Submission {

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
    private char adHoc = 'N';
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "submissionId")
    private Set<SubmissionStep> steps;
    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;
    @ManyToOne
    @JoinColumn(name = "submission_parent_id")
    private ParentSubmission parent;
    @Column(name = "acknowledgement_flag")
    private Boolean acknowledgementFlag = false;
    @Column(name = "acknowledgement_note")
    private String acknowledgementNote;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "acknowledgement_date")
    private Date acknowledgementDate;    
    @Column(name = "notes")
    private String notes = "";
    @Column(name = "ack_failed_email_status")
    private Character ackFailedEmailStatus = '0';
    @Column(name = "period_Year")
    private int periodYear;
    @Column(name = "period_Quarter")
    private int periodQuarter;
    @Column(name = "period_Month")
    private int periodMonth;
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
     * @return the parent
     */
    public Long getParentId() {
        return parent != null ? parent.getId() : 0l;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(ParentSubmission parent) {
        this.parent = parent;
    }

    /**
     * @return the acknowledgementFlag
     */
    public Boolean isAcknowledgementFlag() {
        return this.acknowledgementFlag;
    }

    /**
     * @param acknowledgementFlag the acknowledgementFlag to set
     */
    public void setAcknowledgementFlag(Boolean acknowledgementFlag) {
        this.acknowledgementFlag = acknowledgementFlag;
    }

    /**
     * @return the acknowledgementNote
     */
    public String getAcknowledgementNote() {
        return acknowledgementNote;
    }

    /**
     * @param acknowledgementNote the acknowledgementNote to set
     */
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
     * A = Sent to app owner B = Sent to app owner's manager C = A when no new
     * submission exists D = B when no new submission exists
     */
    public Character getAckfailedEmailStatus() {
        return ackFailedEmailStatus;
    }

    /**
     * A = Sent to app owner B = Sent to app owner's manager C = A when no new
     * submission exists D = B when no new submission exists
     */
    public void setAckfailedEmailStatus(Character ackfailedEmailStatus) {
        this.ackFailedEmailStatus = ackfailedEmailStatus;
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

    public int getPeriodYear() {
        return periodYear;
    }

    public void setPeriodYear(int periodYear) {
        this.periodYear = periodYear;
    }

    public int getPeriodQuarter() {
        return periodQuarter;
    }

    public void setPeriodQuarter(int periodQuarter) {
        this.periodQuarter = periodQuarter;
    }

    public int getPeriodMonth() {
        return periodMonth;
    }

    public void setPeriodMonth(int periodMonth) {
        this.periodMonth = periodMonth;
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getElapsedTime() {
        Date to = (this.endTime != null) ? this.endTime : new Date();
        long msec = to.getTime() - this.startTime.getTime();
        long mins = (long) Math.floor(msec / 60000);
        long hrs = (long) Math.floor(mins / 60);
        long days = (long) Math.floor(hrs / 24);
        mins = mins % 60;
        hrs = hrs % 24;
        long secs = (msec / 1000) % 60;
        String finalResult = "";
        if (days != 0) {
            finalResult += days + "d ";
        }
        if (hrs != 0) {
            finalResult += hrs + "h ";
        }
        if (mins != 0) {
            finalResult += mins + "m ";
        }
        if (secs != 0) {
            finalResult += Math.round(secs) + "s";
        }
        return finalResult;
    }

    public String getBu() {
        return bu;
    }

    public void setBu(String bu) {
        this.bu = bu;
    }

}
