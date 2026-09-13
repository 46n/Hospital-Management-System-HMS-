package hms.ui.common;

import hms.model.Appointment;

import java.text.SimpleDateFormat;

/**
 * Shared table formatting for the appointment lists shown to patients,
 * doctors, and admins. Pure display formatting - no business logic.
 */
public final class AppointmentTableUtil {

    public static final String[] COLUMN_NAMES = {"ID", "Patient", "Doctor", "Date", "Time", "Status"};

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private AppointmentTableUtil() {
    }

    public static Object[] toRow(Appointment appointment) {
        return new Object[]{
                appointment.getAppointmentId(),
                appointment.getPatient().getUsername(),
                appointment.getDoctor().getUsername(),
                DATE_FORMAT.format(appointment.getAppointmentDate()),
                appointment.getAppointmentTime().toString(),
                appointment.getStatus()
        };
    }
}
