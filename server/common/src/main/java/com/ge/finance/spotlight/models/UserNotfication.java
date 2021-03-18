package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "T_USER_NOTIFICATIONS")
public class UserNotfication {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTIFICATION_ID")
    @SequenceGenerator(name = "NOTIFICATION_ID", sequenceName = "NOTIFICATION_ID", allocationSize = 1)
    private Long id;

    @Column(name = "sso")
    private long sso;

    @Column(name = "submission_id")
    private int submissionId;

    @Column(name = "process_name")
    private String processName;

    @Column(name = "status")
    private String status;

    @Column(name = "PROCESS_TYPE")
    private String processType;

    @Column(name = "process_id")
    private long processId;

    @Column(name = "schedule_id")
    private long scheduleId;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(name = "scheduledef_id")
    private long scheduledefId;

    public long getScheduledefId() {
        return scheduledefId;
    }

    public void setScheduledefId(long scheduledefId) {
        this.scheduledefId = scheduledefId;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public long getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(long scheduleId) {
        this.scheduleId = scheduleId;
    }

    public long getSso() {
        return sso;
    }

    public void setSso(long sso) {
        this.sso = sso;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

}