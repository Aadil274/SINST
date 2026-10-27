package com.sinst.dao;

import com.sinst.db.DBConnection;
import com.sinst.model.Certification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CertificationDAOImpl implements CertificationDAO {

    private static final CertificationDAOImpl INSTANCE = new CertificationDAOImpl();

    public static CertificationDAOImpl getInstance() {
        return INSTANCE;
    }

    @Override
    public List<Certification> getByStudentId(int studentId) throws SQLException {
        List<Certification> list = new ArrayList<>();
        String sql = "SELECT * FROM certifications WHERE student_id = ? ORDER BY issue_date DESC, certification_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractCertification(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Certification getById(int certId) throws SQLException {
        String sql = "SELECT * FROM certifications WHERE certification_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCertification(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Certification getByIdAndStudentId(int certId, int studentId) throws SQLException {
        String sql = "SELECT * FROM certifications WHERE certification_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);
            ps.setInt(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCertification(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean hasCertification(int studentId, String certName, String issuingOrg) throws SQLException {
        String sql = "SELECT COUNT(*) FROM certifications WHERE student_id = ? AND LOWER(certificate_name) = LOWER(?) AND LOWER(issuing_organization) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, certName != null ? certName.trim() : "");
            ps.setString(3, issuingOrg != null ? issuingOrg.trim() : "");
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean add(Certification c) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return add(c, conn);
        }
    }

    @Override
    public boolean add(Certification c, Connection conn) throws SQLException {
        String sql = "INSERT INTO certifications (student_id, certificate_name, issuing_organization, issue_date, expiry_date, credential_id, credential_url) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getStudentId());
            ps.setString(2, c.getCertificateName().trim());
            ps.setString(3, c.getIssuingOrganization() != null ? c.getIssuingOrganization().trim() : "");
            ps.setDate(4, c.getIssueDate());
            ps.setDate(5, c.getExpiryDate());
            ps.setString(6, c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty() ? c.getCredentialId().trim() : null);
            ps.setString(7, c.getCredentialUrl() != null && !c.getCredentialUrl().trim().isEmpty() ? c.getCredentialUrl().trim() : null);
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) c.setCertificationId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException ex) {
            // Fallback for older schemas without expiry_date/credential_url
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unknown column")) {
                String legacySql = "INSERT INTO certifications (student_id, certificate_name, issuing_organization, issue_date, credential_id) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps2 = conn.prepareStatement(legacySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps2.setInt(1, c.getStudentId());
                    ps2.setString(2, c.getCertificateName().trim());
                    ps2.setString(3, c.getIssuingOrganization() != null ? c.getIssuingOrganization().trim() : "");
                    ps2.setDate(4, c.getIssueDate());
                    ps2.setString(5, c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty() ? c.getCredentialId().trim() : null);
                    int affected = ps2.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rs = ps2.getGeneratedKeys()) {
                            if (rs.next()) c.setCertificationId(rs.getInt(1));
                        }
                        return true;
                    }
                }
            }
            throw ex;
        }
    }

    @Override
    public boolean update(Certification c) throws SQLException {
        return update(c, c.getStudentId());
    }

    @Override
    public boolean update(Certification c, int studentId) throws SQLException {
        String sql = "UPDATE certifications SET certificate_name = ?, issuing_organization = ?, issue_date = ?, expiry_date = ?, credential_id = ?, credential_url = ? " +
                     "WHERE certification_id = ? AND student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getCertificateName().trim());
            ps.setString(2, c.getIssuingOrganization() != null ? c.getIssuingOrganization().trim() : "");
            ps.setDate(3, c.getIssueDate());
            ps.setDate(4, c.getExpiryDate());
            ps.setString(5, c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty() ? c.getCredentialId().trim() : null);
            ps.setString(6, c.getCredentialUrl() != null && !c.getCredentialUrl().trim().isEmpty() ? c.getCredentialUrl().trim() : null);
            ps.setInt(7, c.getCertificationId());
            ps.setInt(8, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("unknown column")) {
                String legacySql = "UPDATE certifications SET certificate_name = ?, issuing_organization = ?, issue_date = ?, credential_id = ? WHERE certification_id = ? AND student_id = ?";
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps2 = conn.prepareStatement(legacySql)) {
                    ps2.setString(1, c.getCertificateName().trim());
                    ps2.setString(2, c.getIssuingOrganization() != null ? c.getIssuingOrganization().trim() : "");
                    ps2.setDate(3, c.getIssueDate());
                    ps2.setString(4, c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty() ? c.getCredentialId().trim() : null);
                    ps2.setInt(5, c.getCertificationId());
                    ps2.setInt(6, studentId);
                    return ps2.executeUpdate() > 0;
                }
            }
            throw ex;
        }
    }

    @Override
    public boolean delete(int certificationId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return delete(certificationId, conn);
        }
    }

    @Override
    public boolean delete(int certificationId, Connection conn) throws SQLException {
        String sql = "DELETE FROM certifications WHERE certification_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certificationId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int certificationId, int studentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return delete(certificationId, studentId, conn);
        }
    }

    @Override
    public boolean delete(int certificationId, int studentId, Connection conn) throws SQLException {
        String sql = "DELETE FROM certifications WHERE certification_id = ? AND student_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certificationId);
            ps.setInt(2, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Certification extractCertification(ResultSet rs) throws SQLException {
        Date expiryDate = null;
        String credUrl = null;
        try {
            expiryDate = rs.getDate("expiry_date");
            credUrl = rs.getString("credential_url");
        } catch (SQLException ignored) {}

        return new Certification(
            rs.getInt("certification_id"),
            rs.getInt("student_id"),
            rs.getString("certificate_name"),
            rs.getString("issuing_organization"),
            rs.getDate("issue_date"),
            expiryDate,
            rs.getString("credential_id"),
            credUrl
        );
    }
}
