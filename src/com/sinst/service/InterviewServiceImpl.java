package com.sinst.service;

import com.sinst.dao.ApplicationDAO;
import com.sinst.dao.ApplicationDAOImpl;
import com.sinst.dao.InterviewDAO;
import com.sinst.dao.InterviewDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Application;
import com.sinst.model.Interview;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class InterviewServiceImpl implements InterviewService {

    private final InterviewDAO interviewDAO;
    private final ApplicationDAO applicationDAO;

    public InterviewServiceImpl() {
        this(InterviewDAOImpl.getInstance(), ApplicationDAOImpl.getInstance());
    }

    public InterviewServiceImpl(InterviewDAO interviewDAO) {
        this(interviewDAO, ApplicationDAOImpl.getInstance());
    }

    public InterviewServiceImpl(InterviewDAO interviewDAO, ApplicationDAO applicationDAO) {
        this.interviewDAO = interviewDAO;
        this.applicationDAO = applicationDAO;
    }

    @Override
    public List<Interview> getInterviews(int studentId) throws DatabaseException {
        try {
            return interviewDAO.getByStudentId(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving interviews: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Interview> getUpcomingInterviews(int studentId) throws DatabaseException {
        try {
            return interviewDAO.getUpcoming(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving upcoming interviews: " + e.getMessage(), e);
        }
    }

    @Override
    public void scheduleInterview(Interview interview) throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException {
        int studentId = UserSession.getCurrentStudentId();
        scheduleInterview(interview, studentId);
    }

    @Override
    public void scheduleInterview(Interview interview, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException {
        if (interview == null || interview.getApplicationId() <= 0) {
            throw new ValidationException("Application must be selected before scheduling an interview.");
        }
        if (interview.getInterviewDate() == null) {
            throw new ValidationException("Interview date and time cannot be empty.");
        }
        if (!ValidationUtil.isNonEmpty(interview.getRoundType())) {
            throw new ValidationException("Round type is required.");
        }
        if (!ValidationUtil.isValidRoundNumber(interview.getRoundNumber())) {
            throw new ValidationException("Round number must be between 1 and 20.");
        }
        if (ValidationUtil.isNonEmpty(interview.getMode()) && !ValidationUtil.isValidInterviewMode(interview.getMode())) {
            throw new ValidationException("Invalid interview mode: " + interview.getMode());
        }

        try {
            Application app = applicationDAO.getById(interview.getApplicationId());
            if (app == null) {
                throw new DatabaseException("Selected application record not found.");
            }
            if (studentId > 0 && app.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You cannot schedule an interview for an application you do not own.");
            }

            if (app.getApplicationDate() != null && !ValidationUtil.isValidInterviewDate(app.getApplicationDate(), interview.getInterviewDate())) {
                throw new ValidationException("Interview date and time cannot be earlier than the application submission date (" + app.getApplicationDate() + ").");
            }

            if (interviewDAO.hasScheduledInterview(interview.getApplicationId(), interview.getRoundType(), interview.getInterviewDate())) {
                throw new DuplicateEntityException("An interview for " + interview.getRoundType() + " is already scheduled at this date and time.");
            }

            boolean scheduled = interviewDAO.schedule(interview, interview.getApplicationId(), studentId > 0 ? studentId : app.getStudentId(), "Interview");
            if (!scheduled) {
                throw new DatabaseException("Failed to schedule interview.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_interview_app_round_date")) {
                throw new DuplicateEntityException("An interview with the same round and date is already scheduled.");
            }
            throw new DatabaseException("Database error scheduling interview: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void updateInterviewOutcome(int interviewId, String status, String outcome, String feedback) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        updateInterviewOutcome(interviewId, UserSession.getCurrentStudentId(), status, outcome, feedback);
    }

    @Override
    public void updateInterviewOutcome(int interviewId, int studentId, String status, String outcome, String feedback) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (interviewId <= 0) {
            throw new ValidationException("Invalid interview ID.");
        }
        if (ValidationUtil.isNonEmpty(status) && !ValidationUtil.isValidInterviewStatus(status)) {
            throw new ValidationException("Invalid interview status: " + status);
        }
        if (ValidationUtil.isNonEmpty(outcome) && !ValidationUtil.isValidInterviewOutcome(outcome)) {
            throw new ValidationException("Invalid interview outcome: " + outcome);
        }

        try {
            int ownerId = interviewDAO.getInterviewOwnerStudentId(interviewId);
            if (ownerId == 0) {
                throw new DatabaseException("Interview record not found.");
            }
            if (studentId > 0 && ownerId != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this interview.");
            }

            boolean updated = interviewDAO.update(interviewId, studentId > 0 ? studentId : ownerId, status, outcome, feedback);
            if (!updated) {
                throw new DatabaseException("Interview record could not be updated.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating interview outcome: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void updateInterviewFull(Interview interview) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        updateInterviewFull(interview, UserSession.getCurrentStudentId());
    }

    @Override
    public void updateInterviewFull(Interview interview, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (interview == null || interview.getInterviewId() <= 0) {
            throw new ValidationException("Invalid interview details for update.");
        }
        if (interview.getInterviewDate() == null) {
            throw new ValidationException("Interview date and time cannot be empty.");
        }
        if (!ValidationUtil.isNonEmpty(interview.getRoundType())) {
            throw new ValidationException("Round type is required.");
        }
        if (ValidationUtil.isNonEmpty(interview.getMode()) && !ValidationUtil.isValidInterviewMode(interview.getMode())) {
            throw new ValidationException("Invalid interview mode: " + interview.getMode());
        }

        try {
            int ownerId = interviewDAO.getInterviewOwnerStudentId(interview.getInterviewId());
            if (ownerId == 0) {
                throw new DatabaseException("Interview not found.");
            }
            if (studentId > 0 && ownerId != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this interview.");
            }

            boolean updated = interviewDAO.updateFull(interview, studentId > 0 ? studentId : ownerId);
            if (!updated) {
                throw new DatabaseException("Interview could not be updated.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating interview: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void deleteInterview(int interviewId) throws DatabaseException, UnauthorizedAccessException {
        deleteInterview(interviewId, UserSession.getCurrentStudentId());
    }

    @Override
    public void deleteInterview(int interviewId, int studentId) throws DatabaseException, UnauthorizedAccessException {
        if (interviewId <= 0) return;

        try {
            int ownerId = interviewDAO.getInterviewOwnerStudentId(interviewId);
            if (ownerId == 0) {
                throw new DatabaseException("Interview not found.");
            }
            if (studentId > 0 && ownerId != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to delete this interview.");
            }

            boolean deleted = interviewDAO.delete(interviewId, studentId > 0 ? studentId : ownerId);
            if (!deleted) {
                throw new DatabaseException("Interview could not be deleted.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error deleting interview: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }
}
