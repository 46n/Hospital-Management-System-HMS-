package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
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

/** Landing screen for Patients. All patient workflow features are wired up. */
public class PatientDashboardPanel extends AbstractDashboardPanel {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalManager clinicalManager;
    private final BillingManager billingManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public PatientDashboardPanel(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, DoctorShiftManager shiftManager,
            ClinicalManager clinicalManager, BillingManager billingManager,
            MainFrame mainFrame, Runnable onLogout) {
        super(sessionManager, onLogout, "Patient");
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.billingManager = billingManager;
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
                userManager, appointmentManager, shiftManager, clinicalManager, billingManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(editProfileButton);

        JButton bookAppointmentButton = new JButton("Book Appointment");
        UIStyles.applyPrimaryButtonStyle(bookAppointmentButton);
        bookAppointmentButton.addActionListener(e -> mainFrame.showPanel(new AppointmentBookingPanel(
                userManager, appointmentManager, shiftManager, clinicalManager, billingManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(bookAppointmentButton);

        JButton viewHistoryButton = new JButton("View Medical History");
        UIStyles.applyPrimaryButtonStyle(viewHistoryButton);
        viewHistoryButton.addActionListener(e -> mainFrame.showPanel(new ViewMedicalHistoryPanel(
                clinicalManager, appointmentManager, shiftManager, billingManager, userManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(viewHistoryButton);

        JButton viewPrescriptionsButton = new JButton("View Prescriptions & Bills");
        UIStyles.applyPrimaryButtonStyle(viewPrescriptionsButton);
        viewPrescriptionsButton.addActionListener(e -> mainFrame.showPanel(new ViewPrescriptionsBillsPanel(
                clinicalManager, appointmentManager, shiftManager, billingManager, userManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(viewPrescriptionsButton);

        JButton submitFeedbackButton = new JButton("Submit Feedback");
        UIStyles.applyPrimaryButtonStyle(submitFeedbackButton);
        submitFeedbackButton.addActionListener(e -> mainFrame.showPanel(new SubmitFeedbackPanel(
                clinicalManager, appointmentManager, shiftManager, billingManager, userManager,
                sessionManager, mainFrame, onLogout)));
        panel.add(submitFeedbackButton);

        return panel;
    }
}
