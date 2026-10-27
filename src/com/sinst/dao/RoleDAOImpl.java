package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RoleDAOImpl implements RoleDAO {

    private static final RoleDAOImpl INSTANCE = new RoleDAOImpl();

    public static RoleDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Role> getAll() throws SQLException {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_title";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractRole(rs));
            }
        }
        return list;
    }

    @Override
    public Role getById(int roleId) throws SQLException {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractRole(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Integer> getRoleSkillIds(int roleId) throws SQLException {
        List<Integer> list = new ArrayList<>();
        String sql = "SELECT skill_id FROM role_skills WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt("skill_id"));
                }
            }
        }
        return list;
    }

    @Override
    public boolean save(Role role, List<Integer> requiredSkillIds) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int changed;
            if (role.getRoleId() == 0) {
                String sql = "INSERT INTO roles (role_title, description, experience_level) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, role.getRoleTitle().trim());
                    ps.setString(2, role.getDescription());
                    ps.setString(3, role.getExperienceLevel());
                    changed = ps.executeUpdate();
                    if (changed > 0) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) role.setRoleId(rs.getInt(1));
                        }
                    }
                }
            } else {
                String sql = "UPDATE roles SET role_title = ?, description = ?, experience_level = ? WHERE role_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, role.getRoleTitle().trim());
                    ps.setString(2, role.getDescription());
                    ps.setString(3, role.getExperienceLevel());
                    ps.setInt(4, role.getRoleId());
                    changed = ps.executeUpdate();
                }
            }

            if (role.getRoleId() != 0) {
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM role_skills WHERE role_id = ?")) {
                    del.setInt(1, role.getRoleId());
                    del.executeUpdate();
                }

                if (requiredSkillIds != null && !requiredSkillIds.isEmpty()) {
                    Set<Integer> uniqueIds = new LinkedHashSet<>(requiredSkillIds);
                    try (PreparedStatement add = conn.prepareStatement("INSERT INTO role_skills (role_id, skill_id, required_level, is_mandatory) VALUES (?, ?, 'Intermediate', TRUE)")) {
                        for (Integer skillId : uniqueIds) {
                            add.setInt(1, role.getRoleId());
                            add.setInt(2, skillId);
                            add.addBatch();
                        }
                        add.executeBatch();
                    }
                }
            }

            conn.commit();
            return changed > 0;
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
    public boolean delete(int roleId) throws SQLException {
        String sql = "DELETE FROM roles WHERE role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            return ps.executeUpdate() > 0;
        }
    }

    private Role extractRole(ResultSet rs) throws SQLException {
        String infoSrc = null;
        Date lastUp = null;
        try {
            infoSrc = rs.getString("information_source");
            lastUp = rs.getDate("last_updated");
        } catch (SQLException ignored) {}

        return new Role(
            rs.getInt("role_id"),
            rs.getString("role_title"),
            rs.getString("description"),
            rs.getString("experience_level"),
            infoSrc != null ? infoSrc : "Industry Standard Competency Framework",
            lastUp
        );
    }
}
