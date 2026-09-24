package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.common.AbstractDashboardPanel;
import hms.ui.common.MainFrame;

import javax.swing.JPanel;

/**
 * Landing screen for Administrative Staff. All six admin features are wired up.
 *
 * The navigation button grid itself is a separate NetBeans GUI Builder panel
 * (see AdminNavigationPanel / AdminNavigationPanel.form) - this class only
 * wires each button's action listener, since it has no static component tree
 * of its own beyond what AbstractDashboardPanel already provides.
 */
public class AdminDashboardPanel extends AbstractDashboardPanel {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public AdminDashboardPanel(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, MainFrame mainFrame, Runnable onLogout) {
        super(sessionManager, onLogout, "Administrative Staff");
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }

    @Override
    protected JPanel createNavigationPanel() {
        AdminNavigationPanel panel = new AdminNavigationPanel();

        panel.getEditProfileButton().addActionListener(e -> mainFrame.showPanel(new EditProfilePanel(
                userManager, appointmentManager, clinicalManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getManageUsersButton().addActionListener(e -> mainFrame.showPanel(new UserManagementPanel(
                userManager, appointmentManager, clinicalManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getManageDoctorsButton().addActionListener(e -> mainFrame.showPanel(new DoctorManagementPanel(
                userManager, appointmentManager, clinicalManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getViewAppointmentsButton().addActionListener(e -> mainFrame.showPanel(new AppointmentManagementPanel(
                appointmentManager, userManager, clinicalManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getManageRequestsButton().addActionListener(e -> mainFrame.showPanel(new MedicalRequestManagementPanel(
                clinicalManager, userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getManageAssetsButton().addActionListener(e -> mainFrame.showPanel(new HospitalAssetManagementPanel(
                assetManager, userManager, appointmentManager, clinicalManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getManageRatesButton().addActionListener(e -> mainFrame.showPanel(new ConsultationRateManagementPanel(
                rateManager, userManager, appointmentManager, clinicalManager, assetManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));

        panel.getManageInsuranceButton().addActionListener(e -> mainFrame.showPanel(new InsuranceNetworkManagementPanel(
                insuranceManager, userManager, appointmentManager, clinicalManager, assetManager, rateManager,
                sessionManager, mainFrame, onLogout)));

        return panel;
    }
}
