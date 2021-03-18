package com.ge.finance.spotlight.dto;

import java.util.Date;
import java.util.List;

public class NotificationsDTO {

    public List<?> delayedList;

    private long id;
    private int submissionId;
    private String processName;
    private String processType;
    private long processId;
    private long scheduleId;
    private Date startTime;
    private Date endTime;
    private Long scheduleDefID;

    public Long getScheduleDefID() {
        return this.scheduleDefID;
    }

    public void setScheduleDefID(Long scheduleDefID) {
        this.scheduleDefID = scheduleDefID;
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

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public long getProcessId() {
        return processId;
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

    public NotificationsDTO() {
    }

    public NotificationsDTO(List<?> delayedList) {
        this.delayedList = delayedList;
    }
}