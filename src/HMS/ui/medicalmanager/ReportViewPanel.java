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
import hms.model.HospitalReport;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Medical Manager screen for viewing hospital reports. Calls only
 * ReportManager - the concrete report type (HospitalMetricsReport or
 * RevenueReport) is resolved polymorphically through the HospitalReport
 * interface, this panel never depends on either concrete class.
 */
public class ReportViewPanel extends JPanel {

    private final ReportManager reportManager;
    private final AppointmentManager appointmentManager;
    private final BillingManager billingManager;
    private final ClinicalManager clinicalManager;
    private final UserManager userManager;
    private final AssessmentTypeManager typeManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalDepartmentManager departmentManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private JTextArea resultArea;

    public ReportViewPanel(ReportManager reportManager, AppointmentManager appointmentManager,
            BillingManager billingManager, ClinicalManager clinicalManager, UserManager userManager,
            AssessmentTypeManager typeManager, DoctorShiftManager shiftManager,
            ClinicalDepartmentManager departmentManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.reportManager = reportManager;
        this.appointmentManager = appointmentManager;
        this.billingManager = billingManager;
        this.clinicalManager = clinicalManager;
        this.userManager = userManager;
        this.typeManager = typeManager;
        this.shiftManager = shiftManager;
        this.departmentManager = departmentManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("Hospital Reports");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton metricsButton = new JButton("Hospital Metrics");
        JButton revenueButton = new JButton("Revenue");
        JButton assessmentButton = new JButton("Assessment Usage");
        JButton shiftButton = new JButton("Doctor Shifts");
        JButton backButton = new JButton("Back to Dashboard");

        for (JButton button : new JButton[]{metricsButton, revenueButton, assessmentButton, shiftButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

        metricsButton.addActionListener(e -> showReport(reportManager.buildMetricsReport(appointmentManager)));
        revenueButton.addActionListener(e -> showReport(reportManager.buildRevenueReport(billingManager)));
        assessmentButton.addActionListener(e -> showReport(
                reportManager.buildAssessmentUsageReport(clinicalManager, typeManager)));
        shiftButton.addActionListener(e -> showReport(
                reportManager.buildDoctorShiftReport(shiftManager, userManager)));
        backButton.addActionListener(e -> mainFrame.showPanel(new MedicalManagerDashboardPanel(
                sessionManager, userManager, appointmentManager, billingManager, reportManager,
                typeManager, shiftManager, departmentManager, clinicalManager, mainFrame, onLogout)));

        buttonPanel.add(metricsButton);
        buttonPanel.add(revenueButton);
        buttonPanel.add(assessmentButton);
        buttonPanel.add(shiftButton);
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    /** Works identically for either concrete report - the polymorphism ReportManager.generate() provides. */
    private void showReport(HospitalReport report) {
        List<String> rows = reportManager.generate(report);
        StringBuilder text = new StringBuilder(report.getReportName()).append("\n\n");
        for (String row : rows) {
            text.append(row).append("\n");
        }
        resultArea.setText(text.toString());
    }
}
