package hms.ui.common;

import hms.model.MedicalRequest;

import java.text.SimpleDateFormat;

/**
 * Shared table formatting for the medical request list shown to Admin Staff.
 * Pure display formatting - no business logic.
 */
public final class MedicalRequestTableUtil {

    public static final String[] COLUMN_NAMES =
            {"Request ID", "Patient", "Doctor", "Type", "Details", "Date", "Status"};

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private MedicalRequestTableUtil() {
    }

    public static Object[] toRow(MedicalRequest request) {
        String patient = "-";
        if (request.getMedicalRecord() != null && request.getMedicalRecord().getPatient() != null) {
            patient = request.getMedicalRecord().getPatient().getUsername();
        }
        String doctor = request.getDoctor() == null ? "-" : request.getDoctor().getUsername();
        String details = request.getDetails() == null || request.getDetails().isEmpty() ? "-" : request.getDetails();
        return new Object[]{
                request.getRequestId(),
                patient,
                doctor,
                request.getRequestType(),
                details,
                DATE_FORMAT.format(request.getRequestDate()),
                request.getStatus()
        };
    }
}
