package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.AssessmentResult;
import hms.model.MedicalRecord;
import hms.model.Patient;

import hms.ui.common.MainFrame;

import java.text.SimpleDateFormat;
import java.util.List;

public class MedicalHistoryController {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd");

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final BillingManager billingManager;
    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public MedicalHistoryController(
            ClinicalManager clinicalManager,
            AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager,
            BillingManager billingManager,
            SessionManager sessionManager,
            UserManager userManager,
            MainFrame mainFrame,
            Runnable onLogout) {

        this.clinicalManager = clinicalManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.billingManager = billingManager;
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }

    public List<MedicalRecord> getMyMedicalRecords() {

        Patient patient = getCurrentPatient();

        return clinicalManager.getPatientHistory(
                patient.getPatientId()
        );
    }

    public String getRecordDetails(MedicalRecord record) {

        if (record == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Record ID:          ")
                .append(record.getRecordId())
                .append("\n");

        sb.append("Doctor:             ")
                .append(
                        record.getDoctor() != null
                                ? record.getDoctor().getUsername()
                                : "N/A"
                )
                .append("\n");

        sb.append("Date:               ")
                .append(
                        record.getRecordDate() != null
                                ? DATE_FORMAT.format(record.getRecordDate())
                                : "N/A"
                )
                .append("\n");

        sb.append("Vital Signs:        ")
                .append(
                        record.getVitalSigns() != null
                                ? record.getVitalSigns()
                                : ""
                )
                .append("\n");

        sb.append("Diagnosis:          ")
                .append(
                        record.getDiagnosis() != null
                                ? record.getDiagnosis()
                                : ""
                )
                .append("\n");

        sb.append("Consultation Notes: ")
                .append(
                        record.getConsultationNotes() != null
                                ? record.getConsultationNotes()
                                : ""
                )
                .append("\n");

        if (record.getAssessmentResults() != null
                && !record.getAssessmentResults().isEmpty()) {

            sb.append("\nAssessment Results:\n");

            for (AssessmentResult result
                    : record.getAssessmentResults()) {

                sb.append("  ")
                        .append(result.getAssessmentName())
                        .append(": ")
                        .append(result.getValue());

                if (result.getGrade() != null
                        && !result.getGrade().isEmpty()) {

                    sb.append(" [")
                            .append(result.getGrade())
                            .append("]");
                }

                sb.append("\n");
            }
        }

        return sb.toString();
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
                    "No patient is currently logged in."
            );
        }

        if (!(sessionManager.getCurrentUser() instanceof Patient)) {

            throw new IllegalStateException(
                    "The current user is not a patient."
            );
        }

        return (Patient) sessionManager.getCurrentUser();
    }
}