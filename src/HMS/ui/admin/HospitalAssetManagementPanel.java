package hms.ui.admin;

import hms.manager.AppointmentManager;
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
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public HospitalAssetManagementPanel(HospitalAssetManager assetManager, UserManager userManager,
            AppointmentManager appointmentManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.assetManager = assetManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        initComponents();
        customizeComponents();
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

        setLayout(new java.awt.BorderLayout());

        titleLabel.setText("Hospital Asset Management");
        add(titleLabel, java.awt.BorderLayout.NORTH);

        assetTable.setRowHeight(24);
        assetScrollPane.setViewportView(assetTable);
        add(assetScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));

        addButton.setText("Add Asset");
        buttonPanel.add(addButton);

        editButton.setText("Edit Selected");
        buttonPanel.add(editButton);

        toggleButton.setText("Toggle Availability");
        buttonPanel.add(toggleButton);

        deleteButton.setText("Delete Selected");
        buttonPanel.add(deleteButton);

        backButton.setText("Back to Dashboard");
        buttonPanel.add(backButton);

        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Applies HMS theming, builds the table model, and wires listeners. */
    private void customizeComponents() {
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        UIStyles.applyHeadingStyle(titleLabel);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        assetTable.setModel(tableModel);
        assetTable.setFont(Theme.DEFAULT_FONT);

        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        for (JButton button : new JButton[]{addButton, editButton, toggleButton, deleteButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

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
                sessionManager, userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                mainFrame, onLogout)));
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
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        HospitalAsset asset = requireSelection();
        if (asset == null) {
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
        } catch (IllegalArgumentException ex) {
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
                    assetManager.updateAsset(existingAsset.getAssetId(), name, type, location);
                } else {
                    String staffId = sessionManager.getCurrentUser().getUserId();
                    assetManager.createAsset(name, type, location, availableCheckBox.isSelected(), staffId);
                }
                dialog.dispose();
                refreshTable();
            } catch (IllegalArgumentException ex) {
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
    private javax.swing.JLabel titleLabel;
    private javax.swing.JScrollPane assetScrollPane;
    private javax.swing.JTable assetTable;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton toggleButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton backButton;
    // End of variables declaration//GEN-END:variables
}
