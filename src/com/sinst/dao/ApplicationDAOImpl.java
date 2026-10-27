package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAOImpl implements ApplicationDAO {

    private static final ApplicationDAOImpl INSTANCE = new ApplicationDAOImpl();

    public static ApplicationDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Application> getByStudentId(int studentId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = "SELECT a.*, c.company_name, r.role_title " +
                     "FROM internship_applications a " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE a.student_id = ? " +
                     "ORDER BY a.application_date DESC, a.application_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractApplication(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Application getById(int applicationId) throws SQLException {
        String sql = "SELECT a.*, c.company_name, r.role_title " +
                     "FROM internship_applications a " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE a.application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractApplication(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Application getByIdAndStudentId(int applicationId, int studentId) throws SQLException {
        String sql = "SELECT a.*, c.company_name, r.role_title " +
                     "FROM internship_applications a " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE a.application_id = ? AND a.student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractApplication(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasApplication(int studentId, int companyId, int roleId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM internship_applications WHERE student_id = ? AND company_id = ? AND role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, companyId);
            ps.setInt(3, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean add(Application a) throws SQLException {
        String sql = "INSERT INTO internship_applications (student_id, company_id, role_id, application_date, deadline, status, stipend, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getCompanyId());
            ps.setInt(3, a.getRoleId());
            ps.setDate(4, a.getApplicationDate() != null ? a.getApplicationDate() : new java.sql.Date(System.currentTimeMillis()));
            ps.setDate(5, a.getDeadline());
            ps.setString(6, a.getStatus() != null ? a.getStatus() : "Applied");
            ps.setString(7, a.getStipend());
            ps.setString(8, a.getNotes());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) a.setApplicationId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean update(Application a) throws SQLException {
        return update(a, a.getStudentId());
    }

    @Override
    public boolean update(Application a, int studentId) throws SQLException {
        String sql = "UPDATE internship_applications SET deadline = ?, status = ?, stipend = ?, notes = ? " +
                     "WHERE application_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, a.getDeadline());
            ps.setString(2, a.getStatus());
            ps.setString(3, a.getStipend());
            ps.setString(4, a.getNotes());
            ps.setInt(5, a.getApplicationId());
            ps.setInt(6, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE internship_applications SET status = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int applicationId, int studentId, String status) throws SQLException {
        String sql = "UPDATE internship_applications SET status = ? WHERE application_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            ps.setInt(3, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int applicationId) throws SQLException {
        String sql = "DELETE FROM internship_applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int applicationId, int studentId) throws SQLException {
        String sql = "DELETE FROM internship_applications WHERE application_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Application extractApplication(ResultSet rs) throws SQLException {
        Application a = new Application(
            rs.getInt("application_id"),
            rs.getInt("student_id"),
            rs.getInt("company_id"),
            rs.getInt("role_id"),
            rs.getDate("application_date"),
            rs.getDate("deadline"),
            rs.getString("status"),
            rs.getString("stipend"),
            rs.getString("notes")
        );
        a.setCompanyName(rs.getString("company_name"));
        a.setRoleTitle(rs.getString("role_title"));
        return a;
    }
}
