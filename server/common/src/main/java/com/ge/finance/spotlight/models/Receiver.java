package com.ge.finance.spotlight.models;

import javax.persistence.*;


@Entity
@Table(name="T_RECEIVER")
@NamedQuery(name = "Receiver.findAll", query="select r from Receiver r order by r.name")
public class Receiver {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_RECEIVER_ID")
    @SequenceGenerator(name="S_RECEIVER_ID", sequenceName = "S_RECEIVER_ID", allocationSize = 1)
    private Long id;    
    private String name;
    @ManyToOne
    @JoinColumn(name="close_phase_id")
    private ClosePhase closePhase;

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

	public ClosePhase getClosePhase() {
		return closePhase;
	}

	public void setClosePhase(ClosePhase closePhase) {
		this.closePhase = closePhase;
	}

}
