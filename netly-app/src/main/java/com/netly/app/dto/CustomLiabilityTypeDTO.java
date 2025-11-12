package com.netly.app.dto;

public class CustomLiabilityTypeDTO {
    private Long id;
    private String typeName;
    private String displayName;
    private String description;
    private Boolean isActive;

    // Constructors
    public CustomLiabilityTypeDTO() {}

    public CustomLiabilityTypeDTO(Long id, String typeName, String displayName, String description, Boolean isActive) {
        this.id = id;
        this.typeName = typeName;
        this.displayName = displayName;
        this.description = description;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
