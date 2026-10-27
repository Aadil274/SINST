package com.sinst.integration;

import com.sinst.db.DBConnection;
import com.sinst.model.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Integration Tests: Database CRUD, Foreign Keys, Transactions & Isolation")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DaoIntegrationTest {

    private static final String TEST_ROLL = "TEST_INTEG_001";
    private static final String TEST_EMAIL = "test_integ_001@test.edu";
    private static boolean dbAvailable = false;
    private static int testStudentId = 0;
    private static int testCompanyId = 1; // Google from seed
    private static int testRoleId = 1;    // Java Backend from seed

    @BeforeAll
    static void checkDatabaseAndSetup() {
        try {
            dbAvailable = DBConnection.testConnection();
        } catch (Throwable t) {
            dbAvailable = false;
        }

        if (dbAvailable) {
            try (Connection conn = DBConnection.getConnection()) {
                // Ensure clean state: delete previous test fixture if lingering
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM students WHERE roll_number = ?")) {
                    del.setString(1, TEST_ROLL);
                    del.executeUpdate();
                }

                // Insert isolated test fixture student
                String insertSql = "INSERT INTO students (roll_number, full_name, email, password, department, year_of_study, phone) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, TEST_ROLL);
                    ps.setString(2, "Integration Test Student");
                    ps.setString(3, TEST_EMAIL);
                    ps.setString(4, "testpass123");
                    ps.setString(5, "CSE");
                    ps.setInt(6, 3);
                    ps.setString(7, "9876543210");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            testStudentId = rs.getInt(1);
                        }
                    }
                }
            } catch (SQLException e) {
                System.err.println("Notice: Could not initialize integration test fixture: " + e.getMessage());
                dbAvailable = false;
            }
        }
    }

    @AfterAll
    static void cleanUpFixture() {
        if (dbAvailable && testStudentId > 0) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
                ps.setInt(1, testStudentId);
                ps.executeUpdate();
            } catch (SQLException e) {
                System.err.println("Warning: Failed to clean up test student fixture: " + e.getMessage());
            }
        }
    }

    @BeforeEach
    void verifyDatabaseAvailable() {
        Assumptions.assumeTrue(dbAvailable && testStudentId > 0,
            "Integration tests skipped: MySQL database server not available or fixture setup failed.");
    }

    @Test
    @Order(1)
    @DisplayName("CRUD: Read and verify isolated test student record")
    void testReadStudent() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE student_id = ?")) {
            ps.setInt(1, testStudentId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(TEST_ROLL, rs.getString("roll_number"));
                assertEquals("Integration Test Student", rs.getString("full_name"));
                assertEquals(TEST_EMAIL, rs.getString("email"));
            }
        }
    }

    @Test
    @Order(2)
    @DisplayName("CRUD & Data Ownership: Insert and read student-scoped application")
    void testCreateAndReadApplication() throws SQLException {
        int createdAppId = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String insertSql = "INSERT INTO internship_applications (student_id, company_id, role_id, application_date, deadline, status, stipend, notes) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, testStudentId);
                ps.setInt(2, testCompanyId);
                ps.setInt(3, testRoleId);
                ps.setDate(4, Date.valueOf("2026-08-01"));
                ps.setDate(5, Date.valueOf("2026-08-30"));
                ps.setString(6, "Applied");
                ps.setString(7, "₹ 30,000 / mo");
                ps.setString(8, "Test application notes");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    assertTrue(rs.next());
                    createdAppId = rs.getInt(1);
                }
            }

            // Verify student ownership query
            String selectSql = "SELECT * FROM internship_applications WHERE application_id = ? AND student_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, createdAppId);
                ps.setInt(2, testStudentId);
                try (ResultSet rs = ps.executeQuery()) {
                    assertTrue(rs.next());
                    assertEquals("Applied", rs.getString("status"));
                    assertEquals(testCompanyId, rs.getInt("company_id"));
                }
            }

            // Verify that a different student ID cannot read/access this application
            try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                ps.setInt(1, createdAppId);
                ps.setInt(2, 999999); // Other student
                try (ResultSet rs = ps.executeQuery()) {
                    assertFalse(rs.next(), "Application must not be accessible to another student ID");
                }
            }
        }
    }

    @Test
    @Order(3)
    @DisplayName("Unique Constraint: Duplicate application for same company and role is rejected")
    void testDuplicateApplicationConstraint() {
        assertThrows(SQLException.class, () -> {
            try (Connection conn = DBConnection.getConnection()) {
                String insertSql = "INSERT INTO internship_applications (student_id, company_id, role_id, application_date, status) " +
                                   "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, testStudentId);
                    ps.setInt(2, testCompanyId);
                    ps.setInt(3, testRoleId);
                    ps.setDate(4, Date.valueOf("2026-08-02"));
                    ps.setString(5, "Applied");
                    ps.executeUpdate();
                }
            }
        }, "Inserting a duplicate application for same student, company, and role must fail with unique constraint");
    }

    @Test
    @Order(4)
    @DisplayName("Foreign Key Constraint: Inserting application with non-existent company is rejected")
    void testForeignKeyConstraint() {
        assertThrows(SQLException.class, () -> {
            try (Connection conn = DBConnection.getConnection()) {
                String insertSql = "INSERT INTO internship_applications (student_id, company_id, role_id, application_date, status) " +
                                   "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, testStudentId);
                    ps.setInt(2, 999999); // Non-existent company
                    ps.setInt(3, testRoleId);
                    ps.setDate(4, Date.valueOf("2026-08-01"));
                    ps.setString(5, "Applied");
                    ps.executeUpdate();
                }
            }
        }, "Foreign key constraint failure expected for invalid company ID");
    }

    @Test
    @Order(5)
    @DisplayName("Transaction Rollback: Uncommitted changes are completely rolled back on error")
    void testTransactionRollback() throws SQLException {
        String testTitle = "Rollback Test Project";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Step 1: Insert valid project
                String insertSql = "INSERT INTO projects (student_id, title, description) VALUES (?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, testStudentId);
                    ps.setString(2, testTitle);
                    ps.setString(3, "Rollback demonstration");
                    ps.executeUpdate();
                }

                // Step 2: Force failure (insert duplicate unique title)
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, testStudentId);
                    ps.setString(2, testTitle);
                    ps.setString(3, "Duplicate trigger");
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
            } finally {
                conn.setAutoCommit(true);
            }
        }

        // Verify that the rollback reverted the insertion of Step 1
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM projects WHERE student_id = ? AND title = ?")) {
            ps.setInt(1, testStudentId);
            ps.setString(2, testTitle);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Project must not exist in database after rollback");
            }
        }
    }

    @Test
    @Order(6)
    @DisplayName("Certification Mapping Persistence: Verified catalog mappings exist and are queryable")
    void testCertificationMappingPersistence() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT certification_name, skill_id, learning_outcome, suggested_proficiency " +
                 "FROM certification_skills WHERE mapping_status = 'Verified'")) {
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    assertNotNull(rs.getString("certification_name"));
                    assertTrue(rs.getInt("skill_id") > 0);
                    assertNotNull(rs.getString("learning_outcome"));
                    assertNotNull(rs.getString("suggested_proficiency"));
                }
                assertTrue(count >= 5, "Verified catalog mappings should contain standard seed certifications");
            }
        }
    }

    @Test
    @Order(7)
    @DisplayName("Recommendation Queries: Explainable role recommendation query returns valid result set")
    void testRecommendationQuery() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT c.company_id, c.company_name, r.role_id, r.role_title, cr.vacancy_status, c.careers_url " +
                 "FROM company_roles cr " +
                 "JOIN companies c ON cr.company_id = c.company_id " +
                 "JOIN roles r ON cr.role_id = r.role_id " +
                 "LIMIT 5")) {
            try (ResultSet rs = ps.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    count++;
                    assertTrue(rs.getInt("company_id") > 0);
                    assertTrue(rs.getInt("role_id") > 0);
                    assertNotNull(rs.getString("company_name"));
                    assertNotNull(rs.getString("role_title"));
                    assertNotNull(rs.getString("vacancy_status"));
                }
                assertTrue(count > 0, "Company roles query must return seeded pairings");
            }
        }
    }
}
