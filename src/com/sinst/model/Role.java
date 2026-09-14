package com.sinst.model;

public class Role {
    private int roleId;
    private String roleTitle;
    private String description;
    private String experienceLevel;

    public Role() {}

    public Role(int roleId, String roleTitle, String description, String experienceLevel) {
        this.roleId = roleId;
        this.roleTitle = roleTitle;
        this.description = description;
        this.experienceLevel = experienceLevel;
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    @Override
    public String toString() {
        return roleTitle;
    }
}
