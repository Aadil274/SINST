# Smart Internship & Skill Tracker (SINST)

A desktop software application designed to centralize and streamline a student's professional development journey. Built strictly using **pure Java (Swing + JDBC + OOP)** and **MySQL DBMS**.

---

## 📌 Project Overview

College students frequently struggle to manage their internship preparation and application processes efficiently. As they apply to numerous companies, tracking application dates, deadlines, interview schedules, and current application statuses manually becomes overwhelming. Furthermore, maintaining scattered records of personal skills, proficiency levels, certifications, and academic projects often leads to disorganized information, missed deadlines, and a fragmented view of one's professional readiness.

The **Smart Internship & Skill Tracker (SINST)** provides:
- **Student Authentication & Profile Management**: Secure registration and login using student roll numbers, credentials, and profile validation.
- **Internship Application Lifecycle Tracking**: Record target companies, roles, applied dates, deadlines, progressing statuses (`Applied`, `Under Review`, `Shortlisted`, `Interview`, `Accepted`, `Selected`, `Rejected`, `Withdrawn`), and stipends.
- **Interview Scheduling & History**: Schedule upcoming interviews, log rounds (Technical, HR, Coding Assessment), modes (`Online` vs `In-Person`), and track outcomes.
- **Skills Portfolio & Profile Management**: Catalog technical, database, framework, and soft skills alongside standardized proficiency levels (`Beginner`, `Intermediate`, `Advanced`, `Expert`), academic projects, and certifications.
- **Explainable Skill-Gap Analysis**: Direct relational database comparisons compare a selected target role's required skill competencies against the student's recorded profile, identifying exact missing competencies and calculating an explainable readiness percentage without external black-box algorithms.
- **Transparent Role Recommendations**: Multi-tier deterministic ranking that matches student demonstrated abilities with active and potential industry roles, distinguishing between verified evidence and unverified claims.
- **Dynamic Dashboard Summaries**: Uses SQL aggregate functions (`COUNT`, `GROUP BY`) and `JOIN` operations to display application status breakdowns and upcoming interview deadlines.

---

## 🛠️ Technology Stack (Pure Java + DBMS)

| Layer | Technology | Details |
| :--- | :--- | :--- |
| **Language & Runtime** | Java 21+ / JDK | Strictly adheres to Object-Oriented Programming (OOP) principles |
| **User Interface (GUI)** | Java Swing | Forms-based desktop experience with modern FlatLaf theme |
| **Build System** | Apache Maven 3.8+ | Standardized dependency management and executable packaging |
| **Database Management** | MySQL 8.0+ Server | Relational storage with 14 normalized tables & foreign key integrity |
| **Communication Bridge** | JDBC (Java Database Connectivity) | Parameterized `PreparedStatement` queries & transaction handling |
| **Theme & Styling** | FlatLaf 3.5.4 | Clean desktop look-and-feel with high-contrast UI geometry |

---

## 📂 Project Structure

```
SINST/
├── pom.xml                                 # Primary Maven project configuration
├── mvnw / mvnw.cmd                         # Standard cross-platform Maven Wrappers
├── .mvn/wrapper/                           # Maven Wrapper binary and properties
├── compile.bat                             # Standardized Windows compile & packaging script
├── run.bat                                 # Standardized Windows application launcher
├── test.bat                                # Standardized Windows automated test runner
├── db.properties                           # Local database connection configuration
├── db/
│   ├── README.md                           # Database architecture & migration reference
│   ├── database.sql                        # Complete, idempotent fresh setup script (14 tables)
│   ├── migration_v2.sql                    # Migration V2: Application unique constraints
│   ├── migration_v3.sql                    # Migration V3: 4-tier skill proficiencies & benchmarks
│   ├── migration_v4.sql                    # Migration V4: Cert-skill mappings & evidence tracking
│   ├── migration_v5.sql                    # Migration V5: Company-role pairings & recommendations
│   └── migration_v6.sql                    # Migration V6: Data validation, IDOR authorization, composite keys
├── src/
│   └── com/
│       └── sinst/
│           ├── Main.java                   # Desktop Application Entry Point
│           ├── db/
│           │   ├── DBConnection.java       # Database connection pool manager & settings
│           │   └── AppDAO.java             # Consolidated PreparedStatement operations
│           ├── dao/                        # Dedicated domain Data Access Objects
│           ├── model/                      # Domain entities (Student, Application, Role, Skill, etc.)
│           ├── service/                    # Business logic and transaction boundary services
│           ├── ui/
│           │   ├── LoginFrame.java         # Login, Registration & DB Configuration Dialog
│           │   └── MainFrame.java          # Tabbed Management Dashboard with status bar
│           ├── util/
│           │   ├── AppExecutor.java        # Managed background execution & EDT safety
│           │   ├── ValidationUtil.java     # Centralized domain input validation & sanitization
│           │   └── UserSession.java        # Thread-safe authenticated user session
│           └── test/
│               └── SinstTestSuite.java     # Automated test suite (425 validation & model tests)
└── README.md                               # Project documentation
```

---

## 📋 Prerequisites

Before building and running SINST from a clean checkout, ensure the following are installed:

1. **Java Development Kit (JDK) 21 or higher**:
   - Verify in terminal: `java -version` and `javac -version`
   - Download: [Oracle JDK 21+](https://www.oracle.com/java/technologies/downloads/) or [Eclipse Temurin 21+](https://adoptium.net/)
   - Ensure `JAVA_HOME` is set in your environment variables.

2. **MySQL Server 8.0 or higher**:
   - Verify the MySQL service is running locally on port `3306`.
   - Download: [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

3. **Apache Maven (Optional if using bundled wrapper)**:
   - SINST bundles the official Maven Wrapper (`mvnw.cmd` on Windows, `mvnw` on Linux/macOS).
   - If using system Maven, ensure Maven 3.8+ is installed (`mvn -version`).

---

## 🗄️ Database Initialization

### 1. Fresh Database Setup (Clean Checkout)

Open **MySQL Command Line Client** or **MySQL Workbench**, then execute:

```bash
mysql -u root -p < db/database.sql
```

*(This safely creates `sinst_db`, initializes all 14 normalized tables, sets foreign key constraints, and seeds standard industry roles, skills, and sample test profiles).*

> [!NOTE]
> `database.sql` is completely **idempotent** (`CREATE TABLE IF NOT EXISTS` and `INSERT IGNORE`). It will **not** silently overwrite or delete existing records.

### 2. Upgrading an Existing Installation

If you already have an earlier version of `sinst_db`, run the versioned migration scripts in sequential order:

```bash
mysql -u root -p sinst_db < db/migration_v2.sql
mysql -u root -p sinst_db < db/migration_v3.sql
mysql -u root -p sinst_db < db/migration_v4.sql
mysql -u root -p sinst_db < db/migration_v5.sql
mysql -u root -p sinst_db < db/migration_v6.sql
```

For complete details on table relationships, schema versions, and verification queries, consult [`db/README.md`](db/README.md).

### 3. Local Database Connectivity Configuration

Configure your local MySQL credentials using either approach:

**Method A: Edit `db.properties` directly in the project root:**
```properties
# SINST Database Configuration
db.host=localhost
db.port=3306
db.name=sinst_db
db.user=root
db.password=your_mysql_password
```

**Method B: In-App Database Settings Dialog:**
1. Launch the application.
2. Click the **"Database Settings"** button in the lower right of the login window.
3. Enter your host, port, user, and password, test the connection, and save.

---

## ⚙️ Building the Application

SINST uses **Maven** as its primary build system. Choose your preferred build command:

### Option A: One-Click Windows Script
Double-click or run:
```cmd
compile.bat
```
*(Automatically verifies Java and Maven, executes `mvn clean package`, runs the automated test suite, and outputs the packaged artifact).*

### Option B: Using Maven Wrapper (No Maven Installation Required)
```cmd
call mvnw.cmd clean package
```
*(On Linux/macOS: `./mvnw clean package`)*

### Option C: Using System Maven
```bash
mvn clean package
```

### Standardized Build Artifacts
Upon a successful build, Maven produces:
- **`target/sinst-1.0.0.jar`**: A self-contained executable Uber/Fat JAR containing all application classes, FlatLaf styling, and the MySQL JDBC driver.
- **`target/dependency/`**: All resolved runtime library dependencies.

---

## 🚀 Launching the Application

### Option A: One-Click Windows Launcher
Double-click or run:
```cmd
run.bat
```
*(If the application has not been compiled yet, `run.bat` automatically calls `compile.bat` first).*

### Option B: Launching the Packaged JAR
```bash
java -jar target/sinst-1.0.0.jar
```

### Option C: Launching via Maven
```bash
mvn exec:java
# or using the wrapper:
call mvnw.cmd exec:java
```

---

## 🧪 Automated Testing

SINST includes a modern, comprehensive **JUnit 5 Jupiter** test suite covering unit validation, transparent mathematical ranking, and database integration tests under `test/`.

### Run Tests via Windows Script
```cmd
test.bat
```

### Run Tests via Maven
```bash
mvn test
# or using the Maven wrapper:
call mvnw.cmd test
```

### Test Suite Highlights
- **Input Validation (`InputValidationTest`)**: Strict regex email syntax, student roll number bounds, names, year of study (1–4), phone numbers, chronological date/timestamp bounds, negative stipend detection, URL validation, and user-friendly error sanitization.
- **Proficiency & Skill Gap (`ProficiencyAndSkillGapTest`)**: 4-tier domain scale (`None [0]` to `Expert [4]`), level gap calculations (`required - candidate`), fractional match values ($[0.0, 1.0]$), requirement status categorization, and weighted scoring formula via `SkillGapService`.
- **Certification-to-Skill Mapping (`CertificationMappingTest`)**: 1:M cardinality between certifications and skills, learning outcomes, and unknown certification graceful handling.
- **Evidence Review & Tracking (`EvidenceTrackingTest`)**: `SkillEvidence` model, verification statuses (`Verified`, `Unverified`, `Rejected`), and demonstrated proficiency preservation rule (never downgrade higher existing ability).
- **Recommendation Ranking (`RecommendationRankingTest`)**: Weighted role match formula ($w_{mandatory} = 2.0, w_{optional} = 1.0$), configurable weights (`SkillMatchConfig`), 4-tier deterministic ranking (`compareTo`), truthful reporting (`Potential Role Match`, 0% for empty roles), and deduplication.
- **Application Lifecycle (`ApplicationLifecycleTest`)**: Valid status transitions (`Applied`, `Under Review`, `Shortlisted`, `Interview`, `Accepted`, `Selected`, `Rejected`, `Withdrawn`), arbitrary status rejection, interview round bounds (1–20), interview modes, interview statuses, and chronological ordering.
- **Service Authorization (`ServiceAuthorizationTest`)**: `UserSession` state tracking, IDOR cross-student manipulation defense (`UnauthorizedAccessException`), authorized owner access, administrative override, and duplicate entity exceptions (`DuplicateEntityException`).
- **Swing Responsiveness & Concurrency (`ConcurrencyAndResponsivenessTest`)**: Event Dispatch Thread safety, background thread pool isolation, `onSuccess`/`onError` callback dispatch on the EDT, button disabling/re-enabling in `finally` blocks, and cancellation of superseded asynchronous tasks.
- **DAO & Integration Tests (`DaoIntegrationTest`)**: Isolated test fixtures (`TEST_INTEG_001`), CRUD operations, foreign-key constraints, unique constraints, transaction rollback, duplicate prevention, student data ownership, and graceful offline fallback (`Assumptions.assumeTrue(...)`).
- **Comprehensive Historical Regression (`ComprehensiveRegressionTest`)**: Full regression bridge asserting all 425 historical domain checks to ensure 100% backward compatibility.

---

## 🔧 Troubleshooting Common Configuration Errors

### 1. `Communications link failure` / `Connection refused`
- **Cause**: MySQL Server is not running or is listening on a different port.
- **Fix**: Open Windows Services (`services.msc`), find **MySQL80**, and click **Start**. Ensure the port in `db.properties` matches your MySQL port (default: `3306`).

### 2. `Access denied for user 'root'@'localhost'`
- **Cause**: The password specified in `db.properties` does not match your MySQL root account password.
- **Fix**: Update the `db.password` field in `db.properties` or launch `run.bat` and configure credentials via the **Database Settings** dialog.

### 3. `Java runtime not found on PATH` or `Unsupported class file version`
- **Cause**: Java is not installed, not on PATH, or an older Java version (< 21) is active.
- **Fix**: Install JDK 21+ and set `JAVA_HOME` pointing to your JDK folder (e.g. `C:\Program Files\Java\jdk-21`). Add `%JAVA_HOME%\bin` to your system `PATH`.

### 4. `Neither Apache Maven nor Maven Wrapper was found`
- **Cause**: Running on a clean checkout without global Maven installed and without running from the repository root.
- **Fix**: Execute `compile.bat` or `mvnw.cmd` directly from the repository root directory.

### 5. `Unknown column 'careers_url'` or `Table 'sinst_db.company_roles' doesn't exist`
- **Cause**: Running against an existing database that was initialized with an earlier schema version.
- **Fix**: Apply the versioned migration scripts `migration_v2.sql` through `migration_v6.sql` sequentially as documented in the Database Setup section.

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
