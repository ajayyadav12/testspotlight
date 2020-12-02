package com.ge.finance.spotlight.requests;

public class TokenRequest {

    private Long sso;
    private String password;
    private String url;
    private String source;

    public Long getSso() {
        return sso;
    }

    public void setSso(Long sso) {
        this.sso = sso;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
