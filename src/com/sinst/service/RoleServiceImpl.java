package com.sinst.service;

import com.sinst.dao.RoleDAO;
import com.sinst.dao.RoleDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.EntityNotFoundException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Role;
import com.sinst.util.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class RoleServiceImpl implements RoleService {

    private final RoleDAO roleDAO;

    public RoleServiceImpl() {
        this(RoleDAOImpl.getInstance());
    }

    public RoleServiceImpl(RoleDAO roleDAO) {
        this.roleDAO = roleDAO;
    }

    @Override
    public List<Role> getAllRoles() throws DatabaseException {
        try {
            return roleDAO.getAll();
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving roles: " + e.getMessage(), e);
        }
    }

    @Override
    public Role getRoleById(int roleId) throws DatabaseException, EntityNotFoundException {
        try {
            Role r = roleDAO.getById(roleId);
            if (r == null) {
                throw new EntityNotFoundException("Role with ID " + roleId + " not found.");
            }
            return r;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving role: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Integer> getRoleSkillIds(int roleId) throws DatabaseException {
        try {
            return roleDAO.getRoleSkillIds(roleId);
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving role skills: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveRole(Role role, List<Integer> requiredSkillIds) throws ValidationException, DatabaseException, DuplicateEntityException {
        if (role == null || !ValidationUtil.isNonEmpty(role.getRoleTitle())) {
            throw new ValidationException("Role title is required.");
        }

        try {
            boolean success = roleDAO.save(role, requiredSkillIds);
            if (!success) {
                throw new DatabaseException("Failed to save role record.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("duplicate") || msg.contains("unique")) {
                throw new DuplicateEntityException("A role with the title '" + role.getRoleTitle() + "' already exists.");
            }
            throw new DatabaseException("Database error saving role: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteRole(int roleId) throws DatabaseException {
        if (roleId <= 0) return;
        try {
            boolean deleted = roleDAO.delete(roleId);
            if (!deleted) {
                throw new DatabaseException("Role not found or could not be deleted.");
            }
        } catch (SQLException e) {
            String msg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (msg.contains("foreign key") || msg.contains("constraint")) {
                throw new DatabaseException("This role is referenced by internship applications and cannot be deleted.", e);
            }
            throw new DatabaseException("Database error deleting role: " + e.getMessage(), e);
        }
    }
}
