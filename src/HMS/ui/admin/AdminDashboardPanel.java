package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.common.AbstractDashboardPanel;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Landing screen for Administrative Staff. All six admin features are wired up. */
public class AdminDashboardPanel extends AbstractDashboardPanel {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public AdminDashboardPanel(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, HospitalAssetManager assetManager,
            ConsultationRateManager rateManager, InsuranceNetworkManager insuranceManager,
            MainFrame mainFrame, Runnable onLogout) {
        super(sessionManager, onLogout, "Administrative Staff");
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
    }

    @Override
    protected JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 1, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        panel.setBackground(Theme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JButton editProfileButton = new JButton("Edit My Profile");
        UIStyles.applyPrimaryButtonStyle(editProfileButton);
        editProfileButton.addActionListener(e -> mainFrame.showPanel(new EditProfilePanel(
                userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(editProfileButton);

        JButton manageUsersButton = new JButton("Manage Users");
        UIStyles.applyPrimaryButtonStyle(manageUsersButton);
        manageUsersButton.addActionListener(e -> mainFrame.showPanel(new UserManagementPanel(
                userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(manageUsersButton);

        JButton manageDoctorsButton = new JButton("Manage Doctors");
        UIStyles.applyPrimaryButtonStyle(manageDoctorsButton);
        manageDoctorsButton.addActionListener(e -> mainFrame.showPanel(new DoctorManagementPanel(
                userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(manageDoctorsButton);

        JButton viewAppointmentsButton = new JButton("View Appointments");
        UIStyles.applyPrimaryButtonStyle(viewAppointmentsButton);
        viewAppointmentsButton.addActionListener(e -> mainFrame.showPanel(new AppointmentManagementPanel(
                appointmentManager, userManager, assetManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(viewAppointmentsButton);

        JButton manageAssetsButton = new JButton("Manage Hospital Assets");
        UIStyles.applyPrimaryButtonStyle(manageAssetsButton);
        manageAssetsButton.addActionListener(e -> mainFrame.showPanel(new HospitalAssetManagementPanel(
                assetManager, userManager, appointmentManager, rateManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(manageAssetsButton);

        JButton manageRatesButton = new JButton("Manage Consultation Rates");
        UIStyles.applyPrimaryButtonStyle(manageRatesButton);
        manageRatesButton.addActionListener(e -> mainFrame.showPanel(new ConsultationRateManagementPanel(
                rateManager, userManager, appointmentManager, assetManager, insuranceManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(manageRatesButton);

        JButton manageInsuranceButton = new JButton("Manage Insurance Networks");
        UIStyles.applyPrimaryButtonStyle(manageInsuranceButton);
        manageInsuranceButton.addActionListener(e -> mainFrame.showPanel(new InsuranceNetworkManagementPanel(
                insuranceManager, userManager, appointmentManager, assetManager, rateManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(manageInsuranceButton);

        return panel;
    }
}
