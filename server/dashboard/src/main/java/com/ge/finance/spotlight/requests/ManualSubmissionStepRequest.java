package com.ge.finance.spotlight.requests;

public class ManualSubmissionStepRequest {
    
    
    private String status;    
	private String notes = "";
	private String appsApiURL;
	private Long processId;
    
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getAppsApiURL() {
		return appsApiURL;
	}
	public void setAppsApiURL(String appsApiURL) {
		this.appsApiURL = appsApiURL;
	}
	public Long getProcessId() {
		return processId;
	}
	public void setProcessId(Long processId) {
		this.processId = processId;
	}    

    
}
