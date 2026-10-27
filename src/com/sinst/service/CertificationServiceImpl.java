package com.sinst.service;

import com.sinst.dao.*;
import com.sinst.db.DBConnection;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.*;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CertificationServiceImpl implements CertificationService {

    private final CertificationDAO certificationDAO;
    private final CertificationSkillDAO certificationSkillDAO;
    private final SkillEvidenceDAO skillEvidenceDAO;
    private final SkillDAO skillDAO;

    public CertificationServiceImpl() {
        this(
            CertificationDAOImpl.getInstance(),
            CertificationSkillDAOImpl.getInstance(),
            SkillEvidenceDAOImpl.getInstance(),
            SkillDAOImpl.getInstance()
        );
    }

    public CertificationServiceImpl(
            CertificationDAO certificationDAO,
            CertificationSkillDAO certificationSkillDAO,
            SkillEvidenceDAO skillEvidenceDAO,
            SkillDAO skillDAO) {
        this.certificationDAO = certificationDAO;
        this.certificationSkillDAO = certificationSkillDAO;
        this.skillEvidenceDAO = skillEvidenceDAO;
        this.skillDAO = skillDAO;
    }

    @Override
    public List<Certification> getCertifications(int studentId) throws DatabaseException {
        try {
            return certificationDAO.getByStudentId(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving certifications: " + e.getMessage(), e);
        }
    }

    @Override
    public void addCertification(Certification cert) throws ValidationException, DatabaseException, DuplicateEntityException {
        validateCertification(cert);
        try {
            if (certificationDAO.hasCertification(cert.getStudentId(), cert.getCertificateName(), cert.getIssuingOrganization())) {
                throw new DuplicateEntityException("You have already added the certification '" + cert.getCertificateName() + "' to your profile.");
            }

            boolean success = certificationDAO.add(cert);
            if (!success) {
                throw new DatabaseException("Failed to add certification record.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_student_cert")) {
                throw new DuplicateEntityException("You have already added this certification to your profile.");
            }
            throw new DatabaseException("Database error adding certification: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void updateCertification(Certification cert) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        int studentId = cert != null && cert.getStudentId() > 0 ? cert.getStudentId() : UserSession.getCurrentStudentId();
        updateCertification(cert, studentId);
    }

    @Override
    public void updateCertification(Certification cert, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (cert == null || cert.getCertificationId() <= 0) {
            throw new ValidationException("Invalid certification ID for update.");
        }
        validateCertification(cert);

        try {
            Certification existing = certificationDAO.getById(cert.getCertificationId());
            if (existing == null) {
                throw new DatabaseException("Certification record not found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this certification.");
            }

            boolean updated = certificationDAO.update(cert, studentId > 0 ? studentId : existing.getStudentId());
            if (!updated) {
                throw new DatabaseException("Certification record could not be updated.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating certification: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void deleteCertification(int certId) throws DatabaseException, UnauthorizedAccessException {
        deleteCertification(certId, UserSession.getCurrentStudentId());
    }

    @Override
    public void deleteCertification(int certId, int studentId) throws DatabaseException, UnauthorizedAccessException {
        if (certId <= 0) return;

        try {
            Certification existing = certificationDAO.getById(certId);
            if (existing == null) {
                throw new DatabaseException("Certification record not found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to delete this certification.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error verifying certification ownership: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Delete associated student certification skills
            certificationSkillDAO.deleteStudentCertificationSkills(certId, conn);

            // 2. Delete associated skill evidence
            String delEvSql = "DELETE FROM skill_evidence WHERE evidence_type = 'Certification' AND reference_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delEvSql)) {
                ps.setInt(1, certId);
                ps.executeUpdate();
            }

            // 3. Delete certification record with student scoping
            boolean deleted = (studentId > 0)
                ? certificationDAO.delete(certId, studentId, conn)
                : certificationDAO.delete(certId, conn);

            if (!deleted) {
                throw new DatabaseException("Certification not found or could not be removed.");
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Database error deleting certification: " + ValidationUtil.sanitizeErrorMessage(e), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<CertificationSkillMapping> findSuggestedSkills(String certName) throws DatabaseException {
        if (certName == null || certName.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return certificationSkillDAO.findCatalogMappingsByCertName(certName);
        } catch (SQLException e) {
            throw new DatabaseException("Error finding suggested skills for certification: " + e.getMessage(), e);
        }
    }

    @Override
    public List<CertificationSkillMapping> getAllCatalogMappings() throws DatabaseException {
        try {
            return certificationSkillDAO.getAllCatalogMappings();
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving catalog mappings: " + e.getMessage(), e);
        }
    }

    @Override
    public void addCatalogMapping(CertificationSkillMapping mapping) throws ValidationException, DatabaseException {
        if (mapping == null) {
            throw new ValidationException("Mapping details cannot be null.");
        }
        if (!ValidationUtil.isNonEmpty(mapping.getCertificationName())) {
            throw new ValidationException("Certification name is required.");
        }
        if (mapping.getSkillId() <= 0) {
            throw new ValidationException("Associated skill must be selected.");
        }
        if (!ValidationUtil.isNonEmpty(mapping.getLearningOutcome())) {
            throw new ValidationException("Learning outcome / syllabus reference is required.");
        }

        try {
            boolean success = certificationSkillDAO.addCatalogMapping(mapping);
            if (!success) {
                throw new DatabaseException("Failed to add catalog mapping.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error adding catalog mapping: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveCertificationWithReviewedSkills(Certification cert, List<StudentCertificationSkill> reviewedSkills)
            throws ValidationException, DatabaseException, DuplicateEntityException {
        int studentId = cert != null && cert.getStudentId() > 0 ? cert.getStudentId() : UserSession.getCurrentStudentId();
        try {
            saveCertificationWithReviewedSkills(cert, reviewedSkills, studentId);
        } catch (UnauthorizedAccessException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    public void saveCertificationWithReviewedSkills(Certification cert, List<StudentCertificationSkill> reviewedSkills, int studentId)
            throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException {
        validateCertification(cert);

        if (reviewedSkills != null) {
            for (StudentCertificationSkill s : reviewedSkills) {
                if (s == null || s.getSkillId() <= 0) {
                    throw new ValidationException("Invalid skill ID in reviewed skills list.");
                }
            }
        }

        if (studentId > 0 && cert.getStudentId() != studentId) {
            throw new UnauthorizedAccessException("Access denied: You cannot add a certification for another student.");
        }

        try {
            if (certificationDAO.hasCertification(cert.getStudentId(), cert.getCertificateName(), cert.getIssuingOrganization())) {
                throw new DuplicateEntityException("You have already added the certification '" + cert.getCertificateName() + "' to your profile.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error checking certification duplication: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert certification record
            boolean certSaved = certificationDAO.add(cert, conn);
            if (!certSaved || cert.getCertificationId() <= 0) {
                throw new DatabaseException("Failed to record certification details in database.");
            }

            int certId = cert.getCertificationId();
            int ownerStudentId = cert.getStudentId();

            if (reviewedSkills != null && !reviewedSkills.isEmpty()) {
                List<StudentCertificationSkill> toPersist = new ArrayList<>();
                for (StudentCertificationSkill s : reviewedSkills) {
                    s.setCertificationId(certId);
                    s.setStudentId(ownerStudentId);
                    toPersist.add(s);
                }

                // 2. Persist student certification skill relations
                certificationSkillDAO.addStudentCertificationSkills(toPersist, conn);

                // 3. Persist skill evidence & update student demonstrated skills
                for (StudentCertificationSkill s : toPersist) {
                    String reviewStatus = s.getReviewStatus() != null ? s.getReviewStatus() : "Approved";
                    String verificationStatus = "Approved".equalsIgnoreCase(reviewStatus) ? "Verified" : "Pending";

                    SkillEvidence evidence = new SkillEvidence(
                        0,
                        ownerStudentId,
                        s.getSkillId(),
                        s.getSkillName(),
                        "Certification",
                        certId,
                        "Certification: " + cert.getCertificateName(),
                        "Issued by " + cert.getIssuingOrganization() + 
                            (s.getLearningOutcome() != null && !s.getLearningOutcome().isEmpty() ? ". Outcome: " + s.getLearningOutcome() : ""),
                        verificationStatus,
                        null
                    );
                    skillEvidenceDAO.addEvidence(evidence, conn);

                    // 4. Update student skill proficiency preserving demonstrated ability
                    if ("Approved".equalsIgnoreCase(reviewStatus)) {
                        String existingProfStr = skillDAO.getStudentSkillProficiency(ownerStudentId, s.getSkillId(), conn);
                        ProficiencyLevel existingLevel = ProficiencyLevel.fromString(existingProfStr);
                        ProficiencyLevel assessedLevel = ProficiencyLevel.fromString(s.getAssessedProficiency());
                        if (assessedLevel == ProficiencyLevel.NONE) {
                            assessedLevel = ProficiencyLevel.INTERMEDIATE;
                        }

                        if (existingLevel == ProficiencyLevel.NONE) {
                            skillDAO.addOrUpdateStudentSkill(ownerStudentId, s.getSkillId(), assessedLevel.getDisplayName(), conn);
                        } else if (assessedLevel.getLevel() > existingLevel.getLevel()) {
                            skillDAO.addOrUpdateStudentSkill(ownerStudentId, s.getSkillId(), assessedLevel.getDisplayName(), conn);
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_student_cert")) {
                throw new DuplicateEntityException("You have already added this certification to your profile.");
            }
            throw new DatabaseException("Transaction failed while saving certification with skills: " + ValidationUtil.sanitizeErrorMessage(e), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public List<StudentCertificationSkill> getCertificationSkills(int certificationId) throws DatabaseException {
        try {
            return certificationSkillDAO.getStudentCertificationSkills(certificationId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving certification skills: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SkillEvidence> getSkillEvidence(int studentId, int skillId) throws DatabaseException {
        try {
            return skillEvidenceDAO.getEvidenceByStudentAndSkill(studentId, skillId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving skill evidence: " + e.getMessage(), e);
        }
    }

    @Override
    public List<SkillEvidence> getStudentEvidence(int studentId) throws DatabaseException {
        try {
            return skillEvidenceDAO.getEvidenceByStudent(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving student evidence: " + e.getMessage(), e);
        }
    }

    private void validateCertification(Certification cert) throws ValidationException {
        if (cert == null || cert.getStudentId() <= 0) {
            throw new ValidationException("Invalid student identification for certification.");
        }
        if (!ValidationUtil.isNonEmpty(cert.getCertificateName())) {
            throw new ValidationException("Certification Name is required.");
        }
        if (!ValidationUtil.isNonEmpty(cert.getIssuingOrganization())) {
            throw new ValidationException("Issuing Organization is required.");
        }
        if (cert.getIssueDate() == null || !ValidationUtil.isValidDate(cert.getIssueDate().toString())) {
            throw new ValidationException("Please enter a valid issue date in YYYY-MM-DD format.");
        }
        if (cert.getExpiryDate() != null) {
            if (!ValidationUtil.isValidDate(cert.getExpiryDate().toString())) {
                throw new ValidationException("Please enter a valid expiry date in YYYY-MM-DD format.");
            }
            if (!ValidationUtil.isValidCertificationDates(cert.getIssueDate(), cert.getExpiryDate())) {
                throw new ValidationException("Certification expiry date (" + cert.getExpiryDate() + ") cannot be earlier than issue date (" + cert.getIssueDate() + ").");
            }
        }
        if (cert.getCredentialUrl() != null && !cert.getCredentialUrl().trim().isEmpty()) {
            if (!ValidationUtil.isValidUrl(cert.getCredentialUrl())) {
                throw new ValidationException("Please enter a valid credential verification URL (e.g. https://coursera.org/verify/123).");
            }
        }
    }
}
