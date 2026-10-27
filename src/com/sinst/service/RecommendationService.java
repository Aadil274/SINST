package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.model.RoleRecommendation;
import com.sinst.model.SkillMatchConfig;

import java.util.List;

public interface RecommendationService {
    List<RoleRecommendation> getRecommendations(int studentId) throws DatabaseException;
    List<RoleRecommendation> getRecommendations(int studentId, SkillMatchConfig config) throws DatabaseException;
    RoleRecommendation getRoleRecommendation(int studentId, int companyId, int roleId) throws DatabaseException;
}
