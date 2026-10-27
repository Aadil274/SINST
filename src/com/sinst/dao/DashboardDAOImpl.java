package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.DashboardStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DashboardDAOImpl implements DashboardDAO {

    private static final DashboardDAOImpl INSTANCE = new DashboardDAOImpl();

    public static DashboardDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public DashboardStats getStats(int studentId) throws SQLException {
        return DashboardStats.fromMap(getCountsMap(studentId));
    }

    @Override
    public Map<String, Integer> getCountsMap(int studentId) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Total", 0);
        counts.put("Applied", 0);
        counts.put("Shortlisted", 0);
        counts.put("Interview", 0);
        counts.put("Selected", 0);
        counts.put("Rejected", 0);
        counts.put("UpcomingInterviews", 0);
        counts.put("TotalSkills", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM internship_applications WHERE student_id = ? GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                int total = 0;
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("cnt");
                    if (status != null) {
                        counts.put(status, count);
                        total += count;
                    }
                }
                counts.put("Total", total);
            }
        }

        // Count upcoming scheduled interviews
        String interviewSql = "SELECT COUNT(*) FROM interviews i " +
                              "JOIN internship_applications a ON i.application_id = a.application_id " +
                              "WHERE a.student_id = ? AND i.status = 'Scheduled' AND i.interview_date >= NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(interviewSql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    counts.put("UpcomingInterviews", rs.getInt(1));
                }
            }
        }

        // Count total skills
        String skillsSql = "SELECT COUNT(*) FROM student_skills WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(skillsSql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    counts.put("TotalSkills", rs.getInt(1));
                }
            }
        }

        return counts;
    }
}
