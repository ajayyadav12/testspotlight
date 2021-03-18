package com.ge.finance.spotlight.dto;

public class ProcessAlert {    
    
    private char submissionDelayedEscalationAlrt = 'Y';
    private char submissionEscalationAlrt = 'Y';
    private char longRunningSubAlrt = 'Y';
    private char longRunningStepAlrt = 'Y';    
    private char requiredStepAlrt = 'Y';   

    public boolean getSubmissionDelayedEscalationAlrt() {
        return submissionDelayedEscalationAlrt == 'Y';
    }

    public void setSubmissionDelayedEscalationAlrt(boolean submissionDelayedEscalationAlrt) {
        this.submissionDelayedEscalationAlrt = submissionDelayedEscalationAlrt ? 'Y' : 'N';
    }

    public boolean getSubmissionEscalationAlrt() {
        return submissionEscalationAlrt == 'Y';
    }

    public void setSubmissionEscalationAlrt(boolean submissionEscalationAlrt) {
        this.submissionEscalationAlrt = submissionEscalationAlrt ? 'Y' : 'N';
    }

    public boolean getLongRunningSubAlrt() {
        return longRunningSubAlrt == 'Y';
    }

    public void setLongRunningSubAlrt(boolean longRunningSubAlrt) {
        this.longRunningSubAlrt = longRunningSubAlrt ? 'Y' : 'N';
    }

    public boolean getLongRunningStepAlrt() {
        return longRunningStepAlrt == 'Y';
    }

    public void setLongRunningStepAlrt(boolean longRunningStepAlrt) {
        this.longRunningStepAlrt = longRunningStepAlrt ? 'Y' : 'N';
    }

    public boolean getRequiredStepAlrt() {
        return requiredStepAlrt == 'Y';
    }

    public void setRequiredStepAlrt(boolean requiredStepAlrt) {
        this.requiredStepAlrt = requiredStepAlrt ? 'Y' : 'N';
    }

}