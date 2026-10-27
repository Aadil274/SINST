package com.sinst.dao;

import com.sinst.model.Student;
import java.sql.SQLException;

public interface StudentDAO {
    Student getById(int studentId) throws SQLException;
    Student getByRollNumber(String rollNumber) throws SQLException;
    Student login(String identifier, String password) throws SQLException;
    boolean register(Student s) throws SQLException;
    boolean updateProfile(Student s) throws SQLException;
    boolean updateProfile(Student s, int authenticatedStudentId) throws SQLException;
}
