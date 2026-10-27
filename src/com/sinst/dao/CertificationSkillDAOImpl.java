package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.CertificationSkillMapping;
import com.sinst.model.StudentCertificationSkill;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationSkillDAOImpl implements CertificationSkillDAO {

    private static final CertificationSkillDAOImpl INSTANCE = new CertificationSkillDAOImpl();

    public static CertificationSkillDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<CertificationSkillMapping> findCatalogMappingsByCertName(String certName) throws SQLException {
        List<CertificationSkillMapping> list = new ArrayList<>();
        if (certName == null || certName.trim().isEmpty()) {
            return list;
        }

        String search = certName.trim();
        String sql = "SELECT cs.mapping_id, cs.certification_name, cs.issuing_organization, " +
                     "       cs.skill_id, s.skill_name, cs.learning_outcome, cs.evidence_source, " +
                     "       cs.suggested_proficiency, cs.mapping_status " +
                     "FROM certification_skills cs " +
                     "JOIN skills s ON cs.skill_id = s.skill_id " +
                     "WHERE LOWER(cs.certification_name) = LOWER(?) " +
                     "   OR LOWER(?) LIKE CONCAT('%', LOWER(cs.certification_name), '%') " +
                     "   OR LOWER(cs.certification_name) LIKE CONCAT('%', LOWER(?), '%') " +
                     "ORDER BY cs.mapping_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, search);
            ps.setString(3, search);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CertificationSkillMapping(
                        rs.getInt("mapping_id"),
                        rs.getString("certification_name"),
                        rs.getString("issuing_organization"),
                        rs.getInt("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("learning_outcome"),
                        rs.getString("evidence_source"),
                        rs.getString("suggested_proficiency"),
                        rs.getString("mapping_status")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<CertificationSkillMapping> getAllCatalogMappings() throws SQLException {
        List<CertificationSkillMapping> list = new ArrayList<>();
        String sql = "SELECT cs.mapping_id, cs.certification_name, cs.issuing_organization, " +
                     "       cs.skill_id, s.skill_name, cs.learning_outcome, cs.evidence_source, " +
                     "       cs.suggested_proficiency, cs.mapping_status " +
                     "FROM certification_skills cs " +
                     "JOIN skills s ON cs.skill_id = s.skill_id " +
                     "ORDER BY cs.certification_name ASC, s.skill_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new CertificationSkillMapping(
                    rs.getInt("mapping_id"),
                    rs.getString("certification_name"),
                    rs.getString("issuing_organization"),
                    rs.getInt("skill_id"),
                    rs.getString("skill_name"),
                    rs.getString("learning_outcome"),
                    rs.getString("evidence_source"),
                    rs.getString("suggested_proficiency"),
                    rs.getString("mapping_status")
                ));
            }
        }
        return list;
    }

    @Override
    public boolean addCatalogMapping(CertificationSkillMapping mapping) throws SQLException {
        String sql = "INSERT INTO certification_skills (certification_name, issuing_organization, skill_id, " +
                     "learning_outcome, evidence_source, suggested_proficiency, mapping_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, mapping.getCertificationName().trim());
            ps.setString(2, mapping.getIssuingOrganization() != null ? mapping.getIssuingOrganization().trim() : "");
            ps.setInt(3, mapping.getSkillId());
            ps.setString(4, mapping.getLearningOutcome().trim());
            ps.setString(5, mapping.getEvidenceSource() != null ? mapping.getEvidenceSource().trim() : "Catalog Definition");
            ps.setString(6, mapping.getSuggestedProficiency() != null ? mapping.getSuggestedProficiency() : "Intermediate");
            ps.setString(7, mapping.getMappingStatus() != null ? mapping.getMappingStatus() : "Verified");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) mapping.setMappingId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public List<StudentCertificationSkill> getStudentCertificationSkills(int certificationId) throws SQLException {
        List<StudentCertificationSkill> list = new ArrayList<>();
        String sql = "SELECT scs.record_id, scs.student_id, scs.certification_id, scs.skill_id, " +
                     "       s.skill_name, scs.learning_outcome, scs.review_status, scs.assessed_proficiency, scs.review_date " +
                     "FROM student_certification_skills scs " +
                     "JOIN skills s ON scs.skill_id = s.skill_id " +
                     "WHERE scs.certification_id = ? " +
                     "ORDER BY s.skill_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certificationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentCertificationSkill(
                        rs.getInt("record_id"),
                        rs.getInt("student_id"),
                        rs.getInt("certification_id"),
                        rs.getInt("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("learning_outcome"),
                        rs.getString("review_status"),
                        rs.getString("assessed_proficiency"),
                        rs.getTimestamp("review_date")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<StudentCertificationSkill> getStudentCertificationSkillsByStudent(int studentId) throws SQLException {
        List<StudentCertificationSkill> list = new ArrayList<>();
        String sql = "SELECT scs.record_id, scs.student_id, scs.certification_id, scs.skill_id, " +
                     "       s.skill_name, scs.learning_outcome, scs.review_status, scs.assessed_proficiency, scs.review_date " +
                     "FROM student_certification_skills scs " +
                     "JOIN skills s ON scs.skill_id = s.skill_id " +
                     "WHERE scs.student_id = ? " +
                     "ORDER BY scs.review_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new StudentCertificationSkill(
                        rs.getInt("record_id"),
                        rs.getInt("student_id"),
                        rs.getInt("certification_id"),
                        rs.getInt("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("learning_outcome"),
                        rs.getString("review_status"),
                        rs.getString("assessed_proficiency"),
                        rs.getTimestamp("review_date")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public void addStudentCertificationSkills(List<StudentCertificationSkill> skills) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            addStudentCertificationSkills(skills, conn);
        }
    }

    @Override
    public void addStudentCertificationSkills(List<StudentCertificationSkill> skills, Connection conn) throws SQLException {
        if (skills == null || skills.isEmpty()) return;

        String sql = "INSERT INTO student_certification_skills " +
                     "(student_id, certification_id, skill_id, learning_outcome, review_status, assessed_proficiency) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "learning_outcome = VALUES(learning_outcome), " +
                     "assessed_proficiency = VALUES(assessed_proficiency), " +
                     "review_status = VALUES(review_status)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (StudentCertificationSkill s : skills) {
                ps.setInt(1, s.getStudentId());
                ps.setInt(2, s.getCertificationId());
                ps.setInt(3, s.getSkillId());
                ps.setString(4, s.getLearningOutcome() != null ? s.getLearningOutcome().trim() : "");
                ps.setString(5, s.getReviewStatus() != null ? s.getReviewStatus() : "Approved");
                ps.setString(6, s.getAssessedProficiency() != null ? s.getAssessedProficiency() : "Intermediate");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public boolean deleteStudentCertificationSkills(int certificationId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return deleteStudentCertificationSkills(certificationId, conn);
        }
    }

    @Override
    public boolean deleteStudentCertificationSkills(int certificationId, Connection conn) throws SQLException {
        String sql = "DELETE FROM student_certification_skills WHERE certification_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certificationId);
            return ps.executeUpdate() >= 0;
        }
    }
}
