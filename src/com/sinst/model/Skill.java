package com.sinst.model;

public class Skill {
    private int skillId;
    private String skillName;
    private String category;
    private String description;
    private String proficiencyLevel; // Used when mapped to a student or role

    public Skill() {}

    public Skill(int skillId, String skillName, String category, String description) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.description = description;
    }

    public Skill(int skillId, String skillName, String category, String description, String proficiencyLevel) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.description = description;
        this.proficiencyLevel = proficiencyLevel;
    }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProficiencyLevel() { return proficiencyLevel; }
    public void setProficiencyLevel(String proficiencyLevel) { this.proficiencyLevel = proficiencyLevel; }

    @Override
    public String toString() {
        return skillName;
    }
}
