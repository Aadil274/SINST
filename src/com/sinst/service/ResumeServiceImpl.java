package com.sinst.service;

import com.sinst.dao.*;
import com.sinst.exception.DatabaseException;
import com.sinst.model.*;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.*;

public class ResumeServiceImpl implements ResumeService {

    private final StudentDAO studentDAO;
    private final SkillDAO skillDAO;
    private final ProjectDAO projectDAO;
    private final CertificationDAO certificationDAO;
    private final ApplicationDAO applicationDAO;
    private final InterviewDAO interviewDAO;

    public ResumeServiceImpl() {
        this(
            StudentDAOImpl.getInstance(),
            SkillDAOImpl.getInstance(),
            ProjectDAOImpl.getInstance(),
            CertificationDAOImpl.getInstance(),
            ApplicationDAOImpl.getInstance(),
            InterviewDAOImpl.getInstance()
        );
    }

    public ResumeServiceImpl(
            StudentDAO studentDAO,
            SkillDAO skillDAO,
            ProjectDAO projectDAO,
            CertificationDAO certificationDAO,
            ApplicationDAO applicationDAO,
            InterviewDAO interviewDAO) {
        this.studentDAO = studentDAO;
        this.skillDAO = skillDAO;
        this.projectDAO = projectDAO;
        this.certificationDAO = certificationDAO;
        this.applicationDAO = applicationDAO;
        this.interviewDAO = interviewDAO;
    }

    @Override
    public ResumeData buildResumeData(int studentId) throws DatabaseException {
        if (studentId <= 0) {
            throw new DatabaseException("Invalid student ID for resume compilation.");
        }
        try {
            Student student = studentDAO.getById(studentId);
            if (student == null) {
                throw new DatabaseException("Student with ID " + studentId + " not found.");
            }
            List<Skill> skills = skillDAO.getStudentSkills(studentId);
            List<Project> projects = projectDAO.getByStudentId(studentId);
            List<Certification> certifications = certificationDAO.getByStudentId(studentId);
            List<Application> applications = applicationDAO.getByStudentId(studentId);
            List<Interview> interviews = interviewDAO.getByStudentId(studentId);

            return new ResumeData(student, skills, projects, certifications, applications, interviews);
        } catch (SQLException e) {
            throw new DatabaseException("Database error generating resume data: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public String generatePlainText(ResumeData data) {
        if (data == null || data.getStudent() == null) {
            return "No resume data available.";
        }
        Student s = data.getStudent();
        StringBuilder sb = new StringBuilder();

        String lineSep = "================================================================================";
        String subSep  = "--------------------------------------------------------------------------------";

        sb.append(lineSep).append("\n");
        sb.append("                               ").append(s.getFullName().toUpperCase()).append("\n");
        sb.append("  Email: ").append(s.getEmail());
        if (s.getPhone() != null && !s.getPhone().trim().isEmpty()) {
            sb.append(" | Phone: ").append(s.getPhone().trim());
        }
        sb.append(" | Roll No: ").append(s.getRollNumber()).append("\n");
        sb.append("  Department: ").append(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering");
        sb.append(" | Year of Study: Year ").append(s.getYearOfStudy()).append("\n");
        sb.append(lineSep).append("\n\n");

        sb.append("CAREER OBJECTIVE & PROFESSIONAL SUMMARY\n");
        sb.append(subSep).append("\n");
        sb.append("Motivated and detail-oriented undergraduate in ")
          .append(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering")
          .append(" with demonstrated competencies in core software engineering, algorithms, and applied technology.\n")
          .append("Seeking challenging internship and junior engineering opportunities to leverage technical proficiencies and solve real-world problems.\n\n");

        sb.append("EDUCATION\n");
        sb.append(subSep).append("\n");
        sb.append("Bachelor of Technology in ")
          .append(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering")
          .append("\n");
        sb.append("Standing: Year ").append(s.getYearOfStudy())
          .append("  |  Roll Number: ").append(s.getRollNumber()).append("\n\n");

        sb.append("TECHNICAL SKILLS & COMPETENCIES\n");
        sb.append(subSep).append("\n");
        List<Skill> expertSkills = data.getSkillsByProficiency(ProficiencyLevel.EXPERT);
        List<Skill> advSkills = data.getSkillsByProficiency(ProficiencyLevel.ADVANCED);
        List<Skill> intSkills = data.getSkillsByProficiency(ProficiencyLevel.INTERMEDIATE);
        List<Skill> begSkills = data.getSkillsByProficiency(ProficiencyLevel.BEGINNER);

        List<Skill> topSkills = new ArrayList<>();
        topSkills.addAll(expertSkills);
        topSkills.addAll(advSkills);

        if (!topSkills.isEmpty()) {
            sb.append("• Advanced & Expert: ");
            joinSkillNames(sb, topSkills);
            sb.append("\n");
        }
        if (!intSkills.isEmpty()) {
            sb.append("• Intermediate: ");
            joinSkillNames(sb, intSkills);
            sb.append("\n");
        }
        if (!begSkills.isEmpty()) {
            sb.append("• Foundational & Learning: ");
            joinSkillNames(sb, begSkills);
            sb.append("\n");
        }
        if (data.getSkills().isEmpty()) {
            sb.append("• General Computer Science principles, Data Structures, OOP Concepts\n");
        }
        sb.append("\n");

        if (!data.getProjects().isEmpty()) {
            sb.append("ACADEMIC & TECHNICAL PROJECTS\n");
            sb.append(subSep).append("\n");
            for (Project p : data.getProjects()) {
                sb.append("• ").append(p.getTitle());
                if (p.getTechnologiesUsed() != null && !p.getTechnologiesUsed().trim().isEmpty()) {
                    sb.append(" (Tech Stack: ").append(p.getTechnologiesUsed().trim()).append(")");
                }
                sb.append("\n");
                if (p.getDescription() != null && !p.getDescription().trim().isEmpty()) {
                    sb.append("  Description: ").append(p.getDescription().trim()).append("\n");
                }
                if (p.getProjectUrl() != null && !p.getProjectUrl().trim().isEmpty()) {
                    sb.append("  Project Link / Repo: ").append(p.getProjectUrl().trim()).append("\n");
                }
                sb.append("\n");
            }
        }

        if (!data.getCertifications().isEmpty()) {
            sb.append("PROFESSIONAL CERTIFICATIONS\n");
            sb.append(subSep).append("\n");
            for (Certification c : data.getCertifications()) {
                sb.append("• ").append(c.getCertificateName())
                  .append(" — Issued by ").append(c.getIssuingOrganization());
                if (c.getIssueDate() != null) {
                    sb.append(" (").append(ValidationUtil.formatDate(c.getIssueDate())).append(")");
                }
                if (c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty()) {
                    sb.append(" | ID: ").append(c.getCredentialId().trim());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (!data.getApplications().isEmpty()) {
            sb.append("INTERNSHIP PURSUITS & CAREER MILESTONES\n");
            sb.append(subSep).append("\n");
            for (Application a : data.getApplications()) {
                sb.append("• ").append(a.getCompanyName()).append(" — ").append(a.getRoleTitle())
                  .append(" [Status: ").append(a.getStatus()).append("]");
                if (a.getApplicationDate() != null) {
                    sb.append(" | Date: ").append(ValidationUtil.formatDate(a.getApplicationDate()));
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        sb.append(lineSep).append("\n");
        sb.append("Generated by SINST — Smart Internship & Skill Tracker System\n");
        return sb.toString();
    }

    @Override
    public String generateMarkdown(ResumeData data) {
        if (data == null || data.getStudent() == null) {
            return "No resume data available.";
        }
        Student s = data.getStudent();
        StringBuilder sb = new StringBuilder();

        sb.append("# ").append(s.getFullName()).append("\n\n");
        sb.append("**Email:** [").append(s.getEmail()).append("](mailto:").append(s.getEmail()).append(")");
        if (s.getPhone() != null && !s.getPhone().trim().isEmpty()) {
            sb.append(" &nbsp;|&nbsp; **Phone:** ").append(s.getPhone().trim());
        }
        sb.append(" &nbsp;|&nbsp; **Roll Number:** `").append(s.getRollNumber()).append("`\n\n");
        sb.append("**Department:** ").append(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering")
          .append(" &nbsp;|&nbsp; **Year of Study:** Year ").append(s.getYearOfStudy()).append("\n\n");
        sb.append("---\n\n");

        sb.append("## 🎯 Professional Summary\n\n");
        sb.append("Motivated and detail-oriented undergraduate in **")
          .append(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering")
          .append("** with validated competencies in software engineering and modern development paradigms. ")
          .append("Committed to engineering scalable, high-quality solutions and seeking impactful internship opportunities.\n\n");

        sb.append("## 🎓 Education\n\n");
        sb.append("- **Degree:** Bachelor of Technology in ")
          .append(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering").append("\n");
        sb.append("- **Current Standing:** Year ").append(s.getYearOfStudy()).append("\n");
        sb.append("- **Student ID / Roll No:** `").append(s.getRollNumber()).append("`\n\n");

        sb.append("## 💻 Technical Competencies & Skills\n\n");
        List<Skill> expertSkills = data.getSkillsByProficiency(ProficiencyLevel.EXPERT);
        List<Skill> advSkills = data.getSkillsByProficiency(ProficiencyLevel.ADVANCED);
        List<Skill> intSkills = data.getSkillsByProficiency(ProficiencyLevel.INTERMEDIATE);
        List<Skill> begSkills = data.getSkillsByProficiency(ProficiencyLevel.BEGINNER);

        List<Skill> topSkills = new ArrayList<>();
        topSkills.addAll(expertSkills);
        topSkills.addAll(advSkills);

        if (!topSkills.isEmpty()) {
            sb.append("- **Advanced & Expert:** ");
            joinSkillNames(sb, topSkills);
            sb.append("\n");
        }
        if (!intSkills.isEmpty()) {
            sb.append("- **Intermediate:** ");
            joinSkillNames(sb, intSkills);
            sb.append("\n");
        }
        if (!begSkills.isEmpty()) {
            sb.append("- **Foundational:** ");
            joinSkillNames(sb, begSkills);
            sb.append("\n");
        }
        if (data.getSkills().isEmpty()) {
            sb.append("- Object-Oriented Programming, Data Structures, Relational Databases (SQL)\n");
        }
        sb.append("\n");

        if (!data.getProjects().isEmpty()) {
            sb.append("## 🚀 Academic & Software Projects\n\n");
            for (Project p : data.getProjects()) {
                sb.append("### ").append(p.getTitle()).append("\n");
                if (p.getTechnologiesUsed() != null && !p.getTechnologiesUsed().trim().isEmpty()) {
                    sb.append("- **Tech Stack:** `").append(p.getTechnologiesUsed().trim()).append("`\n");
                }
                if (p.getDescription() != null && !p.getDescription().trim().isEmpty()) {
                    sb.append("- **Overview:** ").append(p.getDescription().trim()).append("\n");
                }
                if (p.getProjectUrl() != null && !p.getProjectUrl().trim().isEmpty()) {
                    sb.append("- **Repository / Demonstration:** [").append(p.getProjectUrl().trim()).append("](")
                      .append(p.getProjectUrl().trim()).append(")\n");
                }
                sb.append("\n");
            }
        }

        if (!data.getCertifications().isEmpty()) {
            sb.append("## 📜 Professional Certifications\n\n");
            for (Certification c : data.getCertifications()) {
                sb.append("- **").append(c.getCertificateName()).append("** — *").append(c.getIssuingOrganization()).append("*");
                if (c.getIssueDate() != null) {
                    sb.append(" (").append(ValidationUtil.formatDate(c.getIssueDate())).append(")");
                }
                if (c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty()) {
                    sb.append(" &bull; ID: `").append(c.getCredentialId().trim()).append("`");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        if (!data.getApplications().isEmpty()) {
            sb.append("## 💼 Internship Trajectory & Career Track\n\n");
            for (Application a : data.getApplications()) {
                sb.append("- **").append(a.getCompanyName()).append("** — ").append(a.getRoleTitle())
                  .append(" *(Status: ").append(a.getStatus()).append(")*\n");
            }
            sb.append("\n");
        }

        sb.append("---\n");
        sb.append("*Generated by Smart Internship & Skill Tracker (SINST)*\n");
        return sb.toString();
    }

    @Override
    public String generateHtml(ResumeData data) {
        if (data == null || data.getStudent() == null) {
            return "<html><body><p>No resume data available.</p></body></html>";
        }
        Student s = data.getStudent();
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='UTF-8'>\n");
        sb.append("<title>Resume - ").append(escapeHtml(s.getFullName())).append("</title>\n");
        sb.append("<style>\n");
        sb.append("body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.5; color: #1e293b; margin: 0; padding: 24px; background: #ffffff; }\n");
        sb.append(".header { border-bottom: 2px solid #1e3a8a; padding-bottom: 12px; margin-bottom: 18px; }\n");
        sb.append(".name { font-size: 26px; font-weight: 700; color: #1e3a8a; margin: 0 0 6px 0; }\n");
        sb.append(".contact { font-size: 13px; color: #475569; margin: 2px 0; }\n");
        sb.append(".section { margin-bottom: 20px; }\n");
        sb.append(".section-title { font-size: 15px; font-weight: 700; color: #0f172a; text-transform: uppercase; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px; margin-bottom: 8px; letter-spacing: 0.5px; }\n");
        sb.append(".project-card { margin-bottom: 12px; }\n");
        sb.append(".project-title { font-weight: 700; font-size: 14px; color: #1e293b; }\n");
        sb.append(".tech-badge { display: inline-block; background: #eff6ff; color: #1d4ed8; padding: 2px 6px; border-radius: 4px; font-size: 11px; font-weight: 600; margin-left: 6px; }\n");
        sb.append(".skill-category { font-weight: 700; color: #334155; margin-top: 4px; }\n");
        sb.append(".skill-pill { display: inline-block; background: #f1f5f9; color: #0f172a; padding: 3px 8px; border-radius: 12px; font-size: 12px; margin: 2px; border: 1px solid #e2e8f0; }\n");
        sb.append(".cert-item { margin-bottom: 6px; font-size: 13px; }\n");
        sb.append(".cert-name { font-weight: 600; color: #1e293b; }\n");
        sb.append(".cert-org { color: #64748b; font-style: italic; }\n");
        sb.append(".footer { border-top: 1px solid #e2e8f0; padding-top: 8px; margin-top: 24px; font-size: 11px; color: #94a3b8; text-align: center; }\n");
        sb.append("@media print { body { padding: 0; } }\n");
        sb.append("</style>\n</head>\n<body>\n");

        // Header
        sb.append("<div class='header'>\n");
        sb.append("  <div class='name'>").append(escapeHtml(s.getFullName())).append("</div>\n");
        sb.append("  <div class='contact'>\n");
        sb.append("    <span><b>Email:</b> ").append(escapeHtml(s.getEmail())).append("</span> &nbsp;|&nbsp; \n");
        if (s.getPhone() != null && !s.getPhone().trim().isEmpty()) {
            sb.append("    <span><b>Phone:</b> ").append(escapeHtml(s.getPhone().trim())).append("</span> &nbsp;|&nbsp; \n");
        }
        sb.append("    <span><b>Roll No:</b> ").append(escapeHtml(s.getRollNumber())).append("</span>\n");
        sb.append("  </div>\n");
        sb.append("  <div class='contact'>\n");
        sb.append("    <span><b>Department:</b> ").append(escapeHtml(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering")).append("</span> &nbsp;|&nbsp; \n");
        sb.append("    <span><b>Year:</b> Year ").append(s.getYearOfStudy()).append("</span>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");

        // Summary
        sb.append("<div class='section'>\n");
        sb.append("  <div class='section-title'>Career Objective & Summary</div>\n");
        sb.append("  <p style='font-size: 13px; margin: 4px 0;'>Motivated undergraduate in <b>")
          .append(escapeHtml(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering"))
          .append("</b> with proven technical competence in software engineering and database systems. Seeking internship and entry-level roles to contribute to impactful software products while expanding engineering depth.</p>\n");
        sb.append("</div>\n");

        // Education
        sb.append("<div class='section'>\n");
        sb.append("  <div class='section-title'>Education</div>\n");
        sb.append("  <div style='font-size: 13px;'><b>Bachelor of Technology in ")
          .append(escapeHtml(s.getDepartment() != null ? s.getDepartment() : "Computer Science & Engineering"))
          .append("</b> &nbsp;&bull;&nbsp; Standing: Year ").append(s.getYearOfStudy())
          .append(" &nbsp;&bull;&nbsp; Roll Number: ").append(escapeHtml(s.getRollNumber()))
          .append("</div>\n");
        sb.append("</div>\n");

        // Technical Skills
        sb.append("<div class='section'>\n");
        sb.append("  <div class='section-title'>Technical Competencies</div>\n");
        List<Skill> expertSkills = data.getSkillsByProficiency(ProficiencyLevel.EXPERT);
        List<Skill> advSkills = data.getSkillsByProficiency(ProficiencyLevel.ADVANCED);
        List<Skill> intSkills = data.getSkillsByProficiency(ProficiencyLevel.INTERMEDIATE);
        List<Skill> begSkills = data.getSkillsByProficiency(ProficiencyLevel.BEGINNER);

        List<Skill> topSkills = new ArrayList<>();
        topSkills.addAll(expertSkills);
        topSkills.addAll(advSkills);

        if (!topSkills.isEmpty()) {
            sb.append("  <div style='margin-bottom: 6px;'><span class='skill-category'>Advanced / Expert:</span> ");
            for (Skill sk : topSkills) {
                sb.append("<span class='skill-pill' style='background: #dcfce7; color: #166534; border-color: #bbf7d0;'>")
                  .append(escapeHtml(sk.getSkillName())).append("</span>");
            }
            sb.append("</div>\n");
        }
        if (!intSkills.isEmpty()) {
            sb.append("  <div style='margin-bottom: 6px;'><span class='skill-category'>Intermediate:</span> ");
            for (Skill sk : intSkills) {
                sb.append("<span class='skill-pill'>").append(escapeHtml(sk.getSkillName())).append("</span>");
            }
            sb.append("</div>\n");
        }
        if (!begSkills.isEmpty()) {
            sb.append("  <div style='margin-bottom: 6px;'><span class='skill-category'>Foundational:</span> ");
            for (Skill sk : begSkills) {
                sb.append("<span class='skill-pill' style='background: #f8fafc; color: #64748b;'>")
                  .append(escapeHtml(sk.getSkillName())).append("</span>");
            }
            sb.append("</div>\n");
        }
        if (data.getSkills().isEmpty()) {
            sb.append("  <p style='font-size: 13px; color: #64748b;'>Object-Oriented Programming, Data Structures, Relational Databases (SQL)</p>\n");
        }
        sb.append("</div>\n");

        // Projects
        if (!data.getProjects().isEmpty()) {
            sb.append("<div class='section'>\n");
            sb.append("  <div class='section-title'>Academic & Software Projects</div>\n");
            for (Project p : data.getProjects()) {
                sb.append("  <div class='project-card'>\n");
                sb.append("    <span class='project-title'>").append(escapeHtml(p.getTitle())).append("</span>\n");
                if (p.getTechnologiesUsed() != null && !p.getTechnologiesUsed().trim().isEmpty()) {
                    sb.append("    <span class='tech-badge'>").append(escapeHtml(p.getTechnologiesUsed().trim())).append("</span>\n");
                }
                if (p.getDescription() != null && !p.getDescription().trim().isEmpty()) {
                    sb.append("    <div style='font-size: 13px; color: #334155; margin-top: 3px;'>")
                      .append(escapeHtml(p.getDescription().trim())).append("</div>\n");
                }
                if (p.getProjectUrl() != null && !p.getProjectUrl().trim().isEmpty()) {
                    sb.append("    <div style='font-size: 12px; margin-top: 2px;'><a href='")
                      .append(escapeHtml(p.getProjectUrl().trim())).append("' target='_blank' style='color: #2563eb;'>")
                      .append(escapeHtml(p.getProjectUrl().trim())).append("</a></div>\n");
                }
                sb.append("  </div>\n");
            }
            sb.append("</div>\n");
        }

        // Certifications
        if (!data.getCertifications().isEmpty()) {
            sb.append("<div class='section'>\n");
            sb.append("  <div class='section-title'>Certifications & Credentials</div>\n");
            for (Certification c : data.getCertifications()) {
                sb.append("  <div class='cert-item'>\n");
                sb.append("    <span class='cert-name'>").append(escapeHtml(c.getCertificateName())).append("</span> &mdash; \n");
                sb.append("    <span class='cert-org'>").append(escapeHtml(c.getIssuingOrganization())).append("</span>\n");
                if (c.getIssueDate() != null) {
                    sb.append("    <span style='color: #64748b;'>(").append(ValidationUtil.formatDate(c.getIssueDate())).append(")</span>\n");
                }
                if (c.getCredentialId() != null && !c.getCredentialId().trim().isEmpty()) {
                    sb.append("    <span style='font-size: 11px; background: #f1f5f9; padding: 1px 5px; border-radius: 3px; margin-left: 4px;'>ID: ")
                      .append(escapeHtml(c.getCredentialId().trim())).append("</span>\n");
                }
                sb.append("  </div>\n");
            }
            sb.append("</div>\n");
        }

        // Applications & Milestones
        if (!data.getApplications().isEmpty()) {
            sb.append("<div class='section'>\n");
            sb.append("  <div class='section-title'>Internship Trajectory & Pursuits</div>\n");
            for (Application a : data.getApplications()) {
                sb.append("  <div style='font-size: 13px; margin-bottom: 4px;'>\n");
                sb.append("    <b>").append(escapeHtml(a.getCompanyName())).append("</b> &mdash; ")
                  .append(escapeHtml(a.getRoleTitle()))
                  .append(" &nbsp;<span style='color: #2563eb; font-weight: 600;'>[").append(escapeHtml(a.getStatus())).append("]</span>\n");
                sb.append("  </div>\n");
            }
            sb.append("</div>\n");
        }

        // Footer
        sb.append("<div class='footer'>Generated by Smart Internship & Skill Tracker (SINST) &bull; Verified Academic Competency Record</div>\n");
        sb.append("</body>\n</html>");
        return sb.toString();
    }

    private void joinSkillNames(StringBuilder sb, List<Skill> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(list.get(i).getSkillName());
        }
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&#39;");
    }
}
