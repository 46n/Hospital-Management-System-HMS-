package hms.ui.doctor;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.ClinicalManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.ui.common.AppointmentTableUtil;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Read-only view of the appointments assigned to the logged-in doctor. */
public class MyAppointmentsPanel extends JPanel {

    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final AssessmentTypeManager typeManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public MyAppointmentsPanel(AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            AssessmentTypeManager typeManager, UserManager userManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.typeManager = typeManager;
        this.userManager = userManager;
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

        JLabel titleLabel = new JLabel("My Appointments");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        DefaultTableModel tableModel = new DefaultTableModel(AppointmentTableUtil.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Doctor doctor = (Doctor) sessionManager.getCurrentUser();
        for (Appointment appointment : appointmentManager.listForDoctor(doctor.getDoctorId())) {
            tableModel.addRow(AppointmentTableUtil.toRow(appointment));
        }
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new DoctorDashboardPanel(
                sessionManager, userManager, appointmentManager, clinicalManager, typeManager, mainFrame, onLogout)));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}
