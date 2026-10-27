package com.sinst.service;

import com.sinst.dao.SkillDAO;
import com.sinst.dao.SkillDAOImpl;
import com.sinst.exception.DatabaseException;
import com.sinst.model.SkillGapResult;
import com.sinst.model.SkillMatchConfig;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SkillGapServiceImpl implements SkillGapService {

    private final SkillDAO skillDAO;
    private SkillMatchConfig config;

    public SkillGapServiceImpl() {
        this(SkillDAOImpl.getInstance(), new SkillMatchConfig());
    }

    public SkillGapServiceImpl(SkillDAO skillDAO) {
        this(skillDAO, new SkillMatchConfig());
    }

    public SkillGapServiceImpl(SkillDAO skillDAO, SkillMatchConfig config) {
        this.skillDAO = skillDAO;
        this.config = config != null ? config : new SkillMatchConfig();
    }

    @Override
    public List<SkillGapResult> analyzeSkillGap(int studentId, int roleId) throws DatabaseException {
        try {
            List<SkillGapResult> rawResults = skillDAO.getSkillGapAnalysis(studentId, roleId);
            // Deduplicate defensively by skillId to prevent any duplicate join inflation
            Map<Integer, SkillGapResult> distinctMap = new LinkedHashMap<>();
            for (SkillGapResult r : rawResults) {
                if (r != null && !distinctMap.containsKey(r.getSkillId())) {
                    r.evaluateProficiency();
                    distinctMap.put(r.getSkillId(), r);
                }
            }
            return new ArrayList<>(distinctMap.values());
        } catch (SQLException e) {
            throw new DatabaseException("Error analyzing skill gap: " + e.getMessage(), e);
        }
    }

    @Override
    public int calculateMatchPercentage(List<SkillGapResult> results) {
        return calculateMatchPercentage(results, this.config);
    }

    @Override
    public int calculateMatchPercentage(List<SkillGapResult> results, SkillMatchConfig customConfig) {
        double score = calculateDetailedMatchScore(results, customConfig);
        if (score < 0.0) return 0;
        return (int) Math.round(score);
    }

    @Override
    public double calculateDetailedMatchScore(List<SkillGapResult> results, SkillMatchConfig customConfig) {
        if (results == null || results.isEmpty()) {
            return -1.0; // Indicates Insufficient Role Data
        }

        SkillMatchConfig activeConfig = customConfig != null ? customConfig : this.config;
        double sumWeightedMatchValues = 0.0;
        double sumWeights = 0.0;

        // Deduplicate in calculation as well to avoid inflating scores
        Map<Integer, SkillGapResult> distinct = new LinkedHashMap<>();
        for (SkillGapResult r : results) {
            if (r != null && !distinct.containsKey(r.getSkillId())) {
                distinct.put(r.getSkillId(), r);
            }
        }

        if (distinct.isEmpty()) {
            return -1.0;
        }

        for (SkillGapResult r : distinct.values()) {
            double weight = r.isMandatory() ? activeConfig.getMandatoryWeight() : activeConfig.getOptionalWeight();
            sumWeights += weight;
            sumWeightedMatchValues += (weight * r.getMatchValue());
        }

        if (sumWeights <= 0.0) {
            return 0.0;
        }

        double percentage = (sumWeightedMatchValues / sumWeights) * 100.0;
        return Math.min(100.0, Math.max(0.0, percentage));
    }

    @Override
    public boolean isInsufficientRoleData(List<SkillGapResult> results) {
        return results == null || results.isEmpty();
    }

    @Override
    public String getReadinessAssessment(int matchPercentage, int matchedCount, int totalRequired, String roleTitle) {
        String role = roleTitle != null ? roleTitle : "target role";

        if (totalRequired <= 0) {
            return "Insufficient role data: No skill requirements are defined for " + role + ".";
        }

        if (matchPercentage >= config.getHighReadinessThreshold()) {
            return "High Readiness: You meet " + matchedCount + " of " + totalRequired + " competencies for " + role + " (Match Score: " + matchPercentage + "%).";
        } else if (matchPercentage >= config.getModerateReadinessThreshold()) {
            return "Moderate Match: You possess " + matchedCount + " of " + totalRequired + " skills for " + role + " (Match Score: " + matchPercentage + "%). Review the missing/upgrade skills below.";
        } else {
            return "Skill Gap Detected: You possess " + matchedCount + " of " + totalRequired + " required skills (Match Score: " + matchPercentage + "%). Target the missing skills below.";
        }
    }

    @Override
    public SkillMatchConfig getConfig() {
        return config;
    }

    @Override
    public void setConfig(SkillMatchConfig config) {
        if (config != null) {
            this.config = config;
        }
    }
}
