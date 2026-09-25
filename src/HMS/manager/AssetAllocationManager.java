package hms.manager;

import hms.data.TextFileUtil;
import hms.model.AdminStaff;
import hms.model.AssetAllocation;
import hms.model.HospitalAsset;
import hms.model.MedicalRequest;
import hms.model.User;
import hms.util.IdGenerator;
import hms.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Coordinates Step 2 hospital-asset allocation for APPROVED medical requests:
 * compatibility checking, schedule-conflict detection, and the ALLOCATED/
 * COMPLETED/APPROVED request-status transitions that go with it. Backed by
 * asset_allocations.txt.
 *
 * This class shows a manager collaborating with other managers instead of
 * duplicating their work: it asks ClinicalManager whether a request exists
 * and what status it has, asks HospitalAssetManager whether an asset exists
 * and is operationally available, and asks UserManager whether the allocating
 * user is really Admin Staff. AssetAllocationManager itself only owns the
 * allocation list and the allocation file.
 */
public class AssetAllocationManager {

    private static final String DATA_FILE = "data/asset_allocations.txt";
    private static final String HEADER =
            "#allocationId|requestId|assetId|allocatedByAdminId|startDateTime|endDateTime|status|createdDateTime|cancellationReason";

    /** Strict so an impossible or malformed date/time is rejected rather than silently rolled over. */
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static {
        DATETIME_FORMAT.setLenient(false);
    }

    private final List<AssetAllocation> allocations = new ArrayList<>();
    private final ClinicalManager clinicalManager;
    private final HospitalAssetManager hospitalAssetManager;
    private final UserManager userManager;
    private final File dataFile;

    public AssetAllocationManager(ClinicalManager clinicalManager, HospitalAssetManager hospitalAssetManager,
            UserManager userManager) {
        this(clinicalManager, hospitalAssetManager, userManager, new File(DATA_FILE));
    }

    public AssetAllocationManager(ClinicalManager clinicalManager, HospitalAssetManager hospitalAssetManager,
            UserManager userManager, File dataFile) {
        if (clinicalManager == null || hospitalAssetManager == null || userManager == null) {
            throw new IllegalArgumentException(
                    "Clinical manager, hospital asset manager, and user manager are required");
        }
        this.clinicalManager = clinicalManager;
        this.hospitalAssetManager = hospitalAssetManager;
        this.userManager = userManager;
        this.dataFile = dataFile;
        load();
    }

    /**
     * The Step 2 compatibility mapping (request type -> required asset type), centralized here
     * in one method so the GUI and this manager never duplicate it. Written as a plain if/else
     * chain rather than a lookup table, since there are only four request types to check.
     * Step 3 may add more asset types or clinics; this only covers what already exists
     * (LAB, IMAGING_ROOM).
     */
    public static String requiredAssetTypeFor(String requestType) {
        if (requestType == null) {
            return null;
        }
        if (requestType.equals("LAB")) {
            return "LAB";
        }
        if (requestType.equals("X_RAY") || requestType.equals("SPECIALIZED_IMAGING") || requestType.equals("IMAGING")) {
            return "IMAGING_ROOM";
        }
        return null;
    }

    /** Compatible AND operationally available assets for a request type - what the GUI should offer. */
    public List<HospitalAsset> getCompatibleAvailableAssets(String requestType) {
        List<HospitalAsset> matches = new ArrayList<>();
        String requiredType = requiredAssetTypeFor(requestType);
        if (requiredType == null) {
            return matches;
        }
        for (HospitalAsset asset : hospitalAssetManager.getAssets()) {
            if (requiredType.equals(asset.getAssetType()) && asset.isAvailability()) {
                matches.add(asset);
            }
        }
        return matches;
    }

    public List<AssetAllocation> getAllocations() {
        return new ArrayList<>(allocations);
    }

    public AssetAllocation findById(String allocationId) {
        if (allocationId == null) {
            return null;
        }
        for (AssetAllocation allocation : allocations) {
            if (allocationId.equals(allocation.getAllocationId())) {
                return allocation;
            }
        }
        return null;
    }

    public List<AssetAllocation> getAllocationsForRequest(String requestId) {
        List<AssetAllocation> matches = new ArrayList<>();
        for (AssetAllocation allocation : allocations) {
            if (allocation.getMedicalRequestId().equals(requestId)) {
                matches.add(allocation);
            }
        }
        return matches;
    }

    public List<AssetAllocation> getAllocationsForAsset(String assetId) {
        List<AssetAllocation> matches = new ArrayList<>();
        for (AssetAllocation allocation : allocations) {
            if (allocation.getAssetId().equals(assetId)) {
                matches.add(allocation);
            }
        }
        return matches;
    }

    /** The request's current non-cancelled, non-completed allocation, or null if it has none. */
    public AssetAllocation findActiveAllocation(String requestId) {
        for (AssetAllocation allocation : allocations) {
            if (allocation.getMedicalRequestId().equals(requestId)
                    && AssetAllocation.STATUS_SCHEDULED.equals(allocation.getAllocationStatus())) {
                return allocation;
            }
        }
        return null;
    }

    /** Re-reads the allocation file from scratch, discarding any in-memory state. */
    public void reload() {
        allocations.clear();
        load();
    }

    /**
     * Allocates a compatible, operationally-available, schedule-free asset to an APPROVED
     * request, then marks the request ALLOCATED. Every check below runs before anything is
     * changed or saved. If the allocation write fails, nothing is changed. If the allocation
     * saves but the follow-up request-status update fails for any reason (a write failure or an
     * unexpected runtime/status error), the allocation is rolled back (reverted and re-saved) so
     * the two files stay consistent; if that compensating save also fails, the inconsistency is
     * logged as CRITICAL since no cross-file transaction exists to guarantee it away.
     */
    public AssetAllocation createAllocation(String requestId, String assetId, String adminId, Date start, Date end)
            throws IOException {

        // Step 1: check the simple input values first.
        ValidationUtil.requireNonBlank(requestId, "Medical request");
        ValidationUtil.requireNonBlank(assetId, "Hospital asset");
        ValidationUtil.requireNonBlank(adminId, "Admin");
        requireAdminStaff(adminId);
        if (start == null) {
            throw new IllegalArgumentException("Start date/time is required");
        }
        if (end == null) {
            throw new IllegalArgumentException("End date/time is required");
        }
        if (!end.after(start)) {
            throw new IllegalArgumentException("End date/time must be after the start date/time");
        }

        // Step 2: check the medical request itself.
        MedicalRequest request = clinicalManager.findRequestById(requestId);
        if (request == null) {
            throw new IllegalArgumentException("Medical request not found: " + requestId);
        }
        if (!"APPROVED".equals(request.getStatus())) {
            throw new IllegalStateException(
                    "Only an approved request can be allocated an asset (current status: " + request.getStatus() + ")");
        }
        if (findActiveAllocation(requestId) != null) {
            throw new IllegalStateException("This request already has an active allocation");
        }

        // Step 3: check the hospital asset.
        HospitalAsset asset = hospitalAssetManager.findById(assetId);
        if (asset == null) {
            throw new IllegalArgumentException("Hospital asset not found: " + assetId);
        }
        if (!asset.isAvailability()) {
            throw new IllegalArgumentException("Asset " + assetId + " is not operationally available");
        }
        String requiredType = requiredAssetTypeFor(request.getRequestType());
        if (requiredType == null) {
            throw new IllegalStateException("No compatible asset type is defined for request type: "
                    + request.getRequestType());
        }
        if (!requiredType.equals(asset.getAssetType())) {
            throw new IllegalArgumentException("A " + request.getRequestType() + " request requires a "
                    + requiredType + " asset, not " + asset.getAssetType());
        }

        // Step 4: check the asset's schedule for a conflict.
        if (hasConflict(assetId, start, end, null)) {
            throw new IllegalStateException("Asset " + assetId + " already has a conflicting allocation in that time range");
        }

        // Step 5: every check passed - create the allocation, save it, then update the request.
        AssetAllocation allocation = new AssetAllocation(
                nextAllocationId(), requestId, assetId, adminId, start, end);
        allocations.add(allocation);
        try {
            saveAllocationsChecked();
        } catch (IOException e) {
            allocations.remove(allocation);
            throw e;
        }
        try {
            clinicalManager.markRequestAllocated(requestId);
        } catch (IOException | RuntimeException e) {
            rollBackNewAllocation(allocation, "created");
            throw e;
        }
        return allocation;
    }

    /**
     * Cancels a SCHEDULED allocation with a required reason and reverts its request to APPROVED,
     * freeing the time slot for a future allocation. Confirms the linked request exists and is
     * ALLOCATED before changing anything; same validate-first, roll-back-on-failure approach as
     * createAllocation().
     */
    public AssetAllocation cancelAllocation(String allocationId, String reason) throws IOException {
        ValidationUtil.requireNonBlank(reason, "Cancellation reason");
        AssetAllocation allocation = requireAllocation(allocationId);
        if (!AssetAllocation.STATUS_SCHEDULED.equals(allocation.getAllocationStatus())) {
            throw new IllegalStateException(
                    "Only a scheduled allocation can be cancelled (current status: " + allocation.getAllocationStatus() + ")");
        }
        requireAllocatedLinkedRequest(allocation);

        allocation.cancel(reason);
        try {
            saveAllocationsChecked();
        } catch (IOException e) {
            allocation.revertToScheduled();
            throw e;
        }
        try {
            clinicalManager.revertRequestToApproved(allocation.getMedicalRequestId());
        } catch (IOException | RuntimeException e) {
            allocation.revertToScheduled();
            rollBackAllocationSaveOnly(allocationId, "cancellation");
            throw e;
        }
        return allocation;
    }

    /**
     * Completes a SCHEDULED allocation and marks its request COMPLETED. Confirms the linked
     * request exists and is ALLOCATED before changing anything; same validate-first, roll-back-
     * on-failure approach as createAllocation().
     */
    public AssetAllocation completeAllocation(String allocationId) throws IOException {
        AssetAllocation allocation = requireAllocation(allocationId);
        if (!AssetAllocation.STATUS_SCHEDULED.equals(allocation.getAllocationStatus())) {
            throw new IllegalStateException(
                    "Only a scheduled allocation can be completed (current status: " + allocation.getAllocationStatus() + ")");
        }
        requireAllocatedLinkedRequest(allocation);

        allocation.markCompleted();
        try {
            saveAllocationsChecked();
        } catch (IOException e) {
            allocation.revertToScheduled();
            throw e;
        }
        try {
            clinicalManager.markRequestCompleted(allocation.getMedicalRequestId());
        } catch (IOException | RuntimeException e) {
            allocation.revertToScheduled();
            rollBackAllocationSaveOnly(allocationId, "completion");
            throw e;
        }
        return allocation;
    }

    /**
     * Confirms the allocation's linked medical request exists and is ALLOCATED, without changing
     * any state. Called before cancel/complete touch the allocation, so an inconsistent or
     * missing request is rejected with neither file written.
     */
    private void requireAllocatedLinkedRequest(AssetAllocation allocation) {
        MedicalRequest request = clinicalManager.findRequestById(allocation.getMedicalRequestId());
        if (request == null) {
            throw new IllegalStateException(
                    "Linked medical request not found: " + allocation.getMedicalRequestId());
        }
        if (!"ALLOCATED".equals(request.getStatus())) {
            throw new IllegalStateException("Linked medical request is not ALLOCATED (current status: "
                    + request.getStatus() + ")");
        }
    }

    /** Confirms adminId belongs to a real Admin Staff user - a Doctor, Patient or Medical Manager ID is rejected. */
    private AdminStaff requireAdminStaff(String adminId) {
        User user = userManager.findById(adminId);
        if (!(user instanceof AdminStaff)) {
            throw new IllegalArgumentException("Admin Staff not found: " + adminId);
        }
        return (AdminStaff) user;
    }

    /** Removes a just-created allocation from memory and tries to re-persist that removal. */
    private void rollBackNewAllocation(AssetAllocation allocation, String verb) {
        allocations.remove(allocation);
        try {
            saveAllocationsChecked();
        } catch (IOException rollbackFailure) {
            System.err.println("CRITICAL: allocation " + allocation.getAllocationId() + " (" + verb
                    + ") could not be rolled back after a request-status save failure - the allocation file and "
                    + "request file may now be inconsistent: " + rollbackFailure.getMessage());
        }
    }

    /** Tries to re-persist the current (already-reverted) in-memory allocation state. */
    private void rollBackAllocationSaveOnly(String allocationId, String verb) {
        try {
            saveAllocationsChecked();
        } catch (IOException rollbackFailure) {
            System.err.println("CRITICAL: " + verb + " of allocation " + allocationId
                    + " could not be rolled back after a request-status save failure - the allocation file and "
                    + "request file may now be inconsistent: " + rollbackFailure.getMessage());
        }
    }

    /**
     * Standard overlap rule: newStart < existingEnd AND newEnd > existingStart, checked against
     * every non-cancelled allocation (SCHEDULED or COMPLETED) - a completed booking still blocks
     * its historical time slot. Only CANCELLED allocations free their slot. Back-to-back is allowed.
     */
    private boolean hasConflict(String assetId, Date start, Date end, String excludingAllocationId) {
        for (AssetAllocation allocation : allocations) {
            if (!allocation.getAssetId().equals(assetId)) {
                continue;
            }
            if (AssetAllocation.STATUS_CANCELLED.equals(allocation.getAllocationStatus())) {
                continue;
            }
            if (excludingAllocationId != null && allocation.getAllocationId().equals(excludingAllocationId)) {
                continue;
            }
            if (start.before(allocation.getEndDateTime()) && end.after(allocation.getStartDateTime())) {
                return true;
            }
        }
        return false;
    }

    private AssetAllocation requireAllocation(String allocationId) {
        AssetAllocation allocation = findById(allocationId);
        if (allocation == null) {
            throw new IllegalArgumentException("Asset allocation not found: " + allocationId);
        }
        return allocation;
    }

    private String nextAllocationId() {
        List<String> ids = new ArrayList<>();
        for (AssetAllocation allocation : allocations) {
            ids.add(allocation.getAllocationId());
        }
        return IdGenerator.next("AL", ids);
    }

    private void load() {
        // Keeps every allocation ID seen so far in this file, so a duplicate row can be
        // detected with a plain list search instead of a Set.
        List<String> loadedIds = new ArrayList<>();
        for (String line : TextFileUtil.readLines(dataFile)) {
            String[] fields = line.split("\\|", -1);
            if (fields.length < 9) {
                System.err.println("Skipping malformed asset allocation record: " + line);
                continue;
            }
            try {
                Date start = DATETIME_FORMAT.parse(fields[4]);
                Date end = DATETIME_FORMAT.parse(fields[5]);
                Date created = DATETIME_FORMAT.parse(fields[7]);
                String cancellationReason = fields[8].isEmpty() ? null : fields[8];
                // The AssetAllocation constructor rejects unknown statuses, a CANCELLED row
                // without a reason, and a non-CANCELLED row that has one - all surfaced as
                // IllegalArgumentException and skipped below, same as any other malformed row.
                AssetAllocation allocation = new AssetAllocation(
                        fields[0], fields[1], fields[2], fields[3], start, end, fields[6], created,
                        cancellationReason);
                if (loadedIds.contains(allocation.getAllocationId())) {
                    System.err.println("Skipping duplicate asset allocation ID: " + line);
                    continue;
                }
                loadedIds.add(allocation.getAllocationId());
                allocations.add(allocation);
            } catch (ParseException e) {
                System.err.println("Skipping asset allocation with invalid date/time: " + line);
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping asset allocation with invalid data (" + e.getMessage() + "): " + line);
            }
        }
    }

    /** Propagates a write failure to the caller instead of only logging it - every save uses this. */
    private void saveAllocationsChecked() throws IOException {
        List<String> lines = new ArrayList<>();
        for (AssetAllocation allocation : allocations) {
            lines.add(String.join("|",
                    allocation.getAllocationId(),
                    allocation.getMedicalRequestId(),
                    allocation.getAssetId(),
                    allocation.getAllocatedByAdminId(),
                    DATETIME_FORMAT.format(allocation.getStartDateTime()),
                    DATETIME_FORMAT.format(allocation.getEndDateTime()),
                    allocation.getAllocationStatus(),
                    DATETIME_FORMAT.format(allocation.getCreatedDateTime()),
                    TextFileUtil.sanitize(allocation.getCancellationReason())));
        }
        TextFileUtil.writeLinesChecked(dataFile, HEADER, lines);
    }
}
