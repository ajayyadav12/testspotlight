package com.ge.finance.spotlight.models;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name="T_SYSTEM")
@NamedQuery(name = "System.findAll", query="select s from System s order by s.name")
public class System {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_SYSTEM_ID")
    @SequenceGenerator(name="S_SYSTEM_ID", sequenceName = "S_SYSTEM_ID", allocationSize = 1)
    private Long id;
    @NotBlank    
    private String name;
    @ManyToOne
    @JoinColumn(name="app_owner_id")
    private User appOwner;
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

	public ClosePhase getClosePhase() {
		return closePhase;
	}

	public void setClosePhase(ClosePhase closePhase) {
		this.closePhase = closePhase;
	}

}
