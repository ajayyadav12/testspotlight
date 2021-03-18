package com.ge.finance.spotlight.dto;

import java.util.Date;

import com.ge.finance.spotlight.models.Process;

public class ScheduleDefinitionDTO {

	private Long id;
	private Process process;
	private Long processWorkflowId;
	private String settings;
	private int tolerance;
	private char scheduleChangeFlag;
	private Date criticalDate;
	private String startTime;
	private String endTime;
	private Date scheduleStartDate;
	private Date scheduleEndDate;
	private char recurrencePattern;
	private int RecurrenceTime;
	private Long duration;
	private boolean isPredecessorEndTime;
	private Long predecessorScheduleSubmissionId;
	private int predecessorEndTime;

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

	public Long getProcessWorkflowId() {
		return processWorkflowId;
	}

	public void setProcessWorkflowId(Long processWorkflowId) {
		this.processWorkflowId = processWorkflowId;
	}

	public String getSettings() {
		return settings;
	}

	public void setSettings(String settings) {
		this.settings = settings;
	}

	public Date getCriticalDate() {
		return criticalDate;
	}

	public void setCriticalDate(Date criticalDate) {
		this.criticalDate = criticalDate;
	}

	public boolean getScheduleChangeFlag() {
		return scheduleChangeFlag == 'Y';
	}

	public void setScheduleChangeFlag(boolean scheduleChangeFlag) {
		this.scheduleChangeFlag = scheduleChangeFlag ? 'Y' : 'N';
	}

	public int getTolerance() {
		return tolerance;
	}

	public void setTolerance(int tolerance) {
		this.tolerance = tolerance;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Date getScheduleStartDate() {
		return scheduleStartDate;
	}

	public void setScheduleStartDate(Date scheduleStartDate) {
		this.scheduleStartDate = scheduleStartDate;
	}

	public Date getScheduleEndDate() {
		return scheduleEndDate;
	}

	public void setScheduleEndDate(Date scheduleEndDate) {
		this.scheduleEndDate = scheduleEndDate;
	}

	public char getRecurrencePattern() {
		return recurrencePattern;
	}

	public void setRecurrencePattern(char recurrencePattern) {
		this.recurrencePattern = recurrencePattern;
	}

	public int getRecurrenceTime() {
		return RecurrenceTime;
	}

	public void setRecurrenceTime(int recurrenceTime) {
		RecurrenceTime = recurrenceTime;
	}

	public Long getDuration() {
		return this.duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public boolean isIsPredecessorEndTime() {
		return this.isPredecessorEndTime;
	}

	public boolean getIsPredecessorEndTime() {
		return this.isPredecessorEndTime;
	}

	public void setIsPredecessorEndTime(boolean isPredecessorEndTime) {
		this.isPredecessorEndTime = isPredecessorEndTime;
	}

	public Long getPredecessorScheduleSubmissionId() {
		return this.predecessorScheduleSubmissionId;
	}

	public void setPredecessorScheduleSubmissionId(Long predecessorScheduleSubmissionId) {
		this.predecessorScheduleSubmissionId = predecessorScheduleSubmissionId;
	}

	public int getPredecessorEndTime() {
		return this.predecessorEndTime;
	}

	public void setPredecessorEndTime(int predecessorEndTime) {
		this.predecessorEndTime = predecessorEndTime;
	}

}
