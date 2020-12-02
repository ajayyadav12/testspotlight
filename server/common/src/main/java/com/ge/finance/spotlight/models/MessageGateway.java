package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_MESSAGE_GATEWAYS")
public class MessageGateway {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "T_MESSAGE_GATEWAYS_SEQ")
    @SequenceGenerator(name = "T_MESSAGE_GATEWAYS_SEQ", sequenceName = "T_MESSAGE_GATEWAYS_SEQ", allocationSize = 1)
    private Long id;
    private String carrier;
    private String country;
    private String gateway;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCarrier() {
        return this.carrier + "~" + this.getCountry();
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public String getGateway() {
        return this.gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
