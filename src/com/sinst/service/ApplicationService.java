package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Application;

import java.util.List;

public interface ApplicationService {
    List<Application> getApplications(int studentId) throws DatabaseException;
    void submitApplication(Application app) throws ValidationException, DatabaseException, DuplicateEntityException;
    void updateApplication(Application app) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateApplication(Application app, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateStatus(int applicationId, String status) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateStatus(int applicationId, int studentId, String status) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void deleteApplication(int applicationId) throws DatabaseException, UnauthorizedAccessException;
    void deleteApplication(int applicationId, int studentId) throws DatabaseException, UnauthorizedAccessException;
}
