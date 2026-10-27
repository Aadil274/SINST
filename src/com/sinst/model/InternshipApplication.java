package com.sinst.model;

import java.sql.Date;

public class InternshipApplication extends Application {

    public InternshipApplication() {
        super();
    }

    public InternshipApplication(int applicationId, int studentId, int companyId, int roleId, Date applicationDate, Date deadline, String status, String stipend, String notes) {
        super(applicationId, studentId, companyId, roleId, applicationDate, deadline, status, stipend, notes);
    }

    public InternshipApplication(int applicationId, int studentId, int companyId, int roleId, Date applicationDate, Date deadline, String status, String stipend, String notes, Company company, Role role) {
        super(applicationId, studentId, companyId, roleId, applicationDate, deadline, status, stipend, notes);
        setCompany(company);
        setRole(role);
    }

    public InternshipApplication(Application app) {
        super(app.getApplicationId(), app.getStudentId(), app.getCompanyId(), app.getRoleId(), app.getApplicationDate(), app.getDeadline(), app.getStatus(), app.getStipend(), app.getNotes());
        setCompanyName(app.getCompanyName());
        setRoleTitle(app.getRoleTitle());
        setCompany(app.getCompany());
        setRole(app.getRole());
    }
}
