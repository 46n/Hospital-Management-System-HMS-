package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.HospitalAsset;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Coordinates hospital asset management (consultation rooms, wards, labs,
 * imaging rooms), backed by hospital_assets.txt.
 */
public class HospitalAssetManager {

    private static final String DATA_FILE = "data/hospital_assets.txt";
    private static final String HEADER = "#assetId|assetName|assetType|location|availability|managedByStaffId";
    private static final List<String> VALID_TYPES =
            Arrays.asList("CONSULTATION_ROOM", "WARD", "LAB", "IMAGING_ROOM");

    private final List<HospitalAsset> assets = new ArrayList<>();
    private final UserManager userManager;
    private final File dataFile;

    public HospitalAssetManager(UserManager userManager) {
        this(userManager, new File(DATA_FILE));
    }

    public HospitalAssetManager(UserManager userManager, File dataFile) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }

    public static List<String> getValidTypes() {
        return new ArrayList<>(VALID_TYPES);
    }

    public HospitalAsset createAsset(String assetName, String assetType, String location,
            boolean availability, String createdByStaffId) {
        ValidationUtil.requireNonBlank(assetName, "Asset name");
        validateType(assetType);
        ValidationUtil.requireNonBlank(location, "Location");
        AdminStaff staff = requireAdminStaff(createdByStaffId);

        HospitalAsset asset = new HospitalAsset(
                IdGenerator.next("HA", collectIds()), assetName, assetType, location, availability);
        asset.setManagedBy(staff);
        assets.add(asset);
        save();
        return asset;
    }

    public void updateAsset(String assetId, String assetName, String assetType, String location) {
        HospitalAsset asset = requireAsset(assetId);
        ValidationUtil.requireNonBlank(assetName, "Asset name");
        validateType(assetType);
        ValidationUtil.requireNonBlank(location, "Location");
        asset.setAssetName(assetName);
        asset.setAssetType(assetType);
        asset.setLocation(location);
        save();
    }

    public void setAvailability(String assetId, boolean availability) {
        HospitalAsset asset = requireAsset(assetId);
        asset.setAvailability(availability);
        save();
    }

    public void deleteAsset(String assetId) {
        HospitalAsset asset = requireAsset(assetId);
        assets.remove(asset);
        save();
    }

    public HospitalAsset findById(String assetId) {
        if (assetId == null) {
            return null;
        }
        for (HospitalAsset asset : assets) {
            if (assetId.equals(asset.getAssetId())) {
                return asset;
            }
        }
        return null;
    }

    public List<HospitalAsset> getAssets() {
        return new ArrayList<>(assets);
    }

    private void validateType(String assetType) {
        ValidationUtil.requireNonBlank(assetType, "Asset type");
        if (!VALID_TYPES.contains(assetType)) {
            throw new IllegalArgumentException("Asset type must be one of " + VALID_TYPES);
        }
    }

    private AdminStaff requireAdminStaff(String staffId) {
        User user = userManager.findById(staffId);
        if (!(user instanceof AdminStaff)) {
            throw new IllegalArgumentException("Admin Staff not found: " + staffId);
        }
        return (AdminStaff) user;
    }

    private HospitalAsset requireAsset(String assetId) {
        HospitalAsset asset = findById(assetId);
        if (asset == null) {
            throw new IllegalArgumentException("Hospital asset not found: " + assetId);
        }
        return asset;
    }

    private List<String> collectIds() {
        List<String> ids = new ArrayList<>();
        for (HospitalAsset asset : assets) {
            ids.add(asset.getAssetId());
        }
        return ids;
    }

    private void load() {
        for (String line : TextFileUtil.readLines(dataFile)) {
            HospitalAsset asset = parseLine(line);
            if (asset != null) {
                assets.add(asset);
            }
        }
    }

    private HospitalAsset parseLine(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length < 6) {
            System.err.println("Skipping malformed hospital asset record: " + line);
            return null;
        }
        HospitalAsset asset = new HospitalAsset(
                fields[0], fields[1], fields[2], fields[3], Boolean.parseBoolean(fields[4]));
        if (!fields[5].isEmpty()) {
            User user = userManager.findById(fields[5]);
            if (user instanceof AdminStaff) {
                asset.setManagedBy((AdminStaff) user);
            }
        }
        return asset;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (HospitalAsset asset : assets) {
            lines.add(toLine(asset));
        }
        TextFileUtil.writeLines(dataFile, HEADER, lines);
    }

    private String toLine(HospitalAsset asset) {
        return String.join("|",
                asset.getAssetId(),
                TextFileUtil.emptyIfNull(asset.getAssetName()),
                TextFileUtil.emptyIfNull(asset.getAssetType()),
                TextFileUtil.emptyIfNull(asset.getLocation()),
                String.valueOf(asset.isAvailability()),
                asset.getManagedBy() == null ? "" : asset.getManagedBy().getStaffId());
    }
}
