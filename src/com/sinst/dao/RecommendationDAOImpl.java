package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.CompanyRole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecommendationDAOImpl implements RecommendationDAO {

    private static final RecommendationDAOImpl INSTANCE = new RecommendationDAOImpl();

    public static RecommendationDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<CompanyRole> getAllCompanyRoles() throws SQLException {
        List<CompanyRole> list = new ArrayList<>();
        String sql = "SELECT cr.company_role_id, cr.company_id, cr.role_id, " +
                     "       c.company_name, c.industry, c.location, c.website, c.careers_url, " +
                     "       r.role_title, r.description AS role_desc, r.experience_level, " +
                     "       cr.vacancy_status, cr.vacancy_source, cr.information_source, cr.last_verified " +
                     "FROM company_roles cr " +
                     "JOIN companies c ON cr.company_id = c.company_id " +
                     "JOIN roles r ON cr.role_id = r.role_id " +
                     "ORDER BY c.company_name ASC, r.role_title ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapCompanyRole(rs));
            }
        } catch (SQLException e) {
            // Fallback gracefully if company_roles table has not been created yet
            list = getFallbackCompanyRoles();
        }

        // If list is still empty, populate from distinct roles and companies
        if (list.isEmpty()) {
            list = getFallbackCompanyRoles();
        }

        return list;
    }

    @Override
    public List<CompanyRole> getCompanyRolesByRole(int roleId) throws SQLException {
        List<CompanyRole> list = new ArrayList<>();
        String sql = "SELECT cr.company_role_id, cr.company_id, cr.role_id, " +
                     "       c.company_name, c.industry, c.location, c.website, c.careers_url, " +
                     "       r.role_title, r.description AS role_desc, r.experience_level, " +
                     "       cr.vacancy_status, cr.vacancy_source, cr.information_source, cr.last_verified " +
                     "FROM company_roles cr " +
                     "JOIN companies c ON cr.company_id = c.company_id " +
                     "JOIN roles r ON cr.role_id = r.role_id " +
                     "WHERE cr.role_id = ? " +
                     "ORDER BY c.company_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCompanyRole(rs));
                }
            }
        } catch (SQLException e) {
            for (CompanyRole cr : getFallbackCompanyRoles()) {
                if (cr.getRoleId() == roleId) list.add(cr);
            }
        }
        return list;
    }

    @Override
    public List<CompanyRole> getCompanyRolesByCompany(int companyId) throws SQLException {
        List<CompanyRole> list = new ArrayList<>();
        String sql = "SELECT cr.company_role_id, cr.company_id, cr.role_id, " +
                     "       c.company_name, c.industry, c.location, c.website, c.careers_url, " +
                     "       r.role_title, r.description AS role_desc, r.experience_level, " +
                     "       cr.vacancy_status, cr.vacancy_source, cr.information_source, cr.last_verified " +
                     "FROM company_roles cr " +
                     "JOIN companies c ON cr.company_id = c.company_id " +
                     "JOIN roles r ON cr.role_id = r.role_id " +
                     "WHERE cr.company_id = ? " +
                     "ORDER BY r.role_title ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCompanyRole(rs));
                }
            }
        } catch (SQLException e) {
            for (CompanyRole cr : getFallbackCompanyRoles()) {
                if (cr.getCompanyId() == companyId) list.add(cr);
            }
        }
        return list;
    }

    @Override
    public boolean addCompanyRole(CompanyRole cr) throws SQLException {
        String sql = "INSERT INTO company_roles (company_id, role_id, vacancy_status, vacancy_source, information_source, last_verified) " +
                     "VALUES (?, ?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "vacancy_status = VALUES(vacancy_status), " +
                     "vacancy_source = VALUES(vacancy_source), " +
                     "information_source = VALUES(information_source), " +
                     "last_verified = VALUES(last_verified)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cr.getCompanyId());
            ps.setInt(2, cr.getRoleId());
            ps.setString(3, cr.getVacancyStatus() != null ? cr.getVacancyStatus() : "Potential Role Match");
            ps.setString(4, cr.getVacancySource() != null ? cr.getVacancySource() : "Company Competency Profile");
            ps.setString(5, cr.getInformationSource() != null ? cr.getInformationSource() : "Official Careers Portal");
            ps.setDate(6, cr.getLastVerified() != null ? cr.getLastVerified() : new Date(System.currentTimeMillis()));

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) cr.setCompanyRoleId(rs.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean updateVacancyStatus(int companyRoleId, String vacancyStatus) throws SQLException {
        String sql = "UPDATE company_roles SET vacancy_status = ?, last_verified = CURRENT_DATE WHERE company_role_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vacancyStatus);
            ps.setInt(2, companyRoleId);
            return ps.executeUpdate() > 0;
        }
    }

    private CompanyRole mapCompanyRole(ResultSet rs) throws SQLException {
        CompanyRole cr = new CompanyRole();
        cr.setCompanyRoleId(rs.getInt("company_role_id"));
        cr.setCompanyId(rs.getInt("company_id"));
        cr.setRoleId(rs.getInt("role_id"));
        cr.setCompanyName(rs.getString("company_name"));
        cr.setCompanyIndustry(rs.getString("industry"));
        cr.setCompanyLocation(rs.getString("location"));
        cr.setCompanyWebsite(rs.getString("website"));
        try {
            cr.setCareersUrl(rs.getString("careers_url"));
        } catch (SQLException ignored) {}
        cr.setRoleTitle(rs.getString("role_title"));
        cr.setRoleDescription(rs.getString("role_desc"));
        cr.setExperienceLevel(rs.getString("experience_level"));
        cr.setVacancyStatus(rs.getString("vacancy_status"));
        cr.setVacancySource(rs.getString("vacancy_source"));
        cr.setInformationSource(rs.getString("information_source"));
        cr.setLastVerified(rs.getDate("last_verified"));
        return cr;
    }

    private List<CompanyRole> getFallbackCompanyRoles() {
        List<CompanyRole> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT c.company_id, c.company_name, c.industry, c.location, c.website, " +
                         "       r.role_id, r.role_title, r.description, r.experience_level " +
                         "FROM companies c " +
                         "CROSS JOIN roles r " +
                         "ORDER BY c.company_name ASC, r.role_title ASC";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int dummyId = 1;
                while (rs.next()) {
                    CompanyRole cr = new CompanyRole();
                    cr.setCompanyRoleId(dummyId++);
                    cr.setCompanyId(rs.getInt("company_id"));
                    cr.setRoleId(rs.getInt("role_id"));
                    cr.setCompanyName(rs.getString("company_name"));
                    cr.setCompanyIndustry(rs.getString("industry"));
                    cr.setCompanyLocation(rs.getString("location"));
                    cr.setCompanyWebsite(rs.getString("website"));
                    cr.setRoleTitle(rs.getString("role_title"));
                    cr.setRoleDescription(rs.getString("description"));
                    cr.setExperienceLevel(rs.getString("experience_level"));
                    cr.setVacancyStatus("Potential Role Match");
                    cr.setVacancySource("Company Profile & Role Requirements");
                    cr.setInformationSource("Company Competency Profile");
                    list.add(cr);
                }
            }
        } catch (Exception ignored) {}
        return list;
    }
}
