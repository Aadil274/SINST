package com.sinst.model;

/**
 * Model representing a catalog template mapping between a certification type
 * and the skills & learning outcomes it covers.
 */
public class CertificationSkillMapping {
    private int mappingId;
    private String certificationName;
    private String issuingOrganization;
    private int skillId;
    private String skillName;
    private String learningOutcome;
    private String evidenceSource;
    private String suggestedProficiency; // 'Beginner', 'Intermediate', 'Advanced', 'Expert'
    private String mappingStatus; // 'Verified', 'Pending', 'Deprecated'

    public CertificationSkillMapping() {}

    public CertificationSkillMapping(int mappingId, String certificationName, String issuingOrganization, int skillId, String skillName, String learningOutcome, String evidenceSource, String suggestedProficiency, String mappingStatus) {
        this.mappingId = mappingId;
        this.certificationName = certificationName;
        this.issuingOrganization = issuingOrganization;
        this.skillId = skillId;
        this.skillName = skillName;
        this.learningOutcome = learningOutcome;
        this.evidenceSource = evidenceSource;
        this.suggestedProficiency = suggestedProficiency != null ? suggestedProficiency : "Intermediate";
        this.mappingStatus = mappingStatus != null ? mappingStatus : "Verified";
    }

    public int getMappingId() { return mappingId; }
    public void setMappingId(int mappingId) { this.mappingId = mappingId; }

    public String getCertificationName() { return certificationName; }
    public void setCertificationName(String certificationName) { this.certificationName = certificationName; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getLearningOutcome() { return learningOutcome; }
    public void setLearningOutcome(String learningOutcome) { this.learningOutcome = learningOutcome; }

    public String getEvidenceSource() { return evidenceSource; }
    public void setEvidenceSource(String evidenceSource) { this.evidenceSource = evidenceSource; }

    public String getSuggestedProficiency() { return suggestedProficiency; }
    public void setSuggestedProficiency(String suggestedProficiency) { this.suggestedProficiency = suggestedProficiency; }

    public String getMappingStatus() { return mappingStatus; }
    public void setMappingStatus(String mappingStatus) { this.mappingStatus = mappingStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CertificationSkillMapping that = (CertificationSkillMapping) o;
        if (mappingId > 0 && that.mappingId > 0) {
            return mappingId == that.mappingId;
        }
        return skillId == that.skillId &&
               java.util.Objects.equals(certificationName != null ? certificationName.toLowerCase() : null,
                                       that.certificationName != null ? that.certificationName.toLowerCase() : null);
    }

    @Override
    public int hashCode() {
        if (mappingId > 0) {
            return Integer.hashCode(mappingId);
        }
        return java.util.Objects.hash(certificationName != null ? certificationName.toLowerCase() : null, skillId);
    }

    @Override
    public String toString() {
        return (skillName != null ? skillName : "Skill #" + skillId) + " (" + suggestedProficiency + ")";
    }
}
