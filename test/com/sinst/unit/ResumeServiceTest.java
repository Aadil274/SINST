package com.sinst.unit;

import com.sinst.model.*;
import com.sinst.service.ResumeService;
import com.sinst.service.ResumeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests: ATS Resume Generation & Formatting")
public class ResumeServiceTest {

    private ResumeService resumeService;
    private Student sampleStudent;
    private List<Skill> sampleSkills;
    private List<Project> sampleProjects;
    private List<Certification> sampleCerts;
    private List<Application> sampleApps;
    private List<Interview> sampleInterviews;
    private ResumeData sampleResumeData;

    @BeforeEach
    void setUp() {
        resumeService = new ResumeServiceImpl();

        sampleStudent = new Student(1, "25R11A0501", "Aadil Shaik", "aadil@example.com", "password", "CSE", 3, "+91 9876543210");

        sampleSkills = new ArrayList<>();
        sampleSkills.add(new Skill(1, "Java", "Programming", "Core Java and OOP", "Expert"));
        sampleSkills.add(new Skill(2, "SQL", "Databases", "Relational querying", "Advanced"));
        sampleSkills.add(new Skill(3, "Spring Boot", "Frameworks", "REST APIs", "Intermediate"));
        sampleSkills.add(new Skill(4, "Docker", "DevOps", "Containerization", "Beginner"));

        sampleProjects = new ArrayList<>();
        sampleProjects.add(new Project(
            1, 1, "Smart Internship Tracker", "Desktop management and career tracker system",
            "Java, Swing, MySQL", "https://github.com/Aadil274/SINST"
        ));

        sampleCerts = new ArrayList<>();
        sampleCerts.add(new Certification(
            1, 1, "Oracle Certified Professional", "Oracle", Date.valueOf("2026-03-15"), "OCP-8871"
        ));

        sampleApps = new ArrayList<>();
        Application app = new Application(
            1, 1, 10, 20, Date.valueOf("2026-01-20"), Date.valueOf("2026-02-28"), "Selected", "₹ 40,000 / mo", "Accepted offer"
        );
        app.setCompanyName("Google");
        app.setRoleTitle("Software Engineering Intern");
        sampleApps.add(app);

        sampleInterviews = new ArrayList<>();

        sampleResumeData = new ResumeData(sampleStudent, sampleSkills, sampleProjects, sampleCerts, sampleApps, sampleInterviews);
    }

    @Test
    @DisplayName("Plain text resume contains student details, skills, projects, and certifications")
    void testPlainTextGeneration() {
        String text = resumeService.generatePlainText(sampleResumeData);
        assertNotNull(text);
        assertTrue(text.contains("AADIL SHAIK"));
        assertTrue(text.contains("25R11A0501"));
        assertTrue(text.contains("aadil@example.com"));
        assertTrue(text.contains("Year 3"));
        assertTrue(text.contains("Java"));
        assertTrue(text.contains("Smart Internship Tracker"));
        assertTrue(text.contains("Oracle Certified Professional"));
        assertTrue(text.contains("Google"));
        assertTrue(text.contains("Software Engineering Intern"));
        assertTrue(text.contains("[Status: Selected]"));
    }

    @Test
    @DisplayName("Markdown resume has valid headings, links, and code blocks")
    void testMarkdownGeneration() {
        String md = resumeService.generateMarkdown(sampleResumeData);
        assertNotNull(md);
        assertTrue(md.startsWith("# Aadil Shaik"));
        assertTrue(md.contains("## 🎯 Professional Summary"));
        assertTrue(md.contains("## 🎓 Education"));
        assertTrue(md.contains("## 💻 Technical Competencies"));
        assertTrue(md.contains("## 🚀 Academic & Software Projects"));
        assertTrue(md.contains("## 📜 Professional Certifications"));
        assertTrue(md.contains("## 💼 Internship Trajectory"));
        assertTrue(md.contains("https://github.com/Aadil274/SINST"));
        assertTrue(md.contains("`OCP-8871`"));
    }

    @Test
    @DisplayName("HTML resume is standalone, styled, and contains required sections")
    void testHtmlGeneration() {
        String html = resumeService.generateHtml(sampleResumeData);
        assertNotNull(html);
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<title>Resume - Aadil Shaik</title>"));
        assertTrue(html.contains("Aadil Shaik"));
        assertTrue(html.contains("25R11A0501"));
        assertTrue(html.contains("class='skill-pill'"));
        assertTrue(html.contains("class='project-card'"));
        assertTrue(html.contains("class='cert-item'"));
        assertTrue(html.contains("@media print"));
    }

    @Test
    @DisplayName("ResumeData properly partitions skills by proficiency level")
    void testSkillPartitioning() {
        List<Skill> expert = sampleResumeData.getSkillsByProficiency(ProficiencyLevel.EXPERT);
        assertEquals(1, expert.size());
        assertEquals("Java", expert.get(0).getSkillName());

        List<Skill> adv = sampleResumeData.getSkillsByProficiency(ProficiencyLevel.ADVANCED);
        assertEquals(1, adv.size());
        assertEquals("SQL", adv.get(0).getSkillName());

        List<Skill> inter = sampleResumeData.getSkillsByProficiency(ProficiencyLevel.INTERMEDIATE);
        assertEquals(1, inter.size());
        assertEquals("Spring Boot", inter.get(0).getSkillName());

        List<Skill> beg = sampleResumeData.getSkillsByProficiency(ProficiencyLevel.BEGINNER);
        assertEquals(1, beg.size());
        assertEquals("Docker", beg.get(0).getSkillName());
    }

    @Test
    @DisplayName("ResumeData properly groups skills by category")
    void testSkillGroupingByCategory() {
        var grouped = sampleResumeData.getSkillsGroupedByCategory();
        assertTrue(grouped.containsKey("Programming"));
        assertTrue(grouped.containsKey("Databases"));
        assertTrue(grouped.containsKey("Frameworks"));
        assertTrue(grouped.containsKey("DevOps"));
        assertEquals("Java", grouped.get("Programming").get(0).getSkillName());
    }

    @Test
    @DisplayName("Empty resume data formats gracefully without exceptions")
    void testEmptyResumeData() {
        ResumeData emptyData = new ResumeData(sampleStudent, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        String plain = resumeService.generatePlainText(emptyData);
        assertNotNull(plain);
        assertTrue(plain.contains("AADIL SHAIK"));

        String md = resumeService.generateMarkdown(emptyData);
        assertNotNull(md);
        assertTrue(md.contains("Aadil Shaik"));

        String html = resumeService.generateHtml(emptyData);
        assertNotNull(html);
        assertTrue(html.contains("Aadil Shaik"));
    }

    @Test
    @DisplayName("Null resume data returns safe fallback without NullPointerException")
    void testNullResumeDataHandling() {
        assertEquals("No resume data available.", resumeService.generatePlainText(null));
        assertEquals("No resume data available.", resumeService.generateMarkdown(null));
        assertTrue(resumeService.generateHtml(null).contains("No resume data available."));
    }

    @Test
    @DisplayName("HTML resume escapes special characters safely")
    void testHtmlEscaping() {
        Student evilStudent = new Student(2, "25R11A0502", "<script>alert('xss')</script>", "test&user@example.com", "password", "CSE & IT", 2, "12345");
        ResumeData evilData = new ResumeData(evilStudent, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

        String html = resumeService.generateHtml(evilData);
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("test&amp;user@example.com"));
        assertTrue(html.contains("CSE &amp; IT"));
    }
}
