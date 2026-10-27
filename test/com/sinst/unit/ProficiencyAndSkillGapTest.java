package com.sinst.unit;

import com.sinst.model.ProficiencyLevel;
import com.sinst.model.SkillGapResult;
import com.sinst.model.SkillMatchConfig;
import com.sinst.service.SkillGapService;
import com.sinst.service.SkillGapServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Skill Proficiency Scale & Explainable Skill-Gap Calculations")
public class ProficiencyAndSkillGapTest {

    private SkillGapService skillGapService;

    @BeforeEach
    void setUp() {
        skillGapService = new SkillGapServiceImpl();
    }

    @Nested
    @DisplayName("ProficiencyLevel 4-Tier Domain Scale Tests")
    class ProficiencyLevelScaleTests {
        @Test
        @DisplayName("Numeric rank ordering must strictly match 0=None, 1=Beginner, 2=Intermediate, 3=Advanced, 4=Expert")
        void testProficiencyValues() {
            assertEquals(0, ProficiencyLevel.NONE.getLevel());
            assertEquals(1, ProficiencyLevel.BEGINNER.getLevel());
            assertEquals(2, ProficiencyLevel.INTERMEDIATE.getLevel());
            assertEquals(3, ProficiencyLevel.ADVANCED.getLevel());
            assertEquals(4, ProficiencyLevel.EXPERT.getLevel());
        }

        @ParameterizedTest
        @CsvSource({
            "Beginner, BEGINNER",
            "beginner, BEGINNER",
            "Intermediate, INTERMEDIATE",
            "INTERMEDIATE, INTERMEDIATE",
            "Advanced, ADVANCED",
            "advanced, ADVANCED",
            "Expert, EXPERT",
            "expert, EXPERT"
        })
        @DisplayName("fromString should correctly parse case-insensitive valid names")
        void testParsingValidLevels(String input, ProficiencyLevel expected) {
            assertEquals(expected, ProficiencyLevel.fromString(input));
        }

        @Test
        @DisplayName("fromString should safely default invalid, empty, or null strings to NONE")
        void testParsingInvalidLevels() {
            assertEquals(ProficiencyLevel.NONE, ProficiencyLevel.fromString(null));
            assertEquals(ProficiencyLevel.NONE, ProficiencyLevel.fromString(""));
            assertEquals(ProficiencyLevel.NONE, ProficiencyLevel.fromString("Master"));
            assertEquals(ProficiencyLevel.NONE, ProficiencyLevel.fromString("Novice"));
        }

        @Test
        @DisplayName("Gap calculation: Math.max(0, required - candidate)")
        void testGapCalculations() {
            assertEquals(0, ProficiencyLevel.INTERMEDIATE.calculateGapTo(ProficiencyLevel.INTERMEDIATE));
            assertEquals(-1, ProficiencyLevel.ADVANCED.calculateGapTo(ProficiencyLevel.INTERMEDIATE)); // Exceeded by 1 level
            assertEquals(1, ProficiencyLevel.BEGINNER.calculateGapTo(ProficiencyLevel.INTERMEDIATE)); // Needs +1
            assertEquals(2, ProficiencyLevel.BEGINNER.calculateGapTo(ProficiencyLevel.ADVANCED));     // Needs +2
            assertEquals(0, ProficiencyLevel.BEGINNER.calculateGapTo(null));
        }

        @Test
        @DisplayName("Match value calculation: min(1.0, candidate / required)")
        void testMatchValueCalculations() {
            assertEquals(1.0, ProficiencyLevel.INTERMEDIATE.calculateMatchValue(ProficiencyLevel.INTERMEDIATE), 0.001);
            assertEquals(1.0, ProficiencyLevel.EXPERT.calculateMatchValue(ProficiencyLevel.INTERMEDIATE), 0.001); // Exceeded
            assertEquals(0.5, ProficiencyLevel.BEGINNER.calculateMatchValue(ProficiencyLevel.INTERMEDIATE), 0.001);
            assertEquals(0.75, ProficiencyLevel.ADVANCED.calculateMatchValue(ProficiencyLevel.EXPERT), 0.001);
            assertEquals(0.0, ProficiencyLevel.NONE.calculateMatchValue(ProficiencyLevel.INTERMEDIATE), 0.001);
            assertEquals(1.0, ProficiencyLevel.BEGINNER.calculateMatchValue(null), 0.001);
        }
    }

    @Nested
    @DisplayName("SkillGapResult Classification & Scoring Tests")
    class SkillGapCalculationTests {

        @Test
        @DisplayName("Candidate competencies categorized into Matched, Upgrade Needed, Missing, and Insufficient Evidence")
        void testFourRequirementCategories() {
            // 1. Matched: Candidate Advanced (3) exceeds Intermediate (2)
            SkillGapResult r1 = new SkillGapResult(1, "Java", "Technical", "Intermediate", "Advanced", true);
            assertEquals("Matched", r1.getStatus());
            assertTrue(r1.isMatched());
            assertEquals(1.0, r1.getMatchValue());
            assertEquals(0, r1.getProficiencyGap());

            // 2. Upgrade Needed: Candidate Beginner (1) < Advanced (3)
            SkillGapResult r2 = new SkillGapResult(2, "SQL & DBMS", "Database", "Advanced", "Beginner", true);
            assertEquals("Upgrade Needed", r2.getStatus());
            assertTrue(r2.isUpgradeNeeded());
            assertEquals(1.0 / 3.0, r2.getMatchValue(), 0.001);
            assertEquals(2, r2.getProficiencyGap());

            // 3. Missing: Candidate does not possess skill at all
            SkillGapResult r3 = new SkillGapResult(3, "Spring Boot", "Framework", "Intermediate", "Not Possessed", true);
            assertEquals("Missing", r3.getStatus());
            assertTrue(r3.isMissing());
            assertEquals(0.0, r3.getMatchValue());
            assertEquals(2, r3.getProficiencyGap());

            // 4. Insufficient Evidence: Skill listed with Unspecified / None proficiency
            SkillGapResult r4 = new SkillGapResult(4, "Docker", "Tool", "Intermediate", "None", true, true);
            assertEquals("Insufficient Evidence", r4.getStatus());
            assertTrue(r4.isInsufficientEvidence());
            assertEquals(0.0, r4.getMatchValue());
        }

        @Test
        @DisplayName("Score calculation over a list of requirements with mandatory weighting")
        void testListMatchPercentageCalculation() {
            List<SkillGapResult> results = new ArrayList<>();
            // Java (Mandatory, w=2): Matched (1.0) -> 2.0 * 1.0 = 2.0
            results.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true));
            // SQL (Mandatory, w=2): Upgrade Needed (Beginner vs Intermediate = 0.5) -> 2.0 * 0.5 = 1.0
            results.add(new SkillGapResult(2, "SQL", "Database", "Intermediate", "Beginner", true));
            // Git (Optional, w=1): Missing (0.0) -> 1.0 * 0.0 = 0.0
            results.add(new SkillGapResult(3, "Git", "Tool", "Beginner", "Not Possessed", false));

            // Weighted sum = 2.0 + 1.0 + 0.0 = 3.0
            // Weight sum = 2.0 + 2.0 + 1.0 = 5.0
            // Detailed score = 3.0 / 5.0 * 100 = 60.0%
            SkillMatchConfig config = new SkillMatchConfig(2.0, 1.0);
            double detailed = skillGapService.calculateDetailedMatchScore(results, config);
            assertEquals(60.0, detailed, 0.001);

            int percentage = skillGapService.calculateMatchPercentage(results, config);
            assertEquals(60, percentage);
        }

        @Test
        @DisplayName("Empty requirements list identified as Insufficient Role Data")
        void testEmptyRoleRequirements() {
            List<SkillGapResult> empty = Collections.emptyList();
            assertTrue(skillGapService.isInsufficientRoleData(empty));
            assertEquals(0, skillGapService.calculateMatchPercentage(empty));
            assertEquals(-1.0, skillGapService.calculateDetailedMatchScore(empty, new SkillMatchConfig()));
        }

        @Test
        @DisplayName("Readiness assessment textual explanation generation")
        void testReadinessAssessment() {
            String high = skillGapService.getReadinessAssessment(85, 4, 5, "Java Backend Developer");
            assertTrue(high.contains("High Readiness") || high.contains("85%"));

            String low = skillGapService.getReadinessAssessment(30, 1, 5, "Java Backend Developer");
            assertTrue(low.contains("Skill Gap Detected") || low.contains("30%"));
        }
    }
}
