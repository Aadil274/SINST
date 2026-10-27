package com.sinst.service;

import com.sinst.dao.CompanyDAO;
import com.sinst.dao.CompanyDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.EntityNotFoundException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Company;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class CompanyServiceImpl implements CompanyService {

    private final CompanyDAO companyDAO;

    public CompanyServiceImpl() {
        this(CompanyDAOImpl.getInstance());
    }

    public CompanyServiceImpl(CompanyDAO companyDAO) {
        this.companyDAO = companyDAO;
    }

    @Override
    public List<Company> getAllCompanies() throws DatabaseException {
        try {
            return companyDAO.getAll();
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving companies: " + e.getMessage(), e);
        }
    }

    @Override
    public Company getCompanyById(int companyId) throws DatabaseException, EntityNotFoundException {
        try {
            Company c = companyDAO.getById(companyId);
            if (c == null) {
                throw new EntityNotFoundException("Company with ID " + companyId + " not found.");
            }
            return c;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving company: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveCompany(Company company) throws ValidationException, DatabaseException, DuplicateEntityException {
        if (company == null || !ValidationUtil.isNonEmpty(company.getCompanyName())) {
            throw new ValidationException("Company name is required.");
        }
        if (ValidationUtil.isNonEmpty(company.getContactEmail()) && !ValidationUtil.isValidEmail(company.getContactEmail())) {
            throw new ValidationException("Please enter a valid contact email address (e.g. hr@company.com).");
        }

        try {
            boolean success = companyDAO.save(company);
            if (!success) {
                throw new DatabaseException("Failed to save company record.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique")) {
                throw new DuplicateEntityException("A company with the name '" + company.getCompanyName() + "' already exists.");
            }
            throw new DatabaseException("Database error saving company: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteCompany(int companyId) throws DatabaseException {
        if (companyId <= 0) return;
        try {
            boolean deleted = companyDAO.delete(companyId);
            if (!deleted) {
                throw new DatabaseException("Company not found or could not be deleted.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("foreign key") || msg.contains("constraint")) {
                throw new DatabaseException("This company is referenced by internship applications and cannot be deleted.", e);
            }
            throw new DatabaseException("Database error deleting company: " + e.getMessage(), e);
        }
    }
}
