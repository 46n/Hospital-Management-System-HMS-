package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.ui.common.AppointmentTableUtil;
import hms.ui.common.MainFrame;

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
    private final ClinicalManager clinicalManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final AssetAllocationManager allocationManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public AppointmentManagementPanel(AppointmentManager appointmentManager, UserManager userManager,
            ClinicalManager clinicalManager, HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, AssetAllocationManager allocationManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.appointmentManager = appointmentManager;
        this.userManager = userManager;
        this.clinicalManager = clinicalManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.allocationManager = allocationManager;
        this.insuranceManager = insuranceManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        initComponents();
        initializeBehavior();
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

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setLayout(new java.awt.BorderLayout());

        topPanel.setBackground(new java.awt.Color(255, 255, 255));
        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(33, 33, 33));
        titleLabel.setText("Appointment Management");
        topPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        searchPanel.setBackground(new java.awt.Color(255, 255, 255));

        searchLabel.setText("Search (patient, doctor, or status):");
        searchPanel.add(searchLabel);

        searchField.setColumns(16);
        searchPanel.add(searchField);

        searchButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        searchButton.setForeground(new java.awt.Color(255, 255, 255));
        searchButton.setBackground(new java.awt.Color(31, 78, 121));
        searchButton.setText("Search");
        searchButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        searchButton.setBorderPainted(false);
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true);
        searchPanel.add(searchButton);

        clearButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        clearButton.setForeground(new java.awt.Color(255, 255, 255));
        clearButton.setBackground(new java.awt.Color(31, 78, 121));
        clearButton.setText("Clear");
        clearButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        clearButton.setBorderPainted(false);
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        searchPanel.add(clearButton);

        topPanel.add(searchPanel, java.awt.BorderLayout.EAST);

        add(topPanel, java.awt.BorderLayout.NORTH);

        appointmentTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        appointmentTable.setRowHeight(24);
        tableScrollPane.setViewportView(appointmentTable);

        add(tableScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));

        backButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        backButton.setForeground(new java.awt.Color(255, 255, 255));
        backButton.setBackground(new java.awt.Color(31, 78, 121));
        backButton.setText("Back to Dashboard");
        backButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setOpaque(true);
        buttonPanel.add(backButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Builds the table model and wires screen behavior. */
    private void initializeBehavior() {
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
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardNew(sessionManager, userManager, appointmentManager, clinicalManager,
                        assetManager, rateManager, insuranceManager, allocationManager, mainFrame, onLogout)));
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
    private javax.swing.JTable appointmentTable;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
