package hms.model;

/**
 * The physical categories of hospital asset Admin can create and allocate.
 * Kept as one plain enum instead of several ad-hoc lists of valid-type
 * strings, so every place that needs "what types exist" reads from exactly
 * one source.
 */
public enum HospitalAssetType {

    CONSULTATION_ROOM,
    CLINIC,
    WARD,
    LAB,
    XRAY_ROOM,
    IMAGING_ROOM;

    /**
     * Looks up a type by the exact text stored in hospital_assets.txt, or returns null if the
     * text does not match any known type. Used both to validate a new/updated asset and to skip
     * an unknown or legacy type while loading, instead of crashing.
     */
    public static HospitalAssetType fromStoredValue(String storedValue) {
        if (storedValue == null) {
            return null;
        }
        for (HospitalAssetType type : values()) {
            if (type.name().equals(storedValue)) {
                return type;
            }
        }
        return null;
    }
}
