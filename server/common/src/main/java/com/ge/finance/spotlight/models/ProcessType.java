package com.ge.finance.spotlight.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="T_PROCESS_TYPE")
@NamedQuery(name = "ProcessType.findAll", query="select p from ProcessType p order by p.name")
public class ProcessType {

    @Id
    private Long id;
    private String name;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
