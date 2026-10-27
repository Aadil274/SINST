package com.sinst.dao;

import com.sinst.model.Certification;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface CertificationDAO {
    List<Certification> getByStudentId(int studentId) throws SQLException;
    Certification getById(int certId) throws SQLException;
    Certification getByIdAndStudentId(int certId, int studentId) throws SQLException;
    boolean hasCertification(int studentId, String certName, String issuingOrg) throws SQLException;
    boolean add(Certification c) throws SQLException;
    boolean add(Certification c, Connection conn) throws SQLException;
    boolean update(Certification c) throws SQLException;
    boolean update(Certification c, int studentId) throws SQLException;
    boolean delete(int certificationId) throws SQLException;
    boolean delete(int certificationId, Connection conn) throws SQLException;
    boolean delete(int certificationId, int studentId) throws SQLException;
    boolean delete(int certificationId, int studentId, Connection conn) throws SQLException;
}
