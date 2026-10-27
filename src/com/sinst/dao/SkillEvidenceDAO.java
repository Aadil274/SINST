package com.sinst.dao;

import com.sinst.model.SkillEvidence;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface SkillEvidenceDAO {
    List<SkillEvidence> getEvidenceByStudent(int studentId) throws SQLException;
    List<SkillEvidence> getEvidenceByStudentAndSkill(int studentId, int skillId) throws SQLException;
    void addEvidence(SkillEvidence evidence) throws SQLException;
    void addEvidence(SkillEvidence evidence, Connection conn) throws SQLException;
    boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId) throws SQLException;
    boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId, Connection conn) throws SQLException;
    int getVerifiedEvidenceCount(int studentId, int skillId) throws SQLException;
}

