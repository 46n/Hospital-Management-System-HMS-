package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.HospitalAsset;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for hospital asset management (consultation rooms, wards,
 * labs, imaging rooms). All rules (required fields, valid asset type) live
 * in HospitalAssetManager - this panel only collects input and displays
 * results.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * HospitalAssetManagementPanel.form). Only initComponents() is
 * generated/guarded; the table model, dialogs, and navigation stay
 * hand-written.
 */
public class HospitalAssetManagementPanel extends javax.swing.JPanel {

    private static final String[] COLUMN_NAMES = {"ID", "Name", "Type", "Location", "Available", "Managed By"};

    private final HospitalAssetManager assetManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final AssetAllocationManager allocationManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public HospitalAssetManagementPanel(HospitalAssetManager assetManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            ConsultationRateManager rateManager, InsuranceNetworkManager insuranceManager,
            AssetAllocationManager allocationManager, SessionManager sessionManager, MainFrame mainFrame,
            Runnable onLogout) {
        this.assetManager = assetManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.rateManager = rateManager;
        this.allocationManager = allocationManager;
        this.insuranceManager = insuranceManager;
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
        assetScrollPane = new javax.swing.JScrollPane();
        assetTable = new javax.swing.JTable();
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
        titleLabel.setText("Hospital Asset Management");
        add(titleLabel, java.awt.BorderLayout.NORTH);

        assetTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        assetTable.setRowHeight(24);
        assetScrollPane.setViewportView(assetTable);

        add(assetScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 5));

        addButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        addButton.setForeground(new java.awt.Color(255, 255, 255));
        addButton.setBackground(new java.awt.Color(31, 78, 121));
        addButton.setText("Add Asset");
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
        toggleButton.setText("Toggle Availability");
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
        assetTable.setModel(tableModel);
        addButton.addActionListener(e -> openAssetDialog(null));
        editButton.addActionListener(e -> {
            HospitalAsset asset = requireSelection();
            if (asset != null) {
                openAssetDialog(asset);
            }
        });
        toggleButton.addActionListener(e -> toggleSelectedAvailability());
        deleteButton.addActionListener(e -> deleteSelected());
        backButton.addActionListener(e -> mainFrame.showPanel(new AdminDashboardPanel(
                sessionManager, userManager, appointmentManager, clinicalManager, assetManager, rateManager,
                insuranceManager, allocationManager, mainFrame, onLogout)));
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (HospitalAsset asset : assetManager.getAssets()) {
            tableModel.addRow(new Object[]{
                    asset.getAssetId(),
                    asset.getAssetName(),
                    asset.getAssetType(),
                    asset.getLocation(),
                    asset.isAvailability() ? "Yes" : "No",
                    asset.getManagedBy() == null ? "" : asset.getManagedBy().getUsername()
            });
        }
    }

    private HospitalAsset requireSelection() {
        int row = assetTable.getSelectedRow();
        if (row < 0) {
            showError("Select an asset first.");
            return null;
        }
        String assetId = (String) tableModel.getValueAt(row, 0);
        return assetManager.findById(assetId);
    }

    private void toggleSelectedAvailability() {
        HospitalAsset asset = requireSelection();
        if (asset == null) {
            return;
        }
        try {
            assetManager.setAvailability(asset.getAssetId(), !asset.isAvailability());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        HospitalAsset asset = requireSelection();
        if (asset == null) {
            return;
        }
        // Deleting an asset that already has allocation history (scheduled, completed or
        // cancelled) would leave a dangling asset ID in asset_allocations.txt and destroy that
        // history. The rule itself lives in AssetAllocationManager.validateAssetCanBeDeleted() -
        // not here, and not in HospitalAssetManager (which has no reference to
        // AssetAllocationManager, avoiding a circular dependency between the two managers).
        try {
            allocationManager.validateAssetCanBeDeleted(asset.getAssetId());
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete asset " + asset.getAssetId() + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            assetManager.deleteAsset(asset.getAssetId());
            refreshTable();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openAssetDialog(HospitalAsset existingAsset) {
        boolean isEdit = existingAsset != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Asset" : "Add Asset", true);
        dialog.setLayout(new GridBagLayout());

        List<String> types = HospitalAssetManager.getValidTypes();
        JComboBox<String> typeCombo = new JComboBox<>(types.toArray(new String[0]));
        JTextField nameField = new JTextField(isEdit ? existingAsset.getAssetName() : "", 16);
        JTextField locationField = new JTextField(isEdit ? existingAsset.getLocation() : "", 16);
        JCheckBox availableCheckBox = new JCheckBox("Available", isEdit ? existingAsset.isAvailability() : true);
        availableCheckBox.setBackground(Theme.BACKGROUND_COLOR);
        if (isEdit) {
            typeCombo.setSelectedItem(existingAsset.getAssetType());
            availableCheckBox.setEnabled(false);
            if (allocationManager.hasAllocationHistoryForAsset(existingAsset.getAssetId())) {
                typeCombo.setEnabled(false);
                typeCombo.setToolTipText("This asset has allocation history, so its type cannot be changed.");
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Name:", nameField);
        addFormRow(dialog, gbc, "Type:", typeCombo);
        addFormRow(dialog, gbc, "Location:", locationField);
        if (!isEdit) {
            addFormRow(dialog, gbc, "", availableCheckBox);
        }

        JButton saveButton = new JButton(isEdit ? "Save" : "Add");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String type = (String) typeCombo.getSelectedItem();
                String location = locationField.getText().trim();
                if (isEdit) {
                    // An asset with allocation history keeps its type forever, so the historical
                    // allocation rows (each recording a compatible asset type at the time) always
                    // stay meaningful. Name and location can still change freely. The rule itself
                    // lives in AssetAllocationManager.validateAssetTypeCanChange().
                    try {
                        allocationManager.validateAssetTypeCanChange(
                                existingAsset.getAssetId(), existingAsset.getAssetType(), type);
                    } catch (IllegalStateException ex) {
                        showError(ex.getMessage());
                        return;
                    }
                    assetManager.updateAsset(existingAsset.getAssetId(), name, type, location);
                } else {
                    String staffId = sessionManager.getCurrentUser().getUserId();
                    assetManager.createAsset(name, type, location, availableCheckBox.isSelected(), staffId);
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
    private javax.swing.JScrollPane assetScrollPane;
    private javax.swing.JTable assetTable;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton toggleButton;
    // End of variables declaration//GEN-END:variables
}
