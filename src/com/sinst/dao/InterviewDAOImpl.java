package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Interview;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAOImpl implements InterviewDAO {

    private static final InterviewDAOImpl INSTANCE = new InterviewDAOImpl();

    public static InterviewDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Interview> getByStudentId(int studentId) throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY i.interview_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractInterview(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Interview> getByApplicationId(int applicationId) throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE i.application_id = ? " +
                     "ORDER BY i.interview_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractInterview(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Interview> getByApplicationId(int applicationId, int studentId) throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE i.application_id = ? AND a.student_id = ? " +
                     "ORDER BY i.interview_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractInterview(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Interview getById(int interviewId) throws SQLException {
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE i.interview_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractInterview(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Interview getByIdAndStudentId(int interviewId, int studentId) throws SQLException {
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE i.interview_id = ? AND a.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractInterview(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Interview> getUpcoming(int studentId) throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE a.student_id = ? AND i.status = 'Scheduled' AND i.interview_date >= NOW() " +
                     "ORDER BY i.interview_date ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractInterview(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean schedule(Interview i, int applicationId, String newStatus) throws SQLException {
        int ownerId = getApplicationOwnerStudentId(applicationId);
        return schedule(i, applicationId, ownerId, newStatus);
    }

    @Override
    public boolean schedule(Interview i, int applicationId, int studentId, String newStatus) throws SQLException {
        Connection conn = null;
        String verifyOwnerSql = "SELECT student_id FROM internship_applications WHERE application_id = ?";
        String insertInterviewSql = "INSERT INTO interviews (application_id, interview_date, round_type, mode, status, outcome, feedback) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String updateAppSql = "UPDATE internship_applications SET status = ? WHERE application_id = ? AND student_id = ?";

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Verify ownership if studentId is provided (> 0)
            if (studentId > 0) {
                try (PreparedStatement psVerify = conn.prepareStatement(verifyOwnerSql)) {
                    psVerify.setInt(1, applicationId);
                    try (ResultSet rs = psVerify.executeQuery()) {
                        if (!rs.next() || rs.getInt("student_id") != studentId) {
                            conn.rollback();
                            return false;
                        }
                    }
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertInterviewSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, applicationId);
                psInsert.setTimestamp(2, i.getInterviewDate());
                psInsert.setString(3, i.getRoundType());
                psInsert.setString(4, i.getMode() != null ? i.getMode() : "Online");
                psInsert.setString(5, i.getStatus() != null ? i.getStatus() : "Scheduled");
                psInsert.setString(6, i.getOutcome() != null ? i.getOutcome() : "Pending");
                psInsert.setString(7, i.getFeedback());
                psInsert.executeUpdate();

                try (ResultSet rs = psInsert.getGeneratedKeys()) {
                    if (rs.next()) i.setInterviewId(rs.getInt(1));
                }
            }

            if (newStatus != null && !newStatus.trim().isEmpty()) {
                try (PreparedStatement psUpdate = conn.prepareStatement(updateAppSql)) {
                    psUpdate.setString(1, newStatus.trim());
                    psUpdate.setInt(2, applicationId);
                    psUpdate.setInt(3, studentId > 0 ? studentId : getApplicationOwnerStudentId(applicationId));
                    psUpdate.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw ex;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean update(int interviewId, String status, String outcome, String feedback) throws SQLException {
        String sql = "UPDATE interviews SET status = ?, outcome = ?, feedback = ? WHERE interview_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, outcome);
            ps.setString(3, feedback);
            ps.setInt(4, interviewId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean update(int interviewId, int studentId, String status, String outcome, String feedback) throws SQLException {
        String sql = "UPDATE interviews i JOIN internship_applications a ON i.application_id = a.application_id " +
                     "SET i.status = ?, i.outcome = ?, i.feedback = ? " +
                     "WHERE i.interview_id = ? AND a.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, outcome);
            ps.setString(3, feedback);
            ps.setInt(4, interviewId);
            ps.setInt(5, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateFull(Interview i) throws SQLException {
        String sql = "UPDATE interviews SET interview_date = ?, round_type = ?, mode = ?, status = ?, outcome = ?, feedback = ? " +
                     "WHERE interview_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, i.getInterviewDate());
            ps.setString(2, i.getRoundType());
            ps.setString(3, i.getMode());
            ps.setString(4, i.getStatus());
            ps.setString(5, i.getOutcome());
            ps.setString(6, i.getFeedback());
            ps.setInt(7, i.getInterviewId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateFull(Interview i, int studentId) throws SQLException {
        String sql = "UPDATE interviews i JOIN internship_applications a ON i.application_id = a.application_id " +
                     "SET i.interview_date = ?, i.round_type = ?, i.mode = ?, i.status = ?, i.outcome = ?, i.feedback = ? " +
                     "WHERE i.interview_id = ? AND a.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, i.getInterviewDate());
            ps.setString(2, i.getRoundType());
            ps.setString(3, i.getMode());
            ps.setString(4, i.getStatus());
            ps.setString(5, i.getOutcome());
            ps.setString(6, i.getFeedback());
            ps.setInt(7, i.getInterviewId());
            ps.setInt(8, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int interviewId) throws SQLException {
        String sql = "DELETE FROM interviews WHERE interview_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int interviewId, int studentId) throws SQLException {
        String sql = "DELETE i FROM interviews i JOIN internship_applications a ON i.application_id = a.application_id " +
                     "WHERE i.interview_id = ? AND a.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int getApplicationOwnerStudentId(int applicationId) throws SQLException {
        String sql = "SELECT student_id FROM internship_applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("student_id") : 0;
            }
        }
    }

    @Override
    public int getInterviewOwnerStudentId(int interviewId) throws SQLException {
        String sql = "SELECT a.student_id FROM interviews i JOIN internship_applications a ON i.application_id = a.application_id WHERE i.interview_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("student_id") : 0;
            }
        }
    }

    @Override
    public boolean hasScheduledInterview(int applicationId, String roundType, Timestamp interviewDate) throws SQLException {
        String sql = "SELECT COUNT(*) FROM interviews WHERE application_id = ? AND round_type = ? AND interview_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setString(2, roundType);
            ps.setTimestamp(3, interviewDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private Interview extractInterview(ResultSet rs) throws SQLException {
        Interview inv = new Interview(
            rs.getInt("interview_id"),
            rs.getInt("application_id"),
            rs.getTimestamp("interview_date"),
            rs.getString("round_type"),
            rs.getString("mode"),
            rs.getString("status"),
            rs.getString("outcome"),
            rs.getString("feedback")
        );
        inv.setCompanyName(rs.getString("company_name"));
        inv.setRoleTitle(rs.getString("role_title"));
        return inv;
    }
}
