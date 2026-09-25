package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.InsuranceNetwork;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for accepted insurance network configuration. All rules
 * (required fields, coverage range 0-100) live in InsuranceNetworkManager -
 * this panel only collects input and displays results.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * InsuranceNetworkManagementPanel.form). Only initComponents() is
 * generated/guarded; the table model, dialogs, and navigation stay
 * hand-written.
 */
public class InsuranceNetworkManagementPanel extends javax.swing.JPanel {

    private static final String[] COLUMN_NAMES =
            {"ID", "Provider", "Plan", "Coverage %", "Active", "Managed By"};

    private final InsuranceNetworkManager insuranceManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final AssetAllocationManager allocationManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public InsuranceNetworkManagementPanel(InsuranceNetworkManager insuranceManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            AssetAllocationManager allocationManager, SessionManager sessionManager, MainFrame mainFrame,
            Runnable onLogout) {
        this.insuranceManager = insuranceManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.assetManager = assetManager;
        this.allocationManager = allocationManager;
        this.rateManager = rateManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        initComponents();
        initializeBehavior();
        refreshTable();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        networkScrollPane = new javax.swing.JScrollPane();
        networkTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        toggleButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setLayout(new java.awt.BorderLayout());

        titleLabel.setFont(new java.awt.Font("SansSerif", 1, 20)); // NOI18N
        titleLabel.setForeground(new java.awt.Color(33, 33, 33));
        titleLabel.setText("Insurance Network Management");
        add(titleLabel, java.awt.BorderLayout.NORTH);

        networkTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        networkTable.setRowHeight(24);
        networkScrollPane.setViewportView(networkTable);

        add(networkScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 5));

        addButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        addButton.setForeground(new java.awt.Color(255, 255, 255));
        addButton.setBackground(new java.awt.Color(31, 78, 121));
        addButton.setText("Add Network");
        addButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);
        buttonPanel.add(addButton);

        editButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        editButton.setForeground(new java.awt.Color(255, 255, 255));
        editButton.setBackground(new java.awt.Color(31, 78, 121));
        editButton.setText("Edit Selected");
        editButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.setOpaque(true);
        buttonPanel.add(editButton);

        toggleButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        toggleButton.setForeground(new java.awt.Color(255, 255, 255));
        toggleButton.setBackground(new java.awt.Color(31, 78, 121));
        toggleButton.setText("Toggle Active");
        toggleButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        toggleButton.setBorderPainted(false);
        toggleButton.setFocusPainted(false);
        toggleButton.setOpaque(true);
        buttonPanel.add(toggleButton);

        deleteButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        deleteButton.setForeground(new java.awt.Color(255, 255, 255));
        deleteButton.setBackground(new java.awt.Color(31, 78, 121));
        deleteButton.setText("Delete Selected");
        deleteButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setOpaque(true);
        buttonPanel.add(deleteButton);

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
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        networkTable.setModel(tableModel);
        addButton.addActionListener(e -> openNetworkDialog(null));
        editButton.addActionListener(e -> {
            InsuranceNetwork network = requireSelection();
            if (network != null) {
                openNetworkDialog(network);
            }
        });
        toggleButton.addActionListener(e -> toggleSelectedActive());
        deleteButton.addActionListener(e -> deleteSelected());
        backButton.addActionListener(e -> mainFrame.showPanel(new AdminDashboardPanel(
                sessionManager, userManager, appointmentManager, clinicalManager, assetManager, rateManager,
                insuranceManager, allocationManager, mainFrame, onLogout)));
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (InsuranceNetwork network : insuranceManager.getNetworks()) {
            tableModel.addRow(new Object[]{
                    network.getNetworkId(),
                    network.getProviderName(),
                    network.getPlanName(),
                    network.getCoveragePercentage(),
                    network.isActive() ? "Yes" : "No",
                    network.getManagedBy() == null ? "" : network.getManagedBy().getUsername()
            });
        }
    }

    private InsuranceNetwork requireSelection() {
        int row = networkTable.getSelectedRow();
        if (row < 0) {
            showError("Select a network first.");
            return null;
        }
        String networkId = (String) tableModel.getValueAt(row, 0);
        return insuranceManager.findById(networkId);
    }

    private void toggleSelectedActive() {
        InsuranceNetwork network = requireSelection();
        if (network == null) {
            return;
        }
        try {
            insuranceManager.setActive(network.getNetworkId(), !network.isActive());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        InsuranceNetwork network = requireSelection();
        if (network == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete network " + network.getNetworkId() + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            insuranceManager.deleteNetwork(network.getNetworkId());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openNetworkDialog(InsuranceNetwork existingNetwork) {
        boolean isEdit = existingNetwork != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Network" : "Add Network", true);
        dialog.setLayout(new GridBagLayout());

        JTextField providerField = new JTextField(isEdit ? existingNetwork.getProviderName() : "", 16);
        JTextField planField = new JTextField(isEdit ? existingNetwork.getPlanName() : "", 16);
        JTextField coverageField = new JTextField(
                isEdit ? String.valueOf(existingNetwork.getCoveragePercentage()) : "", 16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Provider Name:", providerField);
        addFormRow(dialog, gbc, "Plan Name:", planField);
        addFormRow(dialog, gbc, "Coverage % (0-100):", coverageField);

        JButton saveButton = new JButton(isEdit ? "Save" : "Add");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            double coverage;
            try {
                coverage = Double.parseDouble(coverageField.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Coverage percentage must be a number.");
                return;
            }
            try {
                String provider = providerField.getText().trim();
                String plan = planField.getText().trim();
                if (isEdit) {
                    insuranceManager.updateNetwork(existingNetwork.getNetworkId(), provider, plan, coverage);
                } else {
                    String staffId = sessionManager.getCurrentUser().getUserId();
                    insuranceManager.createNetwork(provider, plan, coverage, staffId);
                }
                dialog.dispose();
                refreshTable();
            } catch (IllegalArgumentException | IllegalStateException ex) {
                showError(ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(cancelButton, gbc);
        gbc.gridx = 1;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void addFormRow(JDialog dialog, GridBagConstraints gbc, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
        gbc.gridy++;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane networkScrollPane;
    private javax.swing.JTable networkTable;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton toggleButton;
    // End of variables declaration//GEN-END:variables
}
