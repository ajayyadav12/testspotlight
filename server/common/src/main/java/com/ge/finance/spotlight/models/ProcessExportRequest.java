package com.ge.finance.spotlight.models;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="T_PROCESS_EXPORT_REQUEST")
public class ProcessExportRequest {

    public enum State {
        REQUESTED,
        ACCEPTED,
        DECLINED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PROCESS_EXPORT_REQUEST_ID")
    @SequenceGenerator(name = "S_PROCESS_EXPORT_REQUEST_ID", sequenceName = "S_PROCESS_EXPORT_REQUEST_ID", allocationSize = 1)
    private Long id;
    private Long processId;
    private Long userId;
    private String settings;
    private char state;
    private String notes = "";
    @Temporal(TemporalType.TIMESTAMP)
    private Date requested;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String[] getSettings() {
        return settings.split(",");
    }

    public void setSettings(String[] settings) {
        this.settings = String.join(",", settings);
    }

    public State getState() {
        switch (state) {
            case 'A':
                return State.ACCEPTED;
            case 'D':
                return State.DECLINED;
            default:
                return State.REQUESTED;
        }
    }

    public void setState(State state) {
        switch (state) {
            case ACCEPTED:
                this.state = 'A';
                break;
            case DECLINED:
                this.state = 'D';
                break;
            case REQUESTED:
                this.state = 'R';
                break;
        }
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getRequested() {
        return requested;
    }

    public void setRequested(Date requested) {
        this.requested = requested;
    }

}
