package com.sinst.service;

import com.sinst.dao.StudentDAO;
import com.sinst.dao.StudentDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.EntityNotFoundException;
import com.sinst.exception.UnauthorizedAccessException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Student;
import com.sinst.util.UserSession;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;

public class StudentServiceImpl implements StudentService {

    private final StudentDAO studentDAO;

    public StudentServiceImpl() {
        this(StudentDAOImpl.getInstance());
    }

    public StudentServiceImpl(StudentDAO studentDAO) {
        this.studentDAO = studentDAO;
    }

    @Override
    public Student login(String identifier, String password) throws ValidationException, DatabaseException, EntityNotFoundException {
        if (!ValidationUtil.isNonEmpty(identifier) || !ValidationUtil.isNonEmpty(password)) {
            throw new ValidationException("Please enter both Roll Number / Email and Password.");
        }

        try {
            Student student = studentDAO.login(identifier, password);
            if (student == null) {
                throw new EntityNotFoundException("Invalid credentials. Please verify your roll number/email and password.");
            }
            return student;
        } catch (SQLException e) {
            throw new DatabaseException("Database error during login: " + e.getMessage(), e);
        }
    }

    @Override
    public Student register(Student s) throws ValidationException, DatabaseException, DuplicateEntityException {
        if (s == null) {
            throw new ValidationException("Student data cannot be null.");
        }
        if (!ValidationUtil.isValidRollNumber(s.getRollNumber())) {
            throw new ValidationException("Invalid Roll Number. Must be between 3 and 20 alphanumeric characters.");
        }
        if (!ValidationUtil.isValidName(s.getFullName())) {
            throw new ValidationException("Invalid Full Name. Please enter letters, spaces, and hyphens only (2-100 characters).");
        }
        if (!ValidationUtil.isValidEmail(s.getEmail())) {
            throw new ValidationException("Invalid Email Address format. Please provide a valid email (e.g. name@domain.com).");
        }
        if (!ValidationUtil.isNonEmpty(s.getPassword())) {
            throw new ValidationException("Password cannot be blank.");
        }
        if (!ValidationUtil.isValidYearOfStudy(s.getYearOfStudy())) {
            throw new ValidationException("Year of Study must be between 1 and 4.");
        }
        if (!ValidationUtil.isValidPhone(s.getPhone())) {
            throw new ValidationException("Invalid phone number. Must be between 10 and 15 digits.");
        }

        try {
            boolean created = studentDAO.register(s);
            if (!created) {
                throw new DatabaseException("Registration could not be completed.");
            }
            return s;
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique")) {
                throw new DuplicateEntityException("A student with this Roll Number or Email already exists.");
            }
            throw new DatabaseException("Database error during registration: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateProfile(Student s) throws ValidationException, DatabaseException, DuplicateEntityException, UnauthorizedAccessException {
        int sessionStudentId = UserSession.getCurrentStudentId();
        updateProfile(s, sessionStudentId > 0 ? sessionStudentId : (s != null ? s.getStudentId() : 0));
    }

    @Override
    public void updateProfile(Student s, int authenticatedStudentId) throws ValidationException, DatabaseException, DuplicateEntityException, UnauthorizedAccessException {
        if (s == null) {
            throw new ValidationException("Student data cannot be null.");
        }
        if (authenticatedStudentId > 0 && s.getStudentId() != authenticatedStudentId) {
            throw new UnauthorizedAccessException("Access denied: You cannot modify another student's profile.");
        }
        if (!ValidationUtil.isValidName(s.getFullName())) {
            throw new ValidationException("Please enter a valid full name (letters, spaces, dots, hyphens only).");
        }
        if (!ValidationUtil.isValidEmail(s.getEmail())) {
            throw new ValidationException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isNonEmpty(s.getDepartment())) {
            throw new ValidationException("Department cannot be blank.");
        }
        if (!ValidationUtil.isValidYearOfStudy(s.getYearOfStudy())) {
            throw new ValidationException("Year of study must be between 1 and 4.");
        }
        if (!ValidationUtil.isValidPhone(s.getPhone())) {
            throw new ValidationException("Please enter a valid phone number (10-15 digits) or leave it blank.");
        }

        try {
            boolean updated = studentDAO.updateProfile(s, authenticatedStudentId > 0 ? authenticatedStudentId : s.getStudentId());
            if (!updated) {
                throw new DatabaseException("Student profile was not found or could not be updated.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique")) {
                throw new DuplicateEntityException("The email address '" + s.getEmail() + "' is already registered to another account.");
            }
            throw new DatabaseException("Database error during profile update: " + ValidationUtil.sanitizeErrorMessage(e), e);
        }
    }

    @Override
    public Student getStudentById(int studentId) throws DatabaseException, EntityNotFoundException {
        try {
            Student s = studentDAO.getById(studentId);
            if (s == null) {
                throw new EntityNotFoundException("Student with ID " + studentId + " was not found.");
            }
            return s;
        } catch (SQLException e) {
            throw new DatabaseException("Database error fetching student: " + e.getMessage(), e);
        }
    }
}
