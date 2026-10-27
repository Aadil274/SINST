package com.sinst.service;

import com.sinst.dao.ApplicationDAO;
import com.sinst.dao.ApplicationDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Application;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationDAO applicationDAO;

    public ApplicationServiceImpl() {
        this(ApplicationDAOImpl.getInstance());
    }

    public ApplicationServiceImpl(ApplicationDAO applicationDAO) {
        this.applicationDAO = applicationDAO;
    }

    @Override
    public List<Application> getApplications(int studentId) throws DatabaseException {
        try {
            return applicationDAO.getByStudentId(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving applications: " + e.getMessage(), e);
        }
    }

    @Override
    public void submitApplication(Application app) throws ValidationException, DatabaseException, DuplicateEntityException {
        if (app == null) {
            throw new ValidationException("Application data cannot be null.");
        }
        if (app.getStudentId() <= 0) {
            throw new ValidationException("Application must be associated with an authenticated student.");
        }
        if (app.getCompanyId() <= 0 || app.getRoleId() <= 0) {
            throw new ValidationException("Please select both a target company and a role.");
        }
        validateApplicationDetails(app);

        try {
            if (applicationDAO.hasApplication(app.getStudentId(), app.getCompanyId(), app.getRoleId())) {
                throw new DuplicateEntityException("You have already submitted an application for this role at this company.");
            }

            boolean added = applicationDAO.add(app);
            if (!added) {
                throw new DatabaseException("Failed to record application.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_student_company_role")) {
                throw new DuplicateEntityException("An application for this company and role already exists.");
            }
            throw new DatabaseException("Database error adding application: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void updateApplication(Application app) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        int studentId = app != null && app.getStudentId() > 0 ? app.getStudentId() : UserSession.getCurrentStudentId();
        updateApplication(app, studentId);
    }

    @Override
    public void updateApplication(Application app, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (app == null || app.getApplicationId() <= 0) {
            throw new ValidationException("Invalid application record for update.");
        }
        validateApplicationDetails(app);

        try {
            Application existing = applicationDAO.getById(app.getApplicationId());
            if (existing == null) {
                throw new DatabaseException("Application could not be found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this application.");
            }

            boolean updated = applicationDAO.update(app, studentId > 0 ? studentId : existing.getStudentId());
            if (!updated) {
                throw new DatabaseException("Application could not be updated.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating application: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void updateStatus(int applicationId, String status) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        updateStatus(applicationId, UserSession.getCurrentStudentId(), status);
    }

    @Override
    public void updateStatus(int applicationId, int studentId, String status) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (applicationId <= 0) {
            throw new ValidationException("Valid application ID is required.");
        }
        if (!ValidationUtil.isNonEmpty(status) || !ValidationUtil.isValidApplicationStatus(status)) {
            throw new ValidationException("Please provide a valid application status (e.g. Applied, Under Review, Shortlisted, Interview, Accepted, Rejected, Withdrawn).");
        }

        try {
            Application existing = applicationDAO.getById(applicationId);
            if (existing == null) {
                throw new DatabaseException("Application could not be found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this application.");
            }

            boolean updated = applicationDAO.updateStatus(applicationId, studentId > 0 ? studentId : existing.getStudentId(), status.trim());
            if (!updated) {
                throw new DatabaseException("Application status could not be updated.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating status: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void deleteApplication(int applicationId) throws DatabaseException, UnauthorizedAccessException {
        deleteApplication(applicationId, UserSession.getCurrentStudentId());
    }

    @Override
    public void deleteApplication(int applicationId, int studentId) throws DatabaseException, UnauthorizedAccessException {
        if (applicationId <= 0) return;

        try {
            Application existing = applicationDAO.getById(applicationId);
            if (existing == null) {
                throw new DatabaseException("Application not found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to delete this application.");
            }

            boolean deleted = applicationDAO.delete(applicationId, studentId > 0 ? studentId : existing.getStudentId());
            if (!deleted) {
                throw new DatabaseException("Application could not be deleted.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error deleting application: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    private void validateApplicationDetails(Application app) throws ValidationException {
        if (ValidationUtil.isNonEmpty(app.getStatus()) && !ValidationUtil.isValidApplicationStatus(app.getStatus())) {
            throw new ValidationException("Invalid application status: " + app.getStatus());
        }
        if (app.getDeadline() != null) {
            if (!ValidationUtil.isValidDate(app.getDeadline().toString())) {
                throw new ValidationException("Please enter a valid deadline in YYYY-MM-DD format.");
            }
            if (app.getApplicationDate() != null && !ValidationUtil.isValidDateRange(app.getApplicationDate(), app.getDeadline())) {
                throw new ValidationException("Application deadline (" + app.getDeadline() + ") cannot be before the application date (" + app.getApplicationDate() + ").");
            }
        }
        if (ValidationUtil.hasNegativeStipend(app.getStipend())) {
            throw new ValidationException("Stipend amount cannot be negative.");
        }
    }
}
