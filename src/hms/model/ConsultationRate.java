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
        this.effectiveDate = effectiveDate;
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

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
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
