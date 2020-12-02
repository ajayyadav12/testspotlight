package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_PROCESS")
@NamedQueries({ @NamedQuery(name = "Process.findAll", query = "select p from Process p order by p.name"),
        @NamedQuery(name = "Process.findByIdIsIn", query = "select p from Process p where id in :processIdList order by p.name") })
public class Process {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PROCESS_ID")
    @SequenceGenerator(name = "S_PROCESS_ID", sequenceName = "S_PROCESS_ID", allocationSize = 1)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Sender sender;
    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Receiver receiver;
    @ManyToOne
    @JoinColumn(name = "process_type_id")
    private ProcessType processType;
    @ManyToOne
    @JoinColumn(name = "feed_type_id")
    private FeedType feedType;
    @Column(name = "critical_flag")
    private boolean critical;
    @ManyToOne
    @JoinColumn(name = "process_parent_id")
    private Process processParent;
    @Column(name = "process_level")
    private int processLevel;
    @Column(name = "is_Parent")
    private char isParent;
    @ManyToOne
    @JoinColumn(name = "technical_owner_id")
    private User technicalOwner;
    @ManyToOne
    @JoinColumn(name = "functional_owner_id")
    private User functionalOwner;
    @ManyToOne
    @JoinColumn(name = "predecessor")
    private Process predecessor;
    @ManyToOne
    @JoinColumn(name = "successor")
    private Successor successor;
    @ManyToOne
    @JoinColumn(name = "app_owner_id")
    private User appOwner;
    @Column(name = "approved_flag")
    private Character approved = '0';
    @Column(name = "support_team_email")
    private String supportTeamEmail = "";
    @Column(name = "max_run_time_hours")
    private Long maxRunTimeHours;
    @Column(name = "max_run_time_minutes")
    private Long maxRunTimeMinutes;
    @Column(name = "submission_escalation_alrt")
    private char submissionEscalationAlrt = 'Y';
    @Column(name = "long_running_sub_alrt")
    private char longRunningSubAlrt = 'Y';
    @Column(name = "long_running_step_alrt")
    private char longRunningStepAlrt = 'Y';

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

    public void setSender(Sender sender) {
        this.sender = sender;
    }

    public Sender getSender() {
        return this.sender;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public Receiver getReceiver() {
        return receiver;
    }

    public void setProcessType(ProcessType processType) {
        this.processType = processType;
    }

    public ProcessType getProcessType() {
        return processType;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public boolean getCritical() {
        return critical;
    }

    public Process getProcessParent() {
        return this.processParent;
    }

    public void setProcessParent(Process processParent) {
        this.processParent = processParent;
    }

    public int getProcessLevel() {
        return processLevel;
    }

    public void setProcessLevel(int processLevel) {
        this.processLevel = processLevel;
    }

    public boolean getIsParent() {
        return isParent == 'Y';
    }

    public void setIsParent(boolean isParent) {
        this.isParent = isParent ? 'Y' : 'N';
    }

    public User getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(User technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public User getFunctionalOwner() {
        return functionalOwner;
    }

    public void setFunctionalOwner(User functionalOwner) {
        this.functionalOwner = functionalOwner;
    }

    /**
     * @return the predecessor
     */
    public Process getPredecessor() {
        return predecessor;
    }

    /**
     * @param predecessor the predecessor to set
     */
    public void setPredecessor(Process predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * @return the successor
     */
    public Successor getSuccessor() {
        return successor;
    }

    /**
     * @param successor the successor to set
     */
    public void setSuccessor(Successor successor) {
        this.successor = successor;
    }

    /**
     * @return the appOwner
     */
    public User getAppOwner() {
        return appOwner;
    }

    /**
     * @param appOwner the appOwner to set
     */
    public void setAppOwner(User appOwner) {
        this.appOwner = appOwner;
    }

    public FeedType getFeedType() {
        return feedType;
    }

    public void setFeedType(FeedType feedType) {
        this.feedType = feedType;
    }

    /**
     * 0 = Waiting for approval N = Neglected A = Approved
     * 
     * @return
     */
    public Character getApproved() {
        return approved;
    }

    /**
     * 0 = Waiting for approval N = Neglected A = Approved
     * 
     * @param approved
     */
    public void setApproved(Character approved) {
        this.approved = approved;
    }

    public String getSupportTeamEmail() {
        return (this.supportTeamEmail == null) ? "" : this.supportTeamEmail;
    }

    public void setSupportTeamEmail(String supportTeamEmail) {
        this.supportTeamEmail = supportTeamEmail;
    }

    public boolean getSubmissionEscalationAlrt() {
        return submissionEscalationAlrt == 'Y';
    }

    public void setSubmissionEscalationAlrt(boolean submissionEscalationAlrt) {
        this.submissionEscalationAlrt = submissionEscalationAlrt ? 'Y' : 'N';
    }

    public boolean getLongRunningSubAlrt() {
        return longRunningSubAlrt == 'Y';
    }

    public void setLongRunningSubAlrt(boolean longRunningSubAlrt) {
        this.longRunningSubAlrt = longRunningSubAlrt ? 'Y' : 'N';
    }

    public boolean getLongRunningStepAlrt() {
        return longRunningStepAlrt == 'Y';
    }

    public void setLongRunningStepAlrt(boolean longRunningStepAlrt) {
        this.longRunningStepAlrt = longRunningStepAlrt ? 'Y' : 'N';
    }

	public Long getMaxRunTimeHours() {
		return maxRunTimeHours;
	}

	public void setMaxRunTimeHours(Long maxRunTimeHours) {
		this.maxRunTimeHours = maxRunTimeHours;
	}

	public Long getMaxRunTimeMinutes() {
		return maxRunTimeMinutes;
	}

	public void setMaxRunTimeMinutes(Long maxRunTimeMinutes) {
		this.maxRunTimeMinutes = maxRunTimeMinutes;
	}

}
