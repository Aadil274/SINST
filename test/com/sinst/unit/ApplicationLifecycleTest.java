package com.sinst.unit;

import com.sinst.model.Application;
import com.sinst.model.Interview;
import com.sinst.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Application Lifecycle & Interview Tracking")
public class ApplicationLifecycleTest {

    @Nested
    @DisplayName("Application Model & Status Lifecycle Tests")
    class StatusLifecycleTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "Applied",
            "Under Review",
            "Shortlisted",
            "Interview",
            "Accepted",
            "Selected",
            "Rejected",
            "Withdrawn"
        })
        @DisplayName("Valid application lifecycle states must be accepted")
        void testValidStatuses(String status) {
            assertTrue(ValidationUtil.isValidApplicationStatus(status));

            Application app = new Application(1, 100, 10, 5, Date.valueOf("2026-08-01"), null, status, "15000", "Notes");
            assertEquals(status, app.getStatus());
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Pending",
            "Hired",
            "Draft",
            "RandomState",
            ""
        })
        @DisplayName("Arbitrary or unauthorized status strings must be rejected")
        void testInvalidStatuses(String status) {
            assertFalse(ValidationUtil.isValidApplicationStatus(status));
        }

        @Test
        @DisplayName("Application model equality and hash code contracts")
        void testApplicationEquality() {
            Application a1 = new Application(1, 100, 10, 5, Date.valueOf("2026-08-01"), null, "Applied", "15000", "Notes");
            Application a2 = new Application(1, 100, 10, 5, Date.valueOf("2026-08-01"), null, "Applied", "15000", "Notes");
            Application a3 = new Application(2, 100, 10, 5, Date.valueOf("2026-08-01"), null, "Applied", "15000", "Notes");

            assertEquals(a1, a2);
            assertNotEquals(a1, a3);
            assertEquals(a1.hashCode(), a2.hashCode());
        }
    }

    @Nested
    @DisplayName("Interview Scheduling & Round Tracking Tests")
    class InterviewTrackingTests {

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 5, 10, 20})
        @DisplayName("Interview round numbers between 1 and 20 are valid")
        void testValidRoundNumbers(int round) {
            assertTrue(ValidationUtil.isValidRoundNumber(round));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 21, 50, 100})
        @DisplayName("Interview round numbers outside 1..20 are rejected")
        void testInvalidRoundNumbers(int round) {
            assertFalse(ValidationUtil.isValidRoundNumber(round));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Online", "In-Person", "Video Call", "Telephonic"})
        @DisplayName("Supported interview modes must be accepted")
        void testValidInterviewModes(String mode) {
            assertTrue(ValidationUtil.isValidInterviewMode(mode));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Scheduled", "Completed", "Cancelled", "Rescheduled"})
        @DisplayName("Supported interview statuses must be accepted")
        void testValidInterviewStatuses(String status) {
            assertTrue(ValidationUtil.isValidInterviewStatus(status));
        }

        @Test
        @DisplayName("Interview date preceding application date is rejected")
        void testInterviewChronology() {
            Date appDate = Date.valueOf("2026-08-10");
            Timestamp validInterviewDate = Timestamp.valueOf("2026-08-15 10:00:00");
            Timestamp invalidInterviewDate = Timestamp.valueOf("2026-08-01 10:00:00");

            assertTrue(ValidationUtil.isValidInterviewDate(appDate, validInterviewDate));
            assertFalse(ValidationUtil.isValidInterviewDate(appDate, invalidInterviewDate));
        }

        @Test
        @DisplayName("Interview model construction and property getters")
        void testInterviewModel() {
            Timestamp interviewTime = Timestamp.valueOf("2026-09-01 10:00:00");
            Interview interview = new Interview(
                1, 50, interviewTime, "Technical Round 1",
                "Online", "Scheduled", "Pending", "Review DSA fundamentals"
            );
            interview.setRoundNumber(2);

            assertEquals(1, interview.getInterviewId());
            assertEquals(50, interview.getApplicationId());
            assertEquals(interviewTime, interview.getInterviewDate());
            assertEquals("Technical Round 1", interview.getRoundType());
            assertEquals(2, interview.getRoundNumber());
            assertEquals("Online", interview.getMode());
            assertEquals("Scheduled", interview.getStatus());
            assertEquals("Pending", interview.getOutcome());
            assertEquals("Review DSA fundamentals", interview.getFeedback());
        }
    }
}
