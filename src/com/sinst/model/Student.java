package com.sinst.model;

import java.util.List;
import java.util.ArrayList;

public class Student {
    private int studentId;
    private String rollNumber;
    private String fullName;
    private String email;
    private String password;
    private String department;
    private int yearOfStudy;
    private String phone;
    private String role = "STUDENT"; // 'STUDENT', 'ADMIN'

    // Composition references
    private List<Skill> skills = new ArrayList<>();
    private List<Certification> certifications = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();

    public Student() {}

    public Student(int studentId, String rollNumber, String fullName, String email, String password, String department, int yearOfStudy, String phone) {
        this(studentId, rollNumber, fullName, email, password, department, yearOfStudy, phone, "STUDENT");
    }

    public Student(int studentId, String rollNumber, String fullName, String email, String password, String department, int yearOfStudy, String phone, String role) {
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.phone = phone;
        this.role = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "STUDENT";
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = (role != null && !role.trim().isEmpty()) ? role.trim().toUpperCase() : "STUDENT"; }
    public boolean isAdmin() { return "ADMIN".equalsIgnoreCase(role); }

    public List<Skill> getSkills() { return skills; }
    public void setSkills(List<Skill> skills) { this.skills = skills != null ? skills : new ArrayList<>(); }

    public List<Certification> getCertifications() { return certifications; }
    public void setCertifications(List<Certification> certifications) { this.certifications = certifications != null ? certifications : new ArrayList<>(); }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects != null ? projects : new ArrayList<>(); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        if (rollNumber != null && student.rollNumber != null) {
            return rollNumber.equalsIgnoreCase(student.rollNumber);
        }
        return studentId != 0 && studentId == student.studentId;
    }

    @Override
    public int hashCode() {
        return rollNumber != null ? rollNumber.toLowerCase().hashCode() : Integer.hashCode(studentId);
    }

    @Override
    public String toString() {
        return fullName + " (" + rollNumber + ")";
    }
}
