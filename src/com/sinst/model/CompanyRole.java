package com.sinst.model;

import java.sql.Date;
import java.util.Objects;

/**
 * Domain entity representing an association between a Company and an evaluated Role.
 * Tracks vacancy status, vacancy source, information source, and verification dates.
 *
 * Adheres strictly to the Critical Distinction:
 * The default status is "Potential Role Match" indicating curricular qualification match
 * against published hiring profiles, rather than fabricated active vacancies.
 */
public class CompanyRole {

    private int companyRoleId;
    private int companyId;
    private int roleId;
    private String companyName;
    private String companyIndustry;
    private String companyLocation;
    private String companyWebsite;
    private String careersUrl;
    private String roleTitle;
    private String roleDescription;
    private String experienceLevel;
    private String vacancyStatus;
    private String vacancySource;
    private String informationSource;
    private Date lastVerified;

    public CompanyRole() {
        this.vacancyStatus = "Potential Role Match";
        this.vacancySource = "Company Competency Profile";
        this.informationSource = "Official Careers Portal / Campus Listing";
    }

    public CompanyRole(int companyRoleId, int companyId, int roleId, String vacancyStatus,
                       String vacancySource, String informationSource, Date lastVerified) {
        this.companyRoleId = companyRoleId;
        this.companyId = companyId;
        this.roleId = roleId;
        this.vacancyStatus = vacancyStatus != null ? vacancyStatus : "Potential Role Match";
        this.vacancySource = vacancySource != null ? vacancySource : "Company Competency Profile";
        this.informationSource = informationSource != null ? informationSource : "Official Careers Portal";
        this.lastVerified = lastVerified;
    }

    public int getCompanyRoleId() { return companyRoleId; }
    public void setCompanyRoleId(int companyRoleId) { this.companyRoleId = companyRoleId; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyIndustry() { return companyIndustry; }
    public void setCompanyIndustry(String companyIndustry) { this.companyIndustry = companyIndustry; }

    public String getCompanyLocation() { return companyLocation; }
    public void setCompanyLocation(String companyLocation) { this.companyLocation = companyLocation; }

    public String getCompanyWebsite() { return companyWebsite; }
    public void setCompanyWebsite(String companyWebsite) { this.companyWebsite = companyWebsite; }

    public String getCareersUrl() { return careersUrl; }
    public void setCareersUrl(String careersUrl) { this.careersUrl = careersUrl; }

    public String getEffectiveCareersUrl() {
        if (careersUrl != null && !careersUrl.trim().isEmpty()) {
            return careersUrl.trim();
        }
        return companyWebsite != null ? companyWebsite.trim() : "";
    }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    public String getRoleDescription() { return roleDescription; }
    public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getVacancyStatus() { return vacancyStatus; }
    public void setVacancyStatus(String vacancyStatus) { this.vacancyStatus = vacancyStatus; }

    public String getVacancySource() { return vacancySource; }
    public void setVacancySource(String vacancySource) { this.vacancySource = vacancySource; }

    public String getInformationSource() { return informationSource; }
    public void setInformationSource(String informationSource) { this.informationSource = informationSource; }

    public Date getLastVerified() { return lastVerified; }
    public void setLastVerified(Date lastVerified) { this.lastVerified = lastVerified; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyRole that = (CompanyRole) o;
        if (companyRoleId > 0 && that.companyRoleId > 0) {
            return companyRoleId == that.companyRoleId;
        }
        return companyId == that.companyId && roleId == that.roleId;
    }

    @Override
    public int hashCode() {
        if (companyRoleId > 0) {
            return Integer.hashCode(companyRoleId);
        }
        return Objects.hash(companyId, roleId);
    }

    @Override
    public String toString() {
        return (companyName != null ? companyName : "Company #" + companyId) + " - " +
               (roleTitle != null ? roleTitle : "Role #" + roleId) + " [" + vacancyStatus + "]";
    }
}
