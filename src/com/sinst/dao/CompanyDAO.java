package com.sinst.dao;

import com.sinst.model.Company;
import java.sql.SQLException;
import java.util.List;

public interface CompanyDAO {
    List<Company> getAll() throws SQLException;
    Company getById(int companyId) throws SQLException;
    boolean save(Company company) throws SQLException;
    boolean delete(int companyId) throws SQLException;
}
