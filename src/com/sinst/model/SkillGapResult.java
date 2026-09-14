package com.sinst.model;

public class SkillGapResult {
    private int skillId;
    private String skillName;
    private String category;
    private String requiredLevel;
    private String studentLevel;
    private String status; // 'Matched', 'Missing', 'Upgrade Needed'
    private boolean mandatory;

    public SkillGapResult() {}

    public SkillGapResult(int skillId, String skillName, String category, String requiredLevel, String studentLevel, String status, boolean mandatory) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.requiredLevel = requiredLevel;
        this.studentLevel = studentLevel;
        this.status = status;
        this.mandatory = mandatory;
    }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(String requiredLevel) { this.requiredLevel = requiredLevel; }

    public String getStudentLevel() { return studentLevel; }
    public void setStudentLevel(String studentLevel) { this.studentLevel = studentLevel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public boolean isMatched() {
        return "Matched".equalsIgnoreCase(status);
    }
}
