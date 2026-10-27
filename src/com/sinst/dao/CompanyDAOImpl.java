package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Company;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyDAOImpl implements CompanyDAO {

    private static final CompanyDAOImpl INSTANCE = new CompanyDAOImpl();

    public static CompanyDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Company> getAll() throws SQLException {
        List<Company> list = new ArrayList<>();
        String sql = "SELECT * FROM companies ORDER BY company_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractCompany(rs));
            }
        }
        return list;
    }

    @Override
    public Company getById(int companyId) throws SQLException {
        String sql = "SELECT * FROM companies WHERE company_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCompany(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean save(Company company) throws SQLException {
        String sql = company.getCompanyId() == 0
                ? "INSERT INTO companies (company_name, industry, location, website, contact_email) VALUES (?, ?, ?, ?, ?)"
                : "UPDATE companies SET company_name = ?, industry = ?, location = ?, website = ?, contact_email = ? WHERE company_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, company.getCompanyName().trim());
            ps.setString(2, company.getIndustry());
            ps.setString(3, company.getLocation());
            ps.setString(4, company.getWebsite());
            ps.setString(5, company.getContactEmail());
            if (company.getCompanyId() != 0) {
                ps.setInt(6, company.getCompanyId());
            }
            int affected = ps.executeUpdate();
            if (affected > 0 && company.getCompanyId() == 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        company.setCompanyId(keys.getInt(1));
                    }
                }
            }
            return affected > 0;
        }
    }

    @Override
    public boolean delete(int companyId) throws SQLException {
        String sql = "DELETE FROM companies WHERE company_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            return ps.executeUpdate() > 0;
        }
    }

    private Company extractCompany(ResultSet rs) throws SQLException {
        String careersUrl = null;
        try {
            careersUrl = rs.getString("careers_url");
        } catch (SQLException ignored) {}

        return new Company(
            rs.getInt("company_id"),
            rs.getString("company_name"),
            rs.getString("industry"),
            rs.getString("location"),
            rs.getString("website"),
            careersUrl,
            rs.getString("contact_email")
        );
    }
}
