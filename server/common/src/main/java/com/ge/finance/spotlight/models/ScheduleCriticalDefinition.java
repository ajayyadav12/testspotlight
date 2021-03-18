package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "T_SCHEDULE_CRITICAL_DEF")
@NamedQuery(name = "ScheduleCriticalDefinition.findByProcessId", query = "SELECT s FROM ScheduleCriticalDefinition s WHERE s.process.id = :processId ORDER BY s.id DESC")
public class ScheduleCriticalDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SCHEDULE_CRITICAL_DEF_ID")
    @SequenceGenerator(name = "S_SCHEDULE_CRITICAL_DEF_ID", sequenceName = "S_SCHEDULE_CRITICAL_DEF_ID", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "process_id")
    private Process process;
    @Column(name = "settings")
    private String settings;
    @Column(name = "schedule_change_flag")
    private char scheduleChangeFlag;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "schedule_start_date")
    private Date scheduleStartDate;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "schedule_end_date")
    private Date scheduleEndDate;
    @Column(name = "recurrence_pattern")
    private char recurrencePattern;

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

    public boolean getActive() {
        Date now = new Date();
        return this.scheduleEndDate.getTime() > now.getTime();
    }

}
