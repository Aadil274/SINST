package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Student;

import java.sql.*;

public class StudentDAOImpl implements StudentDAO {

    private static final StudentDAOImpl INSTANCE = new StudentDAOImpl();

    public static StudentDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public Student getById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student getByRollNumber(String rollNumber) throws SQLException {
        String sql = "SELECT * FROM students WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rollNumber != null ? rollNumber.trim().toUpperCase() : "");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Student login(String identifier, String password) throws SQLException {
        String sql = "SELECT * FROM students WHERE (roll_number = ? OR email = ?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier.trim());
            ps.setString(2, identifier.trim().toLowerCase());
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean register(Student s) throws SQLException {
        String sql = "INSERT INTO students (roll_number, full_name, email, password, department, year_of_study, phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getRollNumber().trim().toUpperCase());
            ps.setString(2, s.getFullName().trim());
            ps.setString(3, s.getEmail().trim().toLowerCase());
            ps.setString(4, s.getPassword());
            ps.setString(5, s.getDepartment() != null && !s.getDepartment().trim().isEmpty() ? s.getDepartment().trim() : "CSE");
            ps.setInt(6, s.getYearOfStudy() > 0 ? s.getYearOfStudy() : 2);
            ps.setString(7, s.getPhone() != null && !s.getPhone().trim().isEmpty() ? s.getPhone().trim() : null);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        s.setStudentId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean updateProfile(Student s) throws SQLException {
        return updateProfile(s, s.getStudentId());
    }

    @Override
    public boolean updateProfile(Student s, int authenticatedStudentId) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, email = ?, department = ?, year_of_study = ?, phone = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFullName().trim());
            ps.setString(2, s.getEmail().trim().toLowerCase());
            ps.setString(3, s.getDepartment() != null ? s.getDepartment().trim() : "CSE");
            ps.setInt(4, s.getYearOfStudy());
            ps.setString(5, s.getPhone() != null && !s.getPhone().trim().isEmpty() ? s.getPhone().trim() : null);
            ps.setInt(6, authenticatedStudentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Student extractStudent(ResultSet rs) throws SQLException {
        String role = "STUDENT";
        try {
            role = rs.getString("role");
            if (role == null || role.trim().isEmpty()) role = "STUDENT";
        } catch (SQLException ignored) {}

        return new Student(
            rs.getInt("student_id"),
            rs.getString("roll_number"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("department"),
            rs.getInt("year_of_study"),
            rs.getString("phone"),
            role
        );
    }
}
