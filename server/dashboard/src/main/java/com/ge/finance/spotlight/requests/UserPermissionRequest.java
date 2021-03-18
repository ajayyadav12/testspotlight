package com.ge.finance.spotlight.requests;

import com.ge.finance.spotlight.models.User;
import com.ge.finance.spotlight.models.System;

public class UserPermissionRequest {

    private boolean upload;
    private boolean view;
    private User user;
    private System receiver;
    private System sender;

    public boolean isUpload() {
        return upload;
    }

    public void setUpload(boolean upload) {
        this.upload = upload;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public System getReceiver() {
        return receiver;
    }

    public void setReceiver(System receiver) {
        this.receiver = receiver;
    }

    public System getSender() {
        return sender;
    }

    public void setSender(System sender) {
        this.sender = sender;
    }
    
    
}