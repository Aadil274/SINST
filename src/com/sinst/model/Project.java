package com.sinst.model;

public class Project {
    private int projectId;
    private int studentId;
    private String title;
    private String description;
    private String technologiesUsed;
    private String projectUrl;

    public Project() {}

    public Project(int projectId, int studentId, String title, String description, String technologiesUsed, String projectUrl) {
        this.projectId = projectId;
        this.studentId = studentId;
        this.title = title;
        this.description = description;
        this.technologiesUsed = technologiesUsed;
        this.projectUrl = projectUrl;
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
    public void setProjectUrl(String projectUrl) { this.projectUrl = projectUrl; }

    @Override
    public String toString() {
        return title;
    }
}
