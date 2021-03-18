package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_PROCESS_STEP")
@NamedQuery(name = "ProcessStep.findByProcessId", query = "select p from ProcessStep p where p.processId = :processId order by p.id")
public class ProcessStep {

    public static final String START = "start";
    public static final String END = "end";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PROCESS_STEP_ID")
    @SequenceGenerator(name = "S_PROCESS_STEP_ID", sequenceName = "S_PROCESS_STEP_ID", allocationSize = 1)
    private Long id;
    @Column(name = "process_id")
    private Long processId;
    private Long associatedStepId;
    private String name;
    private Long duration;
    private Long manualDuration;
    private char required = 'N';
    private char parallel = 'N';
    private char disabled = 'N';

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean getRequired() {
        return required == 'Y';
    }

    public void setRequired(boolean required) {
        this.required = required ? 'Y' : 'N';
    }

    public boolean getParallel() {
        return parallel == 'Y';
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel ? 'Y' : 'N';
    }

    public boolean getDisabled() {
        return disabled == 'Y';
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled ? 'Y' : 'N';
    }

    public Long getAssociatedStepId() {
        return this.associatedStepId;
    }

    public void setAssociatedStepId(Long associatedStepId) {
        this.associatedStepId = associatedStepId;
    }

    public Long getManualDuration() {
        return this.manualDuration;
    }

    public void setManualDuration(Long manualDuration) {
        this.manualDuration = manualDuration;
    }

}
