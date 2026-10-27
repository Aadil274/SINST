package com.sinst.dao;

import com.sinst.model.Role;
import java.sql.SQLException;
import java.util.List;

public interface RoleDAO {
    List<Role> getAll() throws SQLException;
    Role getById(int roleId) throws SQLException;
    List<Integer> getRoleSkillIds(int roleId) throws SQLException;
    boolean save(Role role, List<Integer> requiredSkillIds) throws SQLException;
    boolean delete(int roleId) throws SQLException;
}
