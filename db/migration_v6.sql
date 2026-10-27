-- =========================================================================
-- SINST DATABASE MIGRATION V6: Data Validation, Authorization & Integrity
-- =========================================================================

USE sinst_db;

DELIMITER $$

DROP PROCEDURE IF EXISTS RunMigrationV6 $$
CREATE PROCEDURE RunMigrationV6()
BEGIN
    -- 1. Add 'role' to students table for role-based authorization
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'students' AND COLUMN_NAME = 'role'
    ) THEN
        ALTER TABLE students ADD COLUMN role ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT' AFTER phone;
    END IF;

    -- 2. Add 'expiry_date' and 'credential_url' to certifications table
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'certifications' AND COLUMN_NAME = 'expiry_date'
    ) THEN
        ALTER TABLE certifications ADD COLUMN expiry_date DATE DEFAULT NULL AFTER issue_date;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'certifications' AND COLUMN_NAME = 'credential_url'
    ) THEN
        ALTER TABLE certifications ADD COLUMN credential_url VARCHAR(255) DEFAULT NULL AFTER credential_id;
    END IF;

    -- 3. Add 'github_url' and 'live_url' to projects table
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'projects' AND COLUMN_NAME = 'github_url'
    ) THEN
        ALTER TABLE projects ADD COLUMN github_url VARCHAR(255) DEFAULT NULL AFTER project_url;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'projects' AND COLUMN_NAME = 'live_url'
    ) THEN
        ALTER TABLE projects ADD COLUMN live_url VARCHAR(255) DEFAULT NULL AFTER github_url;
    END IF;

    -- 4. Add 'round_number' to interviews table
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interviews' AND COLUMN_NAME = 'round_number'
    ) THEN
        ALTER TABLE interviews ADD COLUMN round_number INT DEFAULT 1 AFTER round_type;
    END IF;

    -- 5. Expand application statuses enum in internship_applications
    ALTER TABLE internship_applications 
        MODIFY COLUMN status ENUM('Applied', 'Under Review', 'Shortlisted', 'Interview', 'Accepted', 'Selected', 'Rejected', 'Withdrawn') DEFAULT 'Applied';

    -- 6. Add uniqueness constraint on certifications (prevent duplicate submissions by same student)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'certifications' AND CONSTRAINT_NAME = 'uq_student_cert'
    ) THEN
        ALTER TABLE certifications ADD CONSTRAINT uq_student_cert UNIQUE (student_id, certificate_name, issuing_organization);
    END IF;

    -- 7. Add uniqueness constraint on projects (prevent duplicate project title by same student)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'projects' AND CONSTRAINT_NAME = 'uq_student_proj_title'
    ) THEN
        ALTER TABLE projects ADD CONSTRAINT uq_student_proj_title UNIQUE (student_id, title);
    END IF;

    -- 8. Add uniqueness constraint on interviews (prevent duplicate interview scheduling for same round & date)
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.TABLE_CONSTRAINTS 
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'interviews' AND CONSTRAINT_NAME = 'uq_interview_app_round_date'
    ) THEN
        ALTER TABLE interviews ADD CONSTRAINT uq_interview_app_round_date UNIQUE (application_id, round_type, interview_date);
    END IF;

END $$

DELIMITER ;

CALL RunMigrationV6();
DROP PROCEDURE IF EXISTS RunMigrationV6;
