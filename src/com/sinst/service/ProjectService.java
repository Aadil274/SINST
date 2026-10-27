package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Project;

import java.util.List;

public interface ProjectService {
    List<Project> getProjects(int studentId) throws DatabaseException;
    void addProject(Project project) throws ValidationException, DatabaseException, DuplicateEntityException;
    void updateProject(Project project) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void updateProject(Project project, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException;
    void deleteProject(int projectId) throws DatabaseException, UnauthorizedAccessException;
    void deleteProject(int projectId, int studentId) throws DatabaseException, UnauthorizedAccessException;
}
