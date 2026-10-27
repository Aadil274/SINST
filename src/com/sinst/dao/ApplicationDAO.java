package com.sinst.dao;

import com.sinst.model.Application;
import java.sql.SQLException;
import java.util.List;

public interface ApplicationDAO {
    List<Application> getByStudentId(int studentId) throws SQLException;
    Application getById(int applicationId) throws SQLException;
    Application getByIdAndStudentId(int applicationId, int studentId) throws SQLException;
    boolean hasApplication(int studentId, int companyId, int roleId) throws SQLException;
    boolean add(Application a) throws SQLException;
    boolean update(Application a) throws SQLException;
    boolean update(Application a, int studentId) throws SQLException;
    boolean updateStatus(int applicationId, String status) throws SQLException;
    boolean updateStatus(int applicationId, int studentId, String status) throws SQLException;
    boolean delete(int applicationId) throws SQLException;
    boolean delete(int applicationId, int studentId) throws SQLException;
}
