package hms.model;

import java.util.Date;

/**
 * Records a hospital asset booked against one APPROVED medical request for a
 * specific time range. This is the Step 2 domain model - business rules
 * (compatibility, conflicts, request-status transitions) live in
 * AssetAllocationManager; this class only holds and validates its own state.
 *
 * Encapsulation: every field below is private. Outside code can only read
 * this object's state through the getters, and can only change its status
 * through markCompleted() / cancel(reason), never by assigning an arbitrary
 * status string.
 */
public class AssetAllocation {

    // The only three statuses an allocation can ever be in.
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final String allocationId;
    private final String medicalRequestId;
    private final String assetId;
    private final String allocatedByAdminId;
    private final Date startDateTime;
    private final Date endDateTime;
    private final Date createdDateTime;
    private String allocationStatus;
    private String cancellationReason;

    /** Creates a brand-new allocation: starts SCHEDULED, created now. */
    public AssetAllocation(String allocationId, String medicalRequestId, String assetId,
            String allocatedByAdminId, Date startDateTime, Date endDateTime) {
        this(allocationId, medicalRequestId, assetId, allocatedByAdminId, startDateTime, endDateTime,
                STATUS_SCHEDULED, new Date(), null);
    }

    /**
     * Reconstructs a previously-persisted allocation with its historical status and timestamps.
     * All the checks below run every time an AssetAllocation is built, so an invalid object can
     * never exist - this is the same "validate in the constructor" idea used elsewhere in the
     * model classes.
     */
    public AssetAllocation(String allocationId, String medicalRequestId, String assetId,
            String allocatedByAdminId, Date startDateTime, Date endDateTime, String allocationStatus,
            Date createdDateTime, String cancellationReason) {
        if (allocationId == null || allocationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocation ID is required");
        }
        if (medicalRequestId == null || medicalRequestId.trim().isEmpty()) {
            throw new IllegalArgumentException("Medical request ID is required");
        }
        if (assetId == null || assetId.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset ID is required");
        }
        if (allocatedByAdminId == null || allocatedByAdminId.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocating admin ID is required");
        }
        if (startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Start and end date/time are required");
        }
        if (!endDateTime.after(startDateTime)) {
            throw new IllegalArgumentException("End date/time must be after the start date/time");
        }
        if (!isKnownStatus(allocationStatus)) {
            throw new IllegalArgumentException("Unknown allocation status: " + allocationStatus);
        }
        boolean hasReason = cancellationReason != null && !cancellationReason.trim().isEmpty();
        if (STATUS_CANCELLED.equals(allocationStatus) && !hasReason) {
            throw new IllegalArgumentException("A cancelled allocation requires a cancellation reason");
        }
        if (!STATUS_CANCELLED.equals(allocationStatus) && hasReason) {
            throw new IllegalArgumentException("Only a cancelled allocation may have a cancellation reason");
        }

        this.allocationId = allocationId;
        this.medicalRequestId = medicalRequestId;
        this.assetId = assetId;
        this.allocatedByAdminId = allocatedByAdminId;
        // Dates are mutable in Java, so we copy them here instead of storing the caller's
        // object directly. Otherwise the caller could change our start/end time later just by
        // mutating the Date it originally passed in.
        this.startDateTime = new Date(startDateTime.getTime());
        this.endDateTime = new Date(endDateTime.getTime());
        this.createdDateTime = new Date((createdDateTime == null ? new Date() : createdDateTime).getTime());
        this.allocationStatus = allocationStatus;
        this.cancellationReason = hasReason ? cancellationReason.trim() : null;
    }

    /** True only for one of the three statuses this class allows - guards against a typo or bad file row. */
    private static boolean isKnownStatus(String status) {
        if (status == null) {
            return false;
        }
        return status.equals(STATUS_SCHEDULED) || status.equals(STATUS_COMPLETED) || status.equals(STATUS_CANCELLED);
    }

    public String getAllocationId() {
        return allocationId;
    }

    public String getMedicalRequestId() {
        return medicalRequestId;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAllocatedByAdminId() {
        return allocatedByAdminId;
    }

    /** Defensive copy - mutating the returned Date cannot change this allocation's schedule. */
    public Date getStartDateTime() {
        return new Date(startDateTime.getTime());
    }

    /** Defensive copy - mutating the returned Date cannot change this allocation's schedule. */
    public Date getEndDateTime() {
        return new Date(endDateTime.getTime());
    }

    /** Defensive copy - mutating the returned Date cannot change this allocation's audit trail. */
    public Date getCreatedDateTime() {
        return new Date(createdDateTime.getTime());
    }

    public String getAllocationStatus() {
        return allocationStatus;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    // ---- behavior: the only ways an allocation's status is allowed to change ----

    /** SCHEDULED -> COMPLETED. Only the manager calls this, after confirming the transition is legal. */
    public void markCompleted() {
        if (!STATUS_SCHEDULED.equals(allocationStatus)) {
            throw new IllegalStateException(
                    "Only a scheduled allocation can be completed (current status: " + allocationStatus + ")");
        }
        allocationStatus = STATUS_COMPLETED;
        cancellationReason = null;
    }

    /** SCHEDULED -> CANCELLED with a required, non-blank reason. */
    public void cancel(String reason) {
        if (!STATUS_SCHEDULED.equals(allocationStatus)) {
            throw new IllegalStateException(
                    "Only a scheduled allocation can be cancelled (current status: " + allocationStatus + ")");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("A cancellation reason is required");
        }
        allocationStatus = STATUS_CANCELLED;
        cancellationReason = reason.trim();
    }

    /**
     * Reverts an uncommitted COMPLETED/CANCELLED change back to SCHEDULED. Used only by
     * AssetAllocationManager to roll back an in-memory mutation that failed to persist - this
     * undoes a change that was never actually saved, not a new business transition.
     */
    public void revertToScheduled() {
        allocationStatus = STATUS_SCHEDULED;
        cancellationReason = null;
    }
}
