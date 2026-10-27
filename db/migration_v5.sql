-- ====================================================================
-- SINST DATABASE MIGRATION V5: Company & Internship Role Recommendations
-- ====================================================================
-- Extends the database schema to support explainable, data-driven
-- company and internship role recommendations based on verified skills,
-- competencies, proficiency levels, and vacancy metadata.
--
-- Preserves existing tables, constraints, and data compatibility.
-- ====================================================================

USE sinst_db;

-- 1. Add `careers_url` to `companies` if not already present
SET @col_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'companies' 
      AND COLUMN_NAME = 'careers_url'
);

SET @sql_stmt = IF(@col_exists = 0,
    'ALTER TABLE companies ADD COLUMN careers_url VARCHAR(255) DEFAULT NULL AFTER website;',
    'SELECT "Column careers_url already exists on companies" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Populate verified company careers portal URLs
UPDATE companies SET careers_url = 'https://careers.google.com' WHERE company_id = 1;
UPDATE companies SET careers_url = 'https://careers.microsoft.com' WHERE company_id = 2;
UPDATE companies SET careers_url = 'https://amazon.jobs' WHERE company_id = 3;
UPDATE companies SET careers_url = 'https://infosys.com/careers' WHERE company_id = 4;
UPDATE companies SET careers_url = 'https://tcs.com/careers' WHERE company_id = 5;
UPDATE companies SET careers_url = 'https://careers.wipro.com' WHERE company_id = 6;


-- 2. Add `information_source` and `last_updated` to `roles` if not present
SET @col_src_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'roles' 
      AND COLUMN_NAME = 'information_source'
);

SET @sql_stmt = IF(@col_src_exists = 0,
    'ALTER TABLE roles ADD COLUMN information_source VARCHAR(150) DEFAULT "Industry Standard Competency Framework" AFTER experience_level;',
    'SELECT "Column information_source already exists on roles" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_date_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'roles' 
      AND COLUMN_NAME = 'last_updated'
);

SET @sql_stmt = IF(@col_date_exists = 0,
    'ALTER TABLE roles ADD COLUMN last_updated DATE DEFAULT (CURRENT_DATE) AFTER information_source;',
    'SELECT "Column last_updated already exists on roles" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;


-- 3. Add `importance_weight` to `role_skills` if not present
SET @col_weight_exists = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() 
      AND TABLE_NAME = 'role_skills' 
      AND COLUMN_NAME = 'importance_weight'
);

SET @sql_stmt = IF(@col_weight_exists = 0,
    'ALTER TABLE role_skills ADD COLUMN importance_weight INT DEFAULT 1 AFTER is_mandatory;',
    'SELECT "Column importance_weight already exists on role_skills" AS status;'
);

PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE role_skills SET importance_weight = 2 WHERE is_mandatory = TRUE;


-- 4. Create `company_roles` association table
-- Associates suitable roles with companies, tracking vacancy status,
-- vacancy source, information source, and last verified date.
--
-- Critical Distinction: Default status is 'Potential Role Match'
-- to ensure active vacancies are not fabricated when only company
-- role profiles exist.
CREATE TABLE IF NOT EXISTS company_roles (
    company_role_id INT AUTO_INCREMENT PRIMARY KEY,
    company_id INT NOT NULL,
    role_id INT NOT NULL,
    vacancy_status ENUM('Potential Role Match', 'Verified Vacancy', 'Campus Drive', 'Closed', 'Unverified') DEFAULT 'Potential Role Match',
    vacancy_source VARCHAR(150) DEFAULT 'Company Competency Profile',
    information_source VARCHAR(150) DEFAULT 'Official Careers Portal / Campus Listing',
    last_verified DATE DEFAULT (CURRENT_DATE),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cr_company FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE CASCADE,
    CONSTRAINT fk_cr_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT uq_company_role UNIQUE (company_id, role_id)
);


-- 5. Seed verified company-role pairings
INSERT IGNORE INTO company_roles (company_id, role_id, vacancy_status, vacancy_source, information_source, last_verified) VALUES
-- Google (1)
(1, 1, 'Potential Role Match', 'Company Competency Profile', 'https://careers.google.com', '2026-08-01'),
(1, 3, 'Potential Role Match', 'Company Competency Profile', 'https://careers.google.com', '2026-08-01'),
(1, 4, 'Potential Role Match', 'Company Competency Profile', 'https://careers.google.com', '2026-08-01'),

-- Microsoft (2)
(2, 2, 'Potential Role Match', 'Company Competency Profile', 'https://careers.microsoft.com', '2026-08-15'),
(2, 4, 'Potential Role Match', 'Company Competency Profile', 'https://careers.microsoft.com', '2026-08-15'),
(2, 1, 'Potential Role Match', 'Company Competency Profile', 'https://careers.microsoft.com', '2026-08-15'),

-- Amazon (3)
(3, 4, 'Potential Role Match', 'Company Competency Profile', 'https://amazon.jobs', '2026-08-20'),
(3, 1, 'Potential Role Match', 'Company Competency Profile', 'https://amazon.jobs', '2026-08-20'),
(3, 3, 'Potential Role Match', 'Company Competency Profile', 'https://amazon.jobs', '2026-08-20'),

-- Infosys (4)
(4, 1, 'Potential Role Match', 'Company Competency Profile', 'https://infosys.com/careers', '2026-08-01'),
(4, 2, 'Potential Role Match', 'Company Competency Profile', 'https://infosys.com/careers', '2026-08-01'),

-- TCS (5)
(5, 1, 'Potential Role Match', 'Company Competency Profile', 'https://tcs.com/careers', '2026-08-10'),
(5, 3, 'Potential Role Match', 'Company Competency Profile', 'https://tcs.com/careers', '2026-08-10'),

-- Wipro (6)
(6, 2, 'Potential Role Match', 'Company Competency Profile', 'https://careers.wipro.com', '2026-08-12'),
(6, 4, 'Potential Role Match', 'Company Competency Profile', 'https://careers.wipro.com', '2026-08-12');
