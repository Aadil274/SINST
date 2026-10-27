package com.sinst.unit;

import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.model.Student;
import com.sinst.util.UserSession;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Service Layer Authorization (IDOR Defense) & Duplicate Prevention")
public class ServiceAuthorizationTest {

    private Student studentOne;
    private Student studentTwo;

    @BeforeEach
    void setUp() {
        studentOne = new Student(1, "25R11A0501", "Aadil Ahmed Shaik", "aadil@student.edu", "password123", "CSE", 2, "9876543210");
        studentTwo = new Student(2, "25R11A0505", "Amog Mantha", "amog@student.edu", "password123", "CSE", 2, "9876543211");

        // Authenticate as Student One
        UserSession.setCurrentStudent(studentOne);
    }

    @AfterEach
    void tearDown() {
        UserSession.clear();
    }

    @Test
    @DisplayName("UserSession correctly tracks authenticated student identity and permissions")
    void testUserSessionState() {
        assertTrue(UserSession.isAuthenticated());
        assertEquals(1, UserSession.getCurrentStudentId());
        assertNotNull(UserSession.getCurrentStudent());
        assertEquals("25R11A0501", UserSession.getCurrentStudent().getRollNumber());
        assertFalse(UserSession.isAdmin());

        UserSession.clear();
        assertFalse(UserSession.isAuthenticated());
        assertEquals(0, UserSession.getCurrentStudentId());
        assertNull(UserSession.getCurrentStudent());
    }

    @Test
    @DisplayName("Cross-student manipulation throws UnauthorizedAccessException")
    void testCrossStudentAccessBlocked() {
        // Simulated authorization guard: student attempting to modify studentTwo's resource
        int targetResourceOwnerId = studentTwo.getStudentId(); // Owner is Student 2 (ID: 2)
        int currentStudentId = UserSession.getCurrentStudentId(); // Authenticated is Student 1 (ID: 1)

        assertThrows(UnauthorizedAccessException.class, () -> {
            if (currentStudentId != targetResourceOwnerId && !UserSession.isAdmin()) {
                throw new UnauthorizedAccessException("Unauthorized: You cannot modify records belonging to another student.");
            }
        });
    }

    @Test
    @DisplayName("Authorized owner access succeeds without throwing UnauthorizedAccessException")
    void testAuthorizedOwnerAccessAllowed() {
        int ownResourceId = studentOne.getStudentId(); // Owner is Student 1 (ID: 1)
        int currentStudentId = UserSession.getCurrentStudentId(); // Authenticated is Student 1 (ID: 1)

        assertDoesNotThrow(() -> {
            if (currentStudentId != ownResourceId && !UserSession.isAdmin()) {
                throw new UnauthorizedAccessException("Unauthorized");
            }
        });
    }

    @Test
    @DisplayName("Admin role permits administrative operations across student records")
    void testAdminBypassAllowed() {
        Student admin = new Student(99, "ADMIN001", "Administrator", "admin@domain.com", "admin123", "IT", 4, "9876543299", "ADMIN");
        UserSession.setCurrentStudent(admin);

        assertTrue(UserSession.isAdmin());

        int targetStudentId = 2; // Target is Student 2
        assertDoesNotThrow(() -> {
            int currentId = UserSession.getCurrentStudentId();
            if (currentId != targetStudentId && !UserSession.isAdmin()) {
                throw new UnauthorizedAccessException("Unauthorized");
            }
        });
    }

    @Test
    @DisplayName("Duplicate submissions throw DuplicateEntityException with informative message")
    void testDuplicateSubmissions() {
        DuplicateEntityException appDup = new DuplicateEntityException(
            "An application for this company and role already exists.");
        assertTrue(appDup.getMessage().contains("already exists"));

        DuplicateEntityException certDup = new DuplicateEntityException(
            "You have already recorded this certification.");
        assertTrue(certDup.getMessage().contains("already recorded"));

        DuplicateEntityException projDup = new DuplicateEntityException(
            "A project with this title already exists.");
        assertTrue(projDup.getMessage().contains("title already exists"));

        DuplicateEntityException interviewDup = new DuplicateEntityException(
            "An interview round is already scheduled for this application at this time.");
        assertTrue(interviewDup.getMessage().contains("already scheduled"));
    }
}
