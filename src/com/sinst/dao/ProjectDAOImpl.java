package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAOImpl implements ProjectDAO {

    private static final ProjectDAOImpl INSTANCE = new ProjectDAOImpl();

    public static ProjectDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Project> getByStudentId(int studentId) throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE student_id = ? ORDER BY project_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractProject(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Project getById(int projectId) throws SQLException {
        String sql = "SELECT * FROM projects WHERE project_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProject(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Project getByIdAndStudentId(int projectId, int studentId) throws SQLException {
        String sql = "SELECT * FROM projects WHERE project_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProject(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasProjectTitle(int studentId, String title) throws SQLException {
        String sql = "SELECT COUNT(*) FROM projects WHERE student_id = ? AND LOWER(title) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, title != null ? title.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean add(Project p) throws SQLException {
        String sql = "INSERT INTO projects (student_id, title, description, technologies_used, project_url, github_url, live_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getStudentId());
            ps.setString(2, p.getTitle().trim());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getTechnologiesUsed());
            ps.setString(5, p.getProjectUrl());
            ps.setString(6, p.getGithubUrl());
            ps.setString(7, p.getLiveUrl());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setProjectId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unknown column")) {
                String legacySql = "INSERT INTO projects (student_id, title, description, technologies_used, project_url) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps2 = conn.prepareStatement(legacySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps2.setInt(1, p.getStudentId());
                    ps2.setString(2, p.getTitle().trim());
                    ps2.setString(3, p.getDescription());
                    ps2.setString(4, p.getTechnologiesUsed());
                    ps2.setString(5, p.getProjectUrl());
                    int affected = ps2.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rs = ps2.getGeneratedKeys()) {
                            if (rs.next()) p.setProjectId(rs.getInt(1));
                        }
                        return true;
                    }
                }
            }
            throw ex;
        }
    }

    @Override
    public boolean update(Project p) throws SQLException {
        return update(p, p.getStudentId());
    }

    @Override
    public boolean update(Project p, int studentId) throws SQLException {
        String sql = "UPDATE projects SET title = ?, description = ?, technologies_used = ?, project_url = ?, github_url = ?, live_url = ? " +
                     "WHERE project_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getTitle().trim());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getTechnologiesUsed());
            ps.setString(4, p.getProjectUrl());
            ps.setString(5, p.getGithubUrl());
            ps.setString(6, p.getLiveUrl());
            ps.setInt(7, p.getProjectId());
            ps.setInt(8, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unknown column")) {
                String legacySql = "UPDATE projects SET title = ?, description = ?, technologies_used = ?, project_url = ? WHERE project_id = ? AND student_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps2 = conn.prepareStatement(legacySql)) {
                    ps2.setString(1, p.getTitle().trim());
                    ps2.setString(2, p.getDescription());
                    ps2.setString(3, p.getTechnologiesUsed());
                    ps2.setString(4, p.getProjectUrl());
                    ps2.setInt(5, p.getProjectId());
                    ps2.setInt(6, studentId);
                    return ps2.executeUpdate() > 0;
                }
            }
            throw ex;
        }
    }

    @Override
    public boolean delete(int projectId) throws SQLException {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int projectId, int studentId) throws SQLException {
        String sql = "DELETE FROM projects WHERE project_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Project extractProject(ResultSet rs) throws SQLException {
        String gitUrl = null;
        String liveUrl = null;
        try {
            gitUrl = rs.getString("github_url");
            liveUrl = rs.getString("live_url");
        } catch (SQLException ignored) {}

        String projUrl = rs.getString("project_url");
        if (gitUrl == null) gitUrl = projUrl;

        return new Project(
            rs.getInt("project_id"),
            rs.getInt("student_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getString("technologies_used"),
            projUrl,
            gitUrl,
            liveUrl
        );
    }
}
