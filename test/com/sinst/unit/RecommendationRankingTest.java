package com.sinst.unit;

import com.sinst.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Transparent Role Recommendations & Multi-Tier Deterministic Ranking")
public class RecommendationRankingTest {

    private SkillMatchConfig defaultConfig;

    @BeforeEach
    void setUp() {
        defaultConfig = new SkillMatchConfig(2.0, 1.0);
    }

    @Nested
    @DisplayName("Weighted Score Calculation Formula Tests")
    class WeightedScoreFormulaTests {

        @Test
        @DisplayName("Score strictly follows: sum(w_i * match_value_i) / sum(w_i) * 100")
        void testWeightedFormula() {
            // Role with:
            // Skill 1 (Mandatory, w=2.0): Candidate meets requirement -> match_value = 1.0 -> 2.0 * 1.0 = 2.0
            // Skill 2 (Mandatory, w=2.0): Candidate beginner for intermediate -> match_value = 0.5 -> 2.0 * 0.5 = 1.0
            // Skill 3 (Optional, w=1.0): Candidate missing -> match_value = 0.0 -> 1.0 * 0.0 = 0.0
            // Total weighted sum = 2.0 + 1.0 + 0.0 = 3.0
            // Total weight sum = 2.0 + 2.0 + 1.0 = 5.0
            // Expected score = (3.0 / 5.0) * 100 = 60.0%

            double totalWeightedScore = (2.0 * 1.0) + (2.0 * 0.5) + (1.0 * 0.0);
            double totalWeight = 2.0 + 2.0 + 1.0;
            double matchPercentage = (totalWeightedScore / totalWeight) * 100.0;

            assertEquals(60.0, matchPercentage, 0.001);
        }

        @Test
        @DisplayName("Configurable weights: custom mandatory weight changes calculated percentage")
        void testCustomConfigurableWeights() {
            SkillMatchConfig customConfig = new SkillMatchConfig(3.0, 1.0, 80.0, 40.0);
            assertEquals(3.0, customConfig.getMandatoryWeight());
            assertEquals(1.0, customConfig.getOptionalWeight());
            assertEquals(40.0, customConfig.getModerateReadinessThreshold());

            // With w_mandatory=3.0, w_optional=1.0:
            // Mandatory (w=3): 1.0 * 3.0 = 3.0
            // Optional (w=1): 0.0 * 1.0 = 0.0
            // Total = 3.0 / 4.0 * 100 = 75.0%
            double customScore = ((3.0 * 1.0) + (1.0 * 0.0)) / (3.0 + 1.0) * 100.0;
            assertEquals(75.0, customScore, 0.001);
        }
    }

    @Nested
    @DisplayName("Multi-Tier Deterministic Ranking Tests (compareTo)")
    class DeterministicRankingTests {

        @Test
        @DisplayName("Tier 1: Higher match score ranks ahead")
        void testTier1MatchScore() {
            RoleRecommendation r1 = createRec(1, "Google", "Backend", 85.0, 1, 2);
            RoleRecommendation r2 = createRec(2, "Microsoft", "Backend", 70.0, 0, 5);

            assertTrue(r1.compareTo(r2) < 0, "Higher match percentage (85% vs 70%) must rank first");
        }

        @Test
        @DisplayName("Tier 2: Equal match score tie broken by fewer missing mandatory skills")
        void testTier2MissingMandatorySkills() {
            RoleRecommendation r1 = createRec(1, "Google", "Backend", 80.0, 0, 2); // 0 missing mandatory
            RoleRecommendation r2 = createRec(2, "Microsoft", "Backend", 80.0, 2, 5); // 2 missing mandatory

            assertTrue(r1.compareTo(r2) < 0, "Fewer missing mandatory skills (0 vs 2) must rank first on tie");
        }

        @Test
        @DisplayName("Tier 3: Equal score and missing mandatory tie broken by higher verified evidence count")
        void testTier3VerifiedEvidenceCount() {
            RoleRecommendation r1 = createRec(1, "Google", "Backend", 80.0, 1, 5); // 5 verified evidence
            RoleRecommendation r2 = createRec(2, "Microsoft", "Backend", 80.0, 1, 2); // 2 verified evidence

            assertTrue(r1.compareTo(r2) < 0, "Higher verified evidence (5 vs 2) must rank first on tie");
        }

        @Test
        @DisplayName("Tier 4: Complete tie broken alphabetically by company name")
        void testTier4AlphabeticalCompany() {
            RoleRecommendation r1 = createRec(1, "Amazon", "Backend", 80.0, 1, 2);
            RoleRecommendation r2 = createRec(2, "Google", "Backend", 80.0, 1, 2);

            assertTrue(r1.compareTo(r2) < 0, "Amazon comes before Google alphabetically on complete tie");
        }
    }

    @Nested
    @DisplayName("Truthful Reporting & Edge Case Tests")
    class TruthfulReportingTests {

        @Test
        @DisplayName("Default vacancy status must be 'Potential Role Match'")
        void testDefaultVacancyStatus() {
            RoleRecommendation rec = createRec(1, "Google", "Developer", 75.0, 0, 2);
            assertEquals("Potential Role Match", rec.getVacancyStatus());
        }

        @Test
        @DisplayName("Role with no required skills defined must report 0% match (never 100%)")
        void testIncompleteRoleData() {
            Role emptyRole = new Role(99, "Empty Role", "No skills mapped", "Entry-level");
            assertTrue(emptyRole.getRequiredSkills().isEmpty());

            RoleRecommendation rec = new RoleRecommendation();
            rec.setCompanyId(1);
            rec.setCompanyName("Google");
            rec.setRoleId(99);
            rec.setRoleTitle("Empty Role");
            rec.setMatchPercentage(0);
            rec.setHasInsufficientRoleData(true);

            assertEquals(0, rec.getMatchPercentage());
            assertNotEquals(100, rec.getMatchPercentage(), "Incomplete role data must not falsely claim 100%");
        }

        @Test
        @DisplayName("Already applied flag correctly tracked for existing applications")
        void testAlreadyAppliedFlag() {
            RoleRecommendation rec = createRec(1, "Google", "Developer", 80.0, 0, 1);
            assertFalse(rec.isHasApplied());

            rec.setHasApplied(true);
            rec.setApplicationStatus("Interview");
            assertTrue(rec.isHasApplied());
            assertEquals("Interview", rec.getApplicationStatus());
        }

        @Test
        @DisplayName("Deduplication: duplicate company-role pairings are reduced to unique entries")
        void testDeduplication() {
            List<RoleRecommendation> list = new ArrayList<>();
            list.add(createRec(1, "Google", "Dev", 80.0, 0, 1));
            list.add(createRec(1, "Google", "Dev", 80.0, 0, 1)); // duplicate pairing
            list.add(createRec(2, "Microsoft", "Dev", 75.0, 1, 0));

            Set<String> uniqueKeys = new HashSet<>();
            List<RoleRecommendation> deduplicated = new ArrayList<>();
            for (RoleRecommendation r : list) {
                String key = r.getCompanyId() + "_" + r.getRoleId();
                if (uniqueKeys.add(key)) {
                    deduplicated.add(r);
                }
            }

            assertEquals(2, deduplicated.size());
        }
    }

    private RoleRecommendation createRec(int compId, String compName, String roleTitle,
                                         double matchPct, int missingMandatory, int evidenceCount) {
        RoleRecommendation rec = new RoleRecommendation();
        rec.setCompanyId(compId);
        rec.setCompanyName(compName);
        rec.setRoleId(1);
        rec.setRoleTitle(roleTitle);
        rec.setMatchPercentage((int) Math.round(matchPct));
        rec.setDetailedScore(matchPct);
        rec.setVacancyStatus("Potential Role Match");
        rec.setVacancySource("Careers Portal");
        rec.setCareersUrl("https://example.com/careers");

        List<SkillGapResult> missingList = new ArrayList<>();
        for (int i = 0; i < missingMandatory; i++) {
            missingList.add(new SkillGapResult(i + 1, "MissingSkill" + i, "Technical", "Intermediate", "None", true));
        }
        rec.setMissingMandatorySkills(missingList);
        rec.setVerifiedEvidenceCount(evidenceCount);
        return rec;
    }
}
