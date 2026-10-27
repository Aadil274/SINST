package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.exception.DuplicateEntityException;
import com.sinst.exception.EntityNotFoundException;
import com.sinst.exception.ValidationException;
import com.sinst.model.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAllRoles() throws DatabaseException;
    Role getRoleById(int roleId) throws DatabaseException, EntityNotFoundException;
    List<Integer> getRoleSkillIds(int roleId) throws DatabaseException;
    void saveRole(Role role, List<Integer> requiredSkillIds) throws ValidationException, DatabaseException, DuplicateEntityException;
    void deleteRole(int roleId) throws DatabaseException;
}
