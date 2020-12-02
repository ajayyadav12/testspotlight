package com.ge.finance.spotlight.responses;

import com.ge.finance.spotlight.models.User;

public class TokenResponse {

    private String token;
    private User user;
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

}
