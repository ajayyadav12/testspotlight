package com.ge.finance.spotlight.responses;

import com.ge.finance.spotlight.models.*;
import com.ge.finance.spotlight.models.Process;

import java.util.List;

public class ProcessExport {

    private Process process;
    private List<ProcessStep> steps;
    private List<Notification> notifications;
    private List<ProcessParentChild> processChild;
    private List<User> users;
    private List<ScheduleDefinition> schedules;
    private User requester;

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public List<ProcessStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ProcessStep> steps) {
        this.steps = steps;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<ScheduleDefinition> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<ScheduleDefinition> schedules) {
        this.schedules = schedules;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public List<ProcessParentChild> getProcessChild() {
        return this.processChild;
    }

    public void setProcessChild(List<ProcessParentChild> processChild) {
        this.processChild = processChild;
    }

}
