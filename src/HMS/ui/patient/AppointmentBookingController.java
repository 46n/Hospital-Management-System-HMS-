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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;

public class AppointmentBookingController {

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

    String appointmentId =
            appointmentTable.getValueAt(row, 0).toString();

    String dateText =
            dateCombo.getSelectedItem().toString();

    String timeText =
            timeCombo.getSelectedItem().toString();

    try {

        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd");

        dateFormat.setLenient(false);

        Date newDate = dateFormat.parse(dateText);

        LocalTime newTime = LocalTime.parse(
                timeText,
                DateTimeFormatter.ofPattern("HH:mm")
        );

        rescheduleAppointment(
                appointmentId,
                newDate,
                newTime
        );

        JOptionPane.showMessageDialog(
                mainFrame,
                "Appointment rescheduled successfully."
        );

        if (refreshTable != null) {
            refreshTable.run();
        }

    } catch (ParseException | DateTimeParseException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                "Invalid date or time.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );

    } catch (IllegalArgumentException ex) {

        JOptionPane.showMessageDialog(
                mainFrame,
                ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
}