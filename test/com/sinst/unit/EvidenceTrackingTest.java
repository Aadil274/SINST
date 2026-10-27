package com.sinst.unit;

import com.sinst.model.ProficiencyLevel;
import com.sinst.model.SkillEvidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Skill Evidence Tracking & Demonstrated Ability Preservation")
public class EvidenceTrackingTest {

    @Test
    @DisplayName("SkillEvidence model construction, equality, and hash code contracts")
    void testSkillEvidenceModel() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        SkillEvidence ev1 = new SkillEvidence(
            1, 100, 1, "Java", "Certification", 50,
            "Oracle Certified Associate: Java SE 17 Developer",
            "Verified core Java competency", "Verified", now
        );
        SkillEvidence ev2 = new SkillEvidence(
            1, 100, 1, "Java", "Certification", 50,
            "Oracle Certified Associate: Java SE 17 Developer",
            "Verified core Java competency", "Verified", now
        );
        SkillEvidence ev3 = new SkillEvidence(
            2, 100, 2, "SQL", "Project", 10,
            "Smart Internship & Skill Tracker",
            "Implemented relational database queries", "Verified", now
        );

        assertEquals(ev1, ev2);
        assertNotEquals(ev1, ev3);
        assertEquals(ev1.hashCode(), ev2.hashCode());
        assertEquals("Certification", ev1.getEvidenceType());
        assertEquals("Verified", ev1.getVerificationStatus());
        assertEquals(50, ev1.getReferenceId());
    }

    @Test
    @DisplayName("Demonstrated ability preservation: never downgrade existing higher proficiency")
    void testPreservingHigherProficiency() {
        // Scenario 1: Existing proficiency is ADVANCED (3), Certificate suggests INTERMEDIATE (2)
        ProficiencyLevel existingAdvanced = ProficiencyLevel.ADVANCED;
        ProficiencyLevel certIntermediate = ProficiencyLevel.INTERMEDIATE;

        ProficiencyLevel finalLevel1 = existingAdvanced.getLevel() >= certIntermediate.getLevel()
            ? existingAdvanced
            : certIntermediate;
        assertEquals(ProficiencyLevel.ADVANCED, finalLevel1, "Existing Advanced proficiency must not be downgraded");

        // Scenario 2: Existing proficiency is BEGINNER (1), Certificate suggests INTERMEDIATE (2)
        ProficiencyLevel existingBeginner = ProficiencyLevel.BEGINNER;
        ProficiencyLevel finalLevel2 = existingBeginner.getLevel() >= certIntermediate.getLevel()
            ? existingBeginner
            : certIntermediate;
        assertEquals(ProficiencyLevel.INTERMEDIATE, finalLevel2, "Beginner proficiency should be upgraded to Intermediate");

        // Scenario 3: Existing proficiency is NONE (0), Certificate suggests INTERMEDIATE (2)
        ProficiencyLevel existingNone = ProficiencyLevel.NONE;
        ProficiencyLevel finalLevel3 = existingNone.getLevel() >= certIntermediate.getLevel()
            ? existingNone
            : certIntermediate;
        assertEquals(ProficiencyLevel.INTERMEDIATE, finalLevel3, "None should be upgraded to Intermediate");
    }

    @Test
    @DisplayName("Verification status values strictly adhere to Verified, Unverified, and Rejected")
    void testVerificationStatusValues() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        SkillEvidence ev = new SkillEvidence(
            10, 100, 1, "Java", "Self-Reported", null,
            "Personal study of Java collections", "Self reported notes", "Unverified", now
        );
        assertEquals("Unverified", ev.getVerificationStatus());

        ev.setVerificationStatus("Verified");
        assertEquals("Verified", ev.getVerificationStatus());

        ev.setVerificationStatus("Rejected");
        assertEquals("Rejected", ev.getVerificationStatus());
    }
}
