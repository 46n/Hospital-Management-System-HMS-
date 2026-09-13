package hms.model;

/**
 * Model entity defined by the HMS UML class diagram.
 */
public class HospitalAsset {

    private String assetId;
    private String assetName;
    private String assetType;
    private String location;
    private boolean availability;
    private AdminStaff managedBy;

    public HospitalAsset() {
    }

    public HospitalAsset(String assetId, String assetName, String assetType, String location, boolean availability) {
        this.assetId = assetId;
        this.assetName = assetName;
        this.assetType = assetType;
        this.location = location;
        this.availability = availability;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetType() {
        return assetType;
    }

    public void setAssetType(String assetType) {
        this.assetType = assetType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public AdminStaff getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(AdminStaff managedBy) {
        this.managedBy = managedBy;
    }
}
