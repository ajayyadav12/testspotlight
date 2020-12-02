package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_PROCESS_PARENT_CHILD")
public class ProcessParentChild {
     
	@Id()
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "s_process_parent_child_id")
    @SequenceGenerator(name = "s_process_parent_child_id", sequenceName = "s_process_parent_child_id", allocationSize = 1)
	private long id;	
	@ManyToOne
	@JoinColumn(name = "process_id")    
    private ProcessList process;
    @ManyToOne
    @JoinColumn(name = "child_id")
    private ProcessList processChild;     
    @Column(name = "seq")
    private Long seq;

    public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public ProcessList getProcess() {
		return process;
	}
	public void setProcess(ProcessList process) {
		this.process = process;
	}	
	public ProcessList getProcessChild() {
		return processChild;
	}
	public void setProcessChild(ProcessList processChild) {
		this.processChild = processChild;
	}
	public Long getSeq() {
		return seq;
	}
	public void setSeq(Long seq) {
		this.seq = seq;
    }
	

}
