
-- ====================================================================
-- SMART INTERNSHIP & SKILL TRACKER (SINST) - COMPLETE DATABASE SETUP
-- ====================================================================
-- Comprehensive relational schema initialization script.
--
-- Safety Guarantees:
-- 1. Idempotent: Uses CREATE TABLE IF NOT EXISTS and INSERT IGNORE.
-- 2. Safe execution: Does NOT drop or reset existing databases or data.
-- 3. Normalized: 14 relational tables enforcing 3NF, foreign keys,
--    and composite domain constraints.
--
-- Usage:
--   mysql -u root -p < db/database.sql
-- ====================================================================

CREATE DATABASE IF NOT EXISTS sinst_db;
USE sinst_db;

CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    roll_number VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    department VARCHAR(50) DEFAULT 'CSE',
    year_of_study INT DEFAULT 2,
    phone VARCHAR(15),
    role ENUM('STUDENT', 'ADMIN') DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS skills (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(50) NOT NULL UNIQUE,
    category ENUM('Technical', 'Soft', 'Tool', 'Framework', 'Database') NOT NULL,
    description VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS companies (
    company_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL UNIQUE,
    industry VARCHAR(50),
    location VARCHAR(100),
    website VARCHAR(150),
    careers_url VARCHAR(255),
    contact_email VARCHAR(100)
);


CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_title VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    experience_level VARCHAR(30) DEFAULT 'Intern',
    information_source VARCHAR(150) DEFAULT 'Industry Standard Competency Framework',
    last_updated DATE DEFAULT (CURRENT_DATE)
);


CREATE TABLE IF NOT EXISTS role_skills (
    role_id INT NOT NULL,
    skill_id INT NOT NULL,
    required_level ENUM('Beginner', 'Intermediate', 'Advanced', 'Expert') NOT NULL DEFAULT 'Intermediate',
    is_mandatory BOOLEAN DEFAULT TRUE,
    importance_weight INT DEFAULT 1,
    PRIMARY KEY (role_id, skill_id),
    CONSTRAINT fk_role_skills_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
);

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

CREATE TABLE IF NOT EXISTS student_skills (
    student_id INT NOT NULL,
    skill_id INT NOT NULL,
    proficiency_level ENUM('Beginner', 'Intermediate', 'Advanced', 'Expert') NOT NULL,
    PRIMARY KEY (student_id, skill_id),
    CONSTRAINT fk_student_skills_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_student_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS internship_applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    company_id INT NOT NULL,
    role_id INT NOT NULL,
    application_date DATE NOT NULL,
    deadline DATE,
    status ENUM('Applied', 'Under Review', 'Shortlisted', 'Interview', 'Accepted', 'Selected', 'Rejected', 'Withdrawn') DEFAULT 'Applied',
    stipend VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_company FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT,
    CONSTRAINT uq_student_company_role UNIQUE (student_id, company_id, role_id)
);


CREATE TABLE IF NOT EXISTS interviews (
    interview_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    interview_date DATETIME NOT NULL,
    round_type VARCHAR(50) NOT NULL,
    round_number INT DEFAULT 1,
    mode ENUM('Online', 'In-Person') DEFAULT 'Online',
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'Rescheduled') DEFAULT 'Scheduled',
    outcome ENUM('Pending', 'Passed', 'Failed') DEFAULT 'Pending',
    feedback TEXT,
    CONSTRAINT fk_interview_app FOREIGN KEY (application_id) REFERENCES internship_applications(application_id) ON DELETE CASCADE,
    CONSTRAINT uq_interview_app_round_date UNIQUE (application_id, round_type, interview_date)
);


CREATE TABLE IF NOT EXISTS certifications (
    certification_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    certificate_name VARCHAR(150) NOT NULL,
    issuing_organization VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE DEFAULT NULL,
    credential_id VARCHAR(100),
    credential_url VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_cert UNIQUE (student_id, certificate_name, issuing_organization)
);


CREATE TABLE IF NOT EXISTS projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    technologies_used VARCHAR(255),
    project_url VARCHAR(255),
    github_url VARCHAR(255) DEFAULT NULL,
    live_url VARCHAR(255) DEFAULT NULL,
    CONSTRAINT fk_proj_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT uq_student_proj_title UNIQUE (student_id, title)
);

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



INSERT IGNORE INTO skills (skill_id, skill_name, category, description) VALUES
(1, 'Java', 'Technical', 'Core Java, OOPs, Collections, Multithreading, Exception Handling'),
(2, 'SQL & DBMS', 'Database', 'Relational database design, Normalization, Queries, Joins, Transactions'),
(3, 'Data Structures & Algorithms', 'Technical', 'Arrays, Linked Lists, Trees, Graphs, Sorting, Dynamic Programming'),
(4, 'Git & GitHub', 'Tool', 'Version control, branch management, pull requests, collaborative coding'),
(5, 'REST APIs', 'Technical', 'Web services, HTTP methods, JSON data interchange, API architecture'),
(6, 'Spring Boot', 'Framework', 'Java enterprise framework, Dependency Injection, Microservices'),
(7, 'HTML & CSS', 'Technical', 'Web markup, responsive layouts, flexbox, CSS grid'),
(8, 'JavaScript', 'Technical', 'DOM manipulation, ES6+ syntax, asynchronous programming'),
(9, 'React', 'Framework', 'Component-based frontend development, state management, hooks'),
(10, 'Python', 'Technical', 'Scripting, OOP in Python, standard libraries, automation'),
(11, 'Machine Learning Basics', 'Technical', 'Supervised/unsupervised learning, scikit-learn, data preprocessing'),
(12, 'Docker', 'Tool', 'Containerization, Dockerfiles, container lifecycle management'),
(13, 'Linux & Bash', 'Tool', 'Command line navigation, shell scripting, process management'),
(14, 'Communication', 'Soft', 'Verbal and written articulation of ideas and technical concepts'),
(15, 'Problem Solving', 'Soft', 'Analytical thinking, algorithmic problem formulation, debugging'),
(16, 'Teamwork & Collaboration', 'Soft', 'Working in cross-functional agile teams, code reviews');

INSERT IGNORE INTO roles (role_id, role_title, description, experience_level) VALUES
(1, 'Java Backend Developer Intern', 'Focuses on server-side business logic, database design, REST APIs, and core Java engineering.', 'Intern'),
(2, 'Full Stack Web Developer Intern', 'Builds end-to-end web applications combining interactive frontend interfaces and backend databases.', 'Intern'),
(3, 'Data Analyst Intern', 'Extracts insights from structured data using SQL, Python, and data visualization tools.', 'Intern'),
(4, 'Cloud & DevOps Intern', 'Manages deployment automation, Docker containers, version control pipelines, and Linux environments.', 'Intern');

INSERT IGNORE INTO role_skills (role_id, skill_id, required_level, is_mandatory) VALUES
(1, 1, 'Intermediate', TRUE), 
(1, 2, 'Intermediate', TRUE), 
(1, 3, 'Intermediate', TRUE), 
(1, 4, 'Beginner', TRUE),  
(1, 5, 'Intermediate', TRUE),
(1, 15, 'Intermediate', TRUE); 


INSERT IGNORE INTO role_skills (role_id, skill_id, required_level, is_mandatory) VALUES
(2, 7, 'Intermediate', TRUE), 
(2, 8, 'Intermediate', TRUE), 
(2, 9, 'Beginner', TRUE),     
(2, 1, 'Beginner', TRUE),    
(2, 2, 'Beginner', TRUE),      
(2, 4, 'Beginner', TRUE);   


INSERT IGNORE INTO role_skills (role_id, skill_id, required_level, is_mandatory) VALUES
(3, 10, 'Intermediate', TRUE), 
(3, 2, 'Advanced', TRUE),     
(3, 15, 'Intermediate', TRUE), 
(3, 14, 'Intermediate', TRUE), 
(3, 11, 'Beginner', FALSE);   


INSERT IGNORE INTO role_skills (role_id, skill_id, required_level, is_mandatory) VALUES
(4, 13, 'Intermediate', TRUE), 
(4, 4, 'Intermediate', TRUE),  
(4, 12, 'Beginner', TRUE),  
(4, 10, 'Beginner', TRUE),   
(4, 16, 'Intermediate', TRUE); 

-- Master Companies
INSERT IGNORE INTO companies (company_id, company_name, industry, location, website, careers_url, contact_email) VALUES
(1, 'Google', 'Information Technology', 'Hyderabad, India', 'https://google.com', 'https://careers.google.com', 'campus@google.com'),
(2, 'Microsoft', 'Software & Cloud', 'Hyderabad, India', 'https://microsoft.com', 'https://careers.microsoft.com', 'recruitment@microsoft.com'),
(3, 'Amazon', 'E-Commerce & Cloud', 'Hyderabad, India', 'https://amazon.com', 'https://amazon.jobs', 'internships@amazon.com'),
(4, 'Infosys', 'IT Services & Consulting', 'Bengaluru, India', 'https://infosys.com', 'https://infosys.com/careers', 'careers@infosys.com'),
(5, 'Tata Consultancy Services (TCS)', 'IT & Enterprise Solutions', 'Hyderabad, India', 'https://tcs.com', 'https://tcs.com/careers', 'campus.hiring@tcs.com'),
(6, 'Wipro', 'IT Consulting', 'Bengaluru, India', 'https://wipro.com', 'https://careers.wipro.com', 'careers@wipro.com');

-- Verified Company-Role Pairings
INSERT IGNORE INTO company_roles (company_id, role_id, vacancy_status, vacancy_source, information_source, last_verified) VALUES
(1, 1, 'Potential Role Match', 'Company Competency Profile', 'https://careers.google.com', '2026-08-01'),
(1, 3, 'Potential Role Match', 'Company Competency Profile', 'https://careers.google.com', '2026-08-01'),
(1, 4, 'Potential Role Match', 'Company Competency Profile', 'https://careers.google.com', '2026-08-01'),
(2, 2, 'Potential Role Match', 'Company Competency Profile', 'https://careers.microsoft.com', '2026-08-15'),
(2, 4, 'Potential Role Match', 'Company Competency Profile', 'https://careers.microsoft.com', '2026-08-15'),
(2, 1, 'Potential Role Match', 'Company Competency Profile', 'https://careers.microsoft.com', '2026-08-15'),
(3, 4, 'Potential Role Match', 'Company Competency Profile', 'https://amazon.jobs', '2026-08-20'),
(3, 1, 'Potential Role Match', 'Company Competency Profile', 'https://amazon.jobs', '2026-08-20'),
(3, 3, 'Potential Role Match', 'Company Competency Profile', 'https://amazon.jobs', '2026-08-20'),
(4, 1, 'Potential Role Match', 'Company Competency Profile', 'https://infosys.com/careers', '2026-08-01'),
(4, 2, 'Potential Role Match', 'Company Competency Profile', 'https://infosys.com/careers', '2026-08-01'),
(5, 1, 'Potential Role Match', 'Company Competency Profile', 'https://tcs.com/careers', '2026-08-10'),
(5, 3, 'Potential Role Match', 'Company Competency Profile', 'https://tcs.com/careers', '2026-08-10'),
(6, 2, 'Potential Role Match', 'Company Competency Profile', 'https://careers.wipro.com', '2026-08-12'),
(6, 4, 'Potential Role Match', 'Company Competency Profile', 'https://careers.wipro.com', '2026-08-12');

-- Sample Students (Team Profiles)
INSERT IGNORE INTO students (student_id, roll_number, full_name, email, password, department, year_of_study, phone) VALUES
(1, '25R11A0501', 'Aadil Ahmed Shaik', '25r11a0501@student.edu', 'password123', 'CSE', 2, '6303279385'),
(2, '25R11A0505', 'Amog Mantha', '25r11a0505@student.edu', 'password123', 'CSE', 2, '6301422735'),
(3, '25R11A0548', 'Shreyas Ratnaparkhi', '25r11a0548@student.edu', 'password123', 'CSE', 2, '7075182727');

-- Sample Student Skills for Aadil (Possesses 4 out of 6 skills for Java Backend: demonstrates the abstract''s skill-gap example!)
INSERT IGNORE INTO student_skills (student_id, skill_id, proficiency_level) VALUES
(1, 1, 'Intermediate'), -- Java
(1, 2, 'Intermediate'), -- SQL & DBMS
(1, 3, 'Intermediate'), -- DSA
(1, 4, 'Beginner');     -- Git & GitHub
-- (Missing: REST APIs, Problem Solving -> Perfectly matches abstract scenario!)

-- Sample Internship Applications for Aadil
INSERT IGNORE INTO internship_applications (application_id, student_id, company_id, role_id, application_date, deadline, status, stipend, notes) VALUES
(1, 1, 1, 1, '2026-08-10', '2026-09-30', 'Interview', '₹ 45,000 / mo', 'Completed technical round 1, awaiting managerial round.'),
(2, 1, 2, 2, '2026-08-15', '2026-10-15', 'Shortlisted', '₹ 50,000 / mo', 'Resume screened, scheduled for online assessment.'),
(3, 1, 4, 1, '2026-08-01', '2026-08-25', 'Selected', '₹ 25,000 / mo', 'Official offer letter received. Confirmation pending.'),
(4, 1, 3, 4, '2026-08-20', '2026-10-01', 'Applied', '₹ 40,000 / mo', 'Submitted portal application with updated resume.');

-- Sample Interview Record for Aadil
INSERT IGNORE INTO interviews (interview_id, application_id, interview_date, round_type, mode, status, outcome, feedback) VALUES
(1, 1, DATE_ADD(NOW(), INTERVAL 3 DAY), 'Technical Round 2', 'Online', 'Scheduled', 'Pending', 'Focus on Java Collections, SQL Indexing, and System Design basics.');

-- Sample Certification
INSERT IGNORE INTO certifications (certification_id, student_id, certificate_name, issuing_organization, issue_date, credential_id) VALUES
(1, 1, 'Oracle Certified Associate: Java SE 17 Developer', 'Oracle', '2026-05-15', 'OCA-JAVA-2026-9876'),
(2, 1, 'Database Systems & SQL Specialist', 'HackerRank', '2026-06-20', 'HR-SQL-ADV-4521');

-- Sample Academic Project
INSERT IGNORE INTO projects (project_id, student_id, title, description, technologies_used, project_url) VALUES
(1, 1, 'Smart Internship & Skill Tracker', 'Desktop application integrating Java Swing and MySQL DBMS for tracking applications and analyzing skill gaps.', 'Java, Swing, JDBC, MySQL', 'https://github.com/aadilahmedshaik/sinst');

-- Seed Verified Catalog Mappings for Known Certifications
INSERT IGNORE INTO certification_skills (certification_name, issuing_organization, skill_id, learning_outcome, evidence_source, suggested_proficiency, mapping_status) VALUES
('Oracle Certified Associate: Java SE 17 Developer', 'Oracle', 1, 'Core Java, OOP principles, Collections, Multithreading, and Exception Handling', 'Oracle Exam 1Z0-829 Specification', 'Intermediate', 'Verified'),
('Oracle Certified Associate: Java SE 17 Developer', 'Oracle', 15, 'Algorithm design, syntax precision, and analytical debugging under exam conditions', 'Oracle Exam 1Z0-829 Specification', 'Intermediate', 'Verified'),
('Database Systems & SQL Specialist', 'HackerRank', 2, 'Relational queries, complex joins, subqueries, grouping, and schema normalization', 'HackerRank Skills Assessment', 'Intermediate', 'Verified'),
('AWS Certified Cloud Practitioner', 'Amazon Web Services (AWS)', 5, 'Cloud architecture, RESTful API integrations, and cloud service endpoints', 'AWS Certified Exam Guide', 'Beginner', 'Verified'),
('AWS Certified Cloud Practitioner', 'Amazon Web Services (AWS)', 12, 'Linux cloud instances, security groups, and cloud terminal operations', 'AWS Certified Exam Guide', 'Beginner', 'Verified'),
('Meta Front-End Developer Professional Certificate', 'Meta', 7, 'Responsive UI design, CSS Flexbox/Grid, and HTML5 semantic web structures', 'Coursera / Meta Curriculum', 'Intermediate', 'Verified'),
('Meta Front-End Developer Professional Certificate', 'Meta', 8, 'Modern ES6+ JavaScript, asynchronous operations, and DOM manipulation', 'Coursera / Meta Curriculum', 'Intermediate', 'Verified'),
('Meta Front-End Developer Professional Certificate', 'Meta', 9, 'Component architecture, state management, and React hooks', 'Coursera / Meta Curriculum', 'Intermediate', 'Verified'),
('Google Data Analytics Professional Certificate', 'Google', 2, 'SQL queries, database filtering, data aggregation, and structured data manipulation', 'Google Professional Certificate', 'Intermediate', 'Verified'),
('Google Data Analytics Professional Certificate', 'Google', 10, 'Python programming, pandas, numpy, and statistical data exploration', 'Google Professional Certificate', 'Beginner', 'Verified'),
('Google Data Analytics Professional Certificate', 'Google', 14, 'Interactive business intelligence dashboards and data storytelling', 'Google Professional Certificate', 'Intermediate', 'Verified'),
('Docker Certified Associate', 'Docker', 13, 'Containerization, Dockerfile creation, image building, and container networking', 'Docker Certification Blueprint', 'Intermediate', 'Verified'),
('Docker Certified Associate', 'Docker', 12, 'Linux container runtime, process management, and shell execution', 'Docker Certification Blueprint', 'Intermediate', 'Verified'),
('Git & GitHub Version Control Certification', 'GitHub', 4, 'Branch management, merge conflict resolution, pull request workflows, and collaborative Git', 'GitHub Education Pathway', 'Intermediate', 'Verified');

-- Sample Initial Student Certification Skills & Evidence for Aadil
INSERT IGNORE INTO student_certification_skills (record_id, student_id, certification_id, skill_id, learning_outcome, review_status, assessed_proficiency) VALUES
(1, 1, 1, 1, 'Core Java, OOP principles, Collections, Multithreading, and Exception Handling', 'Approved', 'Intermediate'),
(2, 1, 2, 2, 'Relational queries, complex joins, subqueries, grouping, and schema normalization', 'Approved', 'Intermediate');

INSERT IGNORE INTO skill_evidence (evidence_id, student_id, skill_id, evidence_type, reference_id, evidence_title, evidence_details, verification_status) VALUES
(1, 1, 1, 'Certification', 1, 'Oracle Certified Associate: Java SE 17 Developer', 'Verified credential via Oracle Exam 1Z0-829. Covers core Java SE competency.', 'Verified'),
(2, 1, 2, 'Certification', 2, 'Database Systems & SQL Specialist', 'HackerRank verified assessment in relational database design and advanced SQL queries.', 'Verified'),
(3, 1, 1, 'Project', 1, 'Smart Internship & Skill Tracker', 'Demonstrated core Java, Swing, and JDBC architecture in desktop academic project.', 'Verified'),
(4, 1, 2, 'Project', 1, 'Smart Internship & Skill Tracker', 'Implemented relational database schemas, migrations, and parameterized JDBC queries.', 'Verified');
