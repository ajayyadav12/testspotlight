package com.ge.finance.spotlight.dto;

public class AcknowledgeDTO {
    private String acknowledgementNote;
    private String acknowledgedBy;

    /**
     * @return the acknowledgeNote
     */
    public String getAcknowledgementNote() {
        return acknowledgementNote;
    }

    /**
     * @param acknowledgementNote the acknowledgementNote to set
     */
    public void setAcknowledgementNote(String acknowledgementNote) {
        this.acknowledgementNote = acknowledgementNote;
    }

    public String getAcknowledgedBy() {
        return this.acknowledgedBy;
    }

    public void setAcknowledgedBy(String acknowledgedBy) {
        this.acknowledgedBy = acknowledgedBy;
    }

}