
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
    contact_email VARCHAR(100)
);


CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_title VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    experience_level VARCHAR(30) DEFAULT 'Intern'
);


CREATE TABLE IF NOT EXISTS role_skills (
    role_id INT NOT NULL,
    skill_id INT NOT NULL,
    required_level ENUM('Beginner', 'Intermediate', 'Advanced', 'Expert') NOT NULL DEFAULT 'Intermediate',
    is_mandatory BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (role_id, skill_id),
    CONSTRAINT fk_role_skills_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE,
    CONSTRAINT fk_role_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id) ON DELETE CASCADE
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
    status ENUM('Applied', 'Shortlisted', 'Interview', 'Selected', 'Rejected') DEFAULT 'Applied',
    stipend VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_app_company FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE RESTRICT,
    CONSTRAINT fk_app_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE RESTRICT
);


CREATE TABLE IF NOT EXISTS interviews (
    interview_id INT AUTO_INCREMENT PRIMARY KEY,
    application_id INT NOT NULL,
    interview_date DATETIME NOT NULL,
    round_type VARCHAR(50) NOT NULL,
    mode ENUM('Online', 'In-Person') DEFAULT 'Online',
    status ENUM('Scheduled', 'Completed', 'Cancelled', 'Rescheduled') DEFAULT 'Scheduled',
    outcome ENUM('Pending', 'Passed', 'Failed') DEFAULT 'Pending',
    feedback TEXT,
    CONSTRAINT fk_interview_app FOREIGN KEY (application_id) REFERENCES internship_applications(application_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS certifications (
    certification_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    certificate_name VARCHAR(150) NOT NULL,
    issuing_organization VARCHAR(100) NOT NULL,
    issue_date DATE NOT NULL,
    credential_id VARCHAR(100),
    CONSTRAINT fk_cert_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS projects (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    technologies_used VARCHAR(255),
    project_url VARCHAR(255),
    CONSTRAINT fk_proj_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
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
INSERT IGNORE INTO companies (company_id, company_name, industry, location, website, contact_email) VALUES
(1, 'Google', 'Information Technology', 'Hyderabad, India', 'https://careers.google.com', 'campus@google.com'),
(2, 'Microsoft', 'Software & Cloud', 'Hyderabad, India', 'https://careers.microsoft.com', 'recruitment@microsoft.com'),
(3, 'Amazon', 'E-Commerce & Cloud', 'Hyderabad, India', 'https://amazon.jobs', 'internships@amazon.com'),
(4, 'Infosys', 'IT Services & Consulting', 'Bengaluru, India', 'https://infosys.com/careers', 'careers@infosys.com'),
(5, 'Tata Consultancy Services (TCS)', 'IT & Enterprise Solutions', 'Hyderabad, India', 'https://tcs.com/careers', 'campus.hiring@tcs.com'),
(6, 'Wipro', 'IT Consulting', 'Bengaluru, India', 'https://wipro.com', 'careers@wipro.com');

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
