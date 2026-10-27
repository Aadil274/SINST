package com.sinst.dao;

import com.sinst.model.Project;
import java.sql.SQLException;
import java.util.List;

public interface ProjectDAO {
    List<Project> getByStudentId(int studentId) throws SQLException;
    Project getById(int projectId) throws SQLException;
    Project getByIdAndStudentId(int projectId, int studentId) throws SQLException;
    boolean hasProjectTitle(int studentId, String title) throws SQLException;
    boolean add(Project p) throws SQLException;
    boolean update(Project p) throws SQLException;
    boolean update(Project p, int studentId) throws SQLException;
    boolean delete(int projectId) throws SQLException;
    boolean delete(int projectId, int studentId) throws SQLException;
}
