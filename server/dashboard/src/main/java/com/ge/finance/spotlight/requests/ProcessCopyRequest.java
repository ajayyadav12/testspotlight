package com.ge.finance.spotlight.requests;

public class ProcessCopyRequest {

    private String name;
    private String[] settings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getSettings() {
        return settings;
    }

    public void setSettings(String[] settings) {
        this.settings = settings;
    }

}
