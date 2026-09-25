package hms.model;

import java.util.Date;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class ConsultationRate {

    private String rateId;
    private String consultationType;
    private double baseAmount;
    private Date effectiveDate;
    private boolean active;
    private AdminStaff configuredBy;

    public ConsultationRate() {
    }

    public ConsultationRate(String rateId, String consultationType, double baseAmount, Date effectiveDate, boolean active) {
        this.rateId = rateId;
        this.consultationType = consultationType;
        this.baseAmount = baseAmount;
        this.effectiveDate = copyDate(effectiveDate);
        this.active = active;
    }

    public String getRateId() {
        return rateId;
    }

    public void setRateId(String rateId) {
        this.rateId = rateId;
    }

    public String getConsultationType() {
        return consultationType;
    }

    public void setConsultationType(String consultationType) {
        this.consultationType = consultationType;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    /** Defensive copy - mutating the returned Date cannot change this rate's effective date. */
    public Date getEffectiveDate() {
        return copyDate(effectiveDate);
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = copyDate(effectiveDate);
    }

    // Dates are mutable in Java, so every getter/setter copies instead of sharing the caller's
    // Date instance - otherwise a caller could change this rate's effective date later just by
    // mutating a Date it happened to still hold a reference to, bypassing manager validation and
    // save(). Null-safe since effectiveDate has no non-null guarantee at the model level (only
    // ConsultationRateManager enforces that a rate always has one).
    private static Date copyDate(Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public AdminStaff getConfiguredBy() {
        return configuredBy;
    }

    public void setConfiguredBy(AdminStaff configuredBy) {
        this.configuredBy = configuredBy;
    }
}
