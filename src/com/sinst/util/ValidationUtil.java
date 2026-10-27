package com.sinst.util;

import com.sinst.model.ProficiencyLevel;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern ROLL_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,20}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s.\\-']{2,100}$");
    private static final Pattern NEGATIVE_NUMBER_PATTERN = Pattern.compile("-\\s*[^0-9a-zA-Z\\s]*\\s*\\d+");

    private static final Pattern URL_PATTERN = Pattern.compile(
        "^https?://[a-zA-Z0-9\\-]+(?:\\.[a-zA-Z0-9\\-]+)*(?::\\d+)?(?:/.*)?$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Set<String> VALID_APPLICATION_STATUSES = new HashSet<>(Arrays.asList(
        "APPLIED", "UNDER REVIEW", "SHORTLISTED", "INTERVIEW", "ACCEPTED", "SELECTED", "REJECTED", "WITHDRAWN"
    ));

    private static final Set<String> VALID_INTERVIEW_MODES = new HashSet<>(Arrays.asList(
        "ONLINE", "IN-PERSON", "VIDEO CALL", "TELEPHONIC"
    ));

    private static final Set<String> VALID_INTERVIEW_STATUSES = new HashSet<>(Arrays.asList(
        "SCHEDULED", "COMPLETED", "CANCELLED", "RESCHEDULED"
    ));

    private static final Set<String> VALID_INTERVIEW_OUTCOMES = new HashSet<>(Arrays.asList(
        "PENDING", "PASSED", "FAILED"
    ));

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        String trimmed = email.trim();
        return !trimmed.isEmpty() && EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidRollNumber(String roll) {
        if (roll == null) return false;
        String trimmed = roll.trim();
        return !trimmed.isEmpty() && ROLL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidName(String name) {
        if (name == null) return false;
        String trimmed = name.trim();
        return !trimmed.isEmpty() && NAME_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidYearOfStudy(int year) {
        return year >= 1 && year <= 4;
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return true; // Phone is optional
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            java.util.Date parsed = sdf.parse(dateStr.trim());
            return parsed != null;
        } catch (ParseException e) {
            return false;
        }
    }

    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            java.util.Date parsed = sdf.parse(dateStr.trim());
            return new Date(parsed.getTime());
        } catch (ParseException e) {
            return null;
        }
    }

    public static boolean isValidTimestamp(String tsStr) {
        if (tsStr == null || tsStr.trim().isEmpty()) return false;
        String trimmed = tsStr.trim();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf1.setLenient(false);
        try {
            sdf1.parse(trimmed);
            return true;
        } catch (ParseException e) {
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf2.setLenient(false);
            try {
                sdf2.parse(trimmed);
                return true;
            } catch (ParseException ex) {
                return false;
            }
        }
    }

    public static Timestamp parseTimestamp(String tsStr) {
        if (tsStr == null || tsStr.trim().isEmpty()) return null;
        String trimmed = tsStr.trim();
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf1.setLenient(false);
        try {
            java.util.Date d = sdf1.parse(trimmed);
            return new Timestamp(d.getTime());
        } catch (ParseException e) {
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf2.setLenient(false);
            try {
                java.util.Date d = sdf2.parse(trimmed);
                return new Timestamp(d.getTime());
            } catch (ParseException ex) {
                return null;
            }
        }
    }

    public static String formatDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String formatDisplayDate(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("dd-MMM-yyyy").format(date);
    }

    public static String formatDisplayDateTime(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(date);
    }

    public static String formatDateTime(java.util.Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    public static String currentDateString() {
        return formatDate(new java.util.Date());
    }

    public static String futureDateString(long daysAhead) {
        return formatDate(new java.util.Date(System.currentTimeMillis() + 86400000L * daysAhead));
    }

    public static String futureDateTimeString(long daysAhead) {
        return formatDateTime(new java.util.Date(System.currentTimeMillis() + 86400000L * daysAhead));
    }

    public static boolean hasNegativeStipend(String stipend) {
        if (stipend == null || stipend.trim().isEmpty()) return false;
        return NEGATIVE_NUMBER_PATTERN.matcher(stipend).find();
    }

    public static boolean isNonEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static String clean(String text) {
        return text == null ? "" : text.trim();
    }

    // =========================================================================
    // NEW RELIABILITY & INPUT VALIDATION UTILITIES (PROMPT 6)
    // =========================================================================

    /**
     * Validates optional URLs (returns true if null/blank, false if invalid syntax).
     * Requires http:// or https:// followed by valid domain and path syntax.
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) return true; // Optional URL field
        String trimmed = url.trim();
        return URL_PATTERN.matcher(trimmed).matches();
    }

    /**
     * Validates that start date is on or before end date.
     */
    public static boolean isValidDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) return true;
        return !startDate.after(endDate);
    }

    /**
     * Validates that start date string is on or before end date string.
     */
    public static boolean isValidDateRange(String startStr, String endStr) {
        if (!isNonEmpty(startStr) || !isNonEmpty(endStr)) return true;
        Date start = parseDate(startStr);
        Date end = parseDate(endStr);
        if (start == null || end == null) return false;
        return !start.after(end);
    }

    /**
     * Validates that an interview is scheduled on or after the application date.
     */
    public static boolean isValidInterviewDate(Date applicationDate, Timestamp interviewDate) {
        if (applicationDate == null || interviewDate == null) return true;
        // Application date at 00:00:00
        long appTime = applicationDate.getTime();
        long interviewTime = interviewDate.getTime();
        return interviewTime >= appTime;
    }

    /**
     * Validates certification dates: issue date must be on or before expiry date (if provided).
     */
    public static boolean isValidCertificationDates(Date issueDate, Date expiryDate) {
        if (issueDate == null) return false;
        if (expiryDate == null) return true; // Many certifications do not expire
        return !issueDate.after(expiryDate);
    }

    /**
     * Validates that proficiency is one of the recognized domain levels.
     */
    public static boolean isValidProficiency(String proficiency) {
        if (proficiency == null || proficiency.trim().isEmpty()) return false;
        ProficiencyLevel level = ProficiencyLevel.fromString(proficiency);
        return level != ProficiencyLevel.NONE;
    }

    /**
     * Validates that application status is one of the approved workflow statuses.
     */
    public static boolean isValidApplicationStatus(String status) {
        if (status == null || status.trim().isEmpty()) return false;
        return VALID_APPLICATION_STATUSES.contains(status.trim().toUpperCase());
    }

    /**
     * Validates interview mode.
     */
    public static boolean isValidInterviewMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) return false;
        return VALID_INTERVIEW_MODES.contains(mode.trim().toUpperCase());
    }

    /**
     * Validates interview status.
     */
    public static boolean isValidInterviewStatus(String status) {
        if (status == null || status.trim().isEmpty()) return false;
        return VALID_INTERVIEW_STATUSES.contains(status.trim().toUpperCase());
    }

    /**
     * Validates interview outcome.
     */
    public static boolean isValidInterviewOutcome(String outcome) {
        if (outcome == null || outcome.trim().isEmpty()) return false;
        return VALID_INTERVIEW_OUTCOMES.contains(outcome.trim().toUpperCase());
    }

    /**
     * Validates interview round number.
     */
    public static boolean isValidRoundNumber(int roundNumber) {
        return roundNumber >= 1 && roundNumber <= 20;
    }

    /**
     * Validates that a numeric stipend is non-negative.
     */
    public static boolean isValidNumericStipend(double amount) {
        return amount >= 0.0;
    }

    /**
     * Sanitizes database and technical exceptions into clear, actionable, user-friendly messages.
     * Prevents internal table schemas, connection strings, or stack traces from leaking to the user.
     */
    public static String sanitizeErrorMessage(Throwable t) {
        if (t == null) return "An unexpected error occurred. Please try again.";
        String msg = t.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            return "An unexpected operation failure occurred.";
        }

        String lower = msg.toLowerCase();
        if (lower.contains("communications link failure") || lower.contains("connection refused") || lower.contains("connection reset")) {
            return "Unable to connect to MySQL database. Please verify that your database service is running.";
        }
        if (lower.contains("access denied for user")) {
            return "Database authentication failed. Please check your database credentials in db.properties.";
        }
        if (lower.contains("duplicate entry") || lower.contains("unique constraint") || lower.contains("uq_")) {
            return "A duplicate record already exists with these details.";
        }
        if (lower.contains("foreign key constraint fails") || lower.contains("a foreign key constraint")) {
            return "Cannot complete operation: Related records depend on this item.";
        }
        if (lower.contains("data truncation") || lower.contains("data too long")) {
            return "One of the submitted values exceeds the maximum permitted length.";
        }
        if (lower.contains("table") && lower.contains("doesn't exist")) {
            return "Database schema is missing required tables. Please run the SQL migration scripts.";
        }

        // Strip raw SQL query strings or class prefixes if present
        if (msg.contains("SQLException:")) {
            msg = msg.substring(msg.indexOf("SQLException:") + "SQLException:".length()).trim();
        }
        return msg;
    }
}
