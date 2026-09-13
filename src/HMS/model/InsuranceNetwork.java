package hms.model;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class InsuranceNetwork {

    private String networkId;
    private String providerName;
    private String planName;
    private double coveragePercentage;
    private boolean active;
    private AdminStaff managedBy;

    public InsuranceNetwork() {
    }

    public InsuranceNetwork(String networkId, String providerName, String planName, double coveragePercentage, boolean active) {
        this.networkId = networkId;
        this.providerName = providerName;
        this.planName = planName;
        this.coveragePercentage = coveragePercentage;
        this.active = active;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public double getCoveragePercentage() {
        return coveragePercentage;
    }

    public void setCoveragePercentage(double coveragePercentage) {
        this.coveragePercentage = coveragePercentage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public AdminStaff getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(AdminStaff managedBy) {
        this.managedBy = managedBy;
    }
}
