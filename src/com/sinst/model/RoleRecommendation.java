package com.sinst.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain model representing a calculated, explainable Company & Role recommendation
 * for a student candidate.
 *
 * Grounded strictly in database records (skills, proficiency levels, certifications,
 * role requirements, and company profiles).
 *
 * Adheres strictly to the Critical Distinction:
 * A high skill alignment score reflects curricular qualification match, not an active vacancy.
 */
public class RoleRecommendation implements Comparable<RoleRecommendation> {

    private int companyId;
    private String companyName;
    private String companyLocation;
    private String companyWebsite;
    private String careersUrl;

    private int roleId;
    private String roleTitle;
    private String experienceLevel;

    private String vacancyStatus;
    private String vacancySource;
    private String informationSource;
    private Date lastVerified;

    private int matchPercentage;
    private double detailedScore;
    private String readinessAssessment;

    private List<SkillGapResult> matchingSkills = new ArrayList<>();
    private List<SkillGapResult> partiallyMatchedSkills = new ArrayList<>();
    private List<SkillGapResult> missingMandatorySkills = new ArrayList<>();
    private List<SkillGapResult> missingOptionalSkills = new ArrayList<>();

    private int verifiedEvidenceCount;
    private boolean hasApplied;
    private String applicationStatus;
    private String recommendationReason;
    private boolean hasInsufficientRoleData;

    public RoleRecommendation() {
        this.vacancyStatus = "Potential Role Match";
        this.vacancySource = "Company Competency Profile";
        this.informationSource = "Official Careers Portal / Campus Listing";
    }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

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

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

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

    public int getMatchPercentage() { return matchPercentage; }
    public void setMatchPercentage(int matchPercentage) { this.matchPercentage = matchPercentage; }

    public double getDetailedScore() { return detailedScore; }
    public void setDetailedScore(double detailedScore) { this.detailedScore = detailedScore; }

    public String getReadinessAssessment() { return readinessAssessment; }
    public void setReadinessAssessment(String readinessAssessment) { this.readinessAssessment = readinessAssessment; }

    public List<SkillGapResult> getMatchingSkills() { return matchingSkills; }
    public void setMatchingSkills(List<SkillGapResult> matchingSkills) {
        this.matchingSkills = matchingSkills != null ? matchingSkills : new ArrayList<>();
    }

    public List<SkillGapResult> getPartiallyMatchedSkills() { return partiallyMatchedSkills; }
    public void setPartiallyMatchedSkills(List<SkillGapResult> partiallyMatchedSkills) {
        this.partiallyMatchedSkills = partiallyMatchedSkills != null ? partiallyMatchedSkills : new ArrayList<>();
    }

    public List<SkillGapResult> getMissingMandatorySkills() { return missingMandatorySkills; }
    public void setMissingMandatorySkills(List<SkillGapResult> missingMandatorySkills) {
        this.missingMandatorySkills = missingMandatorySkills != null ? missingMandatorySkills : new ArrayList<>();
    }

    public List<SkillGapResult> getMissingOptionalSkills() { return missingOptionalSkills; }
    public void setMissingOptionalSkills(List<SkillGapResult> missingOptionalSkills) {
        this.missingOptionalSkills = missingOptionalSkills != null ? missingOptionalSkills : new ArrayList<>();
    }

    public int getVerifiedEvidenceCount() { return verifiedEvidenceCount; }
    public void setVerifiedEvidenceCount(int verifiedEvidenceCount) { this.verifiedEvidenceCount = verifiedEvidenceCount; }

    public boolean isHasApplied() { return hasApplied; }
    public void setHasApplied(boolean hasApplied) { this.hasApplied = hasApplied; }

    public String getApplicationStatus() { return applicationStatus; }
    public void setApplicationStatus(String applicationStatus) { this.applicationStatus = applicationStatus; }

    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }

    public boolean isHasInsufficientRoleData() { return hasInsufficientRoleData; }
    public void setHasInsufficientRoleData(boolean hasInsufficientRoleData) { this.hasInsufficientRoleData = hasInsufficientRoleData; }

    public int getTotalRequiredCount() {
        return matchingSkills.size() + partiallyMatchedSkills.size() + missingMandatorySkills.size() + missingOptionalSkills.size();
    }

    public int getSatisfiedCount() {
        return matchingSkills.size();
    }

    public String getMatchingSkillNames() {
        if (matchingSkills.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < matchingSkills.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(matchingSkills.get(i).getSkillName());
        }
        return sb.toString();
    }

    public String getPartiallyMatchedNames() {
        if (partiallyMatchedSkills.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partiallyMatchedSkills.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(partiallyMatchedSkills.get(i).getSkillName());
        }
        return sb.toString();
    }

    public String getMissingMandatoryNames() {
        if (missingMandatorySkills.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missingMandatorySkills.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(missingMandatorySkills.get(i).getSkillName());
        }
        return sb.toString();
    }

    public String getMissingOptionalNames() {
        if (missingOptionalSkills.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < missingOptionalSkills.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(missingOptionalSkills.get(i).getSkillName());
        }
        return sb.toString();
    }

    @Override
    public int compareTo(RoleRecommendation other) {
        if (other == null) return -1;
        // 1. Primary sort: match percentage descending
        int scoreCompare = Integer.compare(other.matchPercentage, this.matchPercentage);
        if (scoreCompare != 0) return scoreCompare;

        // 2. Secondary sort: missing mandatory skills count ascending (fewer missing ranks higher)
        int mandatoryMissingCompare = Integer.compare(this.missingMandatorySkills.size(), other.missingMandatorySkills.size());
        if (mandatoryMissingCompare != 0) return mandatoryMissingCompare;

        // 3. Tertiary sort: verified evidence count descending
        int evidenceCompare = Integer.compare(other.verifiedEvidenceCount, this.verifiedEvidenceCount);
        if (evidenceCompare != 0) return evidenceCompare;

        // 4. Quaternary sort: company name alphabetical
        String thisCompany = this.companyName != null ? this.companyName : "";
        String otherCompany = other.companyName != null ? other.companyName : "";
        return thisCompany.compareToIgnoreCase(otherCompany);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleRecommendation that = (RoleRecommendation) o;
        return companyId == that.companyId && roleId == that.roleId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, roleId);
    }

    @Override
    public String toString() {
        return companyName + " - " + roleTitle + " (" + matchPercentage + "% match) [" + vacancyStatus + "]";
    }
}
