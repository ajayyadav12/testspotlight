package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_USER_PERMISSION")
public class UserPermission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_USER_PERMISSION_ID")
    @SequenceGenerator(name = "S_USER_PERMISSION_ID", sequenceName = "S_USER_PERMISSION_ID", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "permission")
    private Permission permission;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private System receiver;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private System sender;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
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
