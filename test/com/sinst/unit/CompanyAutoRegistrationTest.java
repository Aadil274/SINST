package com.sinst.unit;

import com.sinst.dao.CertificationDAO;
import com.sinst.dao.CertificationSkillDAO;
import com.sinst.dao.CompanyDAO;
import com.sinst.dao.SkillDAO;
import com.sinst.dao.SkillEvidenceDAO;
import com.sinst.model.*;
import com.sinst.service.CertificationService;
import com.sinst.service.CertificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Company Auto-Registration on Certification Creation")
public class CompanyAutoRegistrationTest {

    private MockCompanyDAO mockCompanyDAO;
    private MockCertificationDAO mockCertDAO;
    private CertificationService certService;

    @BeforeEach
    void setUp() {
        mockCompanyDAO = new MockCompanyDAO();
        mockCertDAO = new MockCertificationDAO();

        certService = new CertificationServiceImpl(
            mockCertDAO,
            new MockCertificationSkillDAO(),
            new MockSkillEvidenceDAO(),
            new MockSkillDAO(),
            mockCompanyDAO
        );
    }

    @Test
    @DisplayName("ensureCompanyExists returns true and adds company when company is not present")
    void testEnsureCompanyExistsWhenNew() throws SQLException {
        boolean added = mockCompanyDAO.ensureCompanyExists("Amazon Web Services");
        assertTrue(added);
        assertTrue(mockCompanyDAO.hasCompany("Amazon Web Services"));
    }

    @Test
    @DisplayName("ensureCompanyExists returns false when company already exists (case-insensitive)")
    void testEnsureCompanyExistsWhenAlreadyPresent() throws SQLException {
        mockCompanyDAO.ensureCompanyExists("Oracle");
        boolean addedAgain = mockCompanyDAO.ensureCompanyExists("oracle");
        assertFalse(addedAgain);
        assertEquals(1, mockCompanyDAO.getCompanies().size());
    }

    @Test
    @DisplayName("ensureCompanyExists rejects null or blank company names safely")
    void testEnsureCompanyExistsNullOrEmpty() throws SQLException {
        assertFalse(mockCompanyDAO.ensureCompanyExists(null));
        assertFalse(mockCompanyDAO.ensureCompanyExists("   "));
        assertTrue(mockCompanyDAO.getCompanies().isEmpty());
    }

    @Test
    @DisplayName("Adding a certification automatically registers the issuing organization as a company")
    void testAddCertificationAutoRegistersCompany() throws Exception {
        Certification cert = new Certification(0, 1, "AWS Solutions Architect", "Amazon Web Services", Date.valueOf("2026-04-01"), "AWS-991");

        certService.addCertification(cert);

        assertTrue(mockCompanyDAO.hasCompany("Amazon Web Services"));
        assertEquals("Amazon Web Services", mockCompanyDAO.lastEnsuredCompany);
    }

    @Test
    @DisplayName("addCertificationWithAutoSkills automatically links verified catalog skills and registers company")
    void testAddCertificationWithAutoSkillsCatalog() throws Exception {
        Certification cert = new Certification(0, 1, "AWS Certified Cloud Practitioner", "Amazon Web Services", Date.valueOf("2026-05-10"), "AWS-1002");

        List<StudentCertificationSkill> autoSkills = certService.addCertificationWithAutoSkills(cert, 1);

        assertNotNull(autoSkills);
        assertFalse(autoSkills.isEmpty());
        assertEquals(1, autoSkills.size());
        assertEquals("REST APIs", autoSkills.get(0).getSkillName());
        assertEquals("Beginner", autoSkills.get(0).getAssessedProficiency());
        assertTrue(mockCompanyDAO.hasCompany("Amazon Web Services"));
    }

    @Test
    @DisplayName("addCertificationWithAutoSkills falls back to keyword matching against master skills for custom certs")
    void testAddCertificationWithAutoSkillsKeywordFallback() throws Exception {
        Certification cert = new Certification(0, 1, "Complete Python Developer Bootcamp", "Udemy / TechOrg", Date.valueOf("2026-06-15"), "UDE-552");

        List<StudentCertificationSkill> autoSkills = certService.addCertificationWithAutoSkills(cert, 1);

        assertNotNull(autoSkills);
        assertEquals(1, autoSkills.size());
        assertEquals("Python", autoSkills.get(0).getSkillName());
        assertEquals("Intermediate", autoSkills.get(0).getAssessedProficiency());
        assertTrue(mockCompanyDAO.hasCompany("Udemy / TechOrg"));
    }

    @Test
    @DisplayName("addCertificationWithAutoSkills saves cert and company cleanly without fake skills when unrecognized")
    void testAddCertificationWithAutoSkillsUnrecognized() throws Exception {
        Certification cert = new Certification(0, 1, "Financial Modeling & Valuation", "Corporate Finance Institute", Date.valueOf("2026-07-01"), "CFI-771");

        List<StudentCertificationSkill> autoSkills = certService.addCertificationWithAutoSkills(cert, 1);

        assertNotNull(autoSkills);
        assertTrue(autoSkills.isEmpty());
        assertTrue(mockCompanyDAO.hasCompany("Corporate Finance Institute"));
    }

    // --- Mock Implementations for Isolated Unit Testing ---

    private static class MockCompanyDAO implements CompanyDAO {
        private final Set<String> companies = new HashSet<>();
        private String lastEnsuredCompany;

        @Override
        public List<Company> getAll() {
            List<Company> list = new ArrayList<>();
            int id = 1;
            for (String name : companies) {
                list.add(new Company(id++, name, "Certification & Training", "Global / Online", "", ""));
            }
            return list;
        }

        @Override
        public Company getById(int companyId) {
            return null;
        }

        @Override
        public boolean save(Company company) {
            if (company != null && company.getCompanyName() != null) {
                companies.add(company.getCompanyName().trim().toLowerCase());
                return true;
            }
            return false;
        }

        @Override
        public boolean delete(int companyId) {
            return true;
        }

        @Override
        public boolean ensureCompanyExists(String companyName) {
            return ensureCompanyExists(companyName, null);
        }

        @Override
        public boolean ensureCompanyExists(String companyName, Connection conn) {
            if (companyName == null || companyName.trim().isEmpty()) {
                return false;
            }
            String trimmed = companyName.trim();
            this.lastEnsuredCompany = trimmed;
            String lower = trimmed.toLowerCase();
            if (companies.contains(lower)) {
                return false;
            }
            companies.add(lower);
            return true;
        }

        public boolean hasCompany(String name) {
            return name != null && companies.contains(name.trim().toLowerCase());
        }

        public Set<String> getCompanies() {
            return companies;
        }
    }

    private static class MockCertificationDAO implements CertificationDAO {
        private final List<Certification> certs = new ArrayList<>();

        @Override public List<Certification> getByStudentId(int studentId) { return certs; }
        @Override public Certification getById(int certificationId) { return null; }
        @Override public Certification getByIdAndStudentId(int certId, int studentId) { return null; }
        @Override public boolean add(Certification cert) {
            cert.setCertificationId(certs.size() + 1);
            certs.add(cert);
            return true;
        }
        @Override public boolean add(Certification cert, Connection conn) { return add(cert); }
        @Override public boolean update(Certification cert) { return true; }
        @Override public boolean update(Certification cert, int studentId) { return true; }
        @Override public boolean delete(int certId) { return true; }
        @Override public boolean delete(int certId, int studentId) { return true; }
        @Override public boolean delete(int certId, Connection conn) { return true; }
        @Override public boolean delete(int certId, int studentId, Connection conn) { return true; }
        @Override public boolean hasCertification(int studentId, String certName, String issuingOrg) { return false; }
    }

    private static class MockCertificationSkillDAO implements CertificationSkillDAO {
        @Override
        public List<CertificationSkillMapping> findCatalogMappingsByCertName(String n) {
            List<CertificationSkillMapping> list = new ArrayList<>();
            if (n != null && n.toLowerCase().contains("aws certified cloud practitioner")) {
                list.add(new CertificationSkillMapping(
                    1, "AWS Certified Cloud Practitioner", "Amazon Web Services", 5, "REST APIs",
                    "Cloud architecture and REST endpoints", "AWS Exam Guide", "Beginner", "Verified"
                ));
            }
            return list;
        }
        @Override public List<CertificationSkillMapping> getAllCatalogMappings() { return new ArrayList<>(); }
        @Override public boolean addCatalogMapping(CertificationSkillMapping m) { return true; }
        @Override public List<StudentCertificationSkill> getStudentCertificationSkills(int certId) { return new ArrayList<>(); }
        @Override public List<StudentCertificationSkill> getStudentCertificationSkillsByStudent(int studentId) { return new ArrayList<>(); }
        @Override public void addStudentCertificationSkills(List<StudentCertificationSkill> l) {}
        @Override public void addStudentCertificationSkills(List<StudentCertificationSkill> l, Connection c) {}
        @Override public boolean deleteStudentCertificationSkills(int certId) { return true; }
        @Override public boolean deleteStudentCertificationSkills(int certId, Connection conn) { return true; }
    }

    private static class MockSkillEvidenceDAO implements SkillEvidenceDAO {
        @Override public List<SkillEvidence> getEvidenceByStudent(int studentId) { return new ArrayList<>(); }
        @Override public List<SkillEvidence> getEvidenceByStudentAndSkill(int studentId, int skillId) { return new ArrayList<>(); }
        @Override public void addEvidence(SkillEvidence e) {}
        @Override public void addEvidence(SkillEvidence e, Connection conn) {}
        @Override public boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId) { return true; }
        @Override public boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId, Connection conn) { return true; }
        @Override public int getVerifiedEvidenceCount(int studentId, int skillId) { return 1; }
    }

    private static class MockSkillDAO implements SkillDAO {
        @Override
        public List<Skill> getAllSkills() {
            List<Skill> list = new ArrayList<>();
            list.add(new Skill(1, "Java", "Technical", "Core Java and OOP"));
            list.add(new Skill(2, "SQL & DBMS", "Database", "Relational querying"));
            list.add(new Skill(10, "Python", "Technical", "Python scripting and libraries"));
            return list;
        }
        @Override public List<Skill> getStudentSkills(int studentId) { return new ArrayList<>(); }
        @Override public boolean hasStudentSkill(int studentId, int skillId) { return false; }
        @Override public boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) { return true; }
        @Override public boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency, Connection conn) { return true; }
        @Override public String getStudentSkillProficiency(int studentId, int skillId, Connection conn) { return "None"; }
        @Override public boolean deleteStudentSkill(int studentId, int skillId) { return true; }
        @Override public List<SkillGapResult> getSkillGapAnalysis(int studentId, int roleId) { return new ArrayList<>(); }
    }
}
