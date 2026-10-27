package com.sinst.service;

import com.sinst.dao.ProjectDAO;
import com.sinst.dao.ProjectDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Project;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {

    private final ProjectDAO projectDAO;

    public ProjectServiceImpl() {
        this(ProjectDAOImpl.getInstance());
    }

    public ProjectServiceImpl(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    @Override
    public List<Project> getProjects(int studentId) throws DatabaseException {
        try {
            return projectDAO.getByStudentId(studentId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving projects: " + e.getMessage(), e);
        }
    }

    @Override
    public void addProject(Project project) throws ValidationException, DatabaseException, DuplicateEntityException {
        if (project == null || project.getStudentId() <= 0) {
            throw new ValidationException("Invalid student identification for project.");
        }
        validateProjectDetails(project);

        try {
            if (projectDAO.hasProjectTitle(project.getStudentId(), project.getTitle())) {
                throw new DuplicateEntityException("A project titled '" + project.getTitle() + "' already exists in your profile.");
            }

            boolean success = projectDAO.add(project);
            if (!success) {
                throw new DatabaseException("Failed to add project record.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("uq_student_proj_title")) {
                throw new DuplicateEntityException("A project with this title already exists in your profile.");
            }
            throw new DatabaseException("Database error adding project: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void updateProject(Project project) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        int studentId = project != null && project.getStudentId() > 0 ? project.getStudentId() : UserSession.getCurrentStudentId();
        updateProject(project, studentId);
    }

    @Override
    public void updateProject(Project project, int studentId) throws ValidationException, DatabaseException, UnauthorizedAccessException {
        if (project == null || project.getProjectId() <= 0) {
            throw new ValidationException("Invalid project ID for update.");
        }
        validateProjectDetails(project);

        try {
            Project existing = projectDAO.getById(project.getProjectId());
            if (existing == null) {
                throw new DatabaseException("Project record not found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to modify this project.");
            }

            boolean updated = projectDAO.update(project, studentId > 0 ? studentId : existing.getStudentId());
            if (!updated) {
                throw new DatabaseException("Project record could not be updated.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error updating project: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public void deleteProject(int projectId) throws DatabaseException, UnauthorizedAccessException {
        deleteProject(projectId, UserSession.getCurrentStudentId());
    }

    @Override
    public void deleteProject(int projectId, int studentId) throws DatabaseException, UnauthorizedAccessException {
        if (projectId <= 0) return;

        try {
            Project existing = projectDAO.getById(projectId);
            if (existing == null) {
                throw new DatabaseException("Project not found.");
            }
            if (studentId > 0 && existing.getStudentId() != studentId) {
                throw new UnauthorizedAccessException("Access denied: You do not have permission to delete this project.");
            }

            boolean deleted = projectDAO.delete(projectId, studentId > 0 ? studentId : existing.getStudentId());
            if (!deleted) {
                throw new DatabaseException("Project could not be removed.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error deleting project: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    private void validateProjectDetails(Project project) throws ValidationException {
        if (!ValidationUtil.isNonEmpty(project.getTitle())) {
            throw new ValidationException("Project Title is required.");
        }
        if (!ValidationUtil.isNonEmpty(project.getTechnologiesUsed())) {
            throw new ValidationException("Technologies Used is required.");
        }
        if (ValidationUtil.isNonEmpty(project.getProjectUrl()) && !ValidationUtil.isValidUrl(project.getProjectUrl())) {
            throw new ValidationException("Please enter a valid project repository URL (e.g. https://github.com/user/project).");
        }
        if (ValidationUtil.isNonEmpty(project.getGithubUrl()) && !ValidationUtil.isValidUrl(project.getGithubUrl())) {
            throw new ValidationException("Please enter a valid GitHub repository URL (e.g. https://github.com/user/project).");
        }
        if (ValidationUtil.isNonEmpty(project.getLiveUrl()) && !ValidationUtil.isValidUrl(project.getLiveUrl())) {
            throw new ValidationException("Please enter a valid live demo URL (e.g. https://project.demo.com).");
        }
    }
}
