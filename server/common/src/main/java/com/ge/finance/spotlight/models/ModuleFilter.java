package com.ge.finance.spotlight.models;

import javax.persistence.*;

@Entity
@Table(name = "T_MODULE_FILTER")
public class ModuleFilter {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "S_MODULE_FILTER_ID")
    @SequenceGenerator(name = "S_MODULE_FILTER_ID", sequenceName = "S_MODULE_FILTER_ID", allocationSize = 1)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String name;
    private String moduleName;
    private String settings;
    private String chipFilters;
    private char global = 'N';

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

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public boolean getGlobal() {
        return global == 'Y';
    }

    public void setGlobal(boolean global) {
        this.global = global ? 'Y' : 'N';
    }

    public String getSettings() {
        return settings;
    }

    public void setSettings(String settings) {
        this.settings = settings;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getChipFilters() {
		return chipFilters;
	}

	public void setChipFilters(String chipFilters) {
		this.chipFilters = chipFilters;
	}

}
