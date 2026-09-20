package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.common.MainFrame;

public class PatientController {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalManager clinicalManager;
    private final BillingManager billingManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public PatientController(
            SessionManager sessionManager,
            UserManager userManager,
            AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager,
            ClinicalManager clinicalManager,
            BillingManager billingManager,
            MainFrame mainFrame,
            Runnable onLogout) {

        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.billingManager = billingManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }

    public void openDashboard() {
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

    public void openAppointments() {
        mainFrame.showPanel(
                new AppointmentBookingPanel(
                        userManager,
                        appointmentManager,
                        shiftManager,
                        clinicalManager,
                        billingManager,
                        sessionManager,
                        mainFrame,
                        onLogout
                )
        );
    }

    public void openMedicalHistory() {
        mainFrame.showPanel(
                new ViewMedicalHistoryPanel(
                        clinicalManager,
                        appointmentManager,
                        shiftManager,
                        billingManager,
                        userManager,
                        sessionManager,
                        mainFrame,
                        onLogout
                )
        );
    }

    public void openPrescriptionsBills() {
        mainFrame.showPanel(
                new ViewPrescriptionsBillsPanel(
                        clinicalManager,
                        appointmentManager,
                        shiftManager,
                        billingManager,
                        userManager,
                        sessionManager,
                        mainFrame,
                        onLogout
                )
        );
    }

    public void openFeedback() {
        mainFrame.showPanel(
                new SubmitFeedbackPanel(
                        clinicalManager,
                        appointmentManager,
                        shiftManager,
                        billingManager,
                        userManager,
                        sessionManager,
                        mainFrame,
                        onLogout
                )
        );
    }

    public void openProfile() {
        mainFrame.showPanel(
                new EditProfilePanel(
                        userManager,
                        appointmentManager,
                        shiftManager,
                        clinicalManager,
                        billingManager,
                        sessionManager,
                        mainFrame,
                        onLogout
                )
        );
    }

    public void logout() {
        sessionManager.logout();

        if (onLogout != null) {
            onLogout.run();
        }
    }
}