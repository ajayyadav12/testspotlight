package com.ge.finance.spotlight.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="T_ROLE")
public class Role {

	public final static long ADMIN = 1;
	public final static long USER = 2;
	public final static long APPLICATION = 3;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_ROLE_ID")
    @SequenceGenerator(name="S_ROLE_ID", sequenceName = "S_ROLE_ID", allocationSize = 1)
    private Long id;
    private String description;
    
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}       
}
