package hms.model;

/**
 * Defines a reusable assessment/check-up type a Medical Manager configures
 * (e.g. "Full Blood Count"), distinct from AssessmentResult which records
 * one actual recorded result against a medical record.
 */
public class AssessmentType {

    private String assessmentTypeId;
    private String name;
    private String category;
    private String gradingRule;
    private double baseCharge;
    private boolean active;
    private MedicalManager createdBy;

    public AssessmentType() {
    }

    public AssessmentType(String assessmentTypeId, String name, String category,
            String gradingRule, double baseCharge, boolean active) {
        this.assessmentTypeId = assessmentTypeId;
        this.name = name;
        this.category = category;
        this.gradingRule = gradingRule;
        this.baseCharge = baseCharge;
        this.active = active;
    }

    public String getAssessmentTypeId() {
        return assessmentTypeId;
    }

    public void setAssessmentTypeId(String assessmentTypeId) {
        this.assessmentTypeId = assessmentTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGradingRule() {
        return gradingRule;
    }

    public void setGradingRule(String gradingRule) {
        this.gradingRule = gradingRule;
    }

    public double getBaseCharge() {
        return baseCharge;
    }

    public void setBaseCharge(double baseCharge) {
        this.baseCharge = baseCharge;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public MedicalManager getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(MedicalManager createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Checks a recorded result's value against this type's own grading rule,
     * so the rule is interpreted in exactly one place regardless of caller.
     */
    public void validateValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Result value is required");
        }
        String trimmed = value.trim();
        if ("NUMERIC_RANGE".equals(gradingRule)) {
            try {
                Double.parseDouble(trimmed);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Result value must be numeric for " + name);
            }
        } else if ("PASS_FAIL".equals(gradingRule)) {
            String normalized = trimmed.toUpperCase();
            if (!normalized.equals("PASS") && !normalized.equals("FAIL")) {
                throw new IllegalArgumentException("Result value must be PASS or FAIL for " + name);
            }
        }
        // DESCRIPTIVE (or an unrecognized rule): any non-blank text is acceptable.
    }
}
