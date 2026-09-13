package hms.ui.medicalmanager;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalDepartmentManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.ReportManager;
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

/** Landing screen for Medical Managers. All five features are wired up. */
public class MedicalManagerDashboardPanel extends AbstractDashboardPanel {

    private final SessionManager sessionManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final BillingManager billingManager;
    private final ReportManager reportManager;
    private final AssessmentTypeManager typeManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalDepartmentManager departmentManager;
    private final ClinicalManager clinicalManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public MedicalManagerDashboardPanel(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, BillingManager billingManager, ReportManager reportManager,
            AssessmentTypeManager typeManager, DoctorShiftManager shiftManager,
            ClinicalDepartmentManager departmentManager, ClinicalManager clinicalManager,
            MainFrame mainFrame, Runnable onLogout) {
        super(sessionManager, onLogout, "Medical Manager");
        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.billingManager = billingManager;
        this.reportManager = reportManager;
        this.typeManager = typeManager;
        this.shiftManager = shiftManager;
        this.departmentManager = departmentManager;
        this.clinicalManager = clinicalManager;
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
                userManager, appointmentManager, billingManager, reportManager, typeManager,
                shiftManager, departmentManager, clinicalManager, sessionManager, mainFrame, onLogout)));
        panel.add(editProfileButton);

        JButton manageDepartmentsButton = new JButton("Manage Clinical Departments");
        UIStyles.applyPrimaryButtonStyle(manageDepartmentsButton);
        manageDepartmentsButton.addActionListener(e -> mainFrame.showPanel(new ClinicalDepartmentManagementPanel(
                departmentManager, userManager, appointmentManager, billingManager, reportManager,
                typeManager, shiftManager, clinicalManager, sessionManager, mainFrame, onLogout)));
        panel.add(manageDepartmentsButton);

        JButton manageAssessmentTypesButton = new JButton("Manage Assessment Types");
        UIStyles.applyPrimaryButtonStyle(manageAssessmentTypesButton);
        manageAssessmentTypesButton.addActionListener(e -> mainFrame.showPanel(new AssessmentTypeManagementPanel(
                typeManager, userManager, appointmentManager, billingManager, reportManager,
                shiftManager, departmentManager, clinicalManager, sessionManager, mainFrame, onLogout)));
        panel.add(manageAssessmentTypesButton);

        JButton manageShiftsButton = new JButton("Manage Doctor Rosters/Shifts");
        UIStyles.applyPrimaryButtonStyle(manageShiftsButton);
        manageShiftsButton.addActionListener(e -> mainFrame.showPanel(new DoctorShiftManagementPanel(
                shiftManager, userManager, appointmentManager, billingManager, reportManager,
                typeManager, departmentManager, clinicalManager, sessionManager, mainFrame, onLogout)));
        panel.add(manageShiftsButton);

        JButton viewReportsButton = new JButton("View Reports");
        UIStyles.applyPrimaryButtonStyle(viewReportsButton);
        viewReportsButton.addActionListener(e -> mainFrame.showPanel(new ReportViewPanel(
                reportManager, appointmentManager, billingManager, clinicalManager, userManager,
                typeManager, shiftManager, departmentManager, sessionManager, mainFrame, onLogout)));
        panel.add(viewReportsButton);

        return panel;
    }
}
