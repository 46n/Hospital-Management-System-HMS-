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

    // Dates are mutable in Java, so we copy them here instead of storing the caller's reference -
    // this stops code elsewhere in the app from reaching back in later and mutating the Date it
    // originally passed in.
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
