package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.HospitalAsset;
import hms.model.HospitalAssetType;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Coordinates hospital asset management (consultation rooms, clinics, wards,
 * labs, X-ray rooms, imaging rooms), backed by hospital_assets.txt. The set
 * of valid types lives in one place, the HospitalAssetType enum - this class
 * never keeps its own separate list of type names.
 */
public class HospitalAssetManager {

    private static final String DATA_FILE = "data/hospital_assets.txt";
    private static final String HEADER = "#assetId|assetName|assetType|location|availability|managedByStaffId";

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

    /** The type names Admin may pick from - every value of HospitalAssetType, in declared order. */
    public static List<String> getValidTypes() {
        List<String> types = new ArrayList<>();
        for (HospitalAssetType type : HospitalAssetType.values()) {
            types.add(type.name());
        }
        return types;
    }

    public HospitalAsset createAsset(String assetName, String assetType, String location,
            boolean availability, String createdByStaffId) {
        String trimmedName = requireTrimmedName(assetName);
        String canonicalType = requireCanonicalType(assetType);
        String trimmedLocation = requireTrimmedLocation(location);
        AdminStaff staff = requireAdminStaff(createdByStaffId);
        requireNameAvailable(trimmedName, null);

        HospitalAsset asset = new HospitalAsset(
                IdGenerator.next("HA", collectIds()), trimmedName, canonicalType, trimmedLocation, availability);
        asset.setManagedBy(staff);
        assets.add(asset);
        try {
            save();
        } catch (IllegalStateException e) {
            assets.remove(asset);
            throw e;
        }
        return asset;
    }

    public void updateAsset(String assetId, String assetName, String assetType, String location) {
        HospitalAsset asset = requireAsset(assetId);
        String trimmedName = requireTrimmedName(assetName);
        String canonicalType = requireCanonicalType(assetType);
        String trimmedLocation = requireTrimmedLocation(location);
        requireNameAvailable(trimmedName, assetId);

        String previousName = asset.getAssetName();
        String previousType = asset.getAssetType();
        String previousLocation = asset.getLocation();
        asset.setAssetName(trimmedName);
        asset.setAssetType(canonicalType);
        asset.setLocation(trimmedLocation);
        try {
            save();
        } catch (IllegalStateException e) {
            asset.setAssetName(previousName);
            asset.setAssetType(previousType);
            asset.setLocation(previousLocation);
            throw e;
        }
    }

    public void setAvailability(String assetId, boolean availability) {
        HospitalAsset asset = requireAsset(assetId);
        boolean previousAvailability = asset.isAvailability();
        asset.setAvailability(availability);
        try {
            save();
        } catch (IllegalStateException e) {
            asset.setAvailability(previousAvailability);
            throw e;
        }
    }

    public void deleteAsset(String assetId) {
        HospitalAsset asset = requireAsset(assetId);
        int previousIndex = assets.indexOf(asset);
        assets.remove(asset);
        try {
            save();
        } catch (IllegalStateException e) {
            assets.add(previousIndex, asset);
            throw e;
        }
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

    private String requireTrimmedName(String assetName) {
        ValidationUtil.requireFileSafeText(assetName, "Asset name");
        return assetName.trim();
    }

    private String requireTrimmedLocation(String location) {
        ValidationUtil.requireFileSafeText(location, "Location");
        return location.trim();
    }

    /**
     * Validates the given type text and returns the canonical enum name to store - never the
     * caller's original text. This is what keeps " LAB " and "LAB" from ever being stored as two
     * different-looking values: both resolve to the same canonical "LAB".
     */
    private String requireCanonicalType(String assetType) {
        ValidationUtil.requireNonBlank(assetType, "Asset type");
        HospitalAssetType type = HospitalAssetType.fromStoredValue(assetType.trim());
        if (type == null) {
            throw new IllegalArgumentException("Asset type must be one of " + getValidTypes());
        }
        return type.name();
    }

    /**
     * Rejects a name already used by a different asset, ignoring case and surrounding spaces.
     * excludingAssetId is null when creating (nothing to exclude) and the asset's own ID when
     * updating (so an asset may keep its own name).
     */
    private void requireNameAvailable(String trimmedName, String excludingAssetId) {
        for (HospitalAsset asset : assets) {
            if (excludingAssetId != null && asset.getAssetId().equals(excludingAssetId)) {
                continue;
            }
            String existingName = asset.getAssetName();
            if (existingName != null && existingName.trim().equalsIgnoreCase(trimmedName)) {
                throw new IllegalArgumentException("An asset named \"" + trimmedName + "\" already exists");
            }
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
        // A missing file means "no assets yet", but a file that exists and cannot be read must
        // not be silently treated the same way - that would risk overwriting real asset data
        // with nothing on the next save.
        List<String> rawLines;
        try {
            rawLines = TextFileUtil.readLinesChecked(dataFile);
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + dataFile.getPath() + ": " + e.getMessage(), e);
        }

        // Every ID and every lowercased name already loaded from this file, so a duplicate row
        // can be detected with a plain list search - the same style used elsewhere in the app.
        List<String> loadedIds = new ArrayList<>();
        List<String> loadedNamesLower = new ArrayList<>();
        for (String line : rawLines) {
            HospitalAsset asset = parseLine(line, loadedIds, loadedNamesLower);
            if (asset != null) {
                loadedIds.add(asset.getAssetId());
                loadedNamesLower.add(asset.getAssetName().toLowerCase());
                assets.add(asset);
            }
        }
    }

    private HospitalAsset parseLine(String line, List<String> loadedIds, List<String> loadedNamesLower) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 6) {
            System.err.println("Skipping malformed hospital asset record: " + line);
            return null;
        }
        String assetId = fields[0].trim();
        String assetName = fields[1].trim();
        String assetTypeText = fields[2].trim();
        String location = fields[3].trim();
        String availabilityText = fields[4].trim();
        String managedByStaffId = fields[5].trim();

        if (assetId.isEmpty()) {
            System.err.println("Skipping hospital asset with a blank asset ID: " + line);
            return null;
        }
        if (loadedIds.contains(assetId)) {
            System.err.println("Skipping duplicate hospital asset ID: " + line);
            return null;
        }
        try {
            ValidationUtil.requireFileSafeText(assetName, "Asset name");
        } catch (IllegalArgumentException e) {
            System.err.println("Skipping hospital asset with an invalid name (" + e.getMessage() + "): " + line);
            return null;
        }
        if (loadedNamesLower.contains(assetName.toLowerCase())) {
            System.err.println("Skipping duplicate hospital asset name: " + line);
            return null;
        }
        try {
            ValidationUtil.requireFileSafeText(location, "Location");
        } catch (IllegalArgumentException e) {
            System.err.println("Skipping hospital asset with an invalid location (" + e.getMessage() + "): " + line);
            return null;
        }
        HospitalAssetType type = HospitalAssetType.fromStoredValue(assetTypeText);
        if (type == null) {
            System.err.println("Skipping hospital asset with unknown type \"" + assetTypeText + "\": " + line);
            return null;
        }
        if (!availabilityText.equalsIgnoreCase("true") && !availabilityText.equalsIgnoreCase("false")) {
            System.err.println("Skipping hospital asset with invalid availability value \"" + availabilityText + "\": " + line);
            return null;
        }

        AdminStaff managedBy = null;
        if (!managedByStaffId.isEmpty()) {
            User user = userManager.findById(managedByStaffId);
            if (!(user instanceof AdminStaff)) {
                System.err.println("Skipping hospital asset with an unknown or non-admin managing staff ID \""
                        + managedByStaffId + "\": " + line);
                return null;
            }
            managedBy = (AdminStaff) user;
        }

        // Store the canonical enum name, not the raw file text, so a legacy row that happened to
        // have extra spaces around its type does not perpetuate that inconsistency in memory.
        HospitalAsset asset = new HospitalAsset(
                assetId, assetName, type.name(), location, Boolean.parseBoolean(availabilityText));
        asset.setManagedBy(managedBy);
        return asset;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (HospitalAsset asset : assets) {
            lines.add(toLine(asset));
        }
        try {
            TextFileUtil.writeLinesChecked(dataFile, HEADER, lines);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save hospital asset data: " + e.getMessage(), e);
        }
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
