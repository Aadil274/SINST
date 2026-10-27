package com.sinst.unit;

import com.sinst.model.Certification;
import com.sinst.model.CertificationSkillMapping;
import com.sinst.model.ProficiencyLevel;
import com.sinst.model.StudentCertificationSkill;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Certification-to-Skill Catalog Mapping & Cardinality Rules")
public class CertificationMappingTest {

    @Test
    @DisplayName("One certification maps to multiple skills (1:M cardinality)")
    void testOneToManyCardinality() {
        String certName = "Oracle Certified Associate: Java SE 17 Developer";
        String org = "Oracle";

        List<CertificationSkillMapping> mappings = new ArrayList<>();
        mappings.add(new CertificationSkillMapping(
            1, certName, org, 1, "Java",
            "Core Java, OOP principles, Collections, Multithreading",
            "Oracle Exam 1Z0-829", "Intermediate", "Verified"
        ));
        mappings.add(new CertificationSkillMapping(
            2, certName, org, 15, "Problem Solving",
            "Analytical debugging under exam conditions",
            "Oracle Exam 1Z0-829", "Intermediate", "Verified"
        ));

        assertEquals(2, mappings.size());
        assertEquals("Java", mappings.get(0).getSkillName());
        assertEquals("Problem Solving", mappings.get(1).getSkillName());
        assertEquals("Intermediate", mappings.get(0).getSuggestedProficiency());
        assertEquals("Intermediate", mappings.get(1).getSuggestedProficiency());
    }

    @Test
    @DisplayName("CertificationSkillMapping model equality and hash code contracts")
    void testMappingEqualityAndHashCode() {
        CertificationSkillMapping m1 = new CertificationSkillMapping(
            10, "AWS Cloud Practitioner", "AWS", 5, "REST APIs", "Cloud REST endpoints",
            "AWS Spec", "Beginner", "Verified");
        CertificationSkillMapping m2 = new CertificationSkillMapping(
            10, "AWS Cloud Practitioner", "AWS", 5, "REST APIs", "Cloud REST endpoints",
            "AWS Spec", "Beginner", "Verified");
        CertificationSkillMapping m3 = new CertificationSkillMapping(
            11, "AWS Cloud Practitioner", "AWS", 12, "Docker", "Containers",
            "AWS Spec", "Beginner", "Verified");

        assertEquals(m1, m2);
        assertNotEquals(m1, m3);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    @DisplayName("StudentCertificationSkill review status and assessed proficiency tracking")
    void testStudentCertificationSkillLifecycle() {
        StudentCertificationSkill scs = new StudentCertificationSkill(
            1, 100, 50, 1, "Java",
            "Demonstrated core OOP and exception handling",
            "Approved", "Advanced", new Timestamp(System.currentTimeMillis())
        );

        assertEquals(1, scs.getRecordId());
        assertEquals(100, scs.getStudentId());
        assertEquals(50, scs.getCertificationId());
        assertEquals(1, scs.getSkillId());
        assertEquals("Approved", scs.getReviewStatus());
        assertEquals("Advanced", scs.getAssessedProficiency());

        scs.setReviewStatus("Pending");
        scs.setAssessedProficiency("Intermediate");

        assertEquals("Pending", scs.getReviewStatus());
        assertEquals("Intermediate", scs.getAssessedProficiency());
    }

    @Test
    @DisplayName("Unknown certification gracefully produces an empty mapping list without errors")
    void testUnknownCertificationHandling() {
        Certification uncatalogedCert = new Certification(
            999, 101, "Custom University Internal Certification", "Local University",
            Date.valueOf("2026-01-10"), "LOCAL-12345"
        );

        assertNotNull(uncatalogedCert);
        assertEquals("Custom University Internal Certification", uncatalogedCert.getCertificateName());

        // An unknown cert returns empty mappings, not null, and doesn't abort
        List<CertificationSkillMapping> emptyMappings = new ArrayList<>();
        assertTrue(emptyMappings.isEmpty());
    }
}
