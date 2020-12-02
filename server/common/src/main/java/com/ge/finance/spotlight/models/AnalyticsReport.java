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
@Table(name = "T_ANALYTICS_REPORT")
public class AnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_ANALYTICSREPORT_ID")
    @SequenceGenerator(name = "S_ANALYTICSREPORT_ID", sequenceName = "S_ANALYTICSREPORT_ID", allocationSize = 1)
    private Long id;
    @Column(name = "submission_level")
    private String submissionLevel;
    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time")
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_time")
    private Date endTime;
    @Column(name = "schedule_report_id")
    private Long scheduleReportId;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;

    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Long getScheduleReportId() {
        return this.scheduleReportId;
    }

    public void setScheduleReportId(Long scheduleReportId) {
        this.scheduleReportId = scheduleReportId;
    }

    public Process getProcess() {
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getSubmissionLevel() {
        return this.submissionLevel;
    }

    public void setSubmissionLevel(String submissionLevel) {
        this.submissionLevel = submissionLevel;
    }

}
