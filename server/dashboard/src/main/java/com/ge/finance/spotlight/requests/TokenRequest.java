package com.ge.finance.spotlight.requests;

public class TokenRequest {
        
    private String source;    
    private String code; 
    private String redirectURI;

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRedirectURI() {
		return redirectURI;
	}

	public void setRedirectURI(String redirectURI) {
		this.redirectURI = redirectURI;
	}

}
