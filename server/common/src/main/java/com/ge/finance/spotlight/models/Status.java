package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_STATUS")
public class Status {

    public final static long LONG_RUNNING = 6L;
    public final static long IN_PROGRESS = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_STATUS_ID")
    @SequenceGenerator(name = "S_STATUS_ID", sequenceName = "S_STATUS_ID", allocationSize = 1)
    private Long id;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
