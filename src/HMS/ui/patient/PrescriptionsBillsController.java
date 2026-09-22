package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.Appointment;
import hms.model.MedicalRecord;
import hms.model.Patient;
import hms.model.Payment;
import hms.model.Prescription;

import hms.ui.common.MainFrame;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionsBillsController {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final BillingManager billingManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public PrescriptionsBillsController(
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
    
    public List<Prescription> getMyPrescriptions() {

    Patient patient = getCurrentPatient();

    List<Prescription> prescriptions =
            new ArrayList<>();

    List<MedicalRecord> records =
            clinicalManager.getRecords();

    for (MedicalRecord record : records) {

        if (record.getPatient() != null
                && patient.getPatientId().equals(
                        record.getPatient().getPatientId())) {

            if (record.getPrescriptions() != null) {

                prescriptions.addAll(
                        record.getPrescriptions()
                );
            }
        }
    }

    return prescriptions;
}



    public List<Payment> getMyBills() {

        Patient patient = getCurrentPatient();

        return billingManager.getPatientBills(
                patient.getPatientId());
    }

    public String getAppointmentDisplay(Payment payment) {

        if (payment == null) {
            return "";
        }

        Appointment appointment =
                appointmentManager.findById(
                        payment.getAppointmentId());

        if (appointment == null) {
            return payment.getAppointmentId();
        }

        String doctor =
                appointment.getDoctor() != null
                        ? appointment.getDoctor().getUsername()
                        : "N/A";

        String date =
                appointment.getAppointmentDate() != null
                        ? DATE_FORMAT.format(
                                appointment.getAppointmentDate())
                        : "";

        String time =
                appointment.getAppointmentTime() != null
                        ? appointment.getAppointmentTime()
                                .format(TIME_FORMAT)
                        : "";

        return appointment.getAppointmentId()
                + " | Dr. " + doctor
                + " | " + date
                + " " + time;
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