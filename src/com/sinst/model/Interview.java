package com.sinst.model;

import java.sql.Timestamp;

public class Interview {
    private int interviewId;
    private int applicationId;
    private Timestamp interviewDate;
    private String roundType; // e.g., 'Technical Round 1', 'HR', etc.
    private String mode; // 'Online', 'In-Person'
    private String status; // 'Scheduled', 'Completed', 'Cancelled', 'Rescheduled'
    private String outcome; // 'Pending', 'Passed', 'Failed'
    private String feedback;

    // Display fields
    private String companyName;
    private String roleTitle;

    public Interview() {}

    public Interview(int interviewId, int applicationId, Timestamp interviewDate, String roundType, String mode, String status, String outcome, String feedback) {
        this.interviewId = interviewId;
        this.applicationId = applicationId;
        this.interviewDate = interviewDate;
        this.roundType = roundType;
        this.mode = mode;
        this.status = status;
        this.outcome = outcome;
        this.feedback = feedback;
    }

    public int getInterviewId() { return interviewId; }
    public void setInterviewId(int interviewId) { this.interviewId = interviewId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public Timestamp getInterviewDate() { return interviewDate; }
    public void setInterviewDate(Timestamp interviewDate) { this.interviewDate = interviewDate; }

    public String getRoundType() { return roundType; }
    public void setRoundType(String roundType) { this.roundType = roundType; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRoleTitle() { return roleTitle; }
    public void setRoleTitle(String roleTitle) { this.roleTitle = roleTitle; }

    @Override
    public String toString() {
        return roundType + " - " + (companyName != null ? companyName : "App #" + applicationId);
    }
}
