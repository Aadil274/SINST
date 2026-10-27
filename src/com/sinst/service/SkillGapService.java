package com.sinst.service;

import com.sinst.exception.DatabaseException;
import com.sinst.model.SkillGapResult;
import com.sinst.model.SkillMatchConfig;

import java.util.List;

public interface SkillGapService {
    List<SkillGapResult> analyzeSkillGap(int studentId, int roleId) throws DatabaseException;
    int calculateMatchPercentage(List<SkillGapResult> results);
    int calculateMatchPercentage(List<SkillGapResult> results, SkillMatchConfig config);
    double calculateDetailedMatchScore(List<SkillGapResult> results, SkillMatchConfig config);
    String getReadinessAssessment(int matchPercentage, int matchedCount, int totalRequired, String roleTitle);
    boolean isInsufficientRoleData(List<SkillGapResult> results);
    SkillMatchConfig getConfig();
    void setConfig(SkillMatchConfig config);
}
