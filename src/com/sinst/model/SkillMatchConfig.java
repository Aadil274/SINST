package com.sinst.model;

/**
 * Configuration parameters for the transparent and configurable SINST skill-match algorithm.
 *
 * Match Calculation Formula:
 *   Match Score = (Sum of weighted skill-match values / Sum of applicable skill weights) * 100
 *
 * Where:
 *   - skill-match value = demonstrated level / required level (or 1.0 if demonstrated >= required, 0.0 if missing)
 *   - mandatory skill weight = default 2.0x (configurable)
 *   - optional skill weight  = default 1.0x (configurable)
 */
public class SkillMatchConfig {

    public static final double DEFAULT_MANDATORY_WEIGHT = 2.0;
    public static final double DEFAULT_OPTIONAL_WEIGHT = 1.0;
    public static final double DEFAULT_HIGH_READINESS_THRESHOLD = 80.0;
    public static final double DEFAULT_MODERATE_READINESS_THRESHOLD = 50.0;

    private double mandatoryWeight;
    private double optionalWeight;
    private double highReadinessThreshold;
    private double moderateReadinessThreshold;

    public SkillMatchConfig() {
        this(DEFAULT_MANDATORY_WEIGHT, DEFAULT_OPTIONAL_WEIGHT, DEFAULT_HIGH_READINESS_THRESHOLD, DEFAULT_MODERATE_READINESS_THRESHOLD);
    }

    public SkillMatchConfig(double mandatoryWeight, double optionalWeight) {
        this(mandatoryWeight, optionalWeight, DEFAULT_HIGH_READINESS_THRESHOLD, DEFAULT_MODERATE_READINESS_THRESHOLD);
    }

    public SkillMatchConfig(double mandatoryWeight, double optionalWeight, double highReadinessThreshold, double moderateReadinessThreshold) {
        this.mandatoryWeight = mandatoryWeight > 0 ? mandatoryWeight : DEFAULT_MANDATORY_WEIGHT;
        this.optionalWeight = optionalWeight >= 0 ? optionalWeight : DEFAULT_OPTIONAL_WEIGHT;
        this.highReadinessThreshold = highReadinessThreshold;
        this.moderateReadinessThreshold = moderateReadinessThreshold;
    }

    public double getMandatoryWeight() {
        return mandatoryWeight;
    }

    public void setMandatoryWeight(double mandatoryWeight) {
        if (mandatoryWeight > 0) {
            this.mandatoryWeight = mandatoryWeight;
        }
    }

    public double getOptionalWeight() {
        return optionalWeight;
    }

    public void setOptionalWeight(double optionalWeight) {
        if (optionalWeight >= 0) {
            this.optionalWeight = optionalWeight;
        }
    }

    public double getHighReadinessThreshold() {
        return highReadinessThreshold;
    }

    public void setHighReadinessThreshold(double highReadinessThreshold) {
        this.highReadinessThreshold = highReadinessThreshold;
    }

    public double getModerateReadinessThreshold() {
        return moderateReadinessThreshold;
    }

    public void setModerateReadinessThreshold(double moderateReadinessThreshold) {
        this.moderateReadinessThreshold = moderateReadinessThreshold;
    }
}
