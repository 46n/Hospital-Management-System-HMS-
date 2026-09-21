package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.Patient;
import hms.model.User;

import hms.ui.common.MainFrame;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.format.DateTimeParseException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class AppointmentBookingController {
    

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalManager clinicalManager;
    private final BillingManager billingManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public AppointmentBookingController(
            UserManager userManager,
            AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager,
            ClinicalManager clinicalManager,
            BillingManager billingManager,
            SessionManager sessionManager,
            MainFrame mainFrame,
            Runnable onLogout) {

        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.billingManager = billingManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }

    public List<Doctor> getAvailableDoctors() {

        List<Doctor> doctors = new ArrayList<>();

        for (User user : userManager.getUsers()) {

            if (user instanceof Doctor && user.isActive()) {
                doctors.add((Doctor) user);
            }
        }

        return doctors;
    }

    public List<DoctorShift> getDoctorShifts(String doctorId) {
        return shiftManager.getShiftsForDoctor(doctorId);
    }

    public List<Appointment> getMyAppointments() {

        Patient patient = getCurrentPatient();

        return appointmentManager.listForPatient(
                patient.getPatientId()
        );
    }

    public void bookAppointment(
            String doctorId,
            Date date,
            LocalTime time) {

        Patient patient = getCurrentPatient();

        appointmentManager.book(
                patient.getPatientId(),
                doctorId,
                date,
                time
        );
    }

    public void cancelAppointment(String appointmentId) {

        Patient patient = getCurrentPatient();

        appointmentManager.cancel(
                appointmentId,
                patient.getPatientId()
        );
    }

    public void rescheduleAppointment(
            String appointmentId,
            Date newDate,
            LocalTime newTime) {

        appointmentManager.reschedule(
                appointmentId,
                newDate,
                newTime
        );
    }

    public void backToDashboard() {

        mainFrame.showPanel(
                new PatientDashboardNew(
                        sessionManager,
                        userManager,
                        appointmentManager,
                        shiftManager,
                        clinicalManager,
                        billingManager,
                        mainFrame,
                        onLogout
                )
        );
    }

    private Patient getCurrentPatient() {
        return (Patient) sessionManager.getCurrentUser();
    }
    public void cancelSelectedAppointment(
        JTable appointmentTable,
        Runnable refreshTable) {

    int row = appointmentTable.getSelectedRow();

    if (row < 0) {
        JOptionPane.showMessageDialog(
                mainFrame,
                "Please select an appointment first.",
                "No Appointment Selected",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String appointmentId =
            appointmentTable.getValueAt(row, 0).toString();

    int confirm = JOptionPane.showConfirmDialog(
            mainFrame,
            "Cancel appointment " + appointmentId + "?",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        cancelAppointment(appointmentId);

        JOptionPane.showMessageDialog(
                mainFrame,
                "Appointment cancelled successfully."
        );

        if (refreshTable != null) {
            refreshTable.run();
        }

    } catch (IllegalArgumentException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    public void rescheduleSelectedAppointment(
        JTable appointmentTable,
        JComboBox<String> dateCombo,
        JComboBox<String> timeCombo,
        Runnable refreshTable,
        Runnable refreshAvailableTimes) {

    int row = appointmentTable.getSelectedRow();

    // Validation 1: appointment must be selected
    if (row < 0) {
        JOptionPane.showMessageDialog(
                mainFrame,
                "Please select an appointment first.",
                "No Appointment Selected",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String appointmentId =
            appointmentTable.getValueAt(row, 0).toString();

    try {

        // Find the REAL appointment from AppointmentManager
        Appointment appointment =
                appointmentManager.findById(appointmentId);

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Appointment could not be found."
            );
        }

        // Validation 2: make sure this appointment belongs
        // to the currently logged-in patient
        Patient currentPatient = getCurrentPatient();

        if (appointment.getPatient() == null
                || !currentPatient.getPatientId().equals(
                        appointment.getPatient().getPatientId())) {

            throw new IllegalArgumentException(
                    "You can only reschedule your own appointments."
            );
        }

        // Validation 3: closed appointments cannot be rescheduled
        if ("CANCELLED".equalsIgnoreCase(appointment.getStatus())) {

            JOptionPane.showMessageDialog(
                    mainFrame,
                    "A cancelled appointment cannot be rescheduled.",
                    "Appointment Cancelled",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if ("COMPLETED".equalsIgnoreCase(appointment.getStatus())) {

            JOptionPane.showMessageDialog(
                    mainFrame,
                    "A completed appointment cannot be rescheduled.",
                    "Appointment Completed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Validation 4: new date and time required
        if (dateCombo.getSelectedItem() == null
                || timeCombo.getSelectedItem() == null) {

            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Please choose a new appointment date and time.",
                    "Missing Date or Time",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String dateText =
                dateCombo.getSelectedItem().toString();

        String timeText =
                timeCombo.getSelectedItem().toString();

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd");

        dateFormat.setLenient(false);

        Date newDate =
                dateFormat.parse(dateText);

        LocalTime newTime =
                LocalTime.parse(
                        timeText,
                        TIME_FORMAT
                );

        String oldDateText =
                dateFormat.format(
                        appointment.getAppointmentDate()
                );

        String oldTimeText =
                appointment.getAppointmentTime()
                        .format(TIME_FORMAT);

        // Validation 5: must actually change something
        if (oldDateText.equals(dateText)
                && oldTimeText.equals(timeText)) {

            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Please choose a different date or time.",
                    "No Changes Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (appointment.getDoctor() == null) {
            throw new IllegalArgumentException(
                    "The doctor for this appointment could not be found."
            );
        }

        // Important:
        // Rescheduling keeps the SAME doctor.
        String doctorId =
                appointment.getDoctor().getDoctorId();

        // Validation 6:
        // verify that the new slot is really still available
        List<LocalTime> availableSlots =
                appointmentManager.getAvailableSlots(
                        doctorId,
                        newDate
                );

        if (!availableSlots.contains(newTime)) {

            JOptionPane.showMessageDialog(
                    mainFrame,
                    "The selected consultation time is no longer available.\n"
                    + "Please choose another available time.",
                    "Time Not Available",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Show OLD -> NEW before changing anything
        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Reschedule appointment "
                        + appointmentId
                        + "?\n\n"
                        + "Doctor: "
                        + appointment.getDoctor().getUsername()
                        + "\n\n"
                        + "Current:\n"
                        + oldDateText
                        + " at "
                        + oldTimeText
                        + "\n\n"
                        + "New:\n"
                        + dateText
                        + " at "
                        + timeText,
                "Confirm Reschedule",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Actual backend operation
        rescheduleAppointment(
                appointmentId,
                newDate,
                newTime
        );

        JOptionPane.showMessageDialog(
                mainFrame,
                "Appointment rescheduled successfully.",
                "Reschedule Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Refresh appointment table
        if (refreshTable != null) {
            refreshTable.run();
        }

        // Refresh available times:
        // new slot disappears, old slot becomes available again
        if (refreshAvailableTimes != null) {
            refreshAvailableTimes.run();
        }

    } catch (ParseException | DateTimeParseException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                "Invalid appointment date or time.",
                "Invalid Date or Time",
                JOptionPane.ERROR_MESSAGE
        );

    } catch (IllegalArgumentException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                ex.getMessage(),
                "Unable to Reschedule Appointment",
                JOptionPane.ERROR_MESSAGE
        );
    }
}

    public List<DoctorShift> getFutureShiftsForDoctor(String doctorId) {

    List<DoctorShift> futureShifts = new ArrayList<>();

    LocalDate today = LocalDate.now();

    for (DoctorShift shift : shiftManager.getShiftsForDoctor(doctorId)) {

        LocalDate shiftDate = shift.getShiftDate()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (!shiftDate.isBefore(today)) {
            futureShifts.add(shift);
        }
    }

    return futureShifts;
}
    public List<String> getTimeSlotsForDate(
        String doctorId,
        String selectedDate) {

    if (doctorId == null || doctorId.trim().isEmpty()) {
        throw new IllegalArgumentException(
                "Please select a doctor first."
        );
    }

    if (selectedDate == null || selectedDate.trim().isEmpty()) {
        throw new IllegalArgumentException(
                "Please select an appointment date."
        );
    }

    Date date;

    try {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd");

        dateFormat.setLenient(false);

        date = dateFormat.parse(selectedDate);

    } catch (ParseException ex) {
        throw new IllegalArgumentException(
                "Invalid appointment date."
        );
    }

    List<LocalTime> slots =
            appointmentManager.getAvailableSlots(
                    doctorId,
                    date
            );

    List<String> formattedSlots = new ArrayList<>();

    for (LocalTime slot : slots) {
        formattedSlots.add(
                slot.format(TIME_FORMAT)
        );
    }

    return formattedSlots;
}
    public void bookSelectedAppointment(
        JTable doctorsTable,
        JComboBox<String> dateCombo,
        JComboBox<String> timeCombo,
        Runnable refreshAppointments,
        Runnable refreshAvailableTimes) {

    // Validation 1: doctor selected
    int doctorRow = doctorsTable.getSelectedRow();

    if (doctorRow < 0) {
        JOptionPane.showMessageDialog(
                mainFrame,
                "Please select a doctor first.",
                "Doctor Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // Validation 2: date selected
    if (dateCombo.getSelectedItem() == null) {
        JOptionPane.showMessageDialog(
                mainFrame,
                "Please select an appointment date.",
                "Date Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // Validation 3: time selected
    if (timeCombo.getSelectedItem() == null) {
        JOptionPane.showMessageDialog(
                mainFrame,
                "Please select an available consultation time.",
                "Time Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    String doctorId =
            doctorsTable.getValueAt(doctorRow, 0).toString();

    String dateText =
            dateCombo.getSelectedItem().toString();

    String timeText =
            timeCombo.getSelectedItem().toString();

    try {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd");

        dateFormat.setLenient(false);

        Date appointmentDate =
                dateFormat.parse(dateText);

        LocalTime appointmentTime =
                LocalTime.parse(
                        timeText,
                        TIME_FORMAT
                );

        // Validation 4:
        // Re-check availability at the moment Book is pressed.
        List<LocalTime> availableSlots =
                appointmentManager.getAvailableSlots(
                        doctorId,
                        appointmentDate
                );

        if (!availableSlots.contains(appointmentTime)) {
            throw new IllegalArgumentException(
                    "This consultation time is no longer available. "
                    + "Please select another time."
            );
        }

        // Confirmation
        int confirm = JOptionPane.showConfirmDialog(
                mainFrame,
                "Book appointment with doctor "
                        + doctorId
                        + "\nDate: "
                        + dateText
                        + "\nTime: "
                        + timeText
                        + "?",
                "Confirm Appointment",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        // Actual booking goes through existing backend
        bookAppointment(
                doctorId,
                appointmentDate,
                appointmentTime
        );

        JOptionPane.showMessageDialog(
                mainFrame,
                "Appointment booked successfully.",
                "Booking Successful",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Refresh My Appointments
        if (refreshAppointments != null) {
            refreshAppointments.run();
        }

        // Refresh slots so booked time disappears
        if (refreshAvailableTimes != null) {
            refreshAvailableTimes.run();
        }

    } catch (ParseException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                "Invalid appointment date.",
                "Invalid Date",
                JOptionPane.ERROR_MESSAGE
        );

    } catch (DateTimeParseException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                "Invalid appointment time.",
                "Invalid Time",
                JOptionPane.ERROR_MESSAGE
        );

    } catch (IllegalArgumentException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                ex.getMessage(),
                "Unable to Book Appointment",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
}