package com.sinst.service;

import com.sinst.dao.SkillDAO;
import com.sinst.dao.SkillDAOImpl;
import com.sinst.dao.SkillEvidenceDAO;
import com.sinst.dao.SkillEvidenceDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Skill;
import com.sinst.model.SkillEvidence;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class SkillServiceImpl implements SkillService {

    private final SkillDAO skillDAO;
    private final SkillEvidenceDAO skillEvidenceDAO;

    public SkillServiceImpl() {
        this(SkillDAOImpl.getInstance(), SkillEvidenceDAOImpl.getInstance());
    }

    public SkillServiceImpl(SkillDAO skillDAO) {
        this(skillDAO, SkillEvidenceDAOImpl.getInstance());
    }

    public SkillServiceImpl(SkillDAO skillDAO, SkillEvidenceDAO skillEvidenceDAO) {
        this.skillDAO = skillDAO;
        this.skillEvidenceDAO = skillEvidenceDAO;
    }

    @Override
    public List<Skill> getAllSkills() throws DatabaseException {
        try {
            return skillDAO.getAllSkills();
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving skills catalog: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Skill> getStudentSkills(int studentId) throws DatabaseException {
        try {
            return skillDAO.getStudentSkills(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving student skills: " + e.getMessage(), e);
        }
    }

    @Override
    public void addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (studentId <= 0 || skillId <= 0) {
            throw new ValidationException("Valid student ID and skill ID are required.");
        }
        if (!ValidationUtil.isNonEmpty(proficiency)) {
            throw new ValidationException("Proficiency level is required.");
        }
        if (!ValidationUtil.isValidProficiency(proficiency)) {
            throw new ValidationException("Invalid proficiency level: '" + proficiency + "'. Allowed values: Beginner, Intermediate, Advanced, Expert.");
        }

        int sessionStudentId = UserSession.getCurrentStudentId();
        if (sessionStudentId > 0 && sessionStudentId != studentId) {
            throw new UnauthorizedAccessException("Access denied: You cannot modify skills for another student.");
        }

        try {
            boolean success = skillDAO.addOrUpdateStudentSkill(studentId, skillId, proficiency);
            if (!success) {
                throw new DatabaseException("Failed to save student skill.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error saving skill: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void removeStudentSkill(int studentId, int skillId) throws DatabaseException, UnauthorizedAccessException {
        if (studentId <= 0 || skillId <= 0) return;

        int sessionStudentId = UserSession.getCurrentStudentId();
        if (sessionStudentId > 0 && sessionStudentId != studentId) {
            throw new UnauthorizedAccessException("Access denied: You cannot delete skills from another student's profile.");
        }

        try {
            boolean deleted = skillDAO.deleteStudentSkill(studentId, skillId);
            if (!deleted) {
                throw new DatabaseException("Skill was not found in profile or could not be removed.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error removing skill: " + ValidationUtil.sanitizeErrorMessage(e), e);
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

    @Override
    public void addEvidence(SkillEvidence evidence) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (evidence == null || evidence.getStudentId() <= 0 || evidence.getSkillId() <= 0) {
            throw new ValidationException("Invalid student or skill for evidence record.");
        }
        if (!ValidationUtil.isNonEmpty(evidence.getEvidenceTitle())) {
            throw new ValidationException("Evidence title is required.");
        }

        int sessionStudentId = UserSession.getCurrentStudentId();
        if (sessionStudentId > 0 && sessionStudentId != evidence.getStudentId()) {
            throw new UnauthorizedAccessException("Access denied: You cannot add skill evidence for another student.");
        }

        try {
            skillEvidenceDAO.addEvidence(evidence);
        } catch (SQLException e) {
            throw new DatabaseException("Database error saving skill evidence: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }
}
