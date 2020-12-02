package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name="T_PROCESS")
public class Successor {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PROCESS_ID")
    @SequenceGenerator(name="S_PROCESS_ID", sequenceName = "S_PROCESS_ID", allocationSize = 1)
    private Long id;
    private String name;
    @ManyToOne
    @JoinColumn(name="sender_id")
    private Sender sender;
    @ManyToOne
    @JoinColumn(name="receiver_id")
    private Receiver receiver;
    @ManyToOne
    @JoinColumn(name="process_type_id")
    private ProcessType processType;
    @Column(name="critical_flag")
    private boolean critical;    
    @ManyToOne
    @JoinColumn(name="process_parent_id")
    private Process processParent;
    @Column(name="process_level")
    private int processLevel;
    @Column(name="is_Parent")
    private char isParent;
    @ManyToOne
    @JoinColumn(name="technical_owner_id")
    private User technicalOwner;
    @ManyToOne
    @JoinColumn(name="functional_owner_id")
    private User functionalOwner;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="predecessor")
    private Process predecessor;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="successor")
    private Process successor;
    

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

}
