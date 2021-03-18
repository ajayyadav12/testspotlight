package com.ge.finance.spotlight.models;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name="T_PARENT_SUBMISSION")
public class ParentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PARENT_SUBMISSION_ID")
    @SequenceGenerator(name="S_PARENT_SUBMISSION_ID", sequenceName = "S_PARENT_SUBMISSION_ID", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name="process_id")
    private Process process;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="start_time")
    private Date startTime;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="end_time")
    private Date endTime;     
    @Column(name="ad_hoc_flag")
    private char adHoc;
    @OneToMany(fetch = FetchType.EAGER, mappedBy= "submissionParentId")	
    private Set<ChildSubmission> children;

    @ManyToOne
    @JoinColumn(name="status_id")
    private Status status;

    @Transient
    private int errors;
    @Transient
    private int warnings;
    @Transient
    private int records;
// 
// 
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the process
     */
    public Process getProcess() {
        return process;
    }

    /**
     * @param process the process to set
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * @return the startTime
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }  

    public boolean getAdHoc() {
        return adHoc == 'Y';
    }

    public void setAdHoc(boolean adHoc) {
        this.adHoc = adHoc ? 'Y' : 'N';
    }

    /**
     * @return the children
     */
    public Set<ChildSubmission> getChildren() {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildren(Set<ChildSubmission> children) {
        this.children = children;
    }

    /**
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(Status status) {
        this.status = status;
    }


    public int getErrors() {
        return this.errors;
    }

    public void setErrors(int errors) {
        this.errors = errors;
    }

    public int getWarnings() {
        return this.warnings;
    }

    public void setWarnings(int warnings) {
        this.warnings = warnings;
    }

    public int getRecords() {
        return this.records;
    }

    public void setRecords(int records) {
        this.records = records;
    }

}