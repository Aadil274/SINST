package com.sinst.model;

import java.util.*;

/**
 * Encapsulates aggregated student resume profile data, including contact details,
 * verified skills, academic projects, certifications, and application milestones.
 */
public class ResumeData {

    private final Student student;
    private final List<Skill> skills;
    private final List<Project> projects;
    private final List<Certification> certifications;
    private final List<Application> applications;
    private final List<Interview> interviews;

    public ResumeData(
            Student student,
            List<Skill> skills,
            List<Project> projects,
            List<Certification> certifications,
            List<Application> applications,
            List<Interview> interviews) {
        this.student = student;
        this.skills = skills != null ? Collections.unmodifiableList(new ArrayList<>(skills)) : Collections.emptyList();
        this.projects = projects != null ? Collections.unmodifiableList(new ArrayList<>(projects)) : Collections.emptyList();
        this.certifications = certifications != null ? Collections.unmodifiableList(new ArrayList<>(certifications)) : Collections.emptyList();
        this.applications = applications != null ? Collections.unmodifiableList(new ArrayList<>(applications)) : Collections.emptyList();
        this.interviews = interviews != null ? Collections.unmodifiableList(new ArrayList<>(interviews)) : Collections.emptyList();
    }

    public Student getStudent() {
        return student;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Certification> getCertifications() {
        return certifications;
    }

    public List<Application> getApplications() {
        return applications;
    }

    public List<Interview> getInterviews() {
        return interviews;
    }

    public List<Skill> getSkillsByProficiency(ProficiencyLevel level) {
        if (level == null) return Collections.emptyList();
        List<Skill> result = new ArrayList<>();
        for (Skill s : skills) {
            ProficiencyLevel pl = ProficiencyLevel.fromString(s.getProficiencyLevel());
            if (pl == level) {
                result.add(s);
            }
        }
        return result;
    }

    public Map<String, List<Skill>> getSkillsGroupedByCategory() {
        Map<String, List<Skill>> map = new LinkedHashMap<>();
        for (Skill s : skills) {
            String category = s.getCategory() != null && !s.getCategory().trim().isEmpty()
                ? s.getCategory().trim()
                : "General";
            map.computeIfAbsent(category, k -> new ArrayList<>()).add(s);
        }
        return map;
    }

    public List<Application> getSelectedApplications() {
        List<Application> result = new ArrayList<>();
        for (Application a : applications) {
            if ("Selected".equalsIgnoreCase(a.getStatus())) {
                result.add(a);
            }
        }
        return result;
    }

    public int getVerifiedSkillCount() {
        return skills.size();
    }

    public int getCertificationCount() {
        return certifications.size();
    }

    public int getProjectCount() {
        return projects.size();
    }
}
