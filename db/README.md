# SINST Relational Database Architecture & Setup Guide

This directory contains the database initialization scripts and progressive versioned migrations for the **Smart Internship & Skill Tracker (SINST)** application.

---

## 🗄️ Relational Schema Overview

The database (`sinst_db`) consists of **14 interconnected relational tables** maintaining third normal form (3NF), domain-level constraints, composite unique keys, and cascading/restricted foreign keys:

| Table | Purpose | Primary Key | Key Foreign Keys / Constraints |
| :--- | :--- | :--- | :--- |
| **`students`** | Student user accounts, roll numbers, credentials, and profile details | `student_id` | `UNIQUE(roll_number)`, `UNIQUE(email)` |
| **`skills`** | Master catalog of technical, database, framework, and soft skills | `skill_id` | `UNIQUE(skill_name)` |
| **`companies`** | Hiring organizations, industries, locations, and careers portal URLs | `company_id` | `UNIQUE(company_name)` |
| **`roles`** | Target industry job roles and competency benchmarks | `role_id` | `UNIQUE(role_title)` |
| **`role_skills`** | Associative table defining required skill proficiencies per role | `(role_id, skill_id)` | `FK -> roles`, `FK -> skills` |
| **`company_roles`** | Associative table linking companies to offered roles with vacancy status | `company_role_id` | `UNIQUE(company_id, role_id)`, `FK -> companies`, `FK -> roles` |
| **`student_skills`** | Individual student claimed skill proficiencies | `(student_id, skill_id)` | `FK -> students`, `FK -> skills` |
| **`internship_applications`** | Student internship applications, deadlines, lifecycle statuses, stipends | `application_id` | `UNIQUE(student_id, company_id, role_id)`, `FK -> students`, `FK -> companies`, `FK -> roles` |
| **`interviews`** | Scheduled interview rounds, dates, modes, feedback, and outcomes | `interview_id` | `UNIQUE(application_id, round_type, interview_date)`, `FK -> internship_applications` |
| **`certifications`** | Student-earned credentials, issuing orgs, credential IDs, and URLs | `certification_id` | `UNIQUE(student_id, certificate_name, issuing_organization)`, `FK -> students` |
| **`projects`** | Student academic/personal projects, descriptions, tech stacks, repositories | `project_id` | `UNIQUE(student_id, title)`, `FK -> students` |
| **`certification_skills`** | Catalog mapping industry certifications to verified skills and outcomes | `mapping_id` | `UNIQUE(certification_name, skill_id)`, `FK -> skills` |
| **`student_certification_skills`** | Verified student skill associations derived from completed certifications | `record_id` | `UNIQUE(certification_id, skill_id)`, `FK -> students`, `FK -> certifications`, `FK -> skills` |
| **`skill_evidence`** | Audit trail linking student skills to verifiable evidence (certifications/projects) | `evidence_id` | `UNIQUE(student_id, skill_id, evidence_type, reference_id)`, `FK -> students`, `FK -> skills` |

---

## 🚀 Scenario A: Clean Setup (Fresh Installation)

For a fresh installation on a local MySQL 8.0+ server, execute the complete idempotent setup script:

```bash
# Using MySQL Command Line Client (Windows CMD/PowerShell):
mysql -u root -p < db/database.sql
```

Or within **MySQL Workbench**:
1. Open MySQL Workbench and connect to your local MySQL server instance.
2. Go to **File -> Open SQL Script...** and select `db/database.sql`.
3. Click the **Execute (Lightning Bolt)** icon.

### What `database.sql` Does:
- Creates the database `sinst_db` (`CREATE DATABASE IF NOT EXISTS sinst_db;`).
- Initializes all 14 normalized tables with schema constraints (`CREATE TABLE IF NOT EXISTS`).
- Seeds standard industry skills, target internship roles, role benchmarks, verified company-role associations, and demo student profiles (`INSERT IGNORE INTO`).
- **Safety**: Does **not** drop or overwrite existing tables or user data.

---

## 🔄 Scenario B: Upgrading an Existing Installation (Sequential Migrations)

If upgrading an existing SINST database across feature releases, execute the migration scripts sequentially:

```bash
# Apply migrations in numerical sequence:
mysql -u root -p sinst_db < db/migration_v2.sql
mysql -u root -p sinst_db < db/migration_v3.sql
mysql -u root -p sinst_db < db/migration_v4.sql
mysql -u root -p sinst_db < db/migration_v5.sql
mysql -u root -p sinst_db < db/migration_v6.sql
```

### Migration Version Reference:
- **`migration_v2.sql`**: Application unique constraint `(student_id, company_id, role_id)` preventing duplicate applications.
- **`migration_v3.sql`**: Skill-gap analysis schema, 4-tier standardized proficiency scale (`Beginner`, `Intermediate`, `Advanced`, `Expert`), `is_mandatory` flags, and composite primary keys.
- **`migration_v4.sql`**: Certification-to-skill catalog mapping (`certification_skills`), verified student certification skills (`student_certification_skills`), and evidence tracking (`skill_evidence`).
- **`migration_v5.sql`**: Explainable company and internship role recommendations, careers portal metadata, and company-role associations (`company_roles`).
- **`migration_v6.sql`**: Input validation support, IDOR ownership controls, student roles (`STUDENT`, `ADMIN`), URL and expiration metadata, and composite uniqueness constraints on certifications, projects, and interviews.

---

## ⚙️ Configuring Local Database Connectivity

The application manages database connectivity settings via `db.properties` located in the application working directory.

### Method 1: Editing `db.properties` directly
```properties
# SINST Database Configuration
db.host=localhost
db.port=3306
db.name=sinst_db
db.user=root
db.password=your_mysql_password
```

### Method 2: In-App UI Configuration
1. Launch the application via `run.bat` or `java -jar target/sinst-1.0.0.jar`.
2. On the **Login** window, click **"Database Settings"** in the lower-right corner.
3. Enter your MySQL host, port, database name, username, and password.
4. Click **"Test Connection"** to verify connectivity with the server.
5. Click **"Save"** to persist settings into `db.properties`.

> [!NOTE]
> Database connection credentials are local developer configurations. In keeping with the designated project boundaries, do not commit real personal passwords or production server credentials to source control.

---

## 🔍 Verification & Health Check

After applying scripts, verify your database using these SQL diagnostics:

```sql
USE sinst_db;

-- 1. Verify all 14 tables are present:
SHOW TABLES;

-- 2. Verify master catalog row counts:
SELECT 'skills' AS entity, COUNT(*) AS count FROM skills
UNION ALL
SELECT 'roles', COUNT(*) FROM roles
UNION ALL
SELECT 'companies', COUNT(*) FROM companies
UNION ALL
SELECT 'company_roles', COUNT(*) FROM company_roles
UNION ALL
SELECT 'certification_skills', COUNT(*) FROM certification_skills;

-- 3. Verify demo student profile:
SELECT student_id, roll_number, full_name, email, role FROM students WHERE roll_number = '25R11A0501';
```
