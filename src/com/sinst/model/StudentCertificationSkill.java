package com.sinst.model;

import java.sql.Timestamp;

/**
 * Model representing a specific skill verified by a student's certification.
 */
public class StudentCertificationSkill {
    private int recordId;
    private int studentId;
    private int certificationId;
    private int skillId;
    private String skillName;
    private String learningOutcome;
    private String reviewStatus; // 'Approved', 'Pending', 'Rejected'
    private String assessedProficiency; // 'Beginner', 'Intermediate', 'Advanced', 'Expert'
    private Timestamp reviewDate;

    public StudentCertificationSkill() {}

    public StudentCertificationSkill(int recordId, int studentId, int certificationId, int skillId, String skillName, String learningOutcome, String reviewStatus, String assessedProficiency, Timestamp reviewDate) {
        this.recordId = recordId;
        this.studentId = studentId;
        this.certificationId = certificationId;
        this.skillId = skillId;
        this.skillName = skillName;
        this.learningOutcome = learningOutcome;
        this.reviewStatus = reviewStatus != null ? reviewStatus : "Approved";
        this.assessedProficiency = assessedProficiency != null ? assessedProficiency : "Intermediate";
        this.reviewDate = reviewDate;
    }

    public int getRecordId() { return recordId; }
    public void setRecordId(int recordId) { this.recordId = recordId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCertificationId() { return certificationId; }
    public void setCertificationId(int certificationId) { this.certificationId = certificationId; }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getLearningOutcome() { return learningOutcome; }
    public void setLearningOutcome(String learningOutcome) { this.learningOutcome = learningOutcome; }

    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }

    public String getAssessedProficiency() { return assessedProficiency; }
    public void setAssessedProficiency(String assessedProficiency) { this.assessedProficiency = assessedProficiency; }

    public Timestamp getReviewDate() { return reviewDate; }
    public void setReviewDate(Timestamp reviewDate) { this.reviewDate = reviewDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentCertificationSkill that = (StudentCertificationSkill) o;
        if (recordId > 0 && that.recordId > 0) {
            return recordId == that.recordId;
        }
        return certificationId == that.certificationId && skillId == that.skillId;
    }

    @Override
    public int hashCode() {
        if (recordId > 0) {
            return Integer.hashCode(recordId);
        }
        return java.util.Objects.hash(certificationId, skillId);
    }

    @Override
    public String toString() {
        return (skillName != null ? skillName : "Skill #" + skillId) + " - " + assessedProficiency + " [" + reviewStatus + "]";
    }
}
