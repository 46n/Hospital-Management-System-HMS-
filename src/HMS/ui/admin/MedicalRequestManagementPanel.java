package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.AdminStaff;
import hms.model.MedicalRequest;
import hms.ui.common.MainFrame;
import hms.ui.common.MedicalRequestTableUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for processing lab/imaging requests Doctors have submitted:
 * search/filter, view full details, and approve or reject a PENDING request.
 * Asset booking/allocation for approved requests is out of scope here (Step 2).
 * All transition rules (PENDING-only, rejection reason required, persistence)
 * live in ClinicalManager - this panel only collects input and displays results.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * MedicalRequestManagementPanel.form). Only initComponents() is
 * generated/guarded; the table model, filters, dialogs, and navigation stay
 * hand-written.
 */
public class MedicalRequestManagementPanel extends javax.swing.JPanel {

    private static final String ALL_LABEL = "All";
    private static final String[] STATUS_OPTIONS = {ALL_LABEL, "PENDING", "APPROVED", "REJECTED"};
    /**
     * The assignment-required categories plus the legacy value pre-existing data may still use.
     * The actual filter list (built in initializeBehavior()) also folds in any request type
     * already present in loaded data, so a request never disappears just because its type isn't
     * one of these - see buildTypeFilterOptions().
     */
    private static final String[] BASE_TYPE_OPTIONS = {"LAB", "X_RAY", "SPECIALIZED_IMAGING", "IMAGING"};

    private final ClinicalManager clinicalManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public MedicalRequestManagementPanel(ClinicalManager clinicalManager, UserManager userManager,
            AppointmentManager appointmentManager, HospitalAssetManager assetManager,
            ConsultationRateManager rateManager, InsuranceNetworkManager insuranceManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.clinicalManager = clinicalManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        initComponents();
        initializeBehavior();
        refreshTable(clinicalManager.getAllRequests());
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchLabel = new javax.swing.JLabel();
        searchField = new javax.swing.JTextField();
        typeLabel = new javax.swing.JLabel();
        typeFilterCombo = new javax.swing.JComboBox<>();
        statusLabel = new javax.swing.JLabel();
        statusFilterCombo = new javax.swing.JComboBox<>();
        searchButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();
        tableScrollPane = new javax.swing.JScrollPane();
        requestTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        viewButton = new javax.swing.JButton();
        approveButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setLayout(new java.awt.BorderLayout());

        topPanel.setBackground(new java.awt.Color(255, 255, 255));
        topPanel.setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(33, 33, 33));
        titleLabel.setText("Medical Requests");
        topPanel.add(titleLabel, java.awt.BorderLayout.WEST);

        searchPanel.setBackground(new java.awt.Color(255, 255, 255));

        searchLabel.setText("Search (patient, doctor, or details):");
        searchPanel.add(searchLabel);

        searchField.setColumns(14);
        searchPanel.add(searchField);

        typeLabel.setText("Type:");
        searchPanel.add(typeLabel);

        typeFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {ALL_LABEL}));
        searchPanel.add(typeFilterCombo);

        statusLabel.setText("Status:");
        searchPanel.add(statusLabel);

        statusFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(STATUS_OPTIONS));
        searchPanel.add(statusFilterCombo);

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

        requestTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        requestTable.setRowHeight(24);
        tableScrollPane.setViewportView(requestTable);

        add(tableScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 4));

        viewButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        viewButton.setForeground(new java.awt.Color(255, 255, 255));
        viewButton.setBackground(new java.awt.Color(31, 78, 121));
        viewButton.setText("View Details");
        viewButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        viewButton.setBorderPainted(false);
        viewButton.setFocusPainted(false);
        viewButton.setOpaque(true);
        buttonPanel.add(viewButton);

        approveButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        approveButton.setForeground(new java.awt.Color(255, 255, 255));
        approveButton.setBackground(new java.awt.Color(31, 78, 121));
        approveButton.setText("Approve");
        approveButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        approveButton.setBorderPainted(false);
        approveButton.setFocusPainted(false);
        approveButton.setOpaque(true);
        buttonPanel.add(approveButton);

        rejectButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        rejectButton.setForeground(new java.awt.Color(255, 255, 255));
        rejectButton.setBackground(new java.awt.Color(31, 78, 121));
        rejectButton.setText("Reject");
        rejectButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        rejectButton.setBorderPainted(false);
        rejectButton.setFocusPainted(false);
        rejectButton.setOpaque(true);
        buttonPanel.add(rejectButton);

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
        searchButton.addActionListener(e -> applyFilters());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
            typeFilterCombo.setSelectedIndex(0);
            refreshTable(clinicalManager.getAllRequests());
        });

        tableModel = new DefaultTableModel(MedicalRequestTableUtil.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestTable.setModel(tableModel);

        typeFilterCombo.setModel(new javax.swing.DefaultComboBoxModel<>(buildTypeFilterOptions()));

        viewButton.addActionListener(e -> viewSelectedRequest());
        approveButton.addActionListener(e -> approveSelectedRequest());
        rejectButton.addActionListener(e -> rejectSelectedRequest());
        backButton.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, clinicalManager,
                        assetManager, rateManager, insuranceManager, mainFrame, onLogout)));
    }

    /**
     * The supported categories plus whatever type is actually present in loaded data - so a
     * request never disappears from the filter merely because its type predates or falls
     * outside the hardcoded list (e.g. a legacy or otherwise unexpected value).
     */
    private String[] buildTypeFilterOptions() {
        java.util.LinkedHashSet<String> options = new java.util.LinkedHashSet<>();
        options.add(ALL_LABEL);
        for (String type : BASE_TYPE_OPTIONS) {
            options.add(type);
        }
        for (MedicalRequest request : clinicalManager.getAllRequests()) {
            if (request.getRequestType() != null && !request.getRequestType().isEmpty()) {
                options.add(request.getRequestType());
            }
        }
        return options.toArray(new String[0]);
    }

    private void applyFilters() {
        String query = searchField.getText().trim().toLowerCase();
        String statusFilter = (String) statusFilterCombo.getSelectedItem();
        String typeFilter = (String) typeFilterCombo.getSelectedItem();

        List<MedicalRequest> matches = new ArrayList<>();
        for (MedicalRequest request : clinicalManager.getAllRequests()) {
            if (!ALL_LABEL.equals(statusFilter) && !statusFilter.equals(request.getStatus())) {
                continue;
            }
            if (!ALL_LABEL.equals(typeFilter) && !typeFilter.equalsIgnoreCase(request.getRequestType())) {
                continue;
            }
            if (!query.isEmpty() && !matchesQuery(request, query)) {
                continue;
            }
            matches.add(request);
        }
        refreshTable(matches);
    }

    private boolean matchesQuery(MedicalRequest request, String query) {
        String patient = request.getMedicalRecord() != null && request.getMedicalRecord().getPatient() != null
                ? request.getMedicalRecord().getPatient().getUsername() : "";
        String doctor = request.getDoctor() == null ? "" : request.getDoctor().getUsername();
        String details = request.getDetails() == null ? "" : request.getDetails();
        return request.getRequestId().toLowerCase().contains(query)
                || patient.toLowerCase().contains(query)
                || doctor.toLowerCase().contains(query)
                || details.toLowerCase().contains(query);
    }

    private void refreshTable(List<MedicalRequest> requests) {
        tableModel.setRowCount(0);
        for (MedicalRequest request : requests) {
            tableModel.addRow(MedicalRequestTableUtil.toRow(request));
        }
    }

    private String getSelectedRequestId() {
        int row = requestTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return (String) tableModel.getValueAt(row, 0);
    }

    private String requireSelection() {
        String requestId = getSelectedRequestId();
        if (requestId == null) {
            showError("Select a medical request first.");
        }
        return requestId;
    }

    private MedicalRequest findRequest(String requestId) {
        for (MedicalRequest request : clinicalManager.getAllRequests()) {
            if (request.getRequestId().equals(requestId)) {
                return request;
            }
        }
        return null;
    }

    private void viewSelectedRequest() {
        String requestId = requireSelection();
        if (requestId == null) {
            return;
        }
        MedicalRequest request = findRequest(requestId);
        if (request == null) {
            showError("Medical request not found: " + requestId);
            return;
        }
        String patient = request.getMedicalRecord() != null && request.getMedicalRecord().getPatient() != null
                ? request.getMedicalRecord().getPatient().getUsername() : "-";
        String doctor = request.getDoctor() == null ? "-" : request.getDoctor().getUsername();
        StringBuilder details = new StringBuilder();
        details.append("Request ID: ").append(request.getRequestId())
                .append("\nPatient: ").append(patient)
                .append("\nRequesting Doctor: ").append(doctor)
                .append("\nType: ").append(request.getRequestType())
                .append("\nDetails: ").append(request.getDetails() == null || request.getDetails().isEmpty()
                        ? "-" : request.getDetails())
                .append("\nDate: ").append(request.getRequestDate())
                .append("\nStatus: ").append(request.getStatus());
        if (request.getProcessedBy() != null) {
            details.append("\nProcessed By: ").append(request.getProcessedBy());
        }
        if (request.getProcessedDate() != null) {
            details.append("\nProcessed On: ").append(request.getProcessedDate());
        }
        if (request.getRejectionReason() != null && !request.getRejectionReason().isEmpty()) {
            details.append("\nRejection Reason: ").append(request.getRejectionReason());
        }
        JOptionPane.showMessageDialog(this, details.toString(), "Medical Request Details",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void approveSelectedRequest() {
        String requestId = requireSelection();
        if (requestId == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Approve medical request " + requestId + "?",
                "Confirm Approval", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            clinicalManager.approveRequest(requestId, currentAdminId());
            applyFilters();
            JOptionPane.showMessageDialog(this, "Request " + requestId + " approved.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("The approval could not be saved: " + ex.getMessage());
        }
    }

    private void rejectSelectedRequest() {
        String requestId = requireSelection();
        if (requestId == null) {
            return;
        }
        String reason = JOptionPane.showInputDialog(this,
                "Reason for rejecting request " + requestId + ":", "Reject Medical Request",
                JOptionPane.PLAIN_MESSAGE);
        if (reason == null) {
            return;
        }
        if (reason.trim().isEmpty()) {
            showError("A rejection reason is required.");
            return;
        }
        try {
            clinicalManager.rejectRequest(requestId, currentAdminId(), reason);
            applyFilters();
            JOptionPane.showMessageDialog(this, "Request " + requestId + " rejected.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        } catch (IOException ex) {
            showError("The rejection could not be saved: " + ex.getMessage());
        }
    }

    private String currentAdminId() {
        AdminStaff admin = (AdminStaff) sessionManager.getCurrentUser();
        return admin.getStaffId();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton approveButton;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton rejectButton;
    private javax.swing.JTable requestTable;
    private javax.swing.JButton searchButton;
    private javax.swing.JTextField searchField;
    private javax.swing.JLabel searchLabel;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JComboBox<String> statusFilterCombo;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JComboBox<String> typeFilterCombo;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JButton viewButton;
    // End of variables declaration//GEN-END:variables
}
