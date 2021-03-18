package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "T_SCHEDULE_DEF")
@NamedQuery(name = "ScheduleDefinition.findByProcessId", query = "SELECT s FROM ScheduleDefinition s WHERE s.process.id = :processId ORDER BY s.id DESC")
public class ScheduleDefinition {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SCHEDULE_ID")
	@SequenceGenerator(name = "S_SCHEDULE_ID", sequenceName = "S_SCHEDULE_ID", allocationSize = 1)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "process_id")
	private Process process;
	@Column(name = "process_workflow_id")
	private Long processWorkflowId;
	@Column(name = "settings")
	private String settings;
	@Column(name = "tolerance")
	private int tolerance;
	@Column(name = "schedule_change_flag")
	private char scheduleChangeFlag;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "start_time")
	private Date startTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "end_time")
	private Date endTime;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "schedule_start_date")
	private Date scheduleStartDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "schedule_end_date")
	private Date scheduleEndDate;
	@Column(name = "recurrence_pattern")
	private char recurrencePattern;
	@Column(name = "recurrence_time")
	private int RecurrenceTime;
	@Column(name = "predecessor_submission_id")
	private Long predecessorSubmissionId;

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

	public boolean getActive() {
		Date now = new Date();
		return this.scheduleEndDate.getTime() > now.getTime();
	}

	public Long getPredecessorSubmissionId() {
		return this.predecessorSubmissionId;
	}

	public void setPredecessorSubmissionId(Long predecessorSubmissionId) {
		this.predecessorSubmissionId = predecessorSubmissionId;
	}

}
