package com.ge.finance.spotlight.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "T_AUDIT_LOG")
public class AuditLog {

    public final static String LOGIN = "Login";
    public final static String MOBILE_LOGIN = "Mobile_Login";
    public final static String LOGOUT = "Logout";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_AUDIT_LOG_ID")
    @SequenceGenerator(name = "S_AUDIT_LOG_ID", sequenceName = "S_AUDIT_LOG_ID", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LOG_IN")
    private Date logInTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LOG_OUT")
    private Date logOutTime;

    @Column(name = "MODULE")
    private String module;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getLogInTime() {
        return this.logInTime;
    }

    public void setLogInTime(Date logInTime) {
        this.logInTime = logInTime;
    }

    public Date getLogOutTime() {
        return this.logOutTime;
    }

    public void setLogOutTime(Date logOutTime) {
        this.logOutTime = logOutTime;
    }

    public String getModule() {
        return this.module;
    }

    public void setModule(String module) {
        this.module = module;
    }

}
