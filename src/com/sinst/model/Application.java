package com.sinst.model;

import java.sql.Date;

public class Application {
    private int applicationId;
    private int studentId;
    private int companyId;
    private int roleId;
    private Date applicationDate;
    private Date deadline;
    private String status; // 'Applied', 'Shortlisted', 'Interview', 'Selected', 'Rejected'
    private String stipend;
    private String notes;

    // Display fields joined from other tables
    private String companyName;
    private String roleTitle;

    public Application() {}

    public Application(int applicationId, int studentId, int companyId, int roleId, Date applicationDate, Date deadline, String status, String stipend, String notes) {
        this.applicationId = applicationId;
        this.studentId = studentId;
        this.companyId = companyId;
        this.roleId = roleId;
        this.applicationDate = applicationDate;
        this.deadline = deadline;
        this.status = status;
        this.stipend = stipend;
        this.notes = notes;
    }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getCompanyId() { return companyId; }
    public void setCompanyId(int companyId) { this.companyId = companyId; }

    public int getRoleId() { return roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }

    public Date getApplicationDate() { return applicationDate; }
    public void setApplicationDate(Date applicationDate) { this.applicationDate = applicationDate; }

    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStipend() { return stipend; }
    public void setStipend(String stipend) { this.stipend = stipend; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    @Override
    public String toString() {
        return companyName + " - " + roleTitle + " (" + status + ")";
    }
}
