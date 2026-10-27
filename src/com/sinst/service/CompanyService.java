package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.EntityNotFoundException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Company;

import java.util.List;

public interface CompanyService {
    List<Company> getAllCompanies() throws DatabaseException;
    Company getCompanyById(int companyId) throws DatabaseException, EntityNotFoundException;
    void saveCompany(Company company) throws ValidationException, DatabaseException, DuplicateEntityException;
    void deleteCompany(int companyId) throws DatabaseException;
    boolean ensureCompanyExists(String companyName) throws DatabaseException;
}
