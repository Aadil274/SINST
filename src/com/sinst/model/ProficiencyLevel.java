package com.sinst.model;

/**
 * Standardized proficiency levels representing skill competence in SINST.
 *
 * Consistent Proficiency Scale:
 *   NONE (Level 0)         - Skill is not possessed / missing / unranked
 *   BEGINNER (Level 1)     - Fundamental awareness & basic concepts
 *   INTERMEDIATE (Level 2) - Working practical capability & standard industry benchmark
 *   ADVANCED (Level 3)     - In-depth mastery & complex problem solving
 *   EXPERT (Level 4)       - Architecture, optimization & domain authority
 */
public enum ProficiencyLevel {
    NONE("None", 0),
    BEGINNER("Beginner", 1),
    INTERMEDIATE("Intermediate", 2),
    ADVANCED("Advanced", 3),
    EXPERT("Expert", 4);

    private final String displayName;
    private final int level;

    ProficiencyLevel(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getLevel() {
        return level;
    }

    /**
     * Parses a text string safely into a ProficiencyLevel.
     * Returns NONE for null, empty, or unknown strings.
     */
    public static ProficiencyLevel fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return NONE;
        }
        String clean = text.trim();
        for (ProficiencyLevel p : values()) {
            if (p.displayName.equalsIgnoreCase(clean) || p.name().equalsIgnoreCase(clean)) {
                return p;
            }
        }
        return NONE;
    }

    /**
     * Safe lookup by numeric score (0 to 4).
     */
    public static ProficiencyLevel fromLevel(int level) {
        for (ProficiencyLevel p : values()) {
            if (p.level == level) {
                return p;
            }
        }
        return level <= 0 ? NONE : EXPERT;
    }

    /**
     * Calculates the level gap between this demonstrated level (student)
     * and a required benchmark (role).
     * Returns:
     *   > 0 : Upgrade needed (the student is below the benchmark by N levels)
     *  <= 0 : Benchmark satisfied or exceeded
     */
    public int calculateGapTo(ProficiencyLevel required) {
        if (required == null || required == NONE) {
            return 0;
        }
        return required.level - this.level;
    }

    /**
     * Evaluates the fractional skill-match value [0.0 to 1.0] indicating
     * how closely this demonstrated level satisfies the required benchmark.
     * - If this >= required: 1.0 (fully satisfied requirement)
     * - If this <= 0 (missing or unranked): 0.0
     * - If this < required: (this.level / required.level) (partial match credit)
     */
    public double calculateMatchValue(ProficiencyLevel required) {
        if (required == null || required == NONE) {
            return 1.0; // No requirement specified
        }
        if (this.level >= required.level) {
            return 1.0;
        }
        if (this.level <= 0) {
            return 0.0;
        }
        return (double) this.level / (double) required.level;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
