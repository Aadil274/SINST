package com.sinst.dao;

import com.sinst.model.CompanyRole;

import java.sql.SQLException;
import java.util.List;

public interface RecommendationDAO {
    List<CompanyRole> getAllCompanyRoles() throws SQLException;
    List<CompanyRole> getCompanyRolesByRole(int roleId) throws SQLException;
    List<CompanyRole> getCompanyRolesByCompany(int companyId) throws SQLException;
    boolean addCompanyRole(CompanyRole cr) throws SQLException;
    boolean updateVacancyStatus(int companyRoleId, String vacancyStatus) throws SQLException;
}
