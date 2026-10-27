package com.sinst.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Role {
    private int roleId;
    private String roleTitle;
    private String description;
    private String experienceLevel;
    private String informationSource;
    private Date lastUpdated;
    private List<Skill> requiredSkills = new ArrayList<>();

    public Role() {}

    public Role(int roleId, String roleTitle, String description, String experienceLevel) {
        this(roleId, roleTitle, description, experienceLevel, "Industry Standard Competency Framework", null);
    }

    public Role(int roleId, String roleTitle, String description, String experienceLevel, String informationSource, Date lastUpdated) {
        this.roleId = roleId;
        this.roleTitle = roleTitle;
        this.description = description;
        this.experienceLevel = experienceLevel;
        this.informationSource = informationSource;
        this.lastUpdated = lastUpdated;
    }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getInformationSource() { return informationSource; }
    public void setInformationSource(String informationSource) { this.informationSource = informationSource; }

    public Date getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }

    public List<Skill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(List<Skill> requiredSkills) { this.requiredSkills = requiredSkills != null ? requiredSkills : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        if (roleId != 0 && role.roleId != 0) return roleId == role.roleId;
        return roleTitle != null && roleTitle.equalsIgnoreCase(role.roleTitle);
    }

    @Override
    public int hashCode() {
        return roleId != 0 ? Integer.hashCode(roleId) : (roleTitle != null ? roleTitle.toLowerCase().hashCode() : 0);
    }

    @Override
    public String toString() {
        return roleTitle;
    }
}
