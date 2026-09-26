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
     * Looks up a type by its exact canonical name, or returns null if the text is not one of the
     * six canonical values. Used to validate NEW create/update input (HospitalAssetManager's
     * createAsset()/updateAsset()) - a legacy alias must never be accepted here merely because
     * loading an old row accepts it. See {@link #fromPersistedValue} for loading an existing row.
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

    /**
     * Looks up a type when LOADING an existing row from hospital_assets.txt, additionally
     * recognizing legacy/group-data aliases that predate this enum's canonical names but that
     * real committed demonstration data (merged in from the group's main branch) still uses - so
     * those existing rows load correctly instead of being silently skipped. Resolves to the same
     * canonical enum constant either way, so the in-memory model and any later re-save always use
     * the canonical name, never the legacy alias.
     *
     * Never used to validate new create/update input - see {@link #fromStoredValue} for that.
     * A genuinely unknown value (neither canonical nor a recognized legacy alias) still returns
     * null and is skipped while loading, exactly as before.
     *
     * Legacy aliases recognized:
     * <ul>
     *   <li>INPATIENT_WARD -&gt; WARD</li>
     *   <li>LABORATORY -&gt; LAB</li>
     * </ul>
     */
    public static HospitalAssetType fromPersistedValue(String storedValue) {
        if ("INPATIENT_WARD".equals(storedValue)) {
            return WARD;
        }
        if ("LABORATORY".equals(storedValue)) {
            return LAB;
        }
        return fromStoredValue(storedValue);
    }
}
