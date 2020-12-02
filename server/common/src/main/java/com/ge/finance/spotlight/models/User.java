package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_USER")
@NamedQuery(name = "User.findAll", query = "select u from User u order by u.name")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_USER_ID")
	@SequenceGenerator(name = "S_USER_ID", sequenceName = "S_USER_ID", allocationSize = 1)
	private Long id;
	private String name;
	private Long sso;
	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;
	@ManyToOne
	@JoinColumn(name = "carrier")
	private MessageGateway carrier;
	@Column(name = "mobile")
	private Long phoneNumber;

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

	public Long getSso() {
		return sso;
	}

	public void setSso(Long sso) {
		this.sso = sso;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getEmail() {
		return this.sso + "@ge.com";
	}

	public MessageGateway getCarrier() {
		return this.carrier;
	}

	public void setCarrier(MessageGateway carrier) {
		this.carrier = carrier;
	}

	public Long getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

}
