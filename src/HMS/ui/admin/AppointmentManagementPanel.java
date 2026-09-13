package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.ui.common.AppointmentTableUtil;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin, read-only view of every appointment in the system. Booking,
 * rescheduling, and cancellation stay with the patient/doctor workflows -
 * this screen is for oversight only.
 */
public class AppointmentManagementPanel extends JPanel {

    private final AppointmentManager appointmentManager;
    private final UserManager userManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;
    private JTextField searchField;

    public AppointmentManagementPanel(AppointmentManager appointmentManager, UserManager userManager,
            HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.appointmentManager = appointmentManager;
        this.userManager = userManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
        refreshTable(appointmentManager.getAppointments());
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        add(createTopPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(AppointmentTableUtil.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(24);
        table.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, assetManager,
                        rateManager, insuranceManager, mainFrame, onLogout)));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Appointment Management");
        UIStyles.applyHeadingStyle(titleLabel);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Theme.BACKGROUND_COLOR);
        searchField = new JTextField(16);
        JButton searchButton = new JButton("Search");
        UIStyles.applyPrimaryButtonStyle(searchButton);
        searchButton.addActionListener(e -> applySearch());
        JButton clearButton = new JButton("Clear");
        UIStyles.applyPrimaryButtonStyle(clearButton);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(appointmentManager.getAppointments());
        });
        searchPanel.add(new JLabel("Search (patient, doctor, or status):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        topPanel.add(searchPanel, BorderLayout.EAST);

        return topPanel;
    }

    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(appointmentManager.getAppointments());
            return;
        }
        List<Appointment> matches = new ArrayList<>();
        for (Appointment appointment : appointmentManager.getAppointments()) {
            if (appointment.getPatient().getUsername().toLowerCase().contains(query)
                    || appointment.getDoctor().getUsername().toLowerCase().contains(query)
                    || appointment.getStatus().toLowerCase().contains(query)) {
                matches.add(appointment);
            }
        }
        refreshTable(matches);
    }

    private void refreshTable(List<Appointment> appointments) {
        tableModel.setRowCount(0);
        for (Appointment appointment : appointments) {
            tableModel.addRow(AppointmentTableUtil.toRow(appointment));
        }
    }
}
