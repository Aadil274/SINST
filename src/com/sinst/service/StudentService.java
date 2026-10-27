package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.EntityNotFoundException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Student;

public interface StudentService {
    Student login(String identifier, String password) throws ValidationException, DatabaseException, EntityNotFoundException;
    Student register(Student s) throws ValidationException, DatabaseException, DuplicateEntityException;
    void updateProfile(Student s) throws ValidationException, DatabaseException, DuplicateEntityException, UnauthorizedAccessException;
    void updateProfile(Student s, int authenticatedStudentId) throws ValidationException, DatabaseException, DuplicateEntityException, UnauthorizedAccessException;
    Student getStudentById(int studentId) throws DatabaseException, EntityNotFoundException;
}
