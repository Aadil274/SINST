package com.sinst.model;

import java.sql.Date;

public class Certification {
    private int certificationId;
    private int studentId;
    private String certificateName;
    private String issuingOrganization;
    private Date issueDate;
    private String credentialId;

    public Certification() {}

    public Certification(int certificationId, int studentId, String certificateName, String issuingOrganization, Date issueDate, String credentialId) {
        this.certificationId = certificationId;
        this.studentId = studentId;
        this.certificateName = certificateName;
        this.issuingOrganization = issuingOrganization;
        this.issueDate = issueDate;
        this.credentialId = credentialId;
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

    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }

    @Override
    public String toString() {
        return certificateName + " (" + issuingOrganization + ")";
    }
}
