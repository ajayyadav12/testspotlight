package com.ge.finance.spotlight.dto;

/**
 * ProcessDTO
 */

public class ProcessDTO {

    private Long id;
    private String name;
    private char isParent;

    public ProcessDTO(Long id, String name, char isParent) {

        this.id = id;
        this.name = name;
        this.isParent = isParent;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getIsParent() {
        return isParent == 'Y';
    }

    public void setIsParent(boolean isParent) {
        this.isParent = isParent ? 'Y' : 'N';
    }
}