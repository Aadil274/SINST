-- ====================================================================
-- SINST DATABASE MIGRATION V4: Certification-to-Skill Mapping & Evidence Tracking
-- ====================================================================

USE sinst_db;

-- 1. Catalog table: certification_skills
-- Stores standard/verified mappings between certification types and skills they cover
CREATE TABLE IF NOT EXISTS certification_skills (
    mapping_id INT AUTO_INCREMENT PRIMARY KEY,
    certification_name VARCHAR(150) NOT NULL,
    issuing_organization VARCHAR(100),
    skill_id INT NOT NULL,
    learning_outcome VARCHAR(255) NOT NULL,
    evidence_source VARCHAR(100) DEFAULT 'Official Curriculum / Syllabus',
    suggested_proficiency ENUM('Beginner', 'Intermediate', 'Advanced', 'Expert') DEFAULT 'Intermediate',
    mapping_status ENUM('Verified', 'Pending', 'Deprecated') DEFAULT 'Verified',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cert_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE,
    CONSTRAINT uq_cert_skill UNIQUE (certification_name, skill_id)
);

-- 2. Student certification skills: student_certification_skills
-- Connects a specific student's earned certification to skills verified by it
CREATE TABLE IF NOT EXISTS student_certification_skills (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    certification_id INT NOT NULL,
    skill_id INT NOT NULL,
    learning_outcome VARCHAR(255),
    review_status ENUM('Approved', 'Pending', 'Rejected') DEFAULT 'Approved',
    assessed_proficiency ENUM('Beginner', 'Intermediate', 'Advanced', 'Expert') DEFAULT 'Intermediate',
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stud_cert_skills_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_stud_cert_skills_cert FOREIGN KEY (certification_id) REFERENCES certifications(certification_id) ON DELETE CASCADE,
    CONSTRAINT fk_stud_cert_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE,
    CONSTRAINT uq_stud_cert_skill UNIQUE (certification_id, skill_id)
);

-- 3. Comprehensive evidence tracking: skill_evidence
-- Connects a student's skill to supporting evidence: Certification, Project, Assessment, or Self-Reported
CREATE TABLE IF NOT EXISTS skill_evidence (
    evidence_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    evidence_type ENUM('Self-Reported', 'Certification', 'Project', 'Assessment') NOT NULL,
    reference_id INT DEFAULT NULL,
    evidence_title VARCHAR(200) NOT NULL,
    evidence_details TEXT,
    verification_status ENUM('Unverified', 'Verified', 'Rejected') DEFAULT 'Verified',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_skill_ev_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_skill_ev_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_skill_evidence UNIQUE (student_id, skill_id, evidence_type, reference_id)
);

-- 4. Seed Verified Catalog Mappings for Known Certifications
INSERT IGNORE INTO certification_skills (certification_name, issuing_organization, skill_id, learning_outcome, evidence_source, suggested_proficiency, mapping_status) VALUES
-- Oracle Java SE
('Oracle Certified Associate: Java SE 17 Developer', 'Oracle', 1, 'Core Java, OOP principles, Collections, Multithreading, and Exception Handling', 'Oracle Exam 1Z0-829 Specification', 'Intermediate', 'Verified'),
('Oracle Certified Associate: Java SE 17 Developer', 'Oracle', 15, 'Algorithm design, syntax precision, and analytical debugging under exam conditions', 'Oracle Exam 1Z0-829 Specification', 'Intermediate', 'Verified'),

-- HackerRank SQL
('Database Systems & SQL Specialist', 'HackerRank', 2, 'Relational queries, complex joins, subqueries, grouping, and schema normalization', 'HackerRank Skills Assessment', 'Intermediate', 'Verified'),

-- AWS Certified
('AWS Certified Cloud Practitioner', 'Amazon Web Services (AWS)', 5, 'Cloud architecture, RESTful API integrations, and cloud service endpoints', 'AWS Certified Exam Guide', 'Beginner', 'Verified'),
('AWS Certified Cloud Practitioner', 'Amazon Web Services (AWS)', 12, 'Linux cloud instances, security groups, and cloud terminal operations', 'AWS Certified Exam Guide', 'Beginner', 'Verified'),

-- Meta Front-End
('Meta Front-End Developer Professional Certificate', 'Meta', 7, 'Responsive UI design, CSS Flexbox/Grid, and HTML5 semantic web structures', 'Coursera / Meta Curriculum', 'Intermediate', 'Verified'),
('Meta Front-End Developer Professional Certificate', 'Meta', 8, 'Modern ES6+ JavaScript, asynchronous operations, and DOM manipulation', 'Coursera / Meta Curriculum', 'Intermediate', 'Verified'),
('Meta Front-End Developer Professional Certificate', 'Meta', 9, 'Component architecture, state management, and React hooks', 'Coursera / Meta Curriculum', 'Intermediate', 'Verified'),

-- Google Data Analytics
('Google Data Analytics Professional Certificate', 'Google', 2, 'SQL queries, database filtering, data aggregation, and structured data manipulation', 'Google Professional Certificate', 'Intermediate', 'Verified'),
('Google Data Analytics Professional Certificate', 'Google', 10, 'Python programming, pandas, numpy, and statistical data exploration', 'Google Professional Certificate', 'Beginner', 'Verified'),
('Google Data Analytics Professional Certificate', 'Google', 14, 'Interactive business intelligence dashboards and data storytelling', 'Google Professional Certificate', 'Intermediate', 'Verified'),

-- Docker Certified
('Docker Certified Associate', 'Docker', 13, 'Containerization, Dockerfile creation, image building, and container networking', 'Docker Certification Blueprint', 'Intermediate', 'Verified'),
('Docker Certified Associate', 'Docker', 12, 'Linux container runtime, process management, and shell execution', 'Docker Certification Blueprint', 'Intermediate', 'Verified'),

-- Git & GitHub
('Git & GitHub Version Control Certification', 'GitHub', 4, 'Branch management, merge conflict resolution, pull request workflows, and collaborative Git', 'GitHub Education Pathway', 'Intermediate', 'Verified');

-- Migration V4 Complete
SELECT 'Migration V4 applied successfully: certification_skills, student_certification_skills, and skill_evidence tables configured' AS result;
