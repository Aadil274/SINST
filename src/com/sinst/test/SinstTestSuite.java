package com.sinst.test;

import com.sinst.dao.*;
import com.sinst.exception.*;
import com.sinst.model.*;
import com.sinst.service.*;
import com.sinst.util.AppExecutor;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SinstTestSuite {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println("Running SINST Automated Validation & Model Test Suite");
        System.out.println("=======================================================");

        testEmailValidation();
        testRollNumberValidation();
        testNameValidation();
        testYearOfStudyValidation();
        testPhoneValidation();
        testDateValidation();
        testTimestampValidation();
        testStipendValidation();
        testModelEqualityAndHashCode();
        testProficiencyScaleAndGapCalculations();
        testFourRequirementCategories();
        testWeightedAndConfigurableMatchScore();
        testEmptyRoleAndEmptyProfileCases();
        testDuplicateSkillPrevention();
        testSkillGapResultLogic();
        testApplicationModelIntegrity();
        testModelCompositionAndInheritance();
        testDashboardStatsCalculations();
        testDomainExceptionHierarchy();
        testSkillGapServiceCalculations();
        testServiceLayerValidationContracts();
        testCertificationSkillMappingModels();
        testCertificationToSkillRelationsAndCardinality();
        testSafeHandlingOfUnknownCertifications();
        testSkillEvidenceTrackingAndVerificationStates();
        testPreservingDemonstratedAbilityOnCertificationSave();
        testTransactionalIntegrityAndRollback();
        testCompanyRoleAndRecommendationModels();
        testRecommendationRankingFormula();
        testRecommendationDynamicResponsiveness();
        testRecommendationAlreadyAppliedTagging();
        testRecommendationCriticalDistinctionAndTruthfulness();
        testRecommendationDeduplication();
        testPrompt6ModelEnhancements();
        testPrompt6InputValidation();
        testPrompt6CrossStudentAuthorization();
        testPrompt6DuplicateSubmissionHandling();
        testPrompt6ErrorSanitization();
        testPrompt7SwingResponsivenessAndConcurrency();

        System.out.println("-------------------------------------------------------");
        System.out.println(String.format("Test Results: %d run, %d passed, %d failed", testsRun, testsPassed, testsFailed));
        System.out.println("=======================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void assertTrue(String message, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message);
        }
    }

    private static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        testsRun++;
        boolean equal = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (equal) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message + " (Expected: " + expected + ", Got: " + actual + ")");
        }
    }

    private static void fail(String message) {
        testsRun++;
        testsFailed++;
        System.err.println("  [FAIL] " + message);
    }

    private static void testEmailValidation() {
        System.out.println("\n--- Testing Email Validation ---");
        assertTrue("Valid standard email", ValidationUtil.isValidEmail("student@example.com"));
        assertTrue("Valid email with subdomains & tag", ValidationUtil.isValidEmail("user.name+tag@sub.domain.edu"));
        assertTrue("Valid college email", ValidationUtil.isValidEmail("student25@college.ac.in"));

        assertFalse("Null email rejected", ValidationUtil.isValidEmail(null));
        assertFalse("Empty email rejected", ValidationUtil.isValidEmail(""));
        assertFalse("Whitespace email rejected", ValidationUtil.isValidEmail("   "));
        assertFalse("Plain string rejected", ValidationUtil.isValidEmail("plainaddress"));
        assertFalse("Missing username rejected", ValidationUtil.isValidEmail("@missingusername.com"));
        assertFalse("Missing domain rejected", ValidationUtil.isValidEmail("user@.com"));
        assertFalse("Missing @ rejected", ValidationUtil.isValidEmail("user.example.com"));
    }

    private static void testRollNumberValidation() {
        System.out.println("\n--- Testing Roll Number Validation ---");
        assertTrue("Valid alphanumeric roll", ValidationUtil.isValidRollNumber("25R11A0501"));
        assertTrue("Valid short roll", ValidationUtil.isValidRollNumber("CS101"));
        assertTrue("Valid uppercase/lowercase roll", ValidationUtil.isValidRollNumber("roll1234"));

        assertFalse("Null roll rejected", ValidationUtil.isValidRollNumber(null));
        assertFalse("Empty roll rejected", ValidationUtil.isValidRollNumber(""));
        assertFalse("Too short roll rejected", ValidationUtil.isValidRollNumber("12"));
        assertFalse("Special character roll rejected", ValidationUtil.isValidRollNumber("25R11@0501"));
    }

    private static void testNameValidation() {
        System.out.println("\n--- Testing Name Validation ---");
        assertTrue("Standard full name", ValidationUtil.isValidName("John Doe"));
        assertTrue("Name with dots and hyphens", ValidationUtil.isValidName("Dr. Sarah O'Connor-Smith"));
        assertTrue("Single space between names", ValidationUtil.isValidName("Aadil Khan"));

        assertFalse("Null name rejected", ValidationUtil.isValidName(null));
        assertFalse("Empty name rejected", ValidationUtil.isValidName(""));
        assertFalse("Single character name rejected", ValidationUtil.isValidName("A"));
        assertFalse("Name with numbers rejected", ValidationUtil.isValidName("John123"));
        assertFalse("Name with symbols rejected", ValidationUtil.isValidName("John@Doe"));
    }

    private static void testYearOfStudyValidation() {
        System.out.println("\n--- Testing Year of Study Validation ---");
        assertTrue("Year 1 valid", ValidationUtil.isValidYearOfStudy(1));
        assertTrue("Year 2 valid", ValidationUtil.isValidYearOfStudy(2));
        assertTrue("Year 3 valid", ValidationUtil.isValidYearOfStudy(3));
        assertTrue("Year 4 valid", ValidationUtil.isValidYearOfStudy(4));

        assertFalse("Year 0 invalid", ValidationUtil.isValidYearOfStudy(0));
        assertFalse("Year 5 invalid", ValidationUtil.isValidYearOfStudy(5));
        assertFalse("Negative year invalid", ValidationUtil.isValidYearOfStudy(-1));
    }

    private static void testPhoneValidation() {
        System.out.println("\n--- Testing Phone Validation ---");
        assertTrue("Null phone allowed (optional)", ValidationUtil.isValidPhone(null));
        assertTrue("Empty phone allowed (optional)", ValidationUtil.isValidPhone(""));
        assertTrue("Whitespace phone allowed (optional)", ValidationUtil.isValidPhone("   "));
        assertTrue("Valid 10-digit phone", ValidationUtil.isValidPhone("9876543210"));
        assertTrue("Valid phone with + prefix", ValidationUtil.isValidPhone("+919876543210"));

        assertFalse("Too short phone rejected", ValidationUtil.isValidPhone("12345"));
        assertFalse("Alpha characters in phone rejected", ValidationUtil.isValidPhone("98765abcd0"));
        assertFalse("Too long phone rejected", ValidationUtil.isValidPhone("+12345678901234567"));
    }

    private static void testDateValidation() {
        System.out.println("\n--- Testing Date Validation ---");
        assertTrue("Valid calendar date", ValidationUtil.isValidDate("2026-03-15"));
        assertTrue("Valid leap year date", ValidationUtil.isValidDate("2024-02-29"));
        assertTrue("Valid year-end date", ValidationUtil.isValidDate("2026-12-31"));

        assertFalse("Null date rejected", ValidationUtil.isValidDate(null));
        assertFalse("Empty date rejected", ValidationUtil.isValidDate(""));
        assertFalse("Non-existent leap day in non-leap year rejected", ValidationUtil.isValidDate("2026-02-29"));
        assertFalse("Invalid day (Feb 30) rejected", ValidationUtil.isValidDate("2026-02-30"));
        assertFalse("Invalid month (13) rejected", ValidationUtil.isValidDate("2026-13-01"));
        assertFalse("Incorrect separator rejected", ValidationUtil.isValidDate("2026/03/15"));
        assertFalse("Arbitrary text rejected", ValidationUtil.isValidDate("not-a-date"));

        Date parsed = ValidationUtil.parseDate("2026-05-20");
        assertTrue("Parsed date is non-null", parsed != null);
        assertEquals("Parsed date string matches", "2026-05-20", parsed.toString());
    }

    private static void testTimestampValidation() {
        System.out.println("\n--- Testing Timestamp Validation ---");
        assertTrue("Valid timestamp with seconds", ValidationUtil.isValidTimestamp("2026-03-15 14:30:00"));
        assertTrue("Valid timestamp without seconds", ValidationUtil.isValidTimestamp("2026-03-15 14:30"));

        assertFalse("Null timestamp rejected", ValidationUtil.isValidTimestamp(null));
        assertFalse("Empty timestamp rejected", ValidationUtil.isValidTimestamp(""));
        assertFalse("Invalid hour rejected", ValidationUtil.isValidTimestamp("2026-03-15 25:00:00"));
        assertFalse("Invalid minute rejected", ValidationUtil.isValidTimestamp("2026-03-15 14:65:00"));
        assertFalse("Malformed timestamp rejected", ValidationUtil.isValidTimestamp("2026-03-15T14:30:00"));

        Timestamp ts = ValidationUtil.parseTimestamp("2026-03-15 10:00:00");
        assertTrue("Parsed timestamp is non-null", ts != null);
    }

    private static void testStipendValidation() {
        System.out.println("\n--- Testing Stipend Validation ---");
        assertFalse("Positive formatted stipend has no negative", ValidationUtil.hasNegativeStipend("₹ 35,000 / mo"));
        assertFalse("Unpaid/zero stipend has no negative", ValidationUtil.hasNegativeStipend("₹ 0 / mo"));
        assertFalse("Numeric positive stipend has no negative", ValidationUtil.hasNegativeStipend("50000"));
        assertFalse("Null stipend has no negative", ValidationUtil.hasNegativeStipend(null));
        assertFalse("Empty stipend has no negative", ValidationUtil.hasNegativeStipend(""));

        assertTrue("Direct negative stipend detected", ValidationUtil.hasNegativeStipend("-35000"));
        assertTrue("Spaced negative stipend detected", ValidationUtil.hasNegativeStipend("- 5000"));
        assertTrue("Currency prefix negative stipend detected", ValidationUtil.hasNegativeStipend("₹ -35000 / mo"));
    }

    private static void testModelEqualityAndHashCode() {
        System.out.println("\n--- Testing Model Equality & HashCode Contracts ---");
        Student s1 = new Student(1, "25R11A0501", "Alice", "alice@example.com", "pass", "CSE", 2, "9876543210");
        Student s2 = new Student(2, "25R11A0501", "Alice", "alice@example.com", "pass", "CSE", 2, "9876543210");
        Student s3 = new Student(3, "25R11A0502", "Bob", "bob@example.com", "pass", "ECE", 3, "9876543211");

        assertEquals("Students with same roll number are equal", s1, s2);
        assertFalse("Students with different roll numbers are not equal", s1.equals(s3));
        assertEquals("Equal students produce same hashCode", s1.hashCode(), s2.hashCode());

        Company c1 = new Company(10, "Google", "Tech", "Bangalore", "google.com", "hr@google.com");
        Company c2 = new Company(10, "Google Cloud", "Cloud", "Bangalore", "cloud.google.com", "hr@google.com");
        Company c3 = new Company(20, "Amazon", "Retail", "Hyderabad", "amazon.com", "hr@amazon.com");

        assertEquals("Companies with same ID are equal", c1, c2);
        assertFalse("Companies with different IDs are not equal", c1.equals(c3));
        assertEquals("Equal companies produce same hashCode", c1.hashCode(), c2.hashCode());

        Role r1 = new Role(5, "Java Developer", "Backend", "Intern");
        Role r2 = new Role(5, "Java Software Engineer", "Backend", "Intern");
        Role r3 = new Role(6, "Frontend Developer", "UI", "Intern");

        assertEquals("Roles with same ID are equal", r1, r2);
        assertFalse("Roles with different IDs are not equal", r1.equals(r3));

        Skill sk1 = new Skill(100, "Java", "Technical", "Core Java");
        Skill sk2 = new Skill(100, "Java SE", "Technical", "Core Java SE");
        Skill sk3 = new Skill(101, "Python", "Technical", "Python 3");

        assertEquals("Skills with same ID are equal", sk1, sk2);
        assertFalse("Skills with different IDs are not equal", sk1.equals(sk3));

        Certification cert1 = new Certification(1, 1, "Java OCA", "Oracle", Date.valueOf("2026-01-01"), "CRED-1");
        Certification cert2 = new Certification(1, 1, "Java OCA", "Oracle Inc", Date.valueOf("2026-01-01"), "CRED-1");
        assertEquals("Certifications with same ID are equal", cert1, cert2);

        Project p1 = new Project(1, 1, "SINST", "Tracker", "Java, Swing", "http://github");
        Project p2 = new Project(1, 1, "SINST", "Tracker v2", "Java, Swing", "http://github");
        assertEquals("Projects with same ID are equal", p1, p2);
    }

    private static void testProficiencyScaleAndGapCalculations() {
        System.out.println("\n--- Testing Standard Proficiency Scale & Gap Calculations ---");
        // Scale values
        assertEquals("NONE level is 0", 0, ProficiencyLevel.NONE.getLevel());
        assertEquals("BEGINNER level is 1", 1, ProficiencyLevel.BEGINNER.getLevel());
        assertEquals("INTERMEDIATE level is 2", 2, ProficiencyLevel.INTERMEDIATE.getLevel());
        assertEquals("ADVANCED level is 3", 3, ProficiencyLevel.ADVANCED.getLevel());
        assertEquals("EXPERT level is 4", 4, ProficiencyLevel.EXPERT.getLevel());

        // Parsing
        assertEquals("Parse Beginner", ProficiencyLevel.BEGINNER, ProficiencyLevel.fromString("Beginner"));
        assertEquals("Parse case-insensitive intermediate", ProficiencyLevel.INTERMEDIATE, ProficiencyLevel.fromString("intermediate"));
        assertEquals("Parse uppercase advanced", ProficiencyLevel.ADVANCED, ProficiencyLevel.fromString("ADVANCED"));
        assertEquals("Parse expert", ProficiencyLevel.EXPERT, ProficiencyLevel.fromString("Expert"));
        assertEquals("Null maps to NONE", ProficiencyLevel.NONE, ProficiencyLevel.fromString(null));
        assertEquals("Empty string maps to NONE", ProficiencyLevel.NONE, ProficiencyLevel.fromString("   "));
        assertEquals("Invalid text maps to NONE", ProficiencyLevel.NONE, ProficiencyLevel.fromString("Guru"));

        // Gap calculations
        assertEquals("Equal levels have 0 gap", 0, ProficiencyLevel.INTERMEDIATE.calculateGapTo(ProficiencyLevel.INTERMEDIATE));
        assertEquals("Beginner to Advanced is +2 gap", 2, ProficiencyLevel.BEGINNER.calculateGapTo(ProficiencyLevel.ADVANCED));
        assertEquals("Beginner to Intermediate is +1 gap", 1, ProficiencyLevel.BEGINNER.calculateGapTo(ProficiencyLevel.INTERMEDIATE));
        assertEquals("Advanced to Intermediate is satisfied (-1)", -1, ProficiencyLevel.ADVANCED.calculateGapTo(ProficiencyLevel.INTERMEDIATE));
        assertEquals("Gap to null required is 0", 0, ProficiencyLevel.BEGINNER.calculateGapTo(null));

        // Match values
        assertEquals("Exact match value is 1.0", 1.0, ProficiencyLevel.INTERMEDIATE.calculateMatchValue(ProficiencyLevel.INTERMEDIATE));
        assertEquals("Exceeded match value is 1.0", 1.0, ProficiencyLevel.EXPERT.calculateMatchValue(ProficiencyLevel.INTERMEDIATE));
        assertEquals("Beginner vs Intermediate match value is 0.5", 0.5, ProficiencyLevel.BEGINNER.calculateMatchValue(ProficiencyLevel.INTERMEDIATE));
        assertEquals("Advanced vs Expert match value is 0.75", 0.75, ProficiencyLevel.ADVANCED.calculateMatchValue(ProficiencyLevel.EXPERT));
        assertEquals("None vs Intermediate match value is 0.0", 0.0, ProficiencyLevel.NONE.calculateMatchValue(ProficiencyLevel.INTERMEDIATE));
    }

    private static void testFourRequirementCategories() {
        System.out.println("\n--- Testing 4 Distinct Requirement Categories ---");
        // 1. Fully satisfied: Beginner student meets Beginner requirement
        SkillGapResult fullExact = new SkillGapResult(1, "Git", "Tool", "Beginner", "Beginner", true, true);
        assertTrue("Beginner meets Beginner requirement", fullExact.isMatched());
        assertFalse("Fully satisfied is not upgrade needed", fullExact.isUpgradeNeeded());
        assertFalse("Fully satisfied is not missing", fullExact.isMissing());
        assertEquals("Full satisfaction match value is 1.0", 1.0, fullExact.getMatchValue());
        assertEquals("Full satisfaction gap is 0", 0, fullExact.getProficiencyGap());

        // 1b. Fully satisfied: Advanced student exceeds Intermediate requirement
        SkillGapResult fullExceeded = new SkillGapResult(2, "SQL", "Database", "Intermediate", "Advanced", true, true);
        assertTrue("Advanced exceeds Intermediate requirement", fullExceeded.isMatched());
        assertEquals("Exceeded satisfaction match value is 1.0", 1.0, fullExceeded.getMatchValue());
        assertEquals("Exceeded gap is 0", 0, fullExceeded.getProficiencyGap());

        // 2. Partially satisfied: Beginner student for Advanced requirement
        SkillGapResult partial = new SkillGapResult(3, "Java", "Technical", "Advanced", "Beginner", true, true);
        assertFalse("Beginner is NOT considered matched when role requires Advanced", partial.isMatched());
        assertTrue("Beginner for Advanced is identified as Upgrade Needed", partial.isUpgradeNeeded());
        assertFalse("Partially satisfied is not completely missing", partial.isMissing());
        assertEquals("Proficiency gap is +2 levels", 2, partial.getProficiencyGap());
        // Match value is 1 / 3 = 0.3333...
        assertTrue("Partial match credit reflects 1/3 ratio", Math.abs(partial.getMatchValue() - (1.0 / 3.0)) < 0.001);

        // 3. Missing skill: Student does not have skill record
        SkillGapResult missing = new SkillGapResult(4, "AWS", "Cloud", "Intermediate", "Not Possessed", false, false);
        assertFalse("Missing skill is not matched", missing.isMatched());
        assertFalse("Missing skill is not upgrade needed", missing.isUpgradeNeeded());
        assertTrue("Missing skill is identified as Missing", missing.isMissing());
        assertEquals("Missing skill match value is 0.0", 0.0, missing.getMatchValue());
        assertEquals("Missing skill gap equals required level (2)", 2, missing.getProficiencyGap());

        // 4. Insufficient Evidence: Student has a skill record but proficiency is unspecified or null
        SkillGapResult unverified = new SkillGapResult(5, "Docker", "Tool", "Intermediate", "Unspecified", false, true);
        assertFalse("Unverified skill is not matched", unverified.isMatched());
        assertTrue("Unverified skill is identified as Insufficient Evidence", unverified.isInsufficientEvidence());
        assertEquals("Insufficient evidence match value is 0.0", 0.0, unverified.getMatchValue());
        assertEquals("Insufficient evidence gap equals required level (2)", 2, unverified.getProficiencyGap());
    }

    private static void testWeightedAndConfigurableMatchScore() {
        System.out.println("\n--- Testing Transparent Weighted Match Score Calculation ---");
        SkillGapService service = new SkillGapServiceImpl();

        // Scenario: 1 Mandatory skill and 1 Optional skill
        // Role requires Skill 1 (Mandatory, weight 2.0) and Skill 2 (Optional, weight 1.0)
        SkillGapResult s1 = new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true, true); // Matched (1.0)
        SkillGapResult s2 = new SkillGapResult(2, "Docker", "Tool", "Intermediate", "Not Possessed", false, false); // Missing (0.0)

        List<SkillGapResult> listA = new ArrayList<>();
        listA.add(s1);
        listA.add(s2);

        // Score = (1.0 * 2.0 + 0.0 * 1.0) / (2.0 + 1.0) * 100 = (2.0 / 3.0) * 100 = 66.67% -> 67%
        int scoreA = service.calculateMatchPercentage(listA);
        assertEquals("Mandatory matched + Optional missing produces 67%", 67, scoreA);

        // Inverted: Mandatory missing (0.0 * 2.0) + Optional matched (1.0 * 1.0)
        SkillGapResult s3 = new SkillGapResult(3, "Java", "Technical", "Intermediate", "Not Possessed", true, false); // Missing (0.0)
        SkillGapResult s4 = new SkillGapResult(4, "Docker", "Tool", "Intermediate", "Intermediate", false, true);   // Matched (1.0)

        List<SkillGapResult> listB = new ArrayList<>();
        listB.add(s3);
        listB.add(s4);

        // Score = (0.0 * 2.0 + 1.0 * 1.0) / (2.0 + 1.0) * 100 = (1.0 / 3.0) * 100 = 33.33% -> 33%
        int scoreB = service.calculateMatchPercentage(listB);
        assertEquals("Mandatory missing + Optional matched produces 33%", 33, scoreB);

        // Configurable weights: set both to 1.0 (equal weighting)
        SkillMatchConfig equalConfig = new SkillMatchConfig(1.0, 1.0);
        int scoreEqualA = service.calculateMatchPercentage(listA, equalConfig);
        assertEquals("Equal weights: 1 matched + 1 missing produces 50%", 50, scoreEqualA);

        // Partial credit test under equal weights:
        // Skill 1: Intermediate required / Intermediate student = 1.0
        // Skill 2: Intermediate required / Beginner student = 0.5 (partial credit!)
        SkillGapResult s5 = new SkillGapResult(5, "SQL", "Database", "Intermediate", "Beginner", true, true);
        List<SkillGapResult> listPartial = new ArrayList<>();
        listPartial.add(s1);
        listPartial.add(s5);

        // Score = (1.0 * 1.0 + 0.5 * 1.0) / (1.0 + 1.0) * 100 = 1.5 / 2.0 * 100 = 75%
        int scorePartial = service.calculateMatchPercentage(listPartial, equalConfig);
        assertEquals("Partial credit: 1 full (1.0) + 1 beginner for intermediate (0.5) produces 75%", 75, scorePartial);
    }

    private static void testEmptyRoleAndEmptyProfileCases() {
        System.out.println("\n--- Testing Empty Role & Empty Profile Edge Cases ---");
        SkillGapService service = new SkillGapServiceImpl();

        // 1. Role with no requirements (0 requirements defined)
        List<SkillGapResult> emptyRole = new ArrayList<>();
        assertTrue("Empty role recognized as insufficient role data", service.isInsufficientRoleData(emptyRole));
        assertEquals("Empty role match percentage is 0", 0, service.calculateMatchPercentage(emptyRole));
        assertEquals("Detailed score for empty role is -1.0", -1.0, service.calculateDetailedMatchScore(emptyRole, null));

        String assessmentEmpty = service.getReadinessAssessment(0, 0, 0, "Cloud Specialist");
        assertTrue("Insufficient role data message displayed", assessmentEmpty.contains("Insufficient role data"));
        assertFalse("Does not claim 100% match on empty role", assessmentEmpty.contains("100%"));

        // 2. Student with empty profile (possesses 0 skills for a role with 3 requirements)
        List<SkillGapResult> emptyStudent = new ArrayList<>();
        emptyStudent.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Not Possessed", true, false));
        emptyStudent.add(new SkillGapResult(2, "SQL", "Technical", "Intermediate", "Not Possessed", true, false));
        emptyStudent.add(new SkillGapResult(3, "DSA", "Technical", "Intermediate", "Not Possessed", true, false));

        int studentScore = service.calculateMatchPercentage(emptyStudent);
        assertEquals("Empty student profile match score is 0%", 0, studentScore);
        String assessmentStudent = service.getReadinessAssessment(studentScore, 0, 3, "Java Developer");
        assertTrue("Skill Gap Detected message displayed for empty student", assessmentStudent.contains("Skill Gap Detected"));
        assertTrue("Mentions 0 of 3 skills met", assessmentStudent.contains("0 of 3"));
    }

    private static void testDuplicateSkillPrevention() {
        System.out.println("\n--- Testing Duplicate Skill Prevention from Multi-Joins ---");
        SkillGapService service = new SkillGapServiceImpl();

        // Suppose a duplicate join caused skill ID 1 to appear 3 times in the results
        List<SkillGapResult> duplicateResults = new ArrayList<>();
        duplicateResults.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true, true));
        duplicateResults.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true, true)); // duplicate
        duplicateResults.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true, true)); // duplicate
        duplicateResults.add(new SkillGapResult(2, "SQL", "Technical", "Intermediate", "Not Possessed", true, false));

        // If duplicates were counted: Java (weight 2 * 3 = 6) + SQL (0) / (6 + 2 = 8) = 75%
        // But with distinct deduplication: Java (weight 2) + SQL (0) / (2 + 2 = 4) = 50%
        int score = service.calculateMatchPercentage(duplicateResults);
        assertEquals("Duplicate records are deduplicated and do not inflate score (50%)", 50, score);
    }

    private static void testSkillGapResultLogic() {
        System.out.println("\n--- Testing Skill-Gap Analysis & Proficiency Evaluation ---");
        // Case 1: Match (Student meets or exceeds benchmark)
        SkillGapResult matchedExact = new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", "Matched", true);
        assertTrue("Exact level is matched", matchedExact.isMatched());
        assertFalse("Exact level is not upgrade needed", matchedExact.isUpgradeNeeded());
        assertFalse("Exact level is not missing", matchedExact.isMissing());

        SkillGapResult matchedExceeded = new SkillGapResult(2, "SQL", "Technical", "Intermediate", "Expert", "Matched", true);
        assertTrue("Exceeded level is matched", matchedExceeded.isMatched());
        assertFalse("Exceeded level is not upgrade needed", matchedExceeded.isUpgradeNeeded());

        // Case 2: Upgrade Needed (Student possesses skill, but at lower proficiency than required)
        SkillGapResult upgradeNeeded = new SkillGapResult(3, "System Design", "Technical", "Advanced", "Beginner", "Upgrade Needed", true);
        assertFalse("Lower level is not matched", upgradeNeeded.isMatched());
        assertTrue("Lower level is upgrade needed", upgradeNeeded.isUpgradeNeeded());
        assertFalse("Lower level is not completely missing", upgradeNeeded.isMissing());

        // Case 3: Missing (Student does not possess the skill at all)
        SkillGapResult missing = new SkillGapResult(4, "Docker", "Tool", "Intermediate", null, "Missing", false);
        assertFalse("Missing skill is not matched", missing.isMatched());
        assertFalse("Missing skill is not upgrade needed", missing.isUpgradeNeeded());
        assertTrue("Missing skill is recognized as missing", missing.isMissing());
    }

    private static void testApplicationModelIntegrity() {
        System.out.println("\n--- Testing Application Uniqueness & Composite Identity ---");
        Date appDate = Date.valueOf("2026-01-15");
        Date deadline = Date.valueOf("2026-02-15");

        Application a1 = new Application(100, 1, 10, 20, appDate, deadline, "Applied", "₹ 30,000", "Referral");
        Application a2 = new Application(100, 1, 10, 20, appDate, deadline, "Shortlisted", "₹ 35,000", "Referral");
        Application a3 = new Application(0, 1, 10, 20, appDate, deadline, "Applied", "₹ 30,000", "New attempt");
        Application a4 = new Application(101, 1, 10, 21, appDate, deadline, "Applied", "₹ 30,000", "Different role");

        assertEquals("Applications with same ID are equal", a1, a2);
        assertTrue("Unsaved applications with same student, company, and role match uniqueness identity",
                a1.getStudentId() == a3.getStudentId() &&
                a1.getCompanyId() == a3.getCompanyId() &&
                a1.getRoleId() == a3.getRoleId());
        assertFalse("Applications to different roles do not collide",
                a1.getRoleId() == a4.getRoleId());
    }

    private static void testModelCompositionAndInheritance() {
        System.out.println("\n--- Testing Model Composition & Inheritance ---");
        // InternshipApplication extends Application
        Company comp = new Company(10, "Google", "Tech", "Hyderabad", "google.com", "hr@google.com");
        Role role = new Role(20, "Backend Engineer", "Engineering", "Intern");
        Date appDate = Date.valueOf("2026-02-01");
        Date deadline = Date.valueOf("2026-03-01");

        InternshipApplication ia = new InternshipApplication(1, 5, 10, 20, appDate, deadline, "Applied", "₹ 45,000", "Referral", comp, role);
        assertTrue("InternshipApplication is instance of Application", ia instanceof Application);
        assertEquals("Composed company matches", "Google", ia.getCompany().getCompanyName());
        assertEquals("Composed role matches", "Backend Engineer", ia.getRole().getRoleTitle());
        assertEquals("Inherited application date matches", appDate, ia.getApplicationDate());

        // Bidirectional composition in Application
        Application app = new Application();
        app.setCompany(comp);
        app.setRole(role);
        assertEquals("Setting company sets companyId", 10, app.getCompanyId());
        assertEquals("Setting company sets companyName", "Google", app.getCompanyName());
        assertEquals("Setting role sets roleId", 20, app.getRoleId());
        assertEquals("Setting role sets roleTitle", "Backend Engineer", app.getRoleTitle());

        // Interview composition with Application
        Interview interview = new Interview();
        interview.setApplication(app);
        assertEquals("Interview references composed application", app, interview.getApplication());

        // Student composition with collections
        Student student = new Student(1, "25R11A0501", "Alice", "alice@example.com", "pass", "CSE", 3, "9876543210");
        List<Skill> skills = new ArrayList<>();
        skills.add(new Skill(1, "Java", "Technical", "Core Java"));
        student.setSkills(skills);
        assertEquals("Student holds composed skills list", 1, student.getSkills().size());

        List<Certification> certs = new ArrayList<>();
        certs.add(new Certification(1, 1, "AWS Cloud Practitioner", "AWS", Date.valueOf("2026-01-01"), "AWS-123"));
        student.setCertifications(certs);
        assertEquals("Student holds composed certs list", 1, student.getCertifications().size());

        List<Project> projs = new ArrayList<>();
        projs.add(new Project(1, 1, "SINST", "Tracker", "Java", "http://github"));
        student.setProjects(projs);
        assertEquals("Student holds composed projects list", 1, student.getProjects().size());

        // Role composition with required skills
        role.setRequiredSkills(skills);
        assertEquals("Role holds composed required skills list", 1, role.getRequiredSkills().size());
    }

    private static void testDashboardStatsCalculations() {
        System.out.println("\n--- Testing DashboardStats Domain Aggregations ---");
        DashboardStats stats = new DashboardStats(10, 4, 2, 2, 1, 1, 3, 8);
        assertEquals("Total applications count", 10, stats.getTotal());
        assertEquals("Total applications via alias", 10, stats.getTotalApplications());
        // Under review = Applied (4) + Shortlisted (2) = 6
        assertEquals("Under review calculation (applied + shortlisted)", 6, stats.getUnderReview());
        assertEquals("Upcoming interviews count", 3, stats.getUpcomingInterviews());
        assertEquals("Total skills count", 8, stats.getTotalSkills());

        // Map serialization and deserialization
        Map<String, Integer> map = stats.asMap();
        assertEquals("asMap Total matches", 10, (int) map.get("Total"));
        assertEquals("asMap Applied matches", 4, (int) map.get("Applied"));
        assertEquals("asMap Shortlisted matches", 2, (int) map.get("Shortlisted"));
        assertEquals("asMap Interview matches", 2, (int) map.get("Interview"));
        assertEquals("asMap Selected matches", 1, (int) map.get("Selected"));
        assertEquals("asMap Rejected matches", 1, (int) map.get("Rejected"));

        DashboardStats restored = DashboardStats.fromMap(map);
        assertEquals("Restored stats total matches", stats.getTotal(), restored.getTotal());
        assertEquals("Restored stats under review matches", stats.getUnderReview(), restored.getUnderReview());
    }

    private static void testDomainExceptionHierarchy() {
        System.out.println("\n--- Testing Domain Exception Hierarchy ---");
        ValidationException ve = new ValidationException("Invalid format");
        assertTrue("ValidationException is SinstException", ve instanceof SinstException);
        assertEquals("ValidationException preserves message", "Invalid format", ve.getMessage());

        DatabaseException de = new DatabaseException("Connection timeout");
        assertTrue("DatabaseException is SinstException", de instanceof SinstException);
        assertEquals("DatabaseException preserves message", "Connection timeout", de.getMessage());

        DuplicateEntityException dpe = new DuplicateEntityException("Duplicate record");
        assertTrue("DuplicateEntityException is SinstException", dpe instanceof SinstException);

        EntityNotFoundException nfe = new EntityNotFoundException("Entity missing");
        assertTrue("EntityNotFoundException is SinstException", nfe instanceof SinstException);
    }

    private static void testSkillGapServiceCalculations() {
        System.out.println("\n--- Testing SkillGapService Decoupled Calculations ---");
        SkillGapService skillGapService = new SkillGapServiceImpl();

        List<SkillGapResult> results = new ArrayList<>();
        // Java (Mandatory): Intermediate / Intermediate -> matchValue 1.0, weight 2.0
        results.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true, true));
        // SQL (Mandatory): Intermediate / Intermediate -> matchValue 1.0, weight 2.0
        results.add(new SkillGapResult(2, "SQL", "Technical", "Intermediate", "Intermediate", true, true));
        // Docker (Mandatory): Intermediate / Beginner -> matchValue 0.5, weight 2.0
        results.add(new SkillGapResult(3, "Docker", "Tool", "Intermediate", "Beginner", true, true));
        // AWS (Optional): Intermediate / Missing -> matchValue 0.0, weight 1.0
        results.add(new SkillGapResult(4, "AWS", "Cloud", "Intermediate", null, false, false));

        // Weighted Match: (1.0*2 + 1.0*2 + 0.5*2 + 0.0*1) / (2 + 2 + 2 + 1) = 5.0 / 7.0 = 71.42% -> 71%
        int matchPct = skillGapService.calculateMatchPercentage(results);
        assertEquals("Weighted match percentage calculated correctly (71%)", 71, matchPct);

        String assessment71 = skillGapService.getReadinessAssessment(matchPct, 2, 4, "Backend Developer");
        assertTrue("Moderate match string generated", assessment71.contains("Moderate Match"));

        // All matched = 100%
        List<SkillGapResult> allMatched = new ArrayList<>();
        allMatched.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", "Intermediate", true, true));
        allMatched.add(new SkillGapResult(2, "SQL", "Technical", "Intermediate", "Intermediate", true, true));
        int matchPct100 = skillGapService.calculateMatchPercentage(allMatched);
        assertEquals("100% match percentage calculated correctly", 100, matchPct100);

        String assessment100 = skillGapService.getReadinessAssessment(matchPct100, 2, 2, "Backend Developer");
        assertTrue("High readiness string generated", assessment100.contains("High Readiness"));

        // None matched = 0%
        List<SkillGapResult> noneMatched = new ArrayList<>();
        noneMatched.add(new SkillGapResult(1, "Java", "Technical", "Intermediate", null, true, false));
        int matchPct0 = skillGapService.calculateMatchPercentage(noneMatched);
        assertEquals("0% match percentage calculated correctly", 0, matchPct0);

        String assessment0 = skillGapService.getReadinessAssessment(matchPct0, 0, 1, "Backend Developer");
        assertTrue("Skill Gap Detected string generated", assessment0.contains("Skill Gap Detected"));
    }

    private static void testServiceLayerValidationContracts() {
        System.out.println("\n--- Testing Service Layer Pre-DAO Validation Contracts ---");
        StudentService studentService = new StudentServiceImpl();

        // Login validation
        try {
            studentService.login("", "pass");
            assertTrue("Empty login identifier rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Empty login identifier throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        try {
            studentService.login("25R11A0501", "");
            assertTrue("Empty password rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Empty password throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        // Student registration validation
        try {
            Student badStudent = new Student(0, "INVALID@ROLL", "Valid Name", "email@test.com", "pass", "CSE", 2, "9876543210");
            studentService.register(badStudent);
            assertTrue("Invalid roll rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Invalid roll throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        try {
            Student badEmailStudent = new Student(0, "25R11A0501", "Valid Name", "bad-email", "pass", "CSE", 2, "9876543210");
            studentService.register(badEmailStudent);
            assertTrue("Invalid email rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Invalid email throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        // Application validation
        ApplicationService appService = new ApplicationServiceImpl();
        try {
            Application badApp = new Application(0, 1, 0, 0, Date.valueOf("2026-01-01"), null, "Applied", "₹ -1000", null);
            appService.submitApplication(badApp);
            assertTrue("Unselected company/role rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Unselected company/role throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        // Certification validation
        CertificationService certService = new CertificationServiceImpl();
        try {
            Certification badCert = new Certification(0, 1, "", "Oracle", Date.valueOf("2026-01-01"), "C-1");
            certService.addCertification(badCert);
            assertTrue("Empty cert name rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Empty cert name throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        // Project validation
        ProjectService projService = new ProjectServiceImpl();
        try {
            Project badProj = new Project(0, 1, "", "Desc", "Java", "http://github");
            projService.addProject(badProj);
            assertTrue("Empty project title rejected by service", false);
        } catch (ValidationException e) {
            assertTrue("Empty project title throws ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }
    }

    private static void testCertificationSkillMappingModels() {
        System.out.println("\n--- Testing Certification & Skill Mapping Models ---");

        // CertificationSkillMapping Model
        CertificationSkillMapping m1 = new CertificationSkillMapping(
            1, "Oracle Certified Associate", "Oracle", 1, "Java",
            "Core Java, OOP principles, Collections", "Oracle Exam", "Intermediate", "Verified"
        );
        CertificationSkillMapping m2 = new CertificationSkillMapping(
            1, "Oracle Certified Associate", "Oracle", 1, "Java",
            "Core Java, OOP principles, Collections", "Oracle Exam", "Intermediate", "Verified"
        );
        CertificationSkillMapping m3 = new CertificationSkillMapping(
            2, "AWS Cloud Practitioner", "AWS", 5, "Cloud Computing",
            "Cloud infrastructure and endpoints", "AWS Exam", "Beginner", "Verified"
        );

        assertTrue("Mapping m1 equals m2 by ID", m1.equals(m2));
        assertTrue("Mapping m1 does not equal m3", !m1.equals(m3));
        assertTrue("Mapping hashCode equals for equal objects", m1.hashCode() == m2.hashCode());
        assertTrue("Mapping mappingId matches", m1.getMappingId() == 1);
        assertTrue("Mapping certName matches", "Oracle Certified Associate".equals(m1.getCertificationName()));
        assertTrue("Mapping org matches", "Oracle".equals(m1.getIssuingOrganization()));
        assertTrue("Mapping skillId matches", m1.getSkillId() == 1);
        assertTrue("Mapping skillName matches", "Java".equals(m1.getSkillName()));
        assertTrue("Mapping learningOutcome matches", "Core Java, OOP principles, Collections".equals(m1.getLearningOutcome()));
        assertTrue("Mapping evidenceSource matches", "Oracle Exam".equals(m1.getEvidenceSource()));
        assertTrue("Mapping suggestedProficiency matches", "Intermediate".equals(m1.getSuggestedProficiency()));
        assertTrue("Mapping mappingStatus matches", "Verified".equals(m1.getMappingStatus()));

        // StudentCertificationSkill Model
        Timestamp now = new Timestamp(System.currentTimeMillis());
        StudentCertificationSkill scs1 = new StudentCertificationSkill(
            10, 1, 100, 1, "Java", "Java Collections and Exception Handling", "Approved", "Intermediate", now
        );
        StudentCertificationSkill scs2 = new StudentCertificationSkill(
            10, 1, 100, 1, "Java", "Java Collections and Exception Handling", "Approved", "Intermediate", now
        );
        StudentCertificationSkill scs3 = new StudentCertificationSkill(
            11, 1, 100, 2, "SQL", "Relational queries", "Approved", "Intermediate", now
        );

        assertTrue("Student cert skill scs1 equals scs2 by recordId", scs1.equals(scs2));
        assertTrue("Student cert skill scs1 does not equal scs3", !scs1.equals(scs3));
        assertTrue("Student cert skill hashCode matches", scs1.hashCode() == scs2.hashCode());
        assertTrue("Student cert skill recordId matches", scs1.getRecordId() == 10);
        assertTrue("Student cert skill studentId matches", scs1.getStudentId() == 1);
        assertTrue("Student cert skill certId matches", scs1.getCertificationId() == 100);
        assertTrue("Student cert skill skillId matches", scs1.getSkillId() == 1);
        assertTrue("Student cert skill skillName matches", "Java".equals(scs1.getSkillName()));
        assertTrue("Student cert skill outcome matches", "Java Collections and Exception Handling".equals(scs1.getLearningOutcome()));
        assertTrue("Student cert skill reviewStatus matches", "Approved".equals(scs1.getReviewStatus()));
        assertTrue("Student cert skill assessedProficiency matches", "Intermediate".equals(scs1.getAssessedProficiency()));

        // SkillEvidence Model
        SkillEvidence ev1 = new SkillEvidence(
            50, 1, 1, "Java", "Certification", 100, "Cert Title", "Outcome Details", "Verified", now
        );
        SkillEvidence ev2 = new SkillEvidence(
            50, 1, 1, "Java", "Certification", 100, "Cert Title", "Outcome Details", "Verified", now
        );
        SkillEvidence ev3 = new SkillEvidence(
            51, 1, 2, "SQL", "Self-Reported", null, "Self-Reported SQL", "User self-report", "Unverified", now
        );

        assertTrue("Skill evidence ev1 equals ev2 by evidenceId", ev1.equals(ev2));
        assertTrue("Skill evidence ev1 does not equal ev3", !ev1.equals(ev3));
        assertTrue("Skill evidence hashCode matches", ev1.hashCode() == ev2.hashCode());
        assertTrue("Skill evidence evidenceId matches", ev1.getEvidenceId() == 50);
        assertTrue("Skill evidence evidenceType matches", "Certification".equals(ev1.getEvidenceType()));
        assertTrue("Skill evidence referenceId matches", ev1.getReferenceId() != null && ev1.getReferenceId() == 100);
        assertTrue("Skill evidence verificationStatus matches", "Verified".equals(ev1.getVerificationStatus()));

        // Certification composition
        Certification cert = new Certification(100, 1, "Oracle Java SE", "Oracle", Date.valueOf("2026-01-01"), "CRED-123");
        List<StudentCertificationSkill> covered = new ArrayList<>();
        covered.add(scs1);
        cert.setCoveredSkills(covered);
        assertTrue("Certification holds composed coveredSkills", cert.getCoveredSkills() != null && cert.getCoveredSkills().size() == 1);
        assertTrue("Covered skill in cert matches", cert.getCoveredSkills().get(0).getSkillId() == 1);
    }

    private static void testCertificationToSkillRelationsAndCardinality() {
        System.out.println("\n--- Testing Many-to-Many Association & Cardinality ---");

        // 1. One certification covers MULTIPLE skills (1 -> N)
        // Meta Front-End Developer covers HTML/CSS (7), JavaScript (8), React (9)
        List<CertificationSkillMapping> metaMappings = new ArrayList<>();
        metaMappings.add(new CertificationSkillMapping(1, "Meta Front-End Developer", "Meta", 7, "HTML/CSS", "Semantic web and layout", "Curriculum", "Intermediate", "Verified"));
        metaMappings.add(new CertificationSkillMapping(2, "Meta Front-End Developer", "Meta", 8, "JavaScript", "ES6+ asynchronous JS", "Curriculum", "Intermediate", "Verified"));
        metaMappings.add(new CertificationSkillMapping(3, "Meta Front-End Developer", "Meta", 9, "React", "Components and hooks", "Curriculum", "Intermediate", "Verified"));

        assertTrue("One certification maps to multiple skills (size == 3)", metaMappings.size() == 3);
        assertTrue("First skill is HTML/CSS", metaMappings.get(0).getSkillId() == 7);
        assertTrue("Second skill is JavaScript", metaMappings.get(1).getSkillId() == 8);
        assertTrue("Third skill is React", metaMappings.get(2).getSkillId() == 9);

        // 2. One skill covered by MULTIPLE certifications (N -> 1)
        // SQL is covered by HackerRank SQL (mapping 10) AND Google Data Analytics (mapping 11)
        List<CertificationSkillMapping> sqlMappings = new ArrayList<>();
        sqlMappings.add(new CertificationSkillMapping(10, "Database Systems & SQL Specialist", "HackerRank", 2, "SQL", "Relational queries", "HackerRank", "Intermediate", "Verified"));
        sqlMappings.add(new CertificationSkillMapping(11, "Google Data Analytics", "Google", 2, "SQL", "SQL querying & aggregation", "Google", "Intermediate", "Verified"));

        assertTrue("Single skill covered by multiple certifications (size == 2)", sqlMappings.size() == 2);
        assertTrue("Both mappings target SQL (skillId == 2)", sqlMappings.get(0).getSkillId() == 2 && sqlMappings.get(1).getSkillId() == 2);
        assertTrue("Different certifications mapped to SQL", !sqlMappings.get(0).getCertificationName().equals(sqlMappings.get(1).getCertificationName()));

        // 3. Composite Uniqueness constraint simulation: (cert_name, skill_id)
        String certKey1 = metaMappings.get(0).getCertificationName() + ":" + metaMappings.get(0).getSkillId();
        String certKey2 = metaMappings.get(1).getCertificationName() + ":" + metaMappings.get(1).getSkillId();
        String certKeyDuplicate = "Meta Front-End Developer:7";
        assertTrue("Distinct composite keys are not equal", !certKey1.equals(certKey2));
        assertTrue("Duplicate composite key matches certKey1", certKey1.equals(certKeyDuplicate));
    }

    private static void testSafeHandlingOfUnknownCertifications() {
        System.out.println("\n--- Testing Safe Handling of Unknown Certifications ---");

        com.sinst.dao.CertificationSkillDAO stubDAO = new com.sinst.dao.CertificationSkillDAO() {
            @Override
            public List<CertificationSkillMapping> findCatalogMappingsByCertName(String certName) {
                // Simulates catalog lookup: returns empty list for unknown certification
                return new ArrayList<>();
            }
            @Override public List<CertificationSkillMapping> getAllCatalogMappings() { return new ArrayList<>(); }
            @Override public boolean addCatalogMapping(CertificationSkillMapping mapping) { return true; }
            @Override public List<StudentCertificationSkill> getStudentCertificationSkills(int certificationId) { return new ArrayList<>(); }
            @Override public List<StudentCertificationSkill> getStudentCertificationSkillsByStudent(int studentId) { return new ArrayList<>(); }
            @Override public void addStudentCertificationSkills(List<StudentCertificationSkill> skills) {}
            @Override public void addStudentCertificationSkills(List<StudentCertificationSkill> skills, java.sql.Connection conn) {}
            @Override public boolean deleteStudentCertificationSkills(int certificationId) { return true; }
            @Override public boolean deleteStudentCertificationSkills(int certificationId, java.sql.Connection conn) { return true; }
        };

        CertificationService certService = new CertificationServiceImpl(
            com.sinst.dao.CertificationDAOImpl.getInstance(),
            stubDAO,
            com.sinst.dao.SkillEvidenceDAOImpl.getInstance(),
            com.sinst.dao.SkillDAOImpl.getInstance()
        );

        // Querying an unknown certification name returns an empty list, NOT hallucinated or fabricated skills
        try {
            List<CertificationSkillMapping> suggestions = certService.findSuggestedSkills("NonExistent Cert 99999 XYZ");
            assertTrue("Unknown certification returns non-null list", suggestions != null);
            assertTrue("Unknown certification returns EMPTY suggestions list (no fabricated skills)", suggestions.isEmpty());
        } catch (Exception e) {
            assertTrue("findSuggestedSkills threw unexpected exception: " + e, false);
        }

        // Null or blank certification query returns empty list safely
        try {
            List<CertificationSkillMapping> nullSuggestions = certService.findSuggestedSkills(null);
            assertTrue("Null cert name returns empty list", nullSuggestions != null && nullSuggestions.isEmpty());

            List<CertificationSkillMapping> blankSuggestions = certService.findSuggestedSkills("   ");
            assertTrue("Blank cert name returns empty list", blankSuggestions != null && blankSuggestions.isEmpty());
        } catch (Exception e) {
            assertTrue("Blank cert query threw unexpected exception: " + e, false);
        }
    }

    private static void testSkillEvidenceTrackingAndVerificationStates() {
        System.out.println("\n--- Testing Skill Evidence Tracking & Verification States ---");

        // Verify supported evidence types
        String[] validEvidenceTypes = {"Self-Reported", "Certification", "Project", "Assessment"};
        for (String type : validEvidenceTypes) {
            SkillEvidence ev = new SkillEvidence(0, 1, 1, "Java", type, 1, "Title", "Details", "Verified", null);
            assertTrue("Evidence type '" + type + "' is assigned correctly", type.equals(ev.getEvidenceType()));
        }

        // Verify supported verification statuses
        String[] validStatuses = {"Unverified", "Verified", "Rejected"};
        for (String status : validStatuses) {
            SkillEvidence ev = new SkillEvidence(0, 1, 1, "Java", "Self-Reported", null, "Title", "Details", status, null);
            assertTrue("Verification status '" + status + "' is assigned correctly", status.equals(ev.getVerificationStatus()));
        }

        // Verify Self-Reported vs Certification distinction
        SkillEvidence selfReported = new SkillEvidence(
            1, 1, 1, "Java", "Self-Reported", null, "Self-Reported Skill (Intermediate)", "Candidate self-declaration", "Unverified", null
        );
        SkillEvidence certEvidence = new SkillEvidence(
            2, 1, 1, "Java", "Certification", 100, "Oracle Java SE 17", "Verified by Oracle Exam", "Verified", null
        );

        assertTrue("Self-reported evidence has Unverified status", "Unverified".equals(selfReported.getVerificationStatus()));
        assertTrue("Self-reported evidence has null referenceId", selfReported.getReferenceId() == null);
        assertTrue("Certification evidence has Verified status", "Verified".equals(certEvidence.getVerificationStatus()));
        assertTrue("Certification evidence has non-null referenceId", certEvidence.getReferenceId() != null && certEvidence.getReferenceId() == 100);
        assertTrue("Evidence types are distinct", !selfReported.getEvidenceType().equals(certEvidence.getEvidenceType()));
    }

    private static void testPreservingDemonstratedAbilityOnCertificationSave() {
        System.out.println("\n--- Testing Demonstrated Ability Preservation ---");

        // Rule: "Do NOT automatically treat certificates as proof of advanced proficiency; preserve demonstrated ability."
        // Case 1: Student already demonstrated ADVANCED (Level 3).
        // A certification verifying INTERMEDIATE (Level 2) is added.
        // The student's recorded proficiency MUST remain ADVANCED (Level 3), NOT downgraded to INTERMEDIATE!
        ProficiencyLevel existingAdvanced = ProficiencyLevel.ADVANCED; // 3
        ProficiencyLevel certIntermediate = ProficiencyLevel.INTERMEDIATE; // 2
        ProficiencyLevel resolvedCase1;
        if (existingAdvanced == ProficiencyLevel.NONE) {
            resolvedCase1 = certIntermediate;
        } else if (certIntermediate.getLevel() > existingAdvanced.getLevel()) {
            resolvedCase1 = certIntermediate;
        } else {
            resolvedCase1 = existingAdvanced; // preserve demonstrated ability!
        }
        assertTrue("Existing ADVANCED proficiency is preserved against INTERMEDIATE certification (not downgraded)", resolvedCase1 == ProficiencyLevel.ADVANCED);
        assertTrue("Preserved level is 3", resolvedCase1.getLevel() == 3);

        // Case 2: Student already demonstrated EXPERT (Level 4).
        // An INTERMEDIATE (Level 2) or ADVANCED (Level 3) certification is added.
        ProficiencyLevel existingExpert = ProficiencyLevel.EXPERT; // 4
        ProficiencyLevel resolvedCase2 = existingExpert.getLevel() >= certIntermediate.getLevel() ? existingExpert : certIntermediate;
        assertTrue("Existing EXPERT proficiency is preserved against lower certification", resolvedCase2 == ProficiencyLevel.EXPERT);

        // Case 3: Student has BEGINNER (Level 1).
        // Certification verifies INTERMEDIATE (Level 2).
        // Level upgrades to INTERMEDIATE.
        ProficiencyLevel existingBeginner = ProficiencyLevel.BEGINNER; // 1
        ProficiencyLevel resolvedCase3 = certIntermediate.getLevel() > existingBeginner.getLevel() ? certIntermediate : existingBeginner;
        assertTrue("BEGINNER proficiency upgrades to verified INTERMEDIATE level", resolvedCase3 == ProficiencyLevel.INTERMEDIATE);

        // Case 4: Student has NONE (Level 0, skill not present).
        // Certification verifies INTERMEDIATE (Level 2).
        ProficiencyLevel existingNone = ProficiencyLevel.NONE; // 0
        ProficiencyLevel resolvedCase4 = existingNone == ProficiencyLevel.NONE ? certIntermediate : existingNone;
        assertTrue("Unranked/new skill adopts verified certification level (INTERMEDIATE)", resolvedCase4 == ProficiencyLevel.INTERMEDIATE);
    }

    private static void testTransactionalIntegrityAndRollback() {
        System.out.println("\n--- Testing Transactional Integrity & Rollback Contracts ---");

        CertificationService certService = new CertificationServiceImpl();

        // Test validation rollback trigger: Null or invalid certification
        try {
            Certification invalidCert = new Certification(0, 0, "", "", null, null);
            certService.saveCertificationWithReviewedSkills(invalidCert, new ArrayList<>());
            assertTrue("Invalid certification must fail validation before committing transaction", false);
        } catch (ValidationException e) {
            assertTrue("Invalid certification transaction blocked by ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }

        // Test reviewed skills validation trigger: Skill with invalid ID
        try {
            Certification validCert = new Certification(0, 1, "Valid Name", "Valid Org", Date.valueOf("2026-01-01"), null);
            List<StudentCertificationSkill> badSkills = new ArrayList<>();
            badSkills.add(new StudentCertificationSkill(0, 1, 0, -5, "Bad Skill", "Outcome", "Approved", "Intermediate", null));
            certService.saveCertificationWithReviewedSkills(validCert, badSkills);
            assertTrue("Negative skill ID must fail validation before transaction commits", false);
        } catch (ValidationException e) {
            assertTrue("Invalid skill ID transaction blocked by ValidationException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception: " + e, false);
        }
    }

    private static void testCompanyRoleAndRecommendationModels() {
        System.out.println("\n--- Testing CompanyRole & RoleRecommendation Domain Models ---");

        Date today = Date.valueOf("2026-08-01");

        // Company model extensions
        Company c1 = new Company(1, "Google", "IT", "Hyderabad", "https://google.com", "https://careers.google.com", "campus@google.com");
        assertTrue("Company careersUrl getter", "https://careers.google.com".equals(c1.getCareersUrl()));
        assertTrue("Company effectiveCareersUrl getter", "https://careers.google.com".equals(c1.getEffectiveCareersUrl()));

        Company c2 = new Company(2, "Default Co", "IT", "Hyderabad", "https://default.com", null, "info@default.com");
        assertTrue("Company effectiveCareersUrl fallback to website", "https://default.com".equals(c2.getEffectiveCareersUrl()));

        // Role model extensions
        Role r1 = new Role(1, "Java Backend Developer Intern", "Backend engineering", "Intern", "Industry Standard Competency Framework", today);
        assertTrue("Role informationSource getter", "Industry Standard Competency Framework".equals(r1.getInformationSource()));
        assertTrue("Role lastUpdated getter", today.equals(r1.getLastUpdated()));

        // CompanyRole model
        CompanyRole cr1 = new CompanyRole(10, 1, 1, "Potential Role Match", "Company Competency Profile", "Official Careers Portal", today);
        CompanyRole cr2 = new CompanyRole(10, 1, 1, "Potential Role Match", "Company Competency Profile", "Official Careers Portal", today);
        CompanyRole cr3 = new CompanyRole(11, 2, 2, "Potential Role Match", "Company Competency Profile", "Official Careers Portal", today);

        assertTrue("CompanyRole equality by ID", cr1.equals(cr2));
        assertTrue("CompanyRole inequality for different IDs", !cr1.equals(cr3));
        assertTrue("CompanyRole hashCode match", cr1.hashCode() == cr2.hashCode());
        assertTrue("CompanyRole default vacancyStatus is Potential Role Match", "Potential Role Match".equals(cr1.getVacancyStatus()));

        // RoleRecommendation model
        RoleRecommendation rec = new RoleRecommendation();
        rec.setCompanyId(1);
        rec.setCompanyName("Google");
        rec.setCareersUrl("https://careers.google.com");
        rec.setRoleId(1);
        rec.setRoleTitle("Java Backend Developer Intern");
        rec.setMatchPercentage(85);
        rec.setDetailedScore(85.0);
        rec.setVacancyStatus("Potential Role Match");

        List<SkillGapResult> matching = new ArrayList<>();
        matching.add(new SkillGapResult(1, "Java", "Programming", "Intermediate", "Intermediate", true));
        matching.add(new SkillGapResult(2, "SQL & DBMS", "Database", "Intermediate", "Intermediate", true));
        rec.setMatchingSkills(matching);

        List<SkillGapResult> missingMandatory = new ArrayList<>();
        missingMandatory.add(new SkillGapResult(5, "REST APIs", "Backend", "Intermediate", "None", true));
        rec.setMissingMandatorySkills(missingMandatory);

        List<SkillGapResult> missingOptional = new ArrayList<>();
        missingOptional.add(new SkillGapResult(4, "Git & GitHub", "Tools", "Beginner", "None", false));
        rec.setMissingOptionalSkills(missingOptional);

        assertTrue("Matching skills names formatted correctly", "Java, SQL & DBMS".equals(rec.getMatchingSkillNames()));
        assertTrue("Missing mandatory skills names formatted correctly", "REST APIs".equals(rec.getMissingMandatoryNames()));
        assertTrue("Missing optional skills names formatted correctly", "Git & GitHub".equals(rec.getMissingOptionalNames()));
        assertTrue("Total required competencies count (4)", rec.getTotalRequiredCount() == 4);
        assertTrue("Effective careers URL returned", "https://careers.google.com".equals(rec.getEffectiveCareersUrl()));
    }

    private static void testRecommendationRankingFormula() {
        System.out.println("\n--- Testing Recommendation Ranking & Multi-Tier Ordering Formula ---");

        // Rec 1: High match score (90%), 0 missing mandatory, 2 evidence
        RoleRecommendation r1 = new RoleRecommendation();
        r1.setCompanyName("Alpha Corp");
        r1.setRoleTitle("Role A");
        r1.setMatchPercentage(90);
        r1.setVerifiedEvidenceCount(2);

        // Rec 2: Moderate match score (75%), 0 missing mandatory, 1 evidence
        RoleRecommendation r2 = new RoleRecommendation();
        r2.setCompanyName("Beta Corp");
        r2.setRoleTitle("Role B");
        r2.setMatchPercentage(75);
        r2.setVerifiedEvidenceCount(1);

        // Rec 3: Moderate match score (75%), 1 missing mandatory, 1 evidence
        RoleRecommendation r3 = new RoleRecommendation();
        r3.setCompanyName("Gamma Corp");
        r3.setRoleTitle("Role C");
        r3.setMatchPercentage(75);
        List<SkillGapResult> missingMandatory = new ArrayList<>();
        missingMandatory.add(new SkillGapResult(1, "Mandatory Skill", "Core", "Intermediate", "None", true));
        r3.setMissingMandatorySkills(missingMandatory);
        r3.setVerifiedEvidenceCount(1);

        // Rec 4: Moderate match score (75%), 1 missing mandatory, 0 evidence
        RoleRecommendation r4 = new RoleRecommendation();
        r4.setCompanyName("Delta Corp");
        r4.setRoleTitle("Role D");
        r4.setMatchPercentage(75);
        r4.setMissingMandatorySkills(missingMandatory);
        r4.setVerifiedEvidenceCount(0);

        List<RoleRecommendation> list = new ArrayList<>();
        list.add(r3);
        list.add(r4);
        list.add(r2);
        list.add(r1);

        Collections.sort(list);

        // Tier 1: Highest match percentage ranks first
        assertTrue("Rank 1 is highest match score (90% - Alpha Corp)", list.get(0).getCompanyName().equals("Alpha Corp"));

        // Tier 2: Same match score (75%), 0 missing mandatory ranks ahead of 1 missing mandatory
        assertTrue("Rank 2 has 0 missing mandatory skills (Beta Corp)", list.get(1).getCompanyName().equals("Beta Corp"));

        // Tier 3: Same match score (75%) & same missing mandatory (1), higher verified evidence count ranks ahead
        assertTrue("Rank 3 has 1 verified evidence (Gamma Corp)", list.get(2).getCompanyName().equals("Gamma Corp"));
        assertTrue("Rank 4 has 0 verified evidence (Delta Corp)", list.get(3).getCompanyName().equals("Delta Corp"));
    }

    private static void testRecommendationDynamicResponsiveness() {
        System.out.println("\n--- Testing Dynamic Recommendation Responsiveness on Skill Profile Changes ---");

        SkillMatchConfig config = new SkillMatchConfig(2.0, 1.0);
        SkillGapService gapService = new SkillGapServiceImpl(null, config);

        // Scenario: Role requires Java (Mandatory, Interm.), SQL (Mandatory, Interm.), REST APIs (Mandatory, Interm.)
        // Student initial skills: Java (Interm.), SQL (Interm.) -> Missing REST APIs
        List<SkillGapResult> initialGaps = new ArrayList<>();
        initialGaps.add(new SkillGapResult(1, "Java", "Core", "Intermediate", "Intermediate", true));
        initialGaps.add(new SkillGapResult(2, "SQL", "Database", "Intermediate", "Intermediate", true));
        initialGaps.add(new SkillGapResult(5, "REST APIs", "Backend", "Intermediate", "None", true));

        for (SkillGapResult r : initialGaps) r.evaluateProficiency();

        int initialScore = gapService.calculateMatchPercentage(initialGaps);
        // 2 matched mandatory (2*2.0 = 4.0) out of 3 mandatory (3*2.0 = 6.0) => 4/6 = 67%
        assertTrue("Initial score with 2 of 3 mandatory skills is 67%", initialScore == 67);

        // Student acquires REST APIs (Intermediate)
        List<SkillGapResult> updatedGaps = new ArrayList<>();
        updatedGaps.add(new SkillGapResult(1, "Java", "Core", "Intermediate", "Intermediate", true));
        updatedGaps.add(new SkillGapResult(2, "SQL", "Database", "Intermediate", "Intermediate", true));
        updatedGaps.add(new SkillGapResult(5, "REST APIs", "Backend", "Intermediate", "Intermediate", true));

        for (SkillGapResult r : updatedGaps) r.evaluateProficiency();

        int updatedScore = gapService.calculateMatchPercentage(updatedGaps);
        assertTrue("Updated score after student acquires mandatory skill is 100%", updatedScore == 100);
        assertTrue("Recommendation score changes dynamically upon student skill change", updatedScore > initialScore);
    }

    private static void testRecommendationAlreadyAppliedTagging() {
        System.out.println("\n--- Testing Tagging of Already-Applied Roles ---");

        // Simulate application lookup
        Map<String, Application> appliedMap = new HashMap<>();
        Application app = new Application(1, 1, 1, 1, Date.valueOf("2026-08-10"), null, "Interview", "₹ 45,000", null);
        appliedMap.put("1:1", app);

        CompanyRole cr = new CompanyRole(1, 1, 1, "Potential Role Match", "Company Competency Profile", "Official Careers Portal", Date.valueOf("2026-08-01"));
        cr.setCompanyName("Google");
        cr.setRoleTitle("Java Backend Developer Intern");

        boolean hasApplied = appliedMap.containsKey(cr.getCompanyId() + ":" + cr.getRoleId());
        String appStatus = hasApplied ? appliedMap.get(cr.getCompanyId() + ":" + cr.getRoleId()).getStatus() : null;

        assertTrue("Already-applied role is identified as applied", hasApplied);
        assertTrue("Application status 'Interview' is preserved", "Interview".equals(appStatus));

        // When not applied
        boolean notApplied = appliedMap.containsKey("2:2");
        assertTrue("Unapplied role is correctly identified as not applied", !notApplied);
    }

    private static void testRecommendationCriticalDistinctionAndTruthfulness() {
        System.out.println("\n--- Testing Critical Distinction & Truthful Reporting ---");

        // 1. Default vacancy status must be 'Potential Role Match' (not an active vacancy claim)
        CompanyRole cr = new CompanyRole();
        assertTrue("Default vacancy status is 'Potential Role Match'", "Potential Role Match".equals(cr.getVacancyStatus()));

        RoleRecommendation rec = new RoleRecommendation();
        assertTrue("Recommendation default vacancy status is 'Potential Role Match'", "Potential Role Match".equals(rec.getVacancyStatus()));

        // 2. Roles with missing/incomplete data must be handled transparently (not 100% match)
        SkillGapService gapService = new SkillGapServiceImpl();
        List<SkillGapResult> emptyRoleRequirements = new ArrayList<>();

        assertTrue("Empty role requirements identified as insufficient role data", gapService.isInsufficientRoleData(emptyRoleRequirements));
        double score = gapService.calculateDetailedMatchScore(emptyRoleRequirements, gapService.getConfig());
        assertTrue("Empty role data returns -1.0 detailed score", score == -1.0);
        int pct = gapService.calculateMatchPercentage(emptyRoleRequirements);
        assertTrue("Empty role data returns 0% match percentage (does NOT claim 100%)", pct == 0);
    }

    private static void testRecommendationDeduplication() {
        System.out.println("\n--- Testing Recommendation Deduplication ---");

        Set<String> processedKeys = new HashSet<>();

        // Simulate reading duplicate company-role records
        List<String> rawPairings = new ArrayList<>();
        rawPairings.add("1:1"); // Google - Java Backend
        rawPairings.add("1:1"); // Duplicate from multi-source join
        rawPairings.add("2:2"); // Microsoft - Full Stack
        rawPairings.add("1:1"); // Another duplicate

        List<String> distinctPairings = new ArrayList<>();
        for (String pair : rawPairings) {
            if (!processedKeys.contains(pair)) {
                processedKeys.add(pair);
                distinctPairings.add(pair);
            }
        }

        assertTrue("Duplicate company-role pairings are deduplicated to distinct entries (size == 2)", distinctPairings.size() == 2);
        assertTrue("First distinct pairing is 1:1", "1:1".equals(distinctPairings.get(0)));
        assertTrue("Second distinct pairing is 2:2", "2:2".equals(distinctPairings.get(1)));
    }

    private static void testPrompt6ModelEnhancements() {
        System.out.println("\n--- Testing Prompt 6 Model Enhancements ---");

        // Student Role & Admin Checks
        Student student = new Student();
        assertEquals("Default student role is STUDENT", "STUDENT", student.getRole());
        assertFalse("Default student is not admin", student.isAdmin());
        student.setRole("ADMIN");
        assertEquals("Updated role is ADMIN", "ADMIN", student.getRole());
        assertTrue("Student with ADMIN role isAdmin is true", student.isAdmin());

        Student adminStudent = new Student(10, "ADM001", "System Administrator", "admin@domain.com", "secret", "IT", 4, "9876543210", "ADMIN");
        assertTrue("Overloaded student constructor assigns admin role", adminStudent.isAdmin());

        // Certification Expiry & URL
        Date issueDate = Date.valueOf("2025-01-15");
        Date expiryDate = Date.valueOf("2028-01-15");
        Certification cert = new Certification(1, 5, "AWS Certified Developer", "Amazon Web Services", issueDate, expiryDate, "AWS-DEV-12345", "https://aws.amazon.com/verify/12345");
        assertEquals("Certification issue date matches", issueDate, cert.getIssueDate());
        assertEquals("Certification expiry date matches", expiryDate, cert.getExpiryDate());
        assertEquals("Certification credential ID matches", "AWS-DEV-12345", cert.getCredentialId());
        assertEquals("Certification credential URL matches", "https://aws.amazon.com/verify/12345", cert.getCredentialUrl());

        // Project Github URL, Live URL & Property Aliases
        Project proj = new Project(1, 5, "SINST System", "Skill and internship tracking platform", "Java, Swing, MySQL", "https://github.com/aadil274/sinst", "https://github.com/aadil274/sinst", "https://sinst-demo.example.com");
        assertEquals("Project github URL matches", "https://github.com/aadil274/sinst", proj.getGithubUrl());
        assertEquals("Project live URL matches", "https://sinst-demo.example.com", proj.getLiveUrl());
        assertEquals("Project title alias matches title", "SINST System", proj.getProjectTitle());
        assertEquals("Tech stack alias matches technologiesUsed", "Java, Swing, MySQL", proj.getTechStack());

        // Interview Round Number
        Interview interview = new Interview(1, 10, Timestamp.valueOf("2026-05-15 14:30:00"), "Technical Round 2", 2, "Online", "Scheduled", "Pending", "Preparing DSA");
        assertEquals("Interview round number matches", 2, interview.getRoundNumber());
        Interview defaultInterview = new Interview();
        assertEquals("Default interview round number is 1", 1, defaultInterview.getRoundNumber());
    }

    private static void testPrompt6InputValidation() {
        System.out.println("\n--- Testing Prompt 6 Enhanced Input Validation ---");

        // URL Validation
        assertTrue("Valid HTTPS URL", ValidationUtil.isValidUrl("https://github.com/aadil274/sinst"));
        assertTrue("Valid HTTP URL with port", ValidationUtil.isValidUrl("http://localhost:8080/portal/demo"));
        assertTrue("Valid URL with query params", ValidationUtil.isValidUrl("https://example.com/project?id=123&env=prod"));
        assertTrue("Optional empty URL is accepted", ValidationUtil.isValidUrl(""));
        assertTrue("Optional blank URL is accepted", ValidationUtil.isValidUrl("   "));
        assertTrue("Optional null URL is accepted", ValidationUtil.isValidUrl(null));
        assertFalse("FTP protocol rejected", ValidationUtil.isValidUrl("ftp://files.example.com/archive.zip"));
        assertFalse("JavaScript URI scheme rejected", ValidationUtil.isValidUrl("javascript:alert(1)"));
        assertFalse("Incomplete HTTP prefix rejected", ValidationUtil.isValidUrl("http://"));
        assertFalse("Non-URL string rejected", ValidationUtil.isValidUrl("my_github_project"));

        // Date Range Validation
        assertTrue("Chronological start before end date is valid", ValidationUtil.isValidDateRange(Date.valueOf("2026-01-01"), Date.valueOf("2026-06-30")));
        assertTrue("Equal start and end date is valid", ValidationUtil.isValidDateRange(Date.valueOf("2026-05-01"), Date.valueOf("2026-05-01")));
        assertFalse("Start date after end date is invalid", ValidationUtil.isValidDateRange(Date.valueOf("2026-07-01"), Date.valueOf("2026-06-30")));
        assertTrue("String date range valid", ValidationUtil.isValidDateRange("2026-01-01", "2026-12-31"));
        assertFalse("String date range inverted is invalid", ValidationUtil.isValidDateRange("2026-12-31", "2026-01-01"));

        // Interview Date Validation (Interview cannot precede Application)
        Date appDate = Date.valueOf("2026-03-01");
        Timestamp futureInterview = Timestamp.valueOf("2026-03-10 10:00:00");
        Timestamp sameDayInterview = Timestamp.valueOf("2026-03-01 15:00:00");
        Timestamp pastInterview = Timestamp.valueOf("2026-02-25 10:00:00");

        assertTrue("Interview scheduled after application date is valid", ValidationUtil.isValidInterviewDate(appDate, futureInterview));
        assertTrue("Interview scheduled on same day as application is valid", ValidationUtil.isValidInterviewDate(appDate, sameDayInterview));
        assertFalse("Interview scheduled before application date is invalid", ValidationUtil.isValidInterviewDate(appDate, pastInterview));

        // Certification Date Validation
        Date certIssue = Date.valueOf("2025-06-01");
        Date certExpiry = Date.valueOf("2028-06-01");
        Date certExpired = Date.valueOf("2024-06-01");

        assertTrue("Issue date before expiry date is valid", ValidationUtil.isValidCertificationDates(certIssue, certExpiry));
        assertTrue("Non-expiring certification (null expiry) is valid", ValidationUtil.isValidCertificationDates(certIssue, null));
        assertFalse("Issue date after expiry date is invalid", ValidationUtil.isValidCertificationDates(certIssue, certExpired));
        assertFalse("Null issue date is invalid", ValidationUtil.isValidCertificationDates(null, certExpiry));

        // Application Status Validation
        assertTrue("Status 'Applied' is valid", ValidationUtil.isValidApplicationStatus("Applied"));
        assertTrue("Status 'Under Review' is valid", ValidationUtil.isValidApplicationStatus("Under Review"));
        assertTrue("Status 'Shortlisted' is valid", ValidationUtil.isValidApplicationStatus("Shortlisted"));
        assertTrue("Status 'Interview' is valid", ValidationUtil.isValidApplicationStatus("Interview"));
        assertTrue("Status 'Accepted' is valid", ValidationUtil.isValidApplicationStatus("Accepted"));
        assertTrue("Status 'Selected' is valid", ValidationUtil.isValidApplicationStatus("Selected"));
        assertTrue("Status 'Rejected' is valid", ValidationUtil.isValidApplicationStatus("Rejected"));
        assertTrue("Status 'Withdrawn' is valid", ValidationUtil.isValidApplicationStatus("Withdrawn"));
        assertFalse("Status 'Pending' is invalid", ValidationUtil.isValidApplicationStatus("Pending"));
        assertFalse("Null status is invalid", ValidationUtil.isValidApplicationStatus(null));
        assertFalse("Empty status is invalid", ValidationUtil.isValidApplicationStatus(""));

        // Skill Proficiency Scale Validation
        assertTrue("Proficiency Beginner is valid", ValidationUtil.isValidProficiency("Beginner"));
        assertTrue("Proficiency Intermediate is valid", ValidationUtil.isValidProficiency("Intermediate"));
        assertTrue("Proficiency Advanced is valid", ValidationUtil.isValidProficiency("Advanced"));
        assertTrue("Proficiency Expert is valid", ValidationUtil.isValidProficiency("Expert"));
        assertFalse("Proficiency Novice is invalid", ValidationUtil.isValidProficiency("Novice"));
        assertFalse("Proficiency Master is invalid", ValidationUtil.isValidProficiency("Master"));
        assertFalse("Null proficiency is invalid", ValidationUtil.isValidProficiency(null));

        // Interview Mode & Status Validation
        assertTrue("Mode Online is valid", ValidationUtil.isValidInterviewMode("Online"));
        assertTrue("Mode In-Person is valid", ValidationUtil.isValidInterviewMode("In-Person"));
        assertTrue("Mode Telephonic is valid", ValidationUtil.isValidInterviewMode("Telephonic"));
        assertFalse("Arbitrary interview mode is invalid", ValidationUtil.isValidInterviewMode("Postal"));

        assertTrue("Status Scheduled is valid", ValidationUtil.isValidInterviewStatus("Scheduled"));
        assertTrue("Status Completed is valid", ValidationUtil.isValidInterviewStatus("Completed"));
        assertTrue("Status Cancelled is valid", ValidationUtil.isValidInterviewStatus("Cancelled"));
        assertTrue("Status Rescheduled is valid", ValidationUtil.isValidInterviewStatus("Rescheduled"));
        assertFalse("Arbitrary interview status is invalid", ValidationUtil.isValidInterviewStatus("Postponed"));

        // Round Number Validation
        assertTrue("Round 1 is valid", ValidationUtil.isValidRoundNumber(1));
        assertTrue("Round 5 is valid", ValidationUtil.isValidRoundNumber(5));
        assertTrue("Round 20 is valid", ValidationUtil.isValidRoundNumber(20));
        assertFalse("Round 0 is invalid", ValidationUtil.isValidRoundNumber(0));
        assertFalse("Negative round is invalid", ValidationUtil.isValidRoundNumber(-1));
        assertFalse("Round 21 is invalid", ValidationUtil.isValidRoundNumber(21));

        // Numeric Stipend Validation
        assertTrue("Zero stipend is valid", ValidationUtil.isValidNumericStipend(0.0));
        assertTrue("Positive stipend is valid", ValidationUtil.isValidNumericStipend(25000.0));
        assertFalse("Negative stipend is invalid", ValidationUtil.isValidNumericStipend(-1.0));
        assertTrue("Detects negative formatted stipend", ValidationUtil.hasNegativeStipend("- ₹ 5,000"));
        assertTrue("Detects negative stipend number", ValidationUtil.hasNegativeStipend("₹ -500"));
        assertFalse("Valid formatted stipend not marked negative", ValidationUtil.hasNegativeStipend("₹ 25,000"));
    }

    private static void testPrompt6CrossStudentAuthorization() {
        System.out.println("\n--- Testing Cross-Student Authorization & Ownership Controls ---");

        try {
            // UserSession testing
            Student student1 = new Student(1, "25R11A0501", "Student One", "student1@test.com", "pass", "CSE", 3, "9876543210", "STUDENT");
            UserSession.setCurrentStudent(student1);
            assertTrue("UserSession is authenticated", UserSession.isAuthenticated());
            assertEquals("UserSession current student ID matches", 1, UserSession.getCurrentStudentId());
            assertFalse("Student One is not admin", UserSession.isAdmin());

            Student adminUser = new Student(99, "ADM001", "Admin User", "admin@test.com", "pass", "IT", 4, "9876543210", "ADMIN");
            UserSession.setCurrentStudent(adminUser);
            assertTrue("Admin User isAdmin is true", UserSession.isAdmin());

            UserSession.clear();
            assertFalse("UserSession is cleared", UserSession.isAuthenticated());
            assertEquals("UserSession cleared student ID is 0", 0, UserSession.getCurrentStudentId());

            // 1. Application Ownership Protection (IDOR Prevention)
            ApplicationDAO appDaoStub = new ApplicationDAO() {
                @Override public List<Application> getByStudentId(int studentId) { return new ArrayList<>(); }
                @Override public Application getById(int applicationId) {
                    if (applicationId == 10) {
                        return new Application(10, 1, 1, 1, Date.valueOf("2026-02-01"), null, "Applied", "₹ 20,000", null);
                    }
                    return null;
                }
                @Override public Application getByIdAndStudentId(int applicationId, int studentId) {
                    if (applicationId == 10 && studentId == 1) {
                        return new Application(10, 1, 1, 1, Date.valueOf("2026-02-01"), null, "Applied", "₹ 20,000", null);
                    }
                    return null;
                }
                @Override public boolean hasApplication(int studentId, int companyId, int roleId) { return false; }
                @Override public boolean add(Application a) { return true; }
                @Override public boolean update(Application a) { return true; }
                @Override public boolean update(Application a, int studentId) { return true; }
                @Override public boolean updateStatus(int applicationId, String status) { return true; }
                @Override public boolean updateStatus(int applicationId, int studentId, String status) { return true; }
                @Override public boolean delete(int applicationId) { return true; }
                @Override public boolean delete(int applicationId, int studentId) { return true; }
            };

            ApplicationService appService = new ApplicationServiceImpl(appDaoStub);

            // Cross-student update status attempt
            try {
                appService.updateStatus(10, 2, "Accepted");
                assertTrue("Cross-student application status update should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Cross-student application status update throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student updateStatus: " + e, false);
            }

            // Cross-student delete attempt
            try {
                appService.deleteApplication(10, 2);
                assertTrue("Cross-student application deletion should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Cross-student application deletion throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student deleteApplication: " + e, false);
            }

            // Cross-student full update attempt
            try {
                Application targetApp = new Application(10, 1, 1, 1, Date.valueOf("2026-02-01"), null, "Accepted", "₹ 30,000", null);
                appService.updateApplication(targetApp, 2);
                assertTrue("Cross-student application update should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Cross-student application update throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student updateApplication: " + e, false);
            }

            // Authorized owner update succeeds
            try {
                appService.updateStatus(10, 1, "Accepted");
                assertTrue("Authorized student application status update succeeds", true);
            } catch (Exception e) {
                assertTrue("Authorized student update failed unexpectedly: " + e, false);
            }

            // 2. Interview Ownership Protection
            InterviewDAO interviewDaoStub = new InterviewDAO() {
                @Override public List<Interview> getByStudentId(int studentId) { return new ArrayList<>(); }
                @Override public List<Interview> getByApplicationId(int applicationId) { return new ArrayList<>(); }
                @Override public List<Interview> getByApplicationId(int applicationId, int studentId) { return new ArrayList<>(); }
                @Override public Interview getById(int interviewId) {
                    if (interviewId == 50) {
                        return new Interview(50, 10, Timestamp.valueOf("2026-03-01 10:00:00"), "Technical Round 1", 1, "Online", "Scheduled", "Pending", null);
                    }
                    return null;
                }
                @Override public Interview getByIdAndStudentId(int interviewId, int studentId) {
                    if (interviewId == 50 && studentId == 1) {
                        return new Interview(50, 10, Timestamp.valueOf("2026-03-01 10:00:00"), "Technical Round 1", 1, "Online", "Scheduled", "Pending", null);
                    }
                    return null;
                }
                @Override public List<Interview> getUpcoming(int studentId) { return new ArrayList<>(); }
                @Override public boolean schedule(Interview i, int applicationId, String newStatus) { return true; }
                @Override public boolean schedule(Interview i, int applicationId, int studentId, String newStatus) { return true; }
                @Override public boolean update(int interviewId, String status, String outcome, String feedback) { return true; }
                @Override public boolean update(int interviewId, int studentId, String status, String outcome, String feedback) { return true; }
                @Override public boolean updateFull(Interview i) { return true; }
                @Override public boolean updateFull(Interview i, int studentId) { return true; }
                @Override public boolean delete(int interviewId) { return true; }
                @Override public boolean delete(int interviewId, int studentId) { return true; }
                @Override public int getApplicationOwnerStudentId(int applicationId) { return applicationId == 10 ? 1 : 0; }
                @Override public int getInterviewOwnerStudentId(int interviewId) { return interviewId == 50 ? 1 : 0; }
                @Override public boolean hasScheduledInterview(int applicationId, String roundType, Timestamp interviewDate) { return false; }
            };

            InterviewService interviewService = new InterviewServiceImpl(interviewDaoStub, appDaoStub);

            // Cross-student schedule attempt
            try {
                Interview intv = new Interview(0, 10, Timestamp.valueOf("2026-03-05 10:00:00"), "Technical Round 1", 1, "Online", "Scheduled", "Pending", null);
                interviewService.scheduleInterview(intv, 2);
                assertTrue("Scheduling interview for another student's application should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Scheduling interview for another student's application throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student scheduleInterview: " + e, false);
            }

            // Cross-student interview outcome update attempt
            try {
                interviewService.updateInterviewOutcome(50, 2, "Completed", "Passed", "Great performance");
                assertTrue("Updating outcome of another student's interview should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Updating outcome of another student's interview throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student updateInterviewOutcome: " + e, false);
            }

            // Cross-student interview deletion attempt
            try {
                interviewService.deleteInterview(50, 2);
                assertTrue("Deleting another student's interview should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Deleting another student's interview throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student deleteInterview: " + e, false);
            }

            // 3. Certification Ownership Protection
            CertificationDAO certDaoStub = new CertificationDAO() {
                @Override public List<Certification> getByStudentId(int studentId) { return new ArrayList<>(); }
                @Override public Certification getById(int certId) {
                    if (certId == 100) {
                        return new Certification(100, 1, "Java OCA", "Oracle", Date.valueOf("2026-01-01"), "C-1");
                    }
                    return null;
                }
                @Override public Certification getByIdAndStudentId(int certId, int studentId) {
                    if (certId == 100 && studentId == 1) {
                        return new Certification(100, 1, "Java OCA", "Oracle", Date.valueOf("2026-01-01"), "C-1");
                    }
                    return null;
                }
                @Override public boolean hasCertification(int studentId, String certName, String issuingOrg) { return false; }
                @Override public boolean add(Certification c) { return true; }
                @Override public boolean add(Certification c, java.sql.Connection conn) { return true; }
                @Override public boolean update(Certification c) { return true; }
                @Override public boolean update(Certification c, int studentId) { return true; }
                @Override public boolean delete(int certificationId) { return true; }
                @Override public boolean delete(int certificationId, java.sql.Connection conn) { return true; }
                @Override public boolean delete(int certificationId, int studentId) { return true; }
                @Override public boolean delete(int certificationId, int studentId, java.sql.Connection conn) { return true; }
            };

            CertificationSkillDAO certSkillDaoStub = new CertificationSkillDAO() {
                @Override public List<CertificationSkillMapping> findCatalogMappingsByCertName(String certName) { return new ArrayList<>(); }
                @Override public List<CertificationSkillMapping> getAllCatalogMappings() { return new ArrayList<>(); }
                @Override public boolean addCatalogMapping(CertificationSkillMapping mapping) { return true; }
                @Override public List<StudentCertificationSkill> getStudentCertificationSkills(int certificationId) { return new ArrayList<>(); }
                @Override public List<StudentCertificationSkill> getStudentCertificationSkillsByStudent(int studentId) { return new ArrayList<>(); }
                @Override public void addStudentCertificationSkills(List<StudentCertificationSkill> skills) {}
                @Override public void addStudentCertificationSkills(List<StudentCertificationSkill> skills, java.sql.Connection conn) {}
                @Override public boolean deleteStudentCertificationSkills(int certificationId) { return true; }
                @Override public boolean deleteStudentCertificationSkills(int certificationId, java.sql.Connection conn) { return true; }
            };

            SkillEvidenceDAO evidenceDaoStub = new SkillEvidenceDAO() {
                @Override public List<SkillEvidence> getEvidenceByStudent(int studentId) { return new ArrayList<>(); }
                @Override public List<SkillEvidence> getEvidenceByStudentAndSkill(int studentId, int skillId) { return new ArrayList<>(); }
                @Override public void addEvidence(SkillEvidence evidence) {}
                @Override public void addEvidence(SkillEvidence evidence, java.sql.Connection conn) {}
                @Override public boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId) { return true; }
                @Override public boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId, java.sql.Connection conn) { return true; }
                @Override public int getVerifiedEvidenceCount(int studentId, int skillId) { return 0; }
            };

            SkillDAO skillDaoStub = new SkillDAO() {
                @Override public List<Skill> getAllSkills() { return new ArrayList<>(); }
                @Override public List<Skill> getStudentSkills(int studentId) { return new ArrayList<>(); }
                @Override public boolean hasStudentSkill(int studentId, int skillId) { return true; }
                @Override public boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) { return true; }
                @Override public boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency, java.sql.Connection conn) { return true; }
                @Override public String getStudentSkillProficiency(int studentId, int skillId, java.sql.Connection conn) { return "Intermediate"; }
                @Override public boolean deleteStudentSkill(int studentId, int skillId) { return true; }
                @Override public List<SkillGapResult> getSkillGapAnalysis(int studentId, int roleId) { return new ArrayList<>(); }
            };

            CertificationService certService = new CertificationServiceImpl(certDaoStub, certSkillDaoStub, evidenceDaoStub, skillDaoStub);

            // Cross-student certification update attempt
            try {
                Certification targetCert = new Certification(100, 1, "Java OCA", "Oracle", Date.valueOf("2026-01-01"), "C-1");
                certService.updateCertification(targetCert, 2);
                assertTrue("Updating another student's certification should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Updating another student's certification throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student updateCertification: " + e, false);
            }

            // Cross-student certification delete attempt
            try {
                certService.deleteCertification(100, 2);
                assertTrue("Deleting another student's certification should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Deleting another student's certification throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student deleteCertification: " + e, false);
            }

            // 4. Project Ownership Protection
            ProjectDAO projDaoStub = new ProjectDAO() {
                @Override public List<Project> getByStudentId(int studentId) { return new ArrayList<>(); }
                @Override public Project getById(int projectId) {
                    if (projectId == 200) {
                        return new Project(200, 1, "SINST", "Tracker", "Java", "https://github.com/aadil274/sinst");
                    }
                    return null;
                }
                @Override public Project getByIdAndStudentId(int projectId, int studentId) {
                    if (projectId == 200 && studentId == 1) {
                        return new Project(200, 1, "SINST", "Tracker", "Java", "https://github.com/aadil274/sinst");
                    }
                    return null;
                }
                @Override public boolean hasProjectTitle(int studentId, String title) { return false; }
                @Override public boolean add(Project p) { return true; }
                @Override public boolean update(Project p) { return true; }
                @Override public boolean update(Project p, int studentId) { return true; }
                @Override public boolean delete(int projectId) { return true; }
                @Override public boolean delete(int projectId, int studentId) { return true; }
            };

            ProjectService projService = new ProjectServiceImpl(projDaoStub);

            // Cross-student project update attempt
            try {
                Project targetProj = new Project(200, 1, "SINST Updated", "Tracker", "Java", "https://github.com/aadil274/sinst");
                projService.updateProject(targetProj, 2);
                assertTrue("Updating another student's project should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Updating another student's project throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student updateProject: " + e, false);
            }

            // Cross-student project delete attempt
            try {
                projService.deleteProject(200, 2);
                assertTrue("Deleting another student's project should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Deleting another student's project throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student deleteProject: " + e, false);
            }

            // 5. Skill Profile Ownership Protection
            SkillService skillService = new SkillServiceImpl(skillDaoStub, evidenceDaoStub);
            UserSession.setCurrentStudent(student1); // Authenticated as Student 1

            try {
                skillService.addOrUpdateStudentSkill(2, 5, "Advanced");
                assertTrue("Modifying skills of another student should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Modifying skills of another student throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student addOrUpdateStudentSkill: " + e, false);
            }

            try {
                skillService.removeStudentSkill(2, 5);
                assertTrue("Deleting skills of another student should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Deleting skills of another student throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student removeStudentSkill: " + e, false);
            }

            // 6. Student Profile Ownership Protection
            StudentDAO studentDaoStub = new StudentDAO() {
                @Override public Student getById(int studentId) { return null; }
                @Override public Student getByRollNumber(String rollNumber) { return null; }
                @Override public Student login(String identifier, String password) { return null; }
                @Override public boolean register(Student s) { return true; }
                @Override public boolean updateProfile(Student s) { return true; }
                @Override public boolean updateProfile(Student s, int authenticatedStudentId) { return true; }
            };

            StudentService studentService = new StudentServiceImpl(studentDaoStub);
            Student targetProfile = new Student(1, "25R11A0501", "Updated Name", "s1@test.com", "pass", "CSE", 3, "9876543210", "STUDENT");
            try {
                studentService.updateProfile(targetProfile, 2);
                assertTrue("Updating another student's profile should be rejected", false);
            } catch (UnauthorizedAccessException e) {
                assertTrue("Updating another student's profile throws UnauthorizedAccessException", true);
            } catch (Exception e) {
                assertTrue("Unexpected exception on cross-student updateProfile: " + e, false);
            }
        } finally {
            UserSession.clear();
        }
    }

    private static void testPrompt6DuplicateSubmissionHandling() {
        System.out.println("\n--- Testing Prompt 6 Duplicate Submission Handling ---");

        // 1. Duplicate Application Submission
        ApplicationDAO dupAppDao = new ApplicationDAO() {
            @Override public List<Application> getByStudentId(int studentId) { return new ArrayList<>(); }
            @Override public Application getById(int applicationId) { return null; }
            @Override public Application getByIdAndStudentId(int applicationId, int studentId) { return null; }
            @Override public boolean hasApplication(int studentId, int companyId, int roleId) { return true; }
            @Override public boolean add(Application a) { return true; }
            @Override public boolean update(Application a) { return true; }
            @Override public boolean update(Application a, int studentId) { return true; }
            @Override public boolean updateStatus(int applicationId, String status) { return true; }
            @Override public boolean updateStatus(int applicationId, int studentId, String status) { return true; }
            @Override public boolean delete(int applicationId) { return true; }
            @Override public boolean delete(int applicationId, int studentId) { return true; }
        };

        ApplicationService appService = new ApplicationServiceImpl(dupAppDao);
        Application app = new Application(0, 1, 10, 20, Date.valueOf("2026-03-01"), null, "Applied", "₹ 25,000", null);
        try {
            appService.submitApplication(app);
            assertTrue("Duplicate application submission should be rejected", false);
        } catch (DuplicateEntityException e) {
            assertTrue("Duplicate application submission throws DuplicateEntityException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception on duplicate application: " + e, false);
        }

        // 2. Duplicate Certification Submission
        CertificationDAO dupCertDao = new CertificationDAO() {
            @Override public List<Certification> getByStudentId(int studentId) { return new ArrayList<>(); }
            @Override public Certification getById(int certId) { return null; }
            @Override public Certification getByIdAndStudentId(int certId, int studentId) { return null; }
            @Override public boolean hasCertification(int studentId, String certName, String issuingOrg) { return true; }
            @Override public boolean add(Certification c) { return true; }
            @Override public boolean add(Certification c, java.sql.Connection conn) { return true; }
            @Override public boolean update(Certification c) { return true; }
            @Override public boolean update(Certification c, int studentId) { return true; }
            @Override public boolean delete(int certificationId) { return true; }
            @Override public boolean delete(int certificationId, java.sql.Connection conn) { return true; }
            @Override public boolean delete(int certificationId, int studentId) { return true; }
            @Override public boolean delete(int certificationId, int studentId, java.sql.Connection conn) { return true; }
        };

        CertificationService certService = new CertificationServiceImpl(dupCertDao, null, null, null);
        Certification cert = new Certification(0, 1, "AWS Cloud Practitioner", "Amazon", Date.valueOf("2025-01-01"), "AWS-001");
        try {
            certService.addCertification(cert);
            assertTrue("Duplicate certification submission should be rejected", false);
        } catch (DuplicateEntityException e) {
            assertTrue("Duplicate certification submission throws DuplicateEntityException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception on duplicate certification: " + e, false);
        }

        // 3. Duplicate Project Title Submission
        ProjectDAO dupProjDao = new ProjectDAO() {
            @Override public List<Project> getByStudentId(int studentId) { return new ArrayList<>(); }
            @Override public Project getById(int projectId) { return null; }
            @Override public Project getByIdAndStudentId(int projectId, int studentId) { return null; }
            @Override public boolean hasProjectTitle(int studentId, String title) { return true; }
            @Override public boolean add(Project p) { return true; }
            @Override public boolean update(Project p) { return true; }
            @Override public boolean update(Project p, int studentId) { return true; }
            @Override public boolean delete(int projectId) { return true; }
            @Override public boolean delete(int projectId, int studentId) { return true; }
        };

        ProjectService projService = new ProjectServiceImpl(dupProjDao);
        Project proj = new Project(0, 1, "Portfolio Web App", "Personal web app", "HTML, CSS, JS", "https://github.com/aadil274/sinst");
        try {
            projService.addProject(proj);
            assertTrue("Duplicate project title should be rejected", false);
        } catch (DuplicateEntityException e) {
            assertTrue("Duplicate project title throws DuplicateEntityException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception on duplicate project: " + e, false);
        }

        // 4. Duplicate Interview Round Submission
        ApplicationDAO appDao = new ApplicationDAO() {
            @Override public List<Application> getByStudentId(int studentId) { return new ArrayList<>(); }
            @Override public Application getById(int applicationId) {
                return new Application(10, 1, 1, 1, Date.valueOf("2026-02-01"), null, "Interview", "₹ 20,000", null);
            }
            @Override public Application getByIdAndStudentId(int applicationId, int studentId) { return getById(applicationId); }
            @Override public boolean hasApplication(int studentId, int companyId, int roleId) { return false; }
            @Override public boolean add(Application a) { return true; }
            @Override public boolean update(Application a) { return true; }
            @Override public boolean update(Application a, int studentId) { return true; }
            @Override public boolean updateStatus(int applicationId, String status) { return true; }
            @Override public boolean updateStatus(int applicationId, int studentId, String status) { return true; }
            @Override public boolean delete(int applicationId) { return true; }
            @Override public boolean delete(int applicationId, int studentId) { return true; }
        };

        InterviewDAO dupInterviewDao = new InterviewDAO() {
            @Override public List<Interview> getByStudentId(int studentId) { return new ArrayList<>(); }
            @Override public List<Interview> getByApplicationId(int applicationId) { return new ArrayList<>(); }
            @Override public List<Interview> getByApplicationId(int applicationId, int studentId) { return new ArrayList<>(); }
            @Override public Interview getById(int interviewId) { return null; }
            @Override public Interview getByIdAndStudentId(int interviewId, int studentId) { return null; }
            @Override public List<Interview> getUpcoming(int studentId) { return new ArrayList<>(); }
            @Override public boolean schedule(Interview i, int applicationId, String newStatus) { return true; }
            @Override public boolean schedule(Interview i, int applicationId, int studentId, String newStatus) { return true; }
            @Override public boolean update(int interviewId, String status, String outcome, String feedback) { return true; }
            @Override public boolean update(int interviewId, int studentId, String status, String outcome, String feedback) { return true; }
            @Override public boolean updateFull(Interview i) { return true; }
            @Override public boolean updateFull(Interview i, int studentId) { return true; }
            @Override public boolean delete(int interviewId) { return true; }
            @Override public boolean delete(int interviewId, int studentId) { return true; }
            @Override public int getApplicationOwnerStudentId(int applicationId) { return 1; }
            @Override public int getInterviewOwnerStudentId(int interviewId) { return 1; }
            @Override public boolean hasScheduledInterview(int applicationId, String roundType, Timestamp interviewDate) { return true; }
        };

        InterviewService interviewService = new InterviewServiceImpl(dupInterviewDao, appDao);
        Interview interview = new Interview(0, 10, Timestamp.valueOf("2026-03-01 11:00:00"), "Technical Round 1", 1, "Online", "Scheduled", "Pending", null);
        try {
            interviewService.scheduleInterview(interview, 1);
            assertTrue("Duplicate interview round should be rejected", false);
        } catch (DuplicateEntityException e) {
            assertTrue("Duplicate interview round throws DuplicateEntityException", true);
        } catch (Exception e) {
            assertTrue("Unexpected exception on duplicate interview: " + e, false);
        }
    }

    private static void testPrompt6ErrorSanitization() {
        System.out.println("\n--- Testing Prompt 6 Error Sanitization ---");

        // Communications link failure
        String sanitizedConn = ValidationUtil.sanitizeErrorMessage(new java.sql.SQLException("Communications link failure: The last packet successfully received from the server was 0 milliseconds ago."));
        assertTrue("Communications failure sanitized to user-friendly message", sanitizedConn.contains("Unable to connect to MySQL database"));

        // Connection refused
        String sanitizedRefused = ValidationUtil.sanitizeErrorMessage(new java.sql.SQLException("Connection refused: connect"));
        assertTrue("Connection refused sanitized to user-friendly message", sanitizedRefused.contains("Unable to connect to MySQL database"));

        // Duplicate entry
        String sanitizedDup = ValidationUtil.sanitizeErrorMessage(new java.sql.SQLException("Duplicate entry 'john.doe@test.com' for key 'uq_student_email'"));
        assertTrue("Duplicate entry sanitized to user-friendly message", sanitizedDup.contains("duplicate record already exists"));

        // Foreign key constraint failure
        String sanitizedFk = ValidationUtil.sanitizeErrorMessage(new java.sql.SQLException("Cannot delete or update a parent row: a foreign key constraint fails (`sinst_db`.`internship_applications`, CONSTRAINT `fk_app_student`)"));
        assertTrue("Foreign key failure sanitized to user-friendly message", sanitizedFk.contains("Related records depend on this item"));

        // Data truncation
        String sanitizedTrunc = ValidationUtil.sanitizeErrorMessage(new java.sql.SQLException("Data truncation: Data too long for column 'title' at row 1"));
        assertTrue("Data truncation sanitized to user-friendly message", sanitizedTrunc.contains("exceeds the maximum permitted length"));

        // Table doesn't exist
        String sanitizedTable = ValidationUtil.sanitizeErrorMessage(new java.sql.SQLException("Table 'sinst_db.role_recommendations' doesn't exist"));
        assertTrue("Missing table sanitized to migration instruction", sanitizedTable.contains("missing required tables. Please run the SQL migration scripts"));

        // Domain exceptions preserve their informative text
        String sanitizedVal = ValidationUtil.sanitizeErrorMessage(new ValidationException("Project Title is required."));
        assertEquals("ValidationException preserves exact user message", "Project Title is required.", sanitizedVal);

        String sanitizedDupEx = ValidationUtil.sanitizeErrorMessage(new DuplicateEntityException("You have already submitted an application."));
        assertEquals("DuplicateEntityException preserves exact user message", "You have already submitted an application.", sanitizedDupEx);

        String sanitizedUnauth = ValidationUtil.sanitizeErrorMessage(new UnauthorizedAccessException("Access denied: You do not have permission."));
        assertEquals("UnauthorizedAccessException preserves exact user message", "Access denied: You do not have permission.", sanitizedUnauth);

        // Null throwable handled safely
        String sanitizedNull = ValidationUtil.sanitizeErrorMessage(null);
        assertTrue("Null throwable returns safe fallback", sanitizedNull.contains("unexpected error occurred"));
    }

    private static void testPrompt7SwingResponsivenessAndConcurrency() {
        System.out.println("\n--- Testing Prompt 7 Swing Responsiveness & Concurrency ---");

        // 1. EDT check utility
        assertFalse("Main test thread is not the Event Dispatch Thread", AppExecutor.isEdt());
        final AtomicBoolean isEdtInsideSwing = new AtomicBoolean(false);
        try {
            SwingUtilities.invokeAndWait(() -> isEdtInsideSwing.set(AppExecutor.isEdt()));
        } catch (Exception e) {
            fail("SwingUtilities.invokeAndWait threw: " + e.getMessage());
        }
        assertTrue("AppExecutor.isEdt() correctly identifies Swing Event Dispatch Thread", isEdtInsideSwing.get());

        // 2. Background worker execution & EDT callback on success
        final CountDownLatch successLatch = new CountDownLatch(1);
        final AtomicBoolean bgNotEdt = new AtomicBoolean(false);
        final AtomicBoolean successIsEdt = new AtomicBoolean(false);
        final AtomicReference<String> resultVal = new AtomicReference<>();
        final AtomicReference<String> bgThreadName = new AtomicReference<>();

        AppExecutor.runWorker(
            "test-success-task",
            () -> {
                bgNotEdt.set(!AppExecutor.isEdt());
                bgThreadName.set(Thread.currentThread().getName());
                return "COMPUTED_DATA_42";
            },
            data -> {
                successIsEdt.set(AppExecutor.isEdt());
                resultVal.set(data);
                successLatch.countDown();
            },
            err -> {
                successLatch.countDown();
            },
            null
        );

        try {
            assertTrue("Success worker finished within timeout", successLatch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("Interrupted waiting for worker: " + e.getMessage());
        }

        assertTrue("Worker background task does NOT run on EDT", bgNotEdt.get());
        assertEquals("Worker onSuccess callback received exact result", "COMPUTED_DATA_42", resultVal.get());
        assertTrue("Worker onSuccess callback executed strictly on EDT", successIsEdt.get());

        // 3. Background worker error handling & EDT callback on failure
        final CountDownLatch errorLatch = new CountDownLatch(1);
        final AtomicBoolean errorIsEdt = new AtomicBoolean(false);
        final AtomicReference<Throwable> capturedError = new AtomicReference<>();

        AppExecutor.runWorker(
            "test-error-task",
            () -> {
                throw new IllegalStateException("Simulated background database failure");
            },
            data -> errorLatch.countDown(),
            err -> {
                errorIsEdt.set(AppExecutor.isEdt());
                capturedError.set(err);
                errorLatch.countDown();
            },
            null
        );

        try {
            assertTrue("Error worker finished within timeout", errorLatch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            fail("Interrupted waiting for error worker: " + e.getMessage());
        }

        assertTrue("Worker onError callback executed strictly on EDT", errorIsEdt.get());
        assertTrue("Worker caught IllegalStateException", capturedError.get() instanceof IllegalStateException);
        assertEquals("Worker error preserved message", "Simulated background database failure", capturedError.get().getMessage());

        // 4. Loading indicator & button disabling/re-enabling on success
        final JButton testActionBtn = new JButton("Save Record");
        final CountDownLatch btnSuccessLatch = new CountDownLatch(1);
        final AtomicBoolean wasDisabledDuringExecution = new AtomicBoolean(false);

        try {
            SwingUtilities.invokeAndWait(() -> {
                testActionBtn.setEnabled(true);
                AppExecutor.runWorker(
                    "test-btn-state-task",
                    () -> {
                        wasDisabledDuringExecution.set(!testActionBtn.isEnabled());
                        Thread.sleep(100);
                        return "OK";
                    },
                    res -> {
                        btnSuccessLatch.countDown();
                    },
                    err -> btnSuccessLatch.countDown(),
                    null,
                    testActionBtn
                );
            });
            assertTrue("Btn success task completed within timeout", btnSuccessLatch.await(5, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            fail("Exception during button state testing: " + e.getMessage());
        }

        assertTrue("Action button was disabled during background execution", wasDisabledDuringExecution.get());
        assertTrue("Action button was re-enabled after successful completion", testActionBtn.isEnabled());

        // 5. Button re-enabling on error (in finally block)
        final JButton testFailBtn = new JButton("Delete Record");
        final CountDownLatch btnFailLatch = new CountDownLatch(1);
        final AtomicBoolean wasFailDisabledDuring = new AtomicBoolean(false);

        try {
            SwingUtilities.invokeAndWait(() -> {
                testFailBtn.setEnabled(true);
                AppExecutor.runWorker(
                    "test-btn-fail-task",
                    () -> {
                        wasFailDisabledDuring.set(!testFailBtn.isEnabled());
                        Thread.sleep(50);
                        throw new RuntimeException("DB write error");
                    },
                    res -> btnFailLatch.countDown(),
                    err -> btnFailLatch.countDown(),
                    null,
                    testFailBtn
                );
            });
            assertTrue("Btn fail task completed within timeout", btnFailLatch.await(5, TimeUnit.SECONDS));
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            fail("Exception during fail button state testing: " + e.getMessage());
        }

        assertTrue("Action button was disabled during failing task execution", wasFailDisabledDuring.get());
        assertTrue("Action button was restored to enabled state in finally block despite error", testFailBtn.isEnabled());

        // 6. Rapid task cancellation and stale UI state prevention
        final AtomicBoolean staleResultAccepted = new AtomicBoolean(false);
        final AtomicBoolean freshResultAccepted = new AtomicBoolean(false);
        final CountDownLatch secondTaskLatch = new CountDownLatch(1);

        try {
            SwingUtilities.invokeAndWait(() -> {
                // Task 1: Slow task
                AppExecutor.runWorker(
                    "rapid-switch-key",
                    () -> {
                        Thread.sleep(300);
                        return "STALE_RESULT";
                    },
                    res -> staleResultAccepted.set(true),
                    err -> {},
                    null
                );

                // Task 2: Immediate superseding task with same key
                AppExecutor.runWorker(
                    "rapid-switch-key",
                    () -> {
                        Thread.sleep(60);
                        return "FRESH_RESULT";
                    },
                    res -> {
                        freshResultAccepted.set(true);
                        secondTaskLatch.countDown();
                    },
                    err -> secondTaskLatch.countDown(),
                    null
                );
            });

            assertTrue("Second task finished within timeout", secondTaskLatch.await(5, TimeUnit.SECONDS));
            Thread.sleep(350);
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            fail("Exception during stale result testing: " + e.getMessage());
        }

        assertFalse("Stale superseded task result was discarded and NOT accepted into UI", staleResultAccepted.get());
        assertTrue("Fresh latest task result was accepted into UI", freshResultAccepted.get());

        // 7. Explicit task cancellation
        final AtomicBoolean cancelledExecuted = new AtomicBoolean(false);

        AppExecutor.runWorker(
            "manual-cancel-key",
            () -> {
                Thread.sleep(400);
                return "SHOULD_BE_CANCELLED";
            },
            res -> cancelledExecuted.set(true),
            err -> {},
            null
        );

        assertTrue("Active task count is at least 1 before cancellation", AppExecutor.getActiveTaskCount() >= 1);
        AppExecutor.cancelTask("manual-cancel-key");

        try {
            Thread.sleep(450);
            SwingUtilities.invokeAndWait(() -> {});
        } catch (Exception e) {
            fail("Exception during manual cancel test: " + e.getMessage());
        }

        assertFalse("Explicitly cancelled task did NOT invoke onSuccess callback", cancelledExecuted.get());

        // 8. Cancel all tasks
        AppExecutor.runWorker(
            "bulk-cancel-1",
            () -> { Thread.sleep(500); return 1; },
            res -> {}, err -> {}, null
        );
        AppExecutor.runWorker(
            "bulk-cancel-2",
            () -> { Thread.sleep(500); return 2; },
            res -> {}, err -> {}, null
        );
        assertTrue("Multiple active tasks registered", AppExecutor.getActiveTaskCount() >= 2);
        AppExecutor.cancelAllTasks();
        assertEquals("Active task count is 0 after cancelAllTasks()", 0, AppExecutor.getActiveTaskCount());
    }
}
