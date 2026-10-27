package com.sinst.model;

public class Project {
    private int projectId;
    private int studentId;
    private String title;
    private String description;
    private String technologiesUsed;
    private String projectUrl;
    private String githubUrl;
    private String liveUrl;

    public Project() {}

    public Project(int projectId, int studentId, String title, String description, String technologiesUsed, String projectUrl) {
        this(projectId, studentId, title, description, technologiesUsed, projectUrl, projectUrl, null);
    }

    public Project(int projectId, int studentId, String title, String description, String technologiesUsed, String projectUrl, String githubUrl, String liveUrl) {
        this.projectId = projectId;
        this.studentId = studentId;
        this.title = title;
        this.description = description;
        this.technologiesUsed = technologiesUsed;
        this.projectUrl = projectUrl;
        this.githubUrl = (githubUrl != null && !githubUrl.trim().isEmpty()) ? githubUrl : projectUrl;
        this.liveUrl = liveUrl;
    }

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTechnologiesUsed() { return technologiesUsed; }
    public void setTechnologiesUsed(String technologiesUsed) { this.technologiesUsed = technologiesUsed; }

    public String getProjectUrl() { return projectUrl; }
    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
        if (this.githubUrl == null) this.githubUrl = projectUrl;
    }

    public String getGithubUrl() { return githubUrl != null ? githubUrl : projectUrl; }
    public void setGithubUrl(String githubUrl) { this.githubUrl = githubUrl; }

    public String getLiveUrl() { return liveUrl; }
    public void setLiveUrl(String liveUrl) { this.liveUrl = liveUrl; }

    public String getProjectTitle() { return title; }
    public void setProjectTitle(String title) { this.title = title; }

    public String getTechStack() { return technologiesUsed; }
    public void setTechStack(String techStack) { this.technologiesUsed = techStack; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId != 0 && projectId == project.projectId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(projectId);
    }

    @Override
    public String toString() {
        return title;
    }
}
