package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.Appointment;
import hms.model.Patient;

import hms.ui.common.MainFrame;

import java.util.ArrayList;
import java.util.List;

public class FeedbackController {

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final BillingManager billingManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public FeedbackController(
            ClinicalManager clinicalManager,
            AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager,
            BillingManager billingManager,
            UserManager userManager,
            SessionManager sessionManager,
            MainFrame mainFrame,
            Runnable onLogout) {

        this.clinicalManager = clinicalManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.billingManager = billingManager;
        this.userManager = userManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }

    public List<Appointment> getCompletedAppointments() {

        Patient patient = getCurrentPatient();

        List<Appointment> completed =
                new ArrayList<>();

        for (Appointment appointment
                : appointmentManager.listForPatient(
                        patient.getPatientId())) {

            if ("COMPLETED".equals(
                    appointment.getStatus())) {

                completed.add(appointment);
            }
        }

        return completed;
    }

    public void submitFeedback(
            Appointment appointment,
            int rating,
            String comment) {

        if (appointment == null) {
            throw new IllegalArgumentException(
                    "Please select an appointment.");
        }

        Patient patient = getCurrentPatient();

        clinicalManager.submitFeedback(
                patient.getPatientId(),
                appointment.getAppointmentId(),
                rating,
                comment
        );
    }
    
    public void backToDashboard() {

        new PatientController(
            sessionManager,
            userManager,
            appointmentManager,
            shiftManager,
            clinicalManager,
            billingManager,
            mainFrame,
            onLogout
        ).openDashboard();
}



    private Patient getCurrentPatient() {

        if (sessionManager == null
                || sessionManager.getCurrentUser() == null) {

            throw new IllegalStateException(
                    "No patient is currently logged in.");
        }

        if (!(sessionManager.getCurrentUser()
                instanceof Patient)) {

            throw new IllegalStateException(
                    "The current user is not a patient.");
        }

        return (Patient)
                sessionManager.getCurrentUser();
    }
}