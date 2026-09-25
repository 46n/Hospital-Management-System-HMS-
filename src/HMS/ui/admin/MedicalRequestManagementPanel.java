package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.AdminStaff;
import hms.model.AssetAllocation;
import hms.model.HospitalAsset;
import hms.model.MedicalRequest;
import hms.ui.common.MainFrame;
import hms.ui.common.MedicalRequestTableUtil;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for processing lab/imaging requests submitted by Doctors, and
 * for allocating a hospital asset (Step 2) once a request is APPROVED.
 *
 * This class shows encapsulation in practice: the Swing components below are
 * all private fields, and every action a button can trigger goes through a
 * small, clearly named method (approveSelectedRequest, rejectSelectedRequest,
 * openAllocationDialog, ...). The panel never touches the request/allocation
 * text files directly - it only calls ClinicalManager and
 * AssetAllocationManager, which is a simple example of separating the GUI
 * layer from the business-logic layer.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * MedicalRequestManagementPanel.form). Only initComponents() is
 * generated/guarded; the table model, filters, dialogs, and navigation stay
 * hand-written.
 */
public class MedicalRequestManagementPanel extends javax.swing.JPanel {

    private static final String ALL_LABEL = "All";
    private static final String[] STATUS_OPTIONS =
            {ALL_LABEL, "PENDING", "APPROVED", "REJECTED", "ALLOCATED", "COMPLETED"};

    /**
     * The assignment-required categories plus the legacy value pre-existing data may still use.
     * The actual filter list (built in initializeBehavior()) also folds in any request type
     * already present in loaded data, so a request never disappears just because its type isn't
     * one of these - see buildTypeFilterOptions().
     */
    private static final String[] BASE_TYPE_OPTIONS = {"LAB", "X_RAY", "SPECIALIZED_IMAGING", "IMAGING"};

    /** Format for the Allocate Asset dialog's start/end fields - strict, so an impossible or malformed value is rejected. */
    private static final SimpleDateFormat ALLOCATION_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    static {
        ALLOCATION_DATETIME_FORMAT.setLenient(false);
    }

    // Collaborating managers this panel was given at construction time - it only ever calls
    // their public methods, never reads or writes a data file itself.
    private final ClinicalManager clinicalManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final AssetAllocationManager allocationManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public MedicalRequestManagementPanel(ClinicalManager clinicalManager, UserManager userManager,
            AppointmentManager appointmentManager, HospitalAssetManager assetManager,
            ConsultationRateManager rateManager, InsuranceNetworkManager insuranceManager,
            AssetAllocationManager allocationManager, SessionManager sessionManager, MainFrame mainFrame,
            Runnable onLogout) {
        this.clinicalManager = clinicalManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.allocationManager = allocationManager;
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
        titleLbl = new javax.swing.JLabel();
        searchPanel = new javax.swing.JPanel();
        searchLbl = new javax.swing.JLabel();
        searchTf = new javax.swing.JTextField();
        typeLbl = new javax.swing.JLabel();
        typeCmb = new javax.swing.JComboBox<>();
        statusLbl = new javax.swing.JLabel();
        statusCmb = new javax.swing.JComboBox<>();
        searchBtn = new javax.swing.JButton();
        clearBtn = new javax.swing.JButton();
        tableScrollPane = new javax.swing.JScrollPane();
        requestsTbl = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        viewBtn = new javax.swing.JButton();
        approveBtn = new javax.swing.JButton();
        rejectBtn = new javax.swing.JButton();
        allocateBtn = new javax.swing.JButton();
        backBtn = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setLayout(new java.awt.BorderLayout());

        topPanel.setBackground(new java.awt.Color(255, 255, 255));
        topPanel.setLayout(new java.awt.BorderLayout());

        titleLbl.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        titleLbl.setForeground(new java.awt.Color(33, 33, 33));
        titleLbl.setText("Medical Requests");
        topPanel.add(titleLbl, java.awt.BorderLayout.WEST);

        searchPanel.setBackground(new java.awt.Color(255, 255, 255));

        searchLbl.setText("Search (patient, doctor, or details):");
        searchPanel.add(searchLbl);

        searchTf.setColumns(14);
        searchPanel.add(searchTf);

        typeLbl.setText("Type:");
        searchPanel.add(typeLbl);

        typeCmb.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] {ALL_LABEL}));
        searchPanel.add(typeCmb);

        statusLbl.setText("Status:");
        searchPanel.add(statusLbl);

        statusCmb.setModel(new javax.swing.DefaultComboBoxModel<>(STATUS_OPTIONS));
        searchPanel.add(statusCmb);

        searchBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        searchBtn.setForeground(new java.awt.Color(255, 255, 255));
        searchBtn.setBackground(new java.awt.Color(31, 78, 121));
        searchBtn.setText("Search");
        searchBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        searchBtn.setBorderPainted(false);
        searchBtn.setFocusPainted(false);
        searchBtn.setOpaque(true);
        searchPanel.add(searchBtn);

        clearBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        clearBtn.setForeground(new java.awt.Color(255, 255, 255));
        clearBtn.setBackground(new java.awt.Color(31, 78, 121));
        clearBtn.setText("Clear");
        clearBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        clearBtn.setBorderPainted(false);
        clearBtn.setFocusPainted(false);
        clearBtn.setOpaque(true);
        searchPanel.add(clearBtn);

        topPanel.add(searchPanel, java.awt.BorderLayout.EAST);

        add(topPanel, java.awt.BorderLayout.NORTH);

        requestsTbl.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        requestsTbl.setRowHeight(24);
        tableScrollPane.setViewportView(requestsTbl);

        add(tableScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 5));

        viewBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        viewBtn.setForeground(new java.awt.Color(255, 255, 255));
        viewBtn.setBackground(new java.awt.Color(31, 78, 121));
        viewBtn.setText("View Details");
        viewBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        viewBtn.setBorderPainted(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setOpaque(true);
        buttonPanel.add(viewBtn);

        approveBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        approveBtn.setForeground(new java.awt.Color(255, 255, 255));
        approveBtn.setBackground(new java.awt.Color(31, 78, 121));
        approveBtn.setText("Approve");
        approveBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        approveBtn.setBorderPainted(false);
        approveBtn.setFocusPainted(false);
        approveBtn.setOpaque(true);
        buttonPanel.add(approveBtn);

        rejectBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        rejectBtn.setForeground(new java.awt.Color(255, 255, 255));
        rejectBtn.setBackground(new java.awt.Color(31, 78, 121));
        rejectBtn.setText("Reject");
        rejectBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        rejectBtn.setBorderPainted(false);
        rejectBtn.setFocusPainted(false);
        rejectBtn.setOpaque(true);
        buttonPanel.add(rejectBtn);

        allocateBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        allocateBtn.setForeground(new java.awt.Color(255, 255, 255));
        allocateBtn.setBackground(new java.awt.Color(31, 78, 121));
        allocateBtn.setText("Allocate Asset");
        allocateBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        allocateBtn.setBorderPainted(false);
        allocateBtn.setFocusPainted(false);
        allocateBtn.setOpaque(true);
        buttonPanel.add(allocateBtn);

        backBtn.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        backBtn.setForeground(new java.awt.Color(255, 255, 255));
        backBtn.setBackground(new java.awt.Color(31, 78, 121));
        backBtn.setText("Back to Dashboard");
        backBtn.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setOpaque(true);
        buttonPanel.add(backBtn);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Builds the table model and wires screen behavior (button clicks, filters). */
    private void initializeBehavior() {
        searchBtn.addActionListener(e -> applyFilters());
        clearBtn.addActionListener(e -> {
            searchTf.setText("");
            statusCmb.setSelectedIndex(0);
            typeCmb.setSelectedIndex(0);
            refreshTable(clinicalManager.getAllRequests());
        });

        tableModel = new DefaultTableModel(MedicalRequestTableUtil.COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestsTbl.setModel(tableModel);

        typeCmb.setModel(new javax.swing.DefaultComboBoxModel<>(buildTypeFilterOptions()));

        viewBtn.addActionListener(e -> viewSelectedRequest());
        approveBtn.addActionListener(e -> approveSelectedRequest());
        rejectBtn.addActionListener(e -> rejectSelectedRequest());
        allocateBtn.addActionListener(e -> openAllocationDialog());
        backBtn.addActionListener(e -> mainFrame.showPanel(
                new AdminDashboardPanel(sessionManager, userManager, appointmentManager, clinicalManager,
                        assetManager, rateManager, insuranceManager, allocationManager, mainFrame, onLogout)));
    }

    /**
     * The supported categories plus whatever type is actually present in loaded data - so a
     * request never disappears from the filter merely because its type predates or falls
     * outside the hardcoded list (e.g. a legacy or otherwise unexpected value).
     */
    private String[] buildTypeFilterOptions() {
        List<String> options = new ArrayList<>();
        options.add(ALL_LABEL);
        for (String type : BASE_TYPE_OPTIONS) {
            options.add(type);
        }
        for (MedicalRequest request : clinicalManager.getAllRequests()) {
            String type = request.getRequestType();
            if (type != null && !type.isEmpty() && !options.contains(type)) {
                options.add(type);
            }
        }
        return options.toArray(new String[0]);
    }

    /** Filters the table by search text, status and type - all three conditions must match. */
    private void applyFilters() {
        String query = searchTf.getText().trim().toLowerCase();
        String statusFilter = (String) statusCmb.getSelectedItem();
        String typeFilter = (String) typeCmb.getSelectedItem();

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
        int row = requestsTbl.getSelectedRow();
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

    /**
     * Builds the allocation-history table model for one request - extracted from the dialog so
     * it can be verified directly (component-level) without opening the modal dialog it feeds.
     */
    private DefaultTableModel buildAllocationHistoryModel(String requestId) {
        String[] historyColumns = {"Allocation ID", "Asset", "Start", "End", "Status", "Cancellation Reason"};
        DefaultTableModel historyModel = new DefaultTableModel(historyColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (AssetAllocation allocation : allocationManager.getAllocationsForRequest(requestId)) {
            historyModel.addRow(new Object[]{
                    allocation.getAllocationId(),
                    allocation.getAssetId(),
                    ALLOCATION_DATETIME_FORMAT.format(allocation.getStartDateTime()),
                    ALLOCATION_DATETIME_FORMAT.format(allocation.getEndDateTime()),
                    allocation.getAllocationStatus(),
                    allocation.getCancellationReason() == null ? "-" : allocation.getCancellationReason()
            });
        }
        return historyModel;
    }

    /**
     * A single dialog for the whole Step 2 allocation workflow on the selected request: shows
     * its allocation history, and offers either an Allocate form (APPROVED, no active allocation)
     * or Cancel/Complete actions (has an active SCHEDULED allocation) - whichever applies. All
     * validation and manager calls happen here in hand-written code; only the dialog's own
     * skeleton is built with GridBagLayout, matching this codebase's existing ad-hoc JDialog
     * convention (e.g. HospitalAssetManagementPanel.openAssetDialog) rather than a NetBeans form.
     */
    private void openAllocationDialog() {
        String requestId = requireSelection();
        if (requestId == null) {
            return;
        }
        MedicalRequest request = findRequest(requestId);
        if (request == null) {
            showError("Medical request not found: " + requestId);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Manage Allocation - " + requestId, true);
        dialog.setLayout(new BorderLayout());

        DefaultTableModel historyModel = buildAllocationHistoryModel(requestId);
        JTable historyTbl = new JTable(historyModel);
        historyTbl.setFont(Theme.DEFAULT_FONT);
        JScrollPane historyScroll = new JScrollPane(historyTbl);
        historyScroll.setPreferredSize(new Dimension(600, 140));
        dialog.add(historyScroll, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setBackground(Theme.BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        // Decide which controls to show: an active allocation gets Cancel/Complete; an
        // APPROVED request with no active allocation gets the Allocate form; anything
        // else just explains why nothing can be done here.
        AssetAllocation active = allocationManager.findActiveAllocation(requestId);
        if (active != null) {
            addActiveAllocationControls(dialog, actionPanel, gbc, active);
        } else if ("APPROVED".equals(request.getStatus())) {
            addAllocateForm(dialog, actionPanel, gbc, request);
        } else {
            gbc.gridwidth = 2;
            actionPanel.add(new JLabel("This request cannot be allocated (status: " + request.getStatus() + ")."), gbc);
            gbc.gridy++;
        }

        JButton closeBtn = new JButton("Close");
        UIStyles.applyPrimaryButtonStyle(closeBtn);
        closeBtn.addActionListener(e -> dialog.dispose());
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        actionPanel.add(closeBtn, gbc);

        dialog.add(actionPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /** Adds the Cancel/Complete buttons shown when the request already has an active allocation. */
    private void addActiveAllocationControls(JDialog dialog, JPanel actionPanel, GridBagConstraints gbc,
            AssetAllocation active) {
        JLabel infoLbl = new JLabel("Active: " + active.getAssetId() + "  ("
                + ALLOCATION_DATETIME_FORMAT.format(active.getStartDateTime()) + " to "
                + ALLOCATION_DATETIME_FORMAT.format(active.getEndDateTime()) + ")");
        gbc.gridwidth = 2;
        actionPanel.add(infoLbl, gbc);
        gbc.gridy++;

        JButton completeBtn = new JButton("Complete Allocation");
        UIStyles.applyPrimaryButtonStyle(completeBtn);
        completeBtn.addActionListener(e -> {
            try {
                allocationManager.completeAllocation(active.getAllocationId());
                dialog.dispose();
                applyFilters();
                JOptionPane.showMessageDialog(this, "Allocation completed; request marked COMPLETED.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            } catch (IOException ex) {
                showError("The completion could not be saved: " + ex.getMessage());
            }
        });

        JButton cancelAllocationBtn = new JButton("Cancel Allocation");
        UIStyles.applyPrimaryButtonStyle(cancelAllocationBtn);
        cancelAllocationBtn.addActionListener(e -> {
            String reason = JOptionPane.showInputDialog(dialog,
                    "Reason for cancelling this allocation:", "Cancel Allocation", JOptionPane.PLAIN_MESSAGE);
            if (reason == null) {
                return;
            }
            if (reason.trim().isEmpty()) {
                showError("A cancellation reason is required.");
                return;
            }
            try {
                allocationManager.cancelAllocation(active.getAllocationId(), reason);
                dialog.dispose();
                applyFilters();
                JOptionPane.showMessageDialog(this, "Allocation cancelled; request returned to APPROVED.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            } catch (IOException ex) {
                showError("The cancellation could not be saved: " + ex.getMessage());
            }
        });

        gbc.gridwidth = 1;
        actionPanel.add(cancelAllocationBtn, gbc);
        gbc.gridx = 1;
        actionPanel.add(completeBtn, gbc);
        gbc.gridx = 0;
        gbc.gridy++;
    }

    /** Adds the asset/start/end form and Allocate button shown for an APPROVED request with no active allocation. */
    private void addAllocateForm(JDialog dialog, JPanel actionPanel, GridBagConstraints gbc, MedicalRequest request) {
        List<HospitalAsset> compatibleAssets = allocationManager.getCompatibleAvailableAssets(request.getRequestType());
        String[] assetLabels = new String[compatibleAssets.size()];
        for (int i = 0; i < compatibleAssets.size(); i++) {
            HospitalAsset asset = compatibleAssets.get(i);
            assetLabels[i] = asset.getAssetId() + " - " + asset.getAssetName() + " (" + asset.getLocation() + ")";
        }
        JComboBox<String> assetCmb = new JComboBox<>(assetLabels);
        JTextField startTf = new JTextField(16);
        JTextField endTf = new JTextField(16);

        addFormRow(actionPanel, gbc, "Compatible Asset:", assetCmb);
        addFormRow(actionPanel, gbc, "Start (yyyy-MM-dd HH:mm):", startTf);
        addFormRow(actionPanel, gbc, "End (yyyy-MM-dd HH:mm):", endTf);

        JButton allocateConfirmBtn = new JButton("Allocate");
        UIStyles.applyPrimaryButtonStyle(allocateConfirmBtn);
        allocateConfirmBtn.addActionListener(e -> {
            if (compatibleAssets.isEmpty()) {
                showError("No compatible, operationally available asset exists for a "
                        + request.getRequestType() + " request.");
                return;
            }
            int index = assetCmb.getSelectedIndex();
            if (index < 0) {
                showError("Select an asset.");
                return;
            }
            Date start;
            Date end;
            try {
                start = ALLOCATION_DATETIME_FORMAT.parse(startTf.getText().trim());
            } catch (ParseException ex) {
                showError("Start must be a valid date/time in yyyy-MM-dd HH:mm format.");
                return;
            }
            try {
                end = ALLOCATION_DATETIME_FORMAT.parse(endTf.getText().trim());
            } catch (ParseException ex) {
                showError("End must be a valid date/time in yyyy-MM-dd HH:mm format.");
                return;
            }
            try {
                allocationManager.createAllocation(request.getRequestId(), compatibleAssets.get(index).getAssetId(),
                        currentAdminId(), start, end);
                dialog.dispose();
                applyFilters();
                JOptionPane.showMessageDialog(this, "Asset allocated; request marked ALLOCATED.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            } catch (IOException ex) {
                showError("The allocation could not be saved: " + ex.getMessage());
            }
        });
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        actionPanel.add(allocateConfirmBtn, gbc);
        gbc.gridy++;
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private String currentAdminId() {
        AdminStaff admin = (AdminStaff) sessionManager.getCurrentUser();
        return admin.getStaffId();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton allocateBtn;
    private javax.swing.JButton approveBtn;
    private javax.swing.JButton backBtn;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton clearBtn;
    private javax.swing.JButton rejectBtn;
    private javax.swing.JTable requestsTbl;
    private javax.swing.JButton searchBtn;
    private javax.swing.JTextField searchTf;
    private javax.swing.JLabel searchLbl;
    private javax.swing.JPanel searchPanel;
    private javax.swing.JComboBox<String> statusCmb;
    private javax.swing.JLabel statusLbl;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel titleLbl;
    private javax.swing.JPanel topPanel;
    private javax.swing.JComboBox<String> typeCmb;
    private javax.swing.JLabel typeLbl;
    private javax.swing.JButton viewBtn;
    // End of variables declaration//GEN-END:variables
}
