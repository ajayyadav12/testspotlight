package com.ge.finance.spotlight.dto;

import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;

public class EmailModel {
    
    public Long notificationTemplateId;
    public String to;
    public ScheduledSubmission schedSubmission;
    public Submission submission;
    public Process process;
    public User user;
    public SubmissionStep submissionStep;
    public ParentSubmission parentSubmission;
    public ProcessStep processStep;
    public boolean sms;

	public EmailModel(Long notificationTemplateId, String to, boolean sms) {
		this.notificationTemplateId = notificationTemplateId;
		this.to = to;
		this.sms = sms;
	}
    
}