package hms.app;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalDepartmentManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.DoctorShiftManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.ReportManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.admin.AdminDashboardPanel;
import hms.ui.common.LoginFrame;
import hms.ui.common.MainFrame;
import hms.ui.doctor.DoctorDashboardPanel;
import hms.ui.medicalmanager.MedicalManagerDashboardNew;
import hms.ui.patient.PatientDashboardPanel;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Application entry point for HMS-OODJ. Wires the managers in dependency
 * order (each later manager resolves IDs, or now shift/type coverage,
 * through the earlier ones), then starts the Swing flow: LoginFrame only ->
 * (after authentication) MainFrame showing the correct role dashboard. No
 * application chrome exists before a successful login.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        UserManager userManager = new UserManager();
        DoctorShiftManager shiftManager = new DoctorShiftManager(userManager);
        AppointmentManager appointmentManager = new AppointmentManager(userManager, shiftManager);
        AssessmentTypeManager typeManager = new AssessmentTypeManager(userManager);
        ClinicalManager clinicalManager = new ClinicalManager(userManager, appointmentManager, typeManager);
        BillingManager billingManager = new BillingManager(appointmentManager);
        ReportManager reportManager = new ReportManager();
        HospitalAssetManager assetManager = new HospitalAssetManager(userManager);
        ConsultationRateManager rateManager = new ConsultationRateManager(userManager);
        InsuranceNetworkManager insuranceManager = new InsuranceNetworkManager(userManager);
        ClinicalDepartmentManager departmentManager = new ClinicalDepartmentManager(userManager);
        SessionManager sessionManager = new SessionManager(userManager);

        SwingUtilities.invokeLater(() -> showLoginFrame(sessionManager, userManager, appointmentManager,
                clinicalManager, billingManager, reportManager, assetManager, rateManager, insuranceManager,
                typeManager, shiftManager, departmentManager));
    }

    private static void showLoginFrame(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager, BillingManager billingManager,
            ReportManager reportManager, HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, AssessmentTypeManager typeManager,
            DoctorShiftManager shiftManager, ClinicalDepartmentManager departmentManager) {
        LoginFrame loginFrame = new LoginFrame(sessionManager, user -> showMainFrame(sessionManager, userManager,
                appointmentManager, clinicalManager, billingManager, reportManager, assetManager, rateManager,
                insuranceManager, typeManager, shiftManager, departmentManager));
        loginFrame.setVisible(true);
    }

    private static void showMainFrame(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager, BillingManager billingManager,
            ReportManager reportManager, HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, AssessmentTypeManager typeManager,
            DoctorShiftManager shiftManager, ClinicalDepartmentManager departmentManager) {
        MainFrame frame = new MainFrame();
        Runnable onLogout = () -> {
            frame.dispose();
            showLoginFrame(sessionManager, userManager, appointmentManager, clinicalManager, billingManager,
                    reportManager, assetManager, rateManager, insuranceManager, typeManager, shiftManager,
                    departmentManager);
        };
        frame.showPanel(createDashboardPanel(sessionManager, userManager, appointmentManager, clinicalManager,
                billingManager, reportManager, assetManager, rateManager, insuranceManager, typeManager,
                shiftManager, departmentManager, frame, onLogout));
        frame.setVisible(true);
    }

    /** Dashboard selection depends only on SessionManager's role check - no re-authentication here. */
    private static JPanel createDashboardPanel(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager, BillingManager billingManager,
            ReportManager reportManager, HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, AssessmentTypeManager typeManager,
            DoctorShiftManager shiftManager, ClinicalDepartmentManager departmentManager,
            MainFrame frame, Runnable onLogout) {
        if (sessionManager.hasRole("ADMIN_STAFF")) {
            return new AdminDashboardPanel(sessionManager, userManager, appointmentManager,
                    assetManager, rateManager, insuranceManager, frame, onLogout);
        }
        if (sessionManager.hasRole("MEDICAL_MANAGER")) {
            return new MedicalManagerDashboardNew(sessionManager, userManager, appointmentManager,
            billingManager, reportManager, typeManager, shiftManager, departmentManager,
            clinicalManager, frame, onLogout);
            }
        if (sessionManager.hasRole("DOCTOR")) {
            return new DoctorDashboardPanel(sessionManager, userManager, appointmentManager, clinicalManager,
                    typeManager, frame, onLogout);
        }
        if (sessionManager.hasRole("PATIENT")) {
            return new PatientDashboardPanel(sessionManager, userManager, appointmentManager, shiftManager,
                    clinicalManager, billingManager, frame, onLogout);
        }
        throw new IllegalStateException("No dashboard available for this user's role");
    }
}
