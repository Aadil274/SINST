package com.sinst.model;

/**
 * Encapsulates the evaluation of a single role skill requirement against a student's profile.
 * Distinguishes between:
 *   - Fully Satisfied (Matched): Demonstrated proficiency meets or exceeds required benchmark.
 *   - Partially Satisfied (Upgrade Needed): Demonstrated proficiency is below benchmark (> 0 and < required).
 *   - Missing: Student does not possess the skill at all.
 *   - Insufficient Evidence: Student has recorded the skill, but without a recognized or verified level.
 */
public class SkillGapResult {

    private int skillId;
    private String skillName;
    private String category;
    private String requiredLevel;
    private String studentLevel;
    private String status; // 'Matched', 'Upgrade Needed', 'Missing', 'Insufficient Evidence'
    private boolean mandatory;
    private double matchValue; // 0.0 to 1.0
    private int proficiencyGap; // e.g., +2 levels
    private boolean hasSkillRecord;

    public SkillGapResult() {}

    /**
     * Legacy & full-field constructor preserving backwards compatibility.
     */
    public SkillGapResult(int skillId, String skillName, String category, String requiredLevel, String studentLevel, String status, boolean mandatory) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.requiredLevel = requiredLevel;
        this.studentLevel = studentLevel;
        this.mandatory = mandatory;
        this.hasSkillRecord = studentLevel != null && !"Not Possessed".equalsIgnoreCase(studentLevel) && !"None".equalsIgnoreCase(studentLevel);

        if (status != null && !status.trim().isEmpty()) {
            this.status = status;
            ProficiencyLevel req = ProficiencyLevel.fromString(requiredLevel);
            ProficiencyLevel std = ProficiencyLevel.fromString(studentLevel);
            this.proficiencyGap = std.calculateGapTo(req);
            this.matchValue = std.calculateMatchValue(req);
        } else {
            evaluateProficiency();
        }
    }

    /**
     * Domain constructor evaluating proficiency automatically.
     */
    public SkillGapResult(int skillId, String skillName, String category, String requiredLevel, String studentLevel, boolean mandatory) {
        this(skillId, skillName, category, requiredLevel, studentLevel, mandatory,
             studentLevel != null && !"Not Possessed".equalsIgnoreCase(studentLevel) && !"None".equalsIgnoreCase(studentLevel));
    }

    /**
     * Domain constructor evaluating proficiency automatically with explicit skill record flag.
     */
    public SkillGapResult(int skillId, String skillName, String category, String requiredLevel, String studentLevel, boolean mandatory, boolean hasSkillRecord) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.requiredLevel = requiredLevel;
        this.studentLevel = studentLevel;
        this.mandatory = mandatory;
        this.hasSkillRecord = hasSkillRecord;
        evaluateProficiency();
    }

    /**
     * Evaluates actual demonstrated proficiency against required benchmark.
     */
    public final void evaluateProficiency() {
        ProficiencyLevel req = ProficiencyLevel.fromString(requiredLevel);
        if (req == ProficiencyLevel.NONE) {
            req = ProficiencyLevel.INTERMEDIATE; // Standard default benchmark
            this.requiredLevel = req.getDisplayName();
        }

        ProficiencyLevel std = ProficiencyLevel.fromString(studentLevel);

        if (!hasSkillRecord || studentLevel == null || "Not Possessed".equalsIgnoreCase(studentLevel)) {
            this.status = "Missing";
            this.studentLevel = "Not Possessed";
            this.matchValue = 0.0;
            this.proficiencyGap = req.getLevel();
        } else if (std == ProficiencyLevel.NONE || "Unspecified".equalsIgnoreCase(studentLevel)) {
            this.status = "Insufficient Evidence";
            this.matchValue = 0.0;
            this.proficiencyGap = req.getLevel();
        } else if (std.getLevel() >= req.getLevel()) {
            this.status = "Matched";
            this.matchValue = 1.0;
            this.proficiencyGap = 0;
        } else {
            this.status = "Upgrade Needed";
            this.matchValue = std.calculateMatchValue(req);
            this.proficiencyGap = req.getLevel() - std.getLevel();
        }
    }

    public int getSkillId() { return skillId; }
    public void setSkillId(int skillId) { this.skillId = skillId; }

    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRequiredLevel() { return requiredLevel; }
    public void setRequiredLevel(String requiredLevel) {
        this.requiredLevel = requiredLevel;
        evaluateProficiency();
    }

    public String getStudentLevel() { return studentLevel; }
    public void setStudentLevel(String studentLevel) {
        this.studentLevel = studentLevel;
        evaluateProficiency();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isMandatory() { return mandatory; }
    public void setMandatory(boolean mandatory) { this.mandatory = mandatory; }

    public double getMatchValue() { return matchValue; }
    public void setMatchValue(double matchValue) { this.matchValue = matchValue; }

    public int getProficiencyGap() { return proficiencyGap; }
    public void setProficiencyGap(int proficiencyGap) { this.proficiencyGap = proficiencyGap; }

    public boolean isHasSkillRecord() { return hasSkillRecord; }
    public void setHasSkillRecord(boolean hasSkillRecord) {
        this.hasSkillRecord = hasSkillRecord;
        evaluateProficiency();
    }

    public boolean isMatched() {
        return "Matched".equalsIgnoreCase(status) || "Fully Satisfied".equalsIgnoreCase(status);
    }

    public boolean isUpgradeNeeded() {
        return "Upgrade Needed".equalsIgnoreCase(status) || "Partially Satisfied".equalsIgnoreCase(status);
    }

    public boolean isMissing() {
        return "Missing".equalsIgnoreCase(status) || (!isMatched() && !isUpgradeNeeded() && !isInsufficientEvidence());
    }

    public boolean isInsufficientEvidence() {
        return "Insufficient Evidence".equalsIgnoreCase(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillGapResult that = (SkillGapResult) o;
        return skillId == that.skillId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(skillId);
    }
}
