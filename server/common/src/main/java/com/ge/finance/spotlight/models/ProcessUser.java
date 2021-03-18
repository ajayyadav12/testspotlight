package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name="T_PROCESS_USER")
@NamedQuery(name = "ProcessUser.findByProcessId", query="select p from ProcessUser p where p.processId = :processId order by p.user.name")
public class ProcessUser {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_PROCESS_USER_ID")
    @SequenceGenerator(name="S_PROCESS_USER_ID", sequenceName = "S_PROCESS_USER_ID", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    @Column(name="process_id")
    private Long processId;    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }    

}
