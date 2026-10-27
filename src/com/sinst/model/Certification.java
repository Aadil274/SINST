package com.sinst.model;

import java.sql.Date;

public class Certification {
    private int certificationId;
    private int studentId;
    private String certificateName;
    private String issuingOrganization;
    private Date issueDate;
    private Date expiryDate;
    private String credentialId;
    private String credentialUrl;
    private java.util.List<StudentCertificationSkill> coveredSkills = new java.util.ArrayList<>();

    public Certification() {}

    public Certification(int certificationId, int studentId, String certificateName, String issuingOrganization, Date issueDate, String credentialId) {
        this(certificationId, studentId, certificateName, issuingOrganization, issueDate, null, credentialId, null);
    }

    public Certification(int certificationId, int studentId, String certificateName, String issuingOrganization, Date issueDate, Date expiryDate, String credentialId, String credentialUrl) {
        this.certificationId = certificationId;
        this.studentId = studentId;
        this.certificateName = certificateName;
        this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.credentialId = credentialId;
        this.credentialUrl = credentialUrl;
    }

    public int getCertificationId() { return certificationId; }
    public void setCertificationId(int certificationId) { this.certificationId = certificationId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getCertificateName() { return certificateName; }
    public void setCertificateName(String certificateName) { this.certificateName = certificateName; }

    public String getIssuingOrganization() { return issuingOrganization; }
    public void setIssuingOrganization(String issuingOrganization) { this.issuingOrganization = issuingOrganization; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    public String getCredentialUrl() { return credentialUrl; }
    public void setCredentialUrl(String credentialUrl) { this.credentialUrl = credentialUrl; }

    public java.util.List<StudentCertificationSkill> getCoveredSkills() { return coveredSkills; }
    public void setCoveredSkills(java.util.List<StudentCertificationSkill> coveredSkills) { this.coveredSkills = coveredSkills != null ? coveredSkills : new java.util.ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Certification that = (Certification) o;
        return certificationId != 0 && certificationId == that.certificationId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(certificationId);
    }

    @Override
    public String toString() {
        return certificateName + " (" + issuingOrganization + ")";
    }
}
