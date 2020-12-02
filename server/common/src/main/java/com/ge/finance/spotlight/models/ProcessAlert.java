package com.ge.finance.spotlight.models;

import javax.persistence.*;

public class ProcessAlert {

    private Long id;

    @Column(name = "process_id")
    private Long processId;
    private char failedEscalation = 'Y';
    private char longRunningSubmission = 'Y';
    private char longRunningStep = 'Y';

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

    public boolean getFailedEscalation() {
        return failedEscalation == 'Y';
    }

    public void setFailedEscalation(boolean failedEscalation) {
        this.failedEscalation = failedEscalation ? 'Y' : 'N';
    }

    public boolean getLongRunningSubmission() {
        return longRunningSubmission == 'Y';
    }

    public void setLongRunningSubmission(boolean longRunningSubmission) {
        this.longRunningSubmission = longRunningSubmission ? 'Y' : 'N';
    }

    public boolean getLongRunningStep() {
        return longRunningStep == 'Y';
    }

    public void setLongRunningStep(boolean longRunningStep) {
        this.longRunningStep = longRunningStep ? 'Y' : 'N';
    }

}