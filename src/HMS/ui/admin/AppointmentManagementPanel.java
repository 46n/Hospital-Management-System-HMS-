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

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;

/**
 * Admin, read-only view of every appointment in the system. Booking,
 * rescheduling, and cancellation stay with the patient/doctor workflows -
 * this screen is for oversight only.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * AppointmentManagementPanel.form). Only initComponents() is
 * generated/guarded; the table model, search, and navigation stay
 * hand-written.
 */
public class AppointmentManagementPanel extends javax.swing.JPanel {

    private final AppointmentManager appointmentManager;
    private final UserManager userManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

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
        initComponents();
        customizeComponents();
        refreshTable(appointmentManager.getAppointments());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        searchButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        tableScrollPane = new javax.swing.JScrollPane();
        appointmentTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setText("Appointment Management");
        topPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        searchLabel.setText("Search (patient, doctor, or status):");
        searchPanel.add(searchLabel);

        searchField.setColumns(16);
        searchPanel.add(searchField);

        searchButton.setText("Search");
        searchPanel.add(searchButton);

        clearButton.setText("Clear");
        searchPanel.add(clearButton);

        topPanel.add(searchPanel, java.awt.BorderLayout.EAST);

        add(topPanel, java.awt.BorderLayout.NORTH);

        appointmentTable.setRowHeight(24);
        tableScrollPane.setViewportView(appointmentTable);
        add(tableScrollPane, java.awt.BorderLayout.CENTER);

        backButton.setText("Back to Dashboard");
        buttonPanel.add(backButton);
        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Applies HMS theming, builds the table model, and wires listeners. */
    private void customizeComponents() {
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        topPanel.setBackground(Theme.BACKGROUND_COLOR);
        UIStyles.applyHeadingStyle(titleLabel);

        searchPanel.setBackground(Theme.BACKGROUND_COLOR);
        UIStyles.applyPrimaryButtonStyle(searchButton);
        UIStyles.applyPrimaryButtonStyle(clearButton);
        searchButton.addActionListener(e -> applySearch());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(appointmentManager.getAppointments());
        });

        tableModel = new DefaultTableModel(AppointmentTableUtil.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentTable.setModel(tableModel);
        appointmentTable.setFont(Theme.DEFAULT_FONT);

        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, assetManager,
                        rateManager, insuranceManager, mainFrame, onLogout)));
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JTextField searchField;
    private javax.swing.JButton searchButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JTable appointmentTable;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton backButton;
    // End of variables declaration//GEN-END:variables
}
