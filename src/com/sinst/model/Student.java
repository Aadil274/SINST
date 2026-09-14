package com.sinst.model;

public class Student {
    private int studentId;
    private String rollNumber;
    private String fullName;
    private String email;
    private String password;
    private String department;
    private int yearOfStudy;
    private String phone;

    public Student() {}

    public Student(int studentId, String rollNumber, String fullName, String email, String password, String department, int yearOfStudy, String phone) {
        this.studentId = studentId;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
        this.phone = phone;
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

    @Override
    public String toString() {
        return fullName + " (" + rollNumber + ")";
    }
}
