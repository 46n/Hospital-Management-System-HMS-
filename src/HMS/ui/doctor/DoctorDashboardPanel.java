package hms.ui.doctor;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.ClinicalManager;
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

/** Landing screen for Doctors. All clinical workflow features are wired up. */
public class DoctorDashboardPanel extends AbstractDashboardPanel {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final AssessmentTypeManager typeManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public DoctorDashboardPanel(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            AssessmentTypeManager typeManager, MainFrame mainFrame, Runnable onLogout) {
        super(sessionManager, onLogout, "Doctor");
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.typeManager = typeManager;
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
                userManager, appointmentManager, clinicalManager, typeManager, sessionManager, mainFrame, onLogout)));
        panel.add(editProfileButton);

        JButton viewAppointmentsButton = new JButton("View My Appointments");
        UIStyles.applyPrimaryButtonStyle(viewAppointmentsButton);
        viewAppointmentsButton.addActionListener(e -> mainFrame.showPanel(new MyAppointmentsPanel(
                appointmentManager, clinicalManager, typeManager, userManager, sessionManager, mainFrame, onLogout)));
        panel.add(viewAppointmentsButton);

        JButton recordAssessmentButton = new JButton("Record Assessment Result");
        UIStyles.applyPrimaryButtonStyle(recordAssessmentButton);
        recordAssessmentButton.addActionListener(e -> mainFrame.showPanel(new RecordAssessmentPanel(
                clinicalManager, appointmentManager, typeManager, userManager, sessionManager, mainFrame, onLogout)));
        panel.add(recordAssessmentButton);

        JButton logVitalsButton = new JButton("Log Vitals & Consultation Notes");
        UIStyles.applyPrimaryButtonStyle(logVitalsButton);
        logVitalsButton.addActionListener(e -> mainFrame.showPanel(new LogVitalsPanel(
                clinicalManager, appointmentManager, typeManager, userManager, sessionManager, mainFrame, onLogout)));
        panel.add(logVitalsButton);

        JButton issuePrescriptionButton = new JButton("Issue Prescriptions");
        UIStyles.applyPrimaryButtonStyle(issuePrescriptionButton);
        issuePrescriptionButton.addActionListener(e -> mainFrame.showPanel(new IssuePrescriptionPanel(
                clinicalManager, appointmentManager, typeManager, userManager, sessionManager, mainFrame, onLogout)));
        panel.add(issuePrescriptionButton);

        JButton requestLabButton = new JButton("Request Lab / Imaging");
        UIStyles.applyPrimaryButtonStyle(requestLabButton);
        requestLabButton.addActionListener(e -> mainFrame.showPanel(new RequestLabImagingPanel(
                clinicalManager, appointmentManager, typeManager, userManager, sessionManager, mainFrame, onLogout)));
        panel.add(requestLabButton);

        return panel;
    }
}
