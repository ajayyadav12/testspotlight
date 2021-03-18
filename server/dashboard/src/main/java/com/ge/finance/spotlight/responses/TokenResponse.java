package com.ge.finance.spotlight.responses;

import com.ge.finance.spotlight.models.User;

import java.util.List;

public class TokenResponse {

    private String token;
    private User user;
    private List<Long> processes;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}       

	public List<Long> getProcesses() {
        return processes;
    }

    public void setProcesses(List<Long> processes) {
        this.processes = processes;
    }

}
