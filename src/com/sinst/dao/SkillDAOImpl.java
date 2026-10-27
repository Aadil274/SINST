package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Skill;
import com.sinst.model.SkillGapResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SkillDAOImpl implements SkillDAO {

    private static final SkillDAOImpl INSTANCE = new SkillDAOImpl();

    public static SkillDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Skill> getAllSkills() throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT * FROM skills ORDER BY category, skill_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Skill(
                    rs.getInt("skill_id"),
                    rs.getString("skill_name"),
                    rs.getString("category"),
                    rs.getString("description")
                ));
            }
        }
        return list;
    }

    @Override
    public List<Skill> getStudentSkills(int studentId) throws SQLException {
        List<Skill> list = new ArrayList<>();
        String sql = "SELECT s.*, ss.proficiency_level " +
                     "FROM student_skills ss " +
                     "JOIN skills s ON ss.skill_id = s.skill_id " +
                     "WHERE ss.student_id = ? " +
                     "ORDER BY s.category, s.skill_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Skill s = new Skill(
                        rs.getInt("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("category"),
                        rs.getString("description")
                    );
                    s.setProficiencyLevel(rs.getString("proficiency_level"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    @Override
    public boolean hasStudentSkill(int studentId, int skillId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM student_skills WHERE student_id = ? AND skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return addOrUpdateStudentSkill(studentId, skillId, proficiency, conn);
        }
    }

    @Override
    public boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency, Connection conn) throws SQLException {
        String sql = "INSERT INTO student_skills (student_id, skill_id, proficiency_level) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE proficiency_level = VALUES(proficiency_level)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            ps.setString(3, proficiency);
            int rows = ps.executeUpdate();
            return rows >= 0;
        }
    }

    @Override
    public String getStudentSkillProficiency(int studentId, int skillId, Connection conn) throws SQLException {
        String sql = "SELECT proficiency_level FROM student_skills WHERE student_id = ? AND skill_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("proficiency_level");
                }
            }
        }
        return null;
    }

    @Override
    public boolean deleteStudentSkill(int studentId, int skillId) throws SQLException {
        String sql = "DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<SkillGapResult> getSkillGapAnalysis(int studentId, int roleId) throws SQLException {
        List<SkillGapResult> results = new ArrayList<>();
        // Parameterized SQL with DISTINCT to avoid duplicate counts from joins
        String sql = "SELECT DISTINCT " +
                     "    s.skill_id, " +
                     "    s.skill_name, " +
                     "    s.category, " +
                     "    rs.required_level, " +
                     "    rs.is_mandatory, " +
                     "    ss.proficiency_level AS student_level, " +
                     "    (ss.skill_id IS NOT NULL) AS has_skill_record " +
                     "FROM role_skills rs " +
                     "JOIN skills s ON rs.skill_id = s.skill_id " +
                     "LEFT JOIN student_skills ss ON rs.skill_id = ss.skill_id AND ss.student_id = ? " +
                     "WHERE rs.role_id = ? " +
                     "ORDER BY rs.is_mandatory DESC, s.skill_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int skillId = rs.getInt("skill_id");
                    String skillName = rs.getString("skill_name");
                    String category = rs.getString("category");
                    String requiredLevel = rs.getString("required_level");
                    boolean mandatory = rs.getBoolean("is_mandatory");
                    String studentLevel = rs.getString("student_level");
                    boolean hasSkillRecord = rs.getBoolean("has_skill_record");

                    results.add(new SkillGapResult(
                        skillId,
                        skillName,
                        category,
                        requiredLevel,
                        studentLevel,
                        mandatory,
                        hasSkillRecord
                    ));
                }
            }
        }
        return results;
    }
}
