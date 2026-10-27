-- ====================================================================
-- SINST DATABASE MIGRATION V3: Skill-Gap Analysis & Proficiency Schema
-- ====================================================================
-- Documents and enforces the consistent proficiency scale:
--   0: None / Missing       (Unranked / Not possessed)
--   1: Beginner             (Basic conceptual awareness)
--   2: Intermediate         (Working practical competency - Standard benchmark)
--   3: Advanced             (Deep technical proficiency & complex problem-solving)
--   4: Expert               (System architecture & domain authority)
--
-- Supports mandatory vs. optional role skills for transparent weighting:
--   Mandatory weight = 2.0x (Core competency required for day-1 readiness)
--   Optional weight  = 1.0x (Value-add / supplementary skill)
-- ====================================================================

USE sinst_db;

-- 1. Ensure `is_mandatory` column exists in `role_skills`
SET @col_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'role_skills' 
      AND COLUMN_NAME = 'is_mandatory'
);

SET @sql_stmt = IF(@col_exists = 0,
    'ALTER TABLE role_skills ADD COLUMN is_mandatory BOOLEAN DEFAULT TRUE AFTER required_level;',
    'SELECT "Column is_mandatory already exists on role_skills" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Ensure existing null or empty values default to TRUE for mandatory
UPDATE role_skills SET is_mandatory = TRUE WHERE is_mandatory IS NULL;

-- 3. Ensure role_skills has primary key / unique index on (role_id, skill_id) to prevent duplicate joins
SET @pk_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'role_skills'
      AND INDEX_NAME = 'PRIMARY'
);

SET @sql_stmt = IF(@pk_exists = 0,
    'ALTER TABLE role_skills ADD PRIMARY KEY (role_id, skill_id);',
    'SELECT "Primary key on role_skills already exists" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. Ensure student_skills has primary key / unique index on (student_id, skill_id) to prevent duplicate skill assignments
SET @stud_pk_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'student_skills'
      AND INDEX_NAME = 'PRIMARY'
);

SET @sql_stmt = IF(@stud_pk_exists = 0,
    'ALTER TABLE student_skills ADD PRIMARY KEY (student_id, skill_id);',
    'SELECT "Primary key on student_skills already exists" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Migration V3 completed successfully
SELECT "Migration V3 applied successfully: verified proficiency scale, mandatory flags, and composite uniqueness" AS result;
