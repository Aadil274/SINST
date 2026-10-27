package com.sinst.util;

import com.sinst.model.Student;

/**
 * Manages the authenticated user session throughout the application lifecycle.
 * Provides thread-safe access to the authenticated student's identity and permissions.
 */
public class UserSession {

    private static volatile Student currentStudent;

    private UserSession() {}

    public static synchronized void setCurrentStudent(Student student) {
        currentStudent = student;
    }

    public static Student getCurrentStudent() {
        return currentStudent;
    }

    public static int getCurrentStudentId() {
        Student s = currentStudent;
        return s != null ? s.getStudentId() : 0;
    }

    public static boolean isAuthenticated() {
        return currentStudent != null;
    }

    public static boolean isAdmin() {
        Student s = currentStudent;
        return s != null && s.isAdmin();
    }

    public static synchronized void clear() {
        currentStudent = null;
    }
}
