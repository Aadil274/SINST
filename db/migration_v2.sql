-- ====================================================================
-- SINST DATABASE MIGRATION V2: Schema Integrity & Constraints
-- ====================================================================

USE sinst_db;

-- 1. Prevent duplicate applications by the same student for the same company and role
-- Check and add unique constraint on (student_id, company_id, role_id)
SET @constraint_exists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'internship_applications' 
      AND CONSTRAINT_NAME = 'uq_student_company_role'
);

SET @sql_stmt = IF(@constraint_exists = 0,
    'ALTER TABLE internship_applications ADD CONSTRAINT uq_student_company_role UNIQUE (student_id, company_id, role_id);',
    'SELECT "Constraint uq_student_company_role already exists" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Verify all foreign key integrity
-- Ensure interviews cascade when application is deleted
-- Ensure role_skills cascade when role or skill is deleted
-- Ensure student_skills cascade when student or skill is deleted
-- Ensure companies and roles are RESTRICTED from deletion if applications reference them
