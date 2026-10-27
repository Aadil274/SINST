package com.sinst.dao;

import com.sinst.model.CertificationSkillMapping;
import com.sinst.model.StudentCertificationSkill;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CertificationSkillDAO {
    List<CertificationSkillMapping> findCatalogMappingsByCertName(String certName) throws SQLException;
    List<CertificationSkillMapping> getAllCatalogMappings() throws SQLException;
    boolean addCatalogMapping(CertificationSkillMapping mapping) throws SQLException;
    List<StudentCertificationSkill> getStudentCertificationSkills(int certificationId) throws SQLException;
    List<StudentCertificationSkill> getStudentCertificationSkillsByStudent(int studentId) throws SQLException;
    void addStudentCertificationSkills(List<StudentCertificationSkill> skills) throws SQLException;
    void addStudentCertificationSkills(List<StudentCertificationSkill> skills, Connection conn) throws SQLException;
    boolean deleteStudentCertificationSkills(int certificationId) throws SQLException;
    boolean deleteStudentCertificationSkills(int certificationId, Connection conn) throws SQLException;
}
