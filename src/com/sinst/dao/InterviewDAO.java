package com.sinst.dao;

import com.sinst.model.Interview;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public interface InterviewDAO {
    List<Interview> getByStudentId(int studentId) throws SQLException;
    List<Interview> getByApplicationId(int applicationId) throws SQLException;
    List<Interview> getByApplicationId(int applicationId, int studentId) throws SQLException;
    Interview getById(int interviewId) throws SQLException;
    Interview getByIdAndStudentId(int interviewId, int studentId) throws SQLException;
    List<Interview> getUpcoming(int studentId) throws SQLException;
    boolean schedule(Interview i, int applicationId, String newStatus) throws SQLException;
    boolean schedule(Interview i, int applicationId, int studentId, String newStatus) throws SQLException;
    boolean update(int interviewId, String status, String outcome, String feedback) throws SQLException;
    boolean update(int interviewId, int studentId, String status, String outcome, String feedback) throws SQLException;
    boolean updateFull(Interview i) throws SQLException;
    boolean updateFull(Interview i, int studentId) throws SQLException;
    boolean delete(int interviewId) throws SQLException;
    boolean delete(int interviewId, int studentId) throws SQLException;
    int getApplicationOwnerStudentId(int applicationId) throws SQLException;
    int getInterviewOwnerStudentId(int interviewId) throws SQLException;
    boolean hasScheduledInterview(int applicationId, String roundType, Timestamp interviewDate) throws SQLException;
}
