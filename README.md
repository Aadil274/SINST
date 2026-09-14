# Smart Internship & Skill Tracker (SINST)

A desktop software application designed to centralize and streamline a student's professional development journey. Built strictly using **pure Java (Swing + JDBC + OOP)** and **MySQL DBMS**.

---

## 📌 Project Overview

College students frequently struggle to manage their internship preparation and application processes efficiently. As they apply to numerous companies, tracking application dates, deadlines, interview schedules, and current application statuses manually becomes overwhelming. Furthermore, maintaining scattered records of personal skills, proficiency levels, certifications, and academic projects often leads to disorganized information, missed deadlines, and a fragmented view of one's professional readiness.

The **Smart Internship & Skill Tracker (SINST)** provides:
- **Student Authentication & Profile Management**: Secure registration and login using student roll numbers and credentials.
- **Internship Application Lifecycle Tracking**: Record target companies, roles, applied dates, deadlines, progressing statuses (`Applied`, `Shortlisted`, `Interview`, `Selected`, `Rejected`), and stipends.
- **Interview Scheduling & History**: Schedule upcoming interviews, log rounds (Technical, HR, Coding Assessment), modes (`Online` vs `In-Person`), and track outcomes.
- **Skills Portfolio & Profile Management**: Catalog technical and soft skills alongside proficiency levels (`Beginner`, `Intermediate`, `Advanced`, `Expert`), academic projects, and certifications.
- **Direct Relational Skill-Gap Analysis**: Direct database comparisons compare a selected target role's required skill competencies against the student's recorded profile, identifying exact missing competencies and calculating a readiness percentage without external predictive algorithms.
- **Dynamic Dashboard Summaries**: Uses SQL aggregate functions (`COUNT`, `GROUP BY`) and `JOIN` operations to display application status breakdowns and upcoming interview deadlines.

---

## 🛠️ Technology Stack (Pure Java + DBMS)

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Language & Runtime** | Java 21+ / JDK | Strictly adheres to Object-Oriented Programming (OOP) principles |
| **User Interface (GUI)** | Java Swing | Forms-based desktop experience with modern FlatLaf theme |
| **Database Management** | MySQL 8.0 Server | Relational storage with 10 normalized tables & foreign key integrity |
| **Communication Bridge** | JDBC (Java Database Connectivity) | Parameterized `PreparedStatement` queries & transaction handling |
| **Theme & Styling** | FlatLaf 3.5.4 | Clean desktop look-and-feel with high-contrast buttons & geometry |

---

## 📂 Project Structure

```
SINST/
├── lib/
│   ├── mysql-connector-j-8.4.0.jar         # Bundled MySQL JDBC Driver
│   └── flatlaf-3.5.4.jar                   # Modern FlatLaf Swing Look & Feel
├── db/
│   └── database.sql                        # Complete database creation & seed data script
├── src/
│   └── com/
│       └── sinst/
│           ├── Main.java                   # Desktop Application Entry Point
│           ├── db/
│           │   ├── DBConnection.java       # Database connection manager & settings
│           │   └── AppDAO.java             # Clean, consolidated JDBC PreparedStatement operations
│           ├── model/
│           │   ├── Student.java            # Student account & profile model
│           │   ├── Application.java        # Internship application model
│           │   ├── Interview.java          # Interview details & round tracking model
│           │   ├── Skill.java              # Technical/soft skills model
│           │   ├── Role.java               # Target industry roles model
│           │   ├── Certification.java      # Certifications model
│           │   ├── Project.java            # Academic projects model
│           │   └── SkillGapResult.java     # Relational skill-gap comparison result model
│           └── ui/
│               ├── LoginFrame.java         # Login, Registration & DB Configuration Dialog
│               └── MainFrame.java          # 4-Tab Management Dashboard
├── compile.bat                             # One-click Windows compile script
├── run.bat                                 # One-click Windows launch script
├── pom.xml                                 # Standard Maven file for IDE support
└── README.md                               # Project documentation
```

---

## 🗄️ Relational Database Schema (`db/database.sql`)

The underlying database architecture consists of 10 interconnected tables:
1. **`students`**: Student accounts, roll numbers, contact info, department, year of study.
2. **`skills`**: Master catalog of technical and soft skills with categories.
3. **`companies`**: Participating hiring companies, industries, locations, and websites.
4. **`roles`**: Target internship roles (e.g., Java Backend Developer, Full Stack Engineer, Data Analyst).
5. **`role_skills`**: Associative table defining benchmark skill proficiencies required for each role.
6. **`student_skills`**: Associative table tracking individual student competencies and proficiency levels.
7. **`internship_applications`**: Individual applications, applied dates, deadlines, statuses, and stipends.
8. **`interviews`**: Scheduled interview rounds, modes, statuses, and preparation feedback.
9. **`certifications`**: Certifications earned by students with issuing organizations and credential IDs.
10. **`projects`**: Academic and personal projects cataloged with tech stacks and links.

---

## ⚡ Quick Setup & Execution

### 1. Database Setup
1. Open **MySQL Workbench** or the MySQL Command Line Client.
2. Open and execute the script:
   ```sql
   db/database.sql
   ```
   *(This creates the `sinst_db` database, initializes all 10 normalized tables, and pre-populates sample roles, skills, companies, and test accounts).*

### 2. Configure Database Credentials (if needed)
- By default, the application connects using `user=root` and `password=password123` on `localhost:3306`.
- You can adjust your MySQL credentials either in `db.properties` or by clicking the **"Database Settings"** button directly on the login screen.

### 3. Compile the Application
Double-click:
```cmd
compile.bat
```
*(Or in terminal: `javac -encoding UTF-8 -cp "lib/*" -d bin src\com\sinst\*.java src\com\sinst\model\*.java src\com\sinst\db\*.java src\com\sinst\ui\*.java`)*

### 4. Run the Application
Double-click:
```cmd
run.bat
```
*(Or in terminal: `java -cp "bin;lib/*" com.sinst.Main`)*

---

## 🔑 Demo Login Credentials

The database comes pre-seeded with a ready-to-test student profile:
- **Roll Number:** `25R11A0501`
- **Password:** `password123`

*(You can also register any new student account via the "New Registration" tab on the login screen).*

---

## 👥 Team Members

- **Amog Mantha** — `25R11A0505`
- **Aadil Ahmed Shaik** — `25R11A0501`
- **Shreyas Ratnaparkhi** — `25R11A0548`
