package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_PERMISSION")
public class Permission {

    @Id
    private Long id;
    private String permission;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

}