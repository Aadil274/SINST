package com.sinst.model;

import java.sql.Timestamp;

/**
 * Model representing supporting evidence for a student's demonstrated skill.
 * Distinguishes between:
 *   - Self-Reported
 *   - Certification
 *   - Project
 *   - Assessment
 *
 * Verification statuses:
 *   - Unverified
 *   - Verified
 *   - Rejected
 */
public class SkillEvidence {
    private int evidenceId;
    private int studentId;
    private int skillId;
    private String skillName;
    private String evidenceType; // 'Self-Reported', 'Certification', 'Project', 'Assessment'
    private Integer referenceId; // e.g., certification_id or project_id
    private String evidenceTitle;
    private String evidenceDetails;
    private String verificationStatus; // 'Unverified', 'Verified', 'Rejected'
    private Timestamp createdAt;

    public SkillEvidence() {}

    public SkillEvidence(int evidenceId, int studentId, int skillId, String skillName, String evidenceType, Integer referenceId, String evidenceTitle, String evidenceDetails, String verificationStatus, Timestamp createdAt) {
        this.evidenceId = evidenceId;
        this.studentId = studentId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.evidenceType = evidenceType != null ? evidenceType : "Self-Reported";
        this.referenceId = referenceId;
        this.evidenceTitle = evidenceTitle;
        this.evidenceDetails = evidenceDetails;
        this.verificationStatus = verificationStatus != null ? verificationStatus : "Verified";
        this.createdAt = createdAt;
    }

    public int getEvidenceId() { return evidenceId; }
    public void setEvidenceId(int evidenceId) { this.evidenceId = evidenceId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public Integer getReferenceId() { return referenceId; }
    public void setReferenceId(Integer referenceId) { this.referenceId = referenceId; }

    public String getEvidenceTitle() { return evidenceTitle; }
    public void setEvidenceTitle(String evidenceTitle) { this.evidenceTitle = evidenceTitle; }

    public String getEvidenceDetails() { return evidenceDetails; }
    public void setEvidenceDetails(String evidenceDetails) { this.evidenceDetails = evidenceDetails; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isVerified() {
        return "Verified".equalsIgnoreCase(verificationStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillEvidence that = (SkillEvidence) o;
        if (evidenceId > 0 && that.evidenceId > 0) {
            return evidenceId == that.evidenceId;
        }
        return studentId == that.studentId &&
               skillId == that.skillId &&
               java.util.Objects.equals(evidenceType, that.evidenceType) &&
               java.util.Objects.equals(referenceId, that.referenceId);
    }

    @Override
    public int hashCode() {
        if (evidenceId > 0) {
            return Integer.hashCode(evidenceId);
        }
        return java.util.Objects.hash(studentId, skillId, evidenceType, referenceId);
    }

    @Override
    public String toString() {
        return evidenceType + ": " + evidenceTitle + " (" + verificationStatus + ")";
    }
}
