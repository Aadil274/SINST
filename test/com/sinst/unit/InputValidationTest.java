package com.sinst.unit;

import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Date;
import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: Domain Input Validation & Sanitization (ValidationUtil)")
public class InputValidationTest {

    @Nested
    @DisplayName("Email Validation Tests")
    class EmailTests {
        @ParameterizedTest
        @ValueSource(strings = {
            "student@college.edu",
            "first.last@domain.com",
            "user+tag@sub.domain.org",
            "25r11a0501@student.edu"
        })
        @DisplayName("Valid emails should be accepted")
        void testValidEmails(String email) {
            assertTrue(ValidationUtil.isValidEmail(email));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ",
            "notanemail",
            "@domain.com",
            "user@",
            "user@domain",
            "user@domain@another.com",
            "user name@domain.com"
        })
        @DisplayName("Invalid or malformed emails should be rejected")
        void testInvalidEmails(String email) {
            assertFalse(ValidationUtil.isValidEmail(email));
        }
    }

    @Nested
    @DisplayName("Roll Number Validation Tests")
    class RollNumberTests {
        @ParameterizedTest
        @ValueSource(strings = {
            "25R11A0501",
            "CS101",
            "2026ROLL99",
            "A1B2C3D4"
        })
        @DisplayName("Valid alphanumeric roll numbers should be accepted")
        void testValidRollNumbers(String roll) {
            assertTrue(ValidationUtil.isValidRollNumber(roll));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ",
            "A",
            "12",
            "ROLL#123",
            "ROLL_NO_THAT_IS_WAY_TOO_LONG_EXCEEDING_TWENTY_CHARACTERS"
        })
        @DisplayName("Invalid roll numbers (too short, too long, special chars) should be rejected")
        void testInvalidRollNumbers(String roll) {
            assertFalse(ValidationUtil.isValidRollNumber(roll));
        }
    }

    @Nested
    @DisplayName("Student Name Validation Tests")
    class NameTests {
        @ParameterizedTest
        @ValueSource(strings = {
            "Aadil Ahmed Shaik",
            "Mary-Jane Watson",
            "Dr. John Doe",
            "Albus Percival Wulfric Brian Dumbledore"
        })
        @DisplayName("Standard names with letters, hyphens, and dots should be accepted")
        void testValidNames(String name) {
            assertTrue(ValidationUtil.isValidName(name));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            " ",
            "A",
            "John Doe 2nd",
            "User@Name",
            "Name<script>"
        })
        @DisplayName("Names with digits, illegal symbols, or length < 2 should be rejected")
        void testInvalidNames(String name) {
            assertFalse(ValidationUtil.isValidName(name));
        }
    }

    @Nested
    @DisplayName("Year of Study Bounds Tests")
    class YearOfStudyTests {
        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4})
        @DisplayName("Years 1 through 4 should be valid undergraduate years")
        void testValidYears(int year) {
            assertTrue(ValidationUtil.isValidYearOfStudy(year));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 5, 6, 10})
        @DisplayName("Years outside 1..4 should be rejected")
        void testInvalidYears(int year) {
            assertFalse(ValidationUtil.isValidYearOfStudy(year));
        }
    }

    @Nested
    @DisplayName("Phone Number Validation Tests")
    class PhoneTests {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "6303279385", "+916303279385", "+18005550199"})
        @DisplayName("Null, empty (optional) or valid phone numbers should be accepted")
        void testValidPhones(String phone) {
            assertTrue(ValidationUtil.isValidPhone(phone));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "phone12345", "6303279385999999999"})
        @DisplayName("Malformed or out-of-range phone numbers should be rejected")
        void testInvalidPhones(String phone) {
            assertFalse(ValidationUtil.isValidPhone(phone));
        }
    }

    @Nested
    @DisplayName("Calendar Date & Timestamp Tests")
    class DateTimeTests {
        @Test
        @DisplayName("Valid calendar date and leap day should be accepted")
        void testValidDates() {
            assertTrue(ValidationUtil.isValidDate("2026-10-27"));
            assertTrue(ValidationUtil.isValidDate("2024-02-29")); // Leap year
            Date parsed = ValidationUtil.parseDate("2026-10-27");
            assertNotNull(parsed);
            assertEquals("2026-10-27", parsed.toString());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "2023-02-29", // Non-leap year
            "2026-02-30", // Invalid day
            "2026-13-01", // Invalid month
            "10/27/2026", // Incorrect delimiter
            "not-a-date"
        })
        @DisplayName("Invalid or non-existent dates should be rejected")
        void testInvalidDates(String dateStr) {
            assertFalse(ValidationUtil.isValidDate(dateStr));
            assertNull(ValidationUtil.parseDate(dateStr));
        }

        @Test
        @DisplayName("Valid timestamp should be parsed successfully")
        void testTimestamps() {
            assertTrue(ValidationUtil.isValidTimestamp("2026-10-27 14:30:00"));
            assertTrue(ValidationUtil.isValidTimestamp("2026-10-27 14:30"));
            Timestamp ts = ValidationUtil.parseTimestamp("2026-10-27 14:30:00");
            assertNotNull(ts);

            assertFalse(ValidationUtil.isValidTimestamp("2026-10-27 25:00:00"));
            assertFalse(ValidationUtil.isValidTimestamp("2026-10-27 14:61:00"));
            assertNull(ValidationUtil.parseTimestamp("invalid-timestamp"));
        }

        @Test
        @DisplayName("Date range ordering constraints should be enforced")
        void testDateRanges() {
            Date d1 = ValidationUtil.parseDate("2026-08-01");
            Date d2 = ValidationUtil.parseDate("2026-08-15");
            Date d3 = ValidationUtil.parseDate("2026-07-20");

            assertTrue(ValidationUtil.isValidDateRange(d1, d2));
            assertTrue(ValidationUtil.isValidDateRange(d1, d1)); // equal is valid
            assertFalse(ValidationUtil.isValidDateRange(d2, d1)); // end before start

            Timestamp t2 = new Timestamp(d2.getTime());
            Timestamp t3 = new Timestamp(d3.getTime());
            assertTrue(ValidationUtil.isValidInterviewDate(d1, t2));
            assertFalse(ValidationUtil.isValidInterviewDate(d1, t3)); // interview before application

            assertTrue(ValidationUtil.isValidCertificationDates(d1, d2));
            assertTrue(ValidationUtil.isValidCertificationDates(d1, null)); // non-expiring is valid
            assertFalse(ValidationUtil.isValidCertificationDates(d2, d1)); // expiry before issue
        }
    }

    @Nested
    @DisplayName("Financial Stipend Tests")
    class StipendTests {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "₹ 45,000 / mo",
            "45000",
            "Unpaid",
            "0"
        })
        @DisplayName("Positive or zero stipends should have no negative amounts")
        void testValidStipends(String stipend) {
            assertFalse(ValidationUtil.hasNegativeStipend(stipend));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "-5000",
            "₹ -25,000 / mo",
            "- 1000",
            "Stipend: -100"
        })
        @DisplayName("Negative stipend expressions must be detected and flagged")
        void testNegativeStipends(String stipend) {
            assertTrue(ValidationUtil.hasNegativeStipend(stipend));
        }

        @Test
        @DisplayName("Direct numeric stipend validation")
        void testNumericStipend() {
            assertTrue(ValidationUtil.isValidNumericStipend(0.0));
            assertTrue(ValidationUtil.isValidNumericStipend(50000.0));
            assertFalse(ValidationUtil.isValidNumericStipend(-1.0));
        }
    }

    @Nested
    @DisplayName("URL Syntax Validation Tests")
    class UrlTests {
        @ParameterizedTest
        @ValueSource(strings = {
            "https://github.com/Aadil274/SINST",
            "http://example.com/project",
            "https://careers.google.com/jobs/results/"
        })
        @DisplayName("Valid HTTP and HTTPS URLs must be accepted")
        void testValidUrls(String url) {
            assertTrue(ValidationUtil.isValidUrl(url));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Null or empty optional URLs should return true (optional field)")
        void testEmptyOptionalUrl(String url) {
            assertTrue(ValidationUtil.isValidUrl(url));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ftp://files.example.com",
            "javascript:alert(1)",
            "file:///C:/test.txt",
            "htp://bad-scheme.com",
            "notaurl"
        })
        @DisplayName("Non-HTTP schemes and arbitrary strings must be rejected")
        void testInvalidUrls(String url) {
            assertFalse(ValidationUtil.isValidUrl(url));
        }
    }

    @Nested
    @DisplayName("Error Sanitization Tests")
    class ErrorSanitizationTests {
        @Test
        @DisplayName("Technical exceptions must be converted to user-friendly messages")
        void testTechnicalExceptions() {
            String commMsg = ValidationUtil.sanitizeErrorMessage(
                new RuntimeException("Communications link failure to localhost:3306"));
            assertTrue(commMsg.contains("Unable to connect to MySQL database"));

            String connMsg = ValidationUtil.sanitizeErrorMessage(
                new RuntimeException("Connection refused: connect"));
            assertTrue(connMsg.contains("Unable to connect to MySQL database"));

            String dupMsg = ValidationUtil.sanitizeErrorMessage(
                new RuntimeException("Duplicate entry '25R11A0501' for key 'students.roll_number'"));
            assertTrue(dupMsg.contains("already exists"));

            String fkMsg = ValidationUtil.sanitizeErrorMessage(
                new RuntimeException("Cannot delete or update a parent row: a foreign key constraint fails"));
            assertTrue(fkMsg.contains("Related records depend"));

            String truncMsg = ValidationUtil.sanitizeErrorMessage(
                new RuntimeException("Data truncation: Data too long for column 'phone'"));
            assertTrue(truncMsg.contains("exceeds the maximum permitted length"));

            String tableMsg = ValidationUtil.sanitizeErrorMessage(
                new RuntimeException("Table 'sinst_db.company_roles' doesn't exist"));
            assertTrue(tableMsg.contains("migration scripts"));
        }

        @Test
        @DisplayName("Domain validation and security exceptions preserve their exact user message")
        void testDomainExceptionsPreserved() {
            ValidationException ve = new ValidationException("Custom validation failure");
            assertEquals("Custom validation failure", ValidationUtil.sanitizeErrorMessage(ve));

            DuplicateEntityException de = new DuplicateEntityException("Duplicate record error");
            assertEquals("Duplicate record error", ValidationUtil.sanitizeErrorMessage(de));

            UnauthorizedAccessException ue = new UnauthorizedAccessException("Access denied to record");
            assertEquals("Access denied to record", ValidationUtil.sanitizeErrorMessage(ue));

            assertEquals("An unexpected error occurred. Please try again.", ValidationUtil.sanitizeErrorMessage(null));
        }
    }
}
