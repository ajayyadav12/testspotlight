package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name="T_PROCESS")
public class ProcessList {

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

    public String getSenderName() {
        return this.sender != null ? this.sender.getName() : "";
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public String getReceiverName() {
        return this.receiver != null ? this.receiver.getName() : "";
    }
       

}
