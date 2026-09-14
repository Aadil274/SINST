package com.sinst.db;

import com.sinst.model.*;

import java.sql.*;
import java.util.*;

public class AppDAO {

    // =========================================================================
    // 1. STUDENT AUTHENTICATION & PROFILE
    // =========================================================================

    public static Student login(String identifier, String password) throws SQLException {
        String sql = "SELECT * FROM students WHERE (roll_number = ? OR email = ?) AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, identifier.trim());
            ps.setString(2, identifier.trim());
            ps.setString(3, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudent(rs);
                }
            }
        }
        return null;
    }

    public static boolean registerStudent(Student s) throws SQLException {
        String sql = "INSERT INTO students (roll_number, full_name, email, password, department, year_of_study, phone) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getRollNumber().trim().toUpperCase());
            ps.setString(2, s.getFullName().trim());
            ps.setString(3, s.getEmail().trim().toLowerCase());
            ps.setString(4, s.getPassword());
            ps.setString(5, s.getDepartment() != null ? s.getDepartment() : "CSE");
            ps.setInt(6, s.getYearOfStudy() > 0 ? s.getYearOfStudy() : 2);
            ps.setString(7, s.getPhone());
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

    public static boolean updateStudentProfile(Student s) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, department = ?, year_of_study = ?, phone = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getFullName());
            ps.setString(2, s.getDepartment());
            ps.setInt(3, s.getYearOfStudy());
            ps.setString(4, s.getPhone());
            ps.setInt(5, s.getStudentId());
            return ps.executeUpdate() > 0;
        }
    }

    private static Student extractStudent(ResultSet rs) throws SQLException {
        return new Student(
            rs.getInt("student_id"),
            rs.getString("roll_number"),
            rs.getString("full_name"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("department"),
            rs.getInt("year_of_study"),
            rs.getString("phone")
        );
    }

    // =========================================================================
    // 2. DASHBOARD AGGREGATES & UPCOMING INTERVIEWS
    // =========================================================================

    public static Map<String, Integer> getDashboardCounts(int studentId) throws SQLException {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("Total", 0);
        counts.put("Applied", 0);
        counts.put("Shortlisted", 0);
        counts.put("Interview", 0);
        counts.put("Selected", 0);
        counts.put("Rejected", 0);

        String sql = "SELECT status, COUNT(*) AS cnt FROM internship_applications WHERE student_id = ? GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                int total = 0;
                while (rs.next()) {
                    String status = rs.getString("status");
                    int count = rs.getInt("cnt");
                    counts.put(status, count);
                    total += count;
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

    public static List<Interview> getUpcomingInterviews(int studentId) throws SQLException {
        List<Interview> list = new ArrayList<>();
        String sql = "SELECT i.*, c.company_name, r.role_title " +
                     "FROM interviews i " +
                     "JOIN internship_applications a ON i.application_id = a.application_id " +
                     "JOIN companies c ON a.company_id = c.company_id " +
                     "JOIN roles r ON a.role_id = r.role_id " +
                     "WHERE a.student_id = ? AND i.status = 'Scheduled' AND i.interview_date >= NOW() " +
                     "ORDER BY i.interview_date ASC LIMIT 5";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
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
                    list.add(inv);
                }
            }
        }
        return list;
    }

    // =========================================================================
    // 3. APPLICATIONS MANAGEMENT
    // =========================================================================

    public static List<Application> getApplications(int studentId) throws SQLException {
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
                    Application app = new Application(
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
                    app.setCompanyName(rs.getString("company_name"));
                    app.setRoleTitle(rs.getString("role_title"));
                    list.add(app);
                }
            }
        }
        return list;
    }

    public static boolean addApplication(Application a) throws SQLException {
        String sql = "INSERT INTO internship_applications (student_id, company_id, role_id, application_date, deadline, status, stipend, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getStudentId());
            ps.setInt(2, a.getCompanyId());
            ps.setInt(3, a.getRoleId());
            ps.setDate(4, a.getApplicationDate());
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

    public static boolean updateApplicationStatus(int applicationId, String status) throws SQLException {
        String sql = "UPDATE internship_applications SET status = ? WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteApplication(int applicationId) throws SQLException {
        String sql = "DELETE FROM internship_applications WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 4. INTERVIEWS MANAGEMENT
    // =========================================================================

    public static List<Interview> getInterviews(int studentId) throws SQLException {
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
                    list.add(inv);
                }
            }
        }
        return list;
    }

    public static boolean addInterview(Interview i) throws SQLException {
        String sql = "INSERT INTO interviews (application_id, interview_date, round_type, mode, status, outcome, feedback) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getApplicationId());
            ps.setTimestamp(2, i.getInterviewDate());
            ps.setString(3, i.getRoundType());
            ps.setString(4, i.getMode() != null ? i.getMode() : "Online");
            ps.setString(5, i.getStatus() != null ? i.getStatus() : "Scheduled");
            ps.setString(6, i.getOutcome() != null ? i.getOutcome() : "Pending");
            ps.setString(7, i.getFeedback());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) i.setInterviewId(rs.getInt(1));
                }
                return true;
            }
        }
        return false;
    }

    public static boolean updateInterview(int interviewId, String status, String outcome, String feedback) throws SQLException {
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

    public static boolean deleteInterview(int interviewId) throws SQLException {
        String sql = "DELETE FROM interviews WHERE interview_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, interviewId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 5. SKILLS & PROFILE MANAGEMENT
    // =========================================================================

    public static List<Skill> getAllSkills() throws SQLException {
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

    public static List<Skill> getStudentSkills(int studentId) throws SQLException {
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
                    list.add(new Skill(
                        rs.getInt("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("proficiency_level")
                    ));
                }
            }
        }
        return list;
    }

    public static boolean addOrUpdateStudentSkill(int studentId, int skillId, String proficiency) throws SQLException {
        String sql = "INSERT INTO student_skills (student_id, skill_id, proficiency_level) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE proficiency_level = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            ps.setString(3, proficiency);
            ps.setString(4, proficiency);
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteStudentSkill(int studentId, int skillId) throws SQLException {
        String sql = "DELETE FROM student_skills WHERE student_id = ? AND skill_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, skillId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 6. CERTIFICATIONS & PROJECTS
    // =========================================================================

    public static List<Certification> getCertifications(int studentId) throws SQLException {
        List<Certification> list = new ArrayList<>();
        String sql = "SELECT * FROM certifications WHERE student_id = ? ORDER BY issue_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Certification(
                        rs.getInt("certification_id"),
                        rs.getInt("student_id"),
                        rs.getString("certificate_name"),
                        rs.getString("issuing_organization"),
                        rs.getDate("issue_date"),
                        rs.getString("credential_id")
                    ));
                }
            }
        }
        return list;
    }

    public static boolean addCertification(Certification c) throws SQLException {
        String sql = "INSERT INTO certifications (student_id, certificate_name, issuing_organization, issue_date, credential_id) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, c.getStudentId());
            ps.setString(2, c.getCertificateName());
            ps.setString(3, c.getIssuingOrganization());
            ps.setDate(4, c.getIssueDate());
            ps.setString(5, c.getCredentialId());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteCertification(int certId) throws SQLException {
        String sql = "DELETE FROM certifications WHERE certification_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, certId);
            return ps.executeUpdate() > 0;
        }
    }

    public static List<Project> getProjects(int studentId) throws SQLException {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM projects WHERE student_id = ? ORDER BY project_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Project(
                        rs.getInt("project_id"),
                        rs.getInt("student_id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("technologies_used"),
                        rs.getString("project_url")
                    ));
                }
            }
        }
        return list;
    }

    public static boolean addProject(Project p) throws SQLException {
        String sql = "INSERT INTO projects (student_id, title, description, technologies_used, project_url) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getStudentId());
            ps.setString(2, p.getTitle());
            ps.setString(3, p.getDescription());
            ps.setString(4, p.getTechnologiesUsed());
            ps.setString(5, p.getProjectUrl());
            return ps.executeUpdate() > 0;
        }
    }

    public static boolean deleteProject(int projectId) throws SQLException {
        String sql = "DELETE FROM projects WHERE project_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, projectId);
            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================================
    // 7. ROLES & COMPANIES CATALOG
    // =========================================================================

    public static List<Role> getAllRoles() throws SQLException {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT * FROM roles ORDER BY role_title";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Role(
                    rs.getInt("role_id"),
                    rs.getString("role_title"),
                    rs.getString("description"),
                    rs.getString("experience_level")
                ));
            }
        }
        return list;
    }

    public static List<Company> getAllCompanies() throws SQLException {
        List<Company> list = new ArrayList<>();
        String sql = "SELECT * FROM companies ORDER BY company_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Company(
                    rs.getInt("company_id"),
                    rs.getString("company_name"),
                    rs.getString("industry"),
                    rs.getString("location"),
                    rs.getString("website"),
                    rs.getString("contact_email")
                ));
            }
        }
        return list;
    }

    // =========================================================================
    // 8. DIRECT RELATIONAL SKILL-GAP ANALYSIS (Pure DBMS Comparison)
    // =========================================================================

    public static List<SkillGapResult> getSkillGapAnalysis(int studentId, int roleId) throws SQLException {
        List<SkillGapResult> results = new ArrayList<>();
        
        // This query performs direct relational comparison between role_skills and student_skills
        String sql = "SELECT " +
                     "    s.skill_id, " +
                     "    s.skill_name, " +
                     "    s.category, " +
                     "    rs.required_level, " +
                     "    rs.is_mandatory, " +
                     "    COALESCE(ss.proficiency_level, 'Not Possessed') AS student_level, " +
                     "    CASE " +
                     "        WHEN ss.skill_id IS NULL THEN 'Missing' " +
                     "        ELSE 'Matched' " +
                     "    END AS status " +
                     "FROM role_skills rs " +
                     "JOIN skills s ON rs.skill_id = s.skill_id " +
                     "LEFT JOIN student_skills ss ON rs.skill_id = ss.skill_id AND ss.student_id = ? " +
                     "WHERE rs.role_id = ? " +
                     "ORDER BY status DESC, s.skill_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, roleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(new SkillGapResult(
                        rs.getInt("skill_id"),
                        rs.getString("skill_name"),
                        rs.getString("category"),
                        rs.getString("required_level"),
                        rs.getString("student_level"),
                        rs.getString("status"),
                        rs.getBoolean("is_mandatory")
                    ));
                }
            }
        }
        return results;
    }
}
