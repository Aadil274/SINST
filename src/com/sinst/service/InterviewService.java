package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Interview;

import java.util.List;

public interface InterviewService {
    List<Interview> getInterviews(int studentId) throws DatabaseException;
    List<Interview> getUpcomingInterviews(int studentId) throws DatabaseException;
    void scheduleInterview(Interview interview) throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException;
    void scheduleInterview(Interview interview, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException, DuplicateEntityException;
    void updateInterviewOutcome(int interviewId, String status, String outcome, String feedback) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateInterviewOutcome(int interviewId, int studentId, String status, String outcome, String feedback) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateInterviewFull(Interview interview) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateInterviewFull(Interview interview, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void deleteInterview(int interviewId) throws DatabaseException, UnauthorizedAccessException;
    void deleteInterview(int interviewId, int studentId) throws DatabaseException, UnauthorizedAccessException;
}
