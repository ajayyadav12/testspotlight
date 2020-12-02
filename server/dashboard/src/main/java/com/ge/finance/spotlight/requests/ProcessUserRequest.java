package com.ge.finance.spotlight.requests;

public class ProcessUserRequest {

    private Long userId;
    private Long processId;    

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

}
