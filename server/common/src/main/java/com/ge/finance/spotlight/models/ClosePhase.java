package com.ge.finance.spotlight.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="T_CLOSE_PHASE")
public class ClosePhase {
	
	public final static long SUBLEDGER = 1;
	public final static long ERPGL = 2;
	public final static long BIZ_CONSOLIDATION = 3;
	public final static long TOTAL_COMPANY_CONSOLIDATION = 4;
	public final static long REPORTING = 5;

    @Id    
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
