package com.ge.finance.spotlight.models;

public class StatusReport {

    private ParentSubmission submission;
    private long duration;

    /**
     * @return the submission
     */
    public ParentSubmission getSubmission() {
        return submission;
    }

    /**
     * @param submission the submission to set
     */
    public void setSubmission(ParentSubmission submission) {
        this.submission = submission;
    }

    /**
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

}