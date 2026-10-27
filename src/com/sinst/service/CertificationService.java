package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Certification;
import com.sinst.model.CertificationSkillMapping;
import com.sinst.model.SkillEvidence;
import com.sinst.model.StudentCertificationSkill;

import java.util.List;

public interface CertificationService {
    List<Certification> getCertifications(int studentId) throws DatabaseException;
    void addCertification(Certification cert) throws ValidationException, DatabaseException, DuplicateEntityException;
    void updateCertification(Certification cert) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateCertification(Certification cert, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void deleteCertification(int certId) throws DatabaseException, UnauthorizedAccessException;
    void deleteCertification(int certId, int studentId) throws DatabaseException, UnauthorizedAccessException;

    // Prompt 4: Catalog mappings & skill suggestions
    List<CertificationSkillMapping> findSuggestedSkills(String certName) throws DatabaseException;
    List<CertificationSkillMapping> getAllCatalogMappings() throws DatabaseException;
    void addCatalogMapping(CertificationSkillMapping mapping) throws ValidationException, DatabaseException;

    // Transactional save of certification + reviewed skills + evidence
    void saveCertificationWithReviewedSkills(Certification cert, List<StudentCertificationSkill> reviewedSkills)
        throws ValidationException, DatabaseException, DuplicateEntityException;
    void saveCertificationWithReviewedSkills(Certification cert, List<StudentCertificationSkill> reviewedSkills, int studentId)
        throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException;

    // Automatic skill discovery and binding on certification registration
    List<StudentCertificationSkill> addCertificationWithAutoSkills(Certification cert)
        throws ValidationException, DatabaseException, DuplicateEntityException;
    List<StudentCertificationSkill> addCertificationWithAutoSkills(Certification cert, int studentId)
        throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException;

    List<StudentCertificationSkill> getCertificationSkills(int certificationId) throws DatabaseException;
    List<SkillEvidence> getSkillEvidence(int studentId, int skillId) throws DatabaseException;
    List<SkillEvidence> getStudentEvidence(int studentId) throws DatabaseException;
}
