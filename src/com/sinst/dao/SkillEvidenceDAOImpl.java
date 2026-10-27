package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.SkillEvidence;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillEvidenceDAOImpl implements SkillEvidenceDAO {

    private static final SkillEvidenceDAOImpl INSTANCE = new SkillEvidenceDAOImpl();

    public static SkillEvidenceDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<SkillEvidence> getEvidenceByStudent(int studentId) throws SQLException {
        List<SkillEvidence> list = new ArrayList<>();
        String sql = "SELECT se.*, s.skill_name " +
                     "FROM skill_evidence se " +
                     "JOIN skills s ON se.skill_id = s.skill_id " +
                     "WHERE se.student_id = ? " +
                     "ORDER BY se.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEvidence(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<SkillEvidence> getEvidenceByStudentAndSkill(int studentId, int skillId) throws SQLException {
        List<SkillEvidence> list = new ArrayList<>();
        String sql = "SELECT se.*, s.skill_name " +
                     "FROM skill_evidence se " +
                     "JOIN skills s ON se.skill_id = s.skill_id " +
                     "WHERE se.student_id = ? AND se.skill_id = ? " +
                     "ORDER BY se.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapEvidence(rs));
                }
            }
        }
        return list;
    }

    @Override
    public void addEvidence(SkillEvidence evidence) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            addEvidence(evidence, conn);
        }
    }

    @Override
    public void addEvidence(SkillEvidence evidence, Connection conn) throws SQLException {
        String sql = "INSERT INTO skill_evidence " +
                     "(student_id, skill_id, evidence_type, reference_id, evidence_title, evidence_details, verification_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "evidence_title = VALUES(evidence_title), " +
                     "evidence_details = VALUES(evidence_details), " +
                     "verification_status = VALUES(verification_status)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, evidence.getStudentId());
            ps.setInt(2, evidence.getSkillId());
            ps.setString(3, evidence.getEvidenceType() != null ? evidence.getEvidenceType() : "Self-Reported");
            if (evidence.getReferenceId() != null) {
                ps.setInt(4, evidence.getReferenceId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, evidence.getEvidenceTitle().trim());
            ps.setString(6, evidence.getEvidenceDetails() != null ? evidence.getEvidenceDetails().trim() : "");
            ps.setString(7, evidence.getVerificationStatus() != null ? evidence.getVerificationStatus() : "Verified");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) evidence.setEvidenceId(rs.getInt(1));
            }
        }
    }

    @Override
    public boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return deleteEvidenceByReference(studentId, evidenceType, referenceId, conn);
        }
    }

    @Override
    public boolean deleteEvidenceByReference(int studentId, String evidenceType, int referenceId, Connection conn) throws SQLException {
        String sql = "DELETE FROM skill_evidence WHERE student_id = ? AND evidence_type = ? AND reference_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, evidenceType);
            ps.setInt(3, referenceId);
            return ps.executeUpdate() >= 0;
        }
    }

    @Override
    public int getVerifiedEvidenceCount(int studentId, int skillId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM skill_evidence WHERE student_id = ? AND skill_id = ? AND verification_status = 'Verified'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private SkillEvidence mapEvidence(ResultSet rs) throws SQLException {
        int ref = rs.getInt("reference_id");
        Integer refObj = rs.wasNull() ? null : ref;
        return new SkillEvidence(
            rs.getInt("evidence_id"),
            rs.getInt("student_id"),
            rs.getInt("skill_id"),
            rs.getString("skill_name"),
            rs.getString("evidence_type"),
            refObj,
            rs.getString("evidence_title"),
            rs.getString("evidence_details"),
            rs.getString("verification_status"),
            rs.getTimestamp("created_at")
        );
    }
}
