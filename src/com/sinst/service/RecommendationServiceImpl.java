package com.sinst.service;

import com.sinst.dao.*;
import com.sinst.exception.DatabaseException;
import com.sinst.model.*;

import java.sql.SQLException;
import java.util.*;

public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationDAO recommendationDAO;
    private final SkillGapService skillGapService;
    private final ApplicationDAO applicationDAO;
    private final SkillEvidenceDAO skillEvidenceDAO;
    private final RoleDAO roleDAO;
    private final CompanyDAO companyDAO;

    public RecommendationServiceImpl() {
        this(
            RecommendationDAOImpl.getInstance(),
            new SkillGapServiceImpl(),
            ApplicationDAOImpl.getInstance(),
            SkillEvidenceDAOImpl.getInstance(),
            RoleDAOImpl.getInstance(),
            CompanyDAOImpl.getInstance()
        );
    }

    public RecommendationServiceImpl(
            RecommendationDAO recommendationDAO,
            SkillGapService skillGapService,
            ApplicationDAO applicationDAO,
            SkillEvidenceDAO skillEvidenceDAO,
            RoleDAO roleDAO,
            CompanyDAO companyDAO) {
        this.recommendationDAO = recommendationDAO;
        this.skillGapService = skillGapService;
        this.applicationDAO = applicationDAO;
        this.skillEvidenceDAO = skillEvidenceDAO;
        this.roleDAO = roleDAO;
        this.companyDAO = companyDAO;
    }

    @Override
    public List<RoleRecommendation> getRecommendations(int studentId) throws DatabaseException {
        return getRecommendations(studentId, skillGapService.getConfig());
    }

    @Override
    public List<RoleRecommendation> getRecommendations(int studentId, SkillMatchConfig config) throws DatabaseException {
        if (studentId <= 0) {
            return new ArrayList<>();
        }

        SkillMatchConfig activeConfig = config != null ? config : skillGapService.getConfig();
        List<RoleRecommendation> recommendations = new ArrayList<>();

        try {
            // 1. Retrieve student's existing applications for status tagging
            Map<String, Application> appliedMap = new HashMap<>();
            List<Application> studentApps = applicationDAO.getByStudentId(studentId);
            if (studentApps != null) {
                for (Application a : studentApps) {
                    appliedMap.put(a.getCompanyId() + ":" + a.getRoleId(), a);
                }
            }

            // 2. Retrieve student's verified evidence map (skillId -> count)
            Set<Integer> verifiedEvidenceSkills = new HashSet<>();
            List<SkillEvidence> evidenceList = skillEvidenceDAO.getEvidenceByStudent(studentId);
            if (evidenceList != null) {
                for (SkillEvidence ev : evidenceList) {
                    if (ev.isVerified()) {
                        verifiedEvidenceSkills.add(ev.getSkillId());
                    }
                }
            }

            // 3. Retrieve available company-role associations
            List<CompanyRole> companyRoles = recommendationDAO.getAllCompanyRoles();

            // Guard against duplicate recommendations for the same (companyId, roleId)
            Set<String> processedKeys = new HashSet<>();

            for (CompanyRole cr : companyRoles) {
                if (cr == null) continue;
                String key = cr.getCompanyId() + ":" + cr.getRoleId();
                if (processedKeys.contains(key)) {
                    continue;
                }
                processedKeys.add(key);

                RoleRecommendation rec = buildRecommendation(
                    studentId, cr, activeConfig, appliedMap.get(key), verifiedEvidenceSkills
                );
                recommendations.add(rec);
            }

            // 4. Rank recommendations using the documented formula & criteria
            Collections.sort(recommendations);
            return recommendations;

        } catch (SQLException e) {
            throw new DatabaseException("Error generating company and role recommendations: " + e.getMessage(), e);
        }
    }

    @Override
    public RoleRecommendation getRoleRecommendation(int studentId, int companyId, int roleId) throws DatabaseException {
        List<RoleRecommendation> all = getRecommendations(studentId);
        for (RoleRecommendation r : all) {
            if (r.getCompanyId() == companyId && r.getRoleId() == roleId) {
                return r;
            }
        }
        return null;
    }

    private RoleRecommendation buildRecommendation(
            int studentId,
            CompanyRole cr,
            SkillMatchConfig config,
            Application existingApp,
            Set<Integer> verifiedEvidenceSkills) throws DatabaseException {

        RoleRecommendation rec = new RoleRecommendation();
        rec.setCompanyId(cr.getCompanyId());
        rec.setCompanyName(cr.getCompanyName());
        rec.setCompanyLocation(cr.getCompanyLocation());
        rec.setCompanyWebsite(cr.getCompanyWebsite());
        rec.setCareersUrl(cr.getEffectiveCareersUrl());

        rec.setRoleId(cr.getRoleId());
        rec.setRoleTitle(cr.getRoleTitle());
        rec.setExperienceLevel(cr.getExperienceLevel());

        rec.setVacancyStatus(cr.getVacancyStatus() != null ? cr.getVacancyStatus() : "Potential Role Match");
        rec.setVacancySource(cr.getVacancySource() != null ? cr.getVacancySource() : "Company Competency Profile");
        rec.setInformationSource(cr.getInformationSource() != null ? cr.getInformationSource() : "Official Careers Portal / Campus Listing");
        rec.setLastVerified(cr.getLastVerified());

        // Check if student has already applied
        if (existingApp != null) {
            rec.setHasApplied(true);
            rec.setApplicationStatus(existingApp.getStatus());
        } else {
            rec.setHasApplied(false);
            rec.setApplicationStatus(null);
        }

        // Analyze skill gaps and evaluate proficiency
        List<SkillGapResult> gapResults = skillGapService.analyzeSkillGap(studentId, cr.getRoleId());

        if (skillGapService.isInsufficientRoleData(gapResults)) {
            rec.setHasInsufficientRoleData(true);
            rec.setMatchPercentage(0);
            rec.setDetailedScore(-1.0);
            rec.setReadinessAssessment("Insufficient Role Data");
            rec.setRecommendationReason("Insufficient role data: No skill requirements are defined in the database for " + cr.getRoleTitle() + ".");
            return rec;
        }

        rec.setHasInsufficientRoleData(false);
        int matchPct = skillGapService.calculateMatchPercentage(gapResults, config);
        double detailedScore = skillGapService.calculateDetailedMatchScore(gapResults, config);
        rec.setMatchPercentage(matchPct);
        rec.setDetailedScore(detailedScore);

        // Categorize competencies
        List<SkillGapResult> matching = new ArrayList<>();
        List<SkillGapResult> partiallyMatched = new ArrayList<>();
        List<SkillGapResult> missingMandatory = new ArrayList<>();
        List<SkillGapResult> missingOptional = new ArrayList<>();
        int verifiedEvidenceCount = 0;

        for (SkillGapResult r : gapResults) {
            if (r.isMatched()) {
                matching.add(r);
                if (verifiedEvidenceSkills.contains(r.getSkillId())) {
                    verifiedEvidenceCount++;
                }
            } else if (r.isUpgradeNeeded()) {
                partiallyMatched.add(r);
                if (verifiedEvidenceSkills.contains(r.getSkillId())) {
                    verifiedEvidenceCount++;
                }
            } else if (r.isMissing()) {
                if (r.isMandatory()) {
                    missingMandatory.add(r);
                } else {
                    missingOptional.add(r);
                }
            }
        }

        rec.setMatchingSkills(matching);
        rec.setPartiallyMatchedSkills(partiallyMatched);
        rec.setMissingMandatorySkills(missingMandatory);
        rec.setMissingOptionalSkills(missingOptional);
        rec.setVerifiedEvidenceCount(verifiedEvidenceCount);

        String assessment = skillGapService.getReadinessAssessment(matchPct, matching.size(), gapResults.size(), cr.getRoleTitle());
        rec.setReadinessAssessment(assessment);

        // Generate explainable, factual reason
        String reason = generateRecommendationReason(rec, gapResults.size());
        rec.setRecommendationReason(reason);

        return rec;
    }

    private String generateRecommendationReason(RoleRecommendation rec, int totalSkills) {
        StringBuilder sb = new StringBuilder();

        if (rec.isHasApplied()) {
            sb.append("Active Application (").append(rec.getApplicationStatus()).append("): ");
            sb.append("You have an existing application for ").append(rec.getRoleTitle())
              .append(" at ").append(rec.getCompanyName()).append(". Current curricular match is ")
              .append(rec.getMatchPercentage()).append("%.");
            return sb.toString();
        }

        int score = rec.getMatchPercentage();
        int matched = rec.getMatchingSkills().size();
        int missingMandatory = rec.getMissingMandatorySkills().size();

        if (score >= 80) {
            sb.append("High Readiness (").append(score).append("% match): ");
            sb.append("Strong qualification alignment. You meet ").append(matched).append(" of ").append(totalSkills)
              .append(" competencies");
            if (rec.getVerifiedEvidenceCount() > 0) {
                sb.append(" (").append(rec.getVerifiedEvidenceCount()).append(" supported by verified evidence)");
            }
            if (missingMandatory == 0) {
                sb.append(" with 0 missing mandatory skills.");
            } else {
                sb.append(", but review missing: ").append(rec.getMissingMandatoryNames()).append(".");
            }
        } else if (score >= 50) {
            sb.append("Moderate Match (").append(score).append("% match): ");
            sb.append("Solid foundation for ").append(rec.getRoleTitle()).append(" at ").append(rec.getCompanyName()).append(". ");
            sb.append("You satisfy ").append(matched).append(" competencies");
            if (!rec.getPartiallyMatchedSkills().isEmpty()) {
                sb.append(", need proficiency upgrade in ").append(rec.getPartiallyMatchedSkills().size()).append(" skill(s)");
            }
            if (missingMandatory > 0) {
                sb.append(", and are missing mandatory: ").append(rec.getMissingMandatoryNames()).append(".");
            } else if (!rec.getMissingOptionalSkills().isEmpty()) {
                sb.append(", and are missing optional: ").append(rec.getMissingOptionalNames()).append(".");
            } else {
                sb.append(".");
            }
        } else {
            sb.append("Skill Gap Detected (").append(score).append("% match): ");
            sb.append("Foundational curricular gap for ").append(rec.getRoleTitle()).append(" at ").append(rec.getCompanyName()).append(". ");
            if (missingMandatory > 0) {
                sb.append("Missing ").append(missingMandatory).append(" mandatory skill(s): ").append(rec.getMissingMandatoryNames()).append(".");
            } else {
                sb.append("Skill proficiency levels require upgrading to meet day-1 readiness benchmarks.");
            }
        }

        return sb.toString();
    }
}
