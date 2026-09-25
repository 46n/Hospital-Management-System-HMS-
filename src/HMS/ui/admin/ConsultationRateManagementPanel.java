package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.ConsultationRate;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;
import hms.util.ValidationUtil;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for consultation rate configuration. All rules (required
 * fields, non-negative amount) live in ConsultationRateManager - this panel
 * only collects input and displays results.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * ConsultationRateManagementPanel.form). Only initComponents() is
 * generated/guarded; the table model, dialogs, and navigation stay
 * hand-written.
 */
public class ConsultationRateManagementPanel extends javax.swing.JPanel {

    private static final String[] COLUMN_NAMES =
            {"ID", "Type", "Base Amount", "Effective Date", "Active", "Configured By"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final ConsultationRateManager rateManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final ClinicalManager clinicalManager;
    private final HospitalAssetManager assetManager;
    private final InsuranceNetworkManager insuranceManager;
    private final AssetAllocationManager allocationManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public ConsultationRateManagementPanel(ConsultationRateManager rateManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            HospitalAssetManager assetManager, InsuranceNetworkManager insuranceManager,
            AssetAllocationManager allocationManager, SessionManager sessionManager, MainFrame mainFrame,
            Runnable onLogout) {
        this.rateManager = rateManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.assetManager = assetManager;
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
        rateScrollPane = new javax.swing.JScrollPane();
        rateTable = new javax.swing.JTable();
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
        titleLabel.setText("Consultation Rate Management");
        add(titleLabel, java.awt.BorderLayout.NORTH);

        rateTable.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        rateTable.setRowHeight(24);
        rateScrollPane.setViewportView(rateTable);

        add(rateScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setBackground(new java.awt.Color(255, 255, 255));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 5));

        addButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        addButton.setForeground(new java.awt.Color(255, 255, 255));
        addButton.setBackground(new java.awt.Color(31, 78, 121));
        addButton.setText("Add Rate");
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
        rateTable.setModel(tableModel);
        addButton.addActionListener(e -> openRateDialog(null));
        editButton.addActionListener(e -> {
            ConsultationRate rate = requireSelection();
            if (rate != null) {
                openRateDialog(rate);
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
        for (ConsultationRate rate : rateManager.getRates()) {
            tableModel.addRow(new Object[]{
                    rate.getRateId(),
                    rate.getConsultationType(),
                    rate.getBaseAmount(),
                    DATE_FORMAT.format(rate.getEffectiveDate()),
                    rate.isActive() ? "Yes" : "No",
                    rate.getConfiguredBy() == null ? "" : rate.getConfiguredBy().getUsername()
            });
        }
    }

    private ConsultationRate requireSelection() {
        int row = rateTable.getSelectedRow();
        if (row < 0) {
            showError("Select a rate first.");
            return null;
        }
        String rateId = (String) tableModel.getValueAt(row, 0);
        return rateManager.findById(rateId);
    }

    private void toggleSelectedActive() {
        ConsultationRate rate = requireSelection();
        if (rate == null) {
            return;
        }
        try {
            rateManager.setActive(rate.getRateId(), !rate.isActive());
            refreshTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        ConsultationRate rate = requireSelection();
        if (rate == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete rate " + rate.getRateId() + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            rateManager.deleteRate(rate.getRateId());
            refreshTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openRateDialog(ConsultationRate existingRate) {
        boolean isEdit = existingRate != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Rate" : "Add Rate", true);
        dialog.setLayout(new GridBagLayout());

        JTextField typeField = new JTextField(isEdit ? existingRate.getConsultationType() : "", 16);
        JTextField amountField = new JTextField(
                isEdit ? String.valueOf(existingRate.getBaseAmount()) : "", 16);
        JTextField dateField = new JTextField(
                isEdit ? DATE_FORMAT.format(existingRate.getEffectiveDate()) : "", 16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Consultation Type:", typeField);
        addFormRow(dialog, gbc, "Base Amount:", amountField);
        addFormRow(dialog, gbc, "Effective Date (yyyy-MM-dd):", dateField);

        JButton saveButton = new JButton(isEdit ? "Save" : "Add");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            double amount;
            Date effectiveDate;
            try {
                amount = Double.parseDouble(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Base amount must be a number.");
                return;
            }
            try {
                effectiveDate = ValidationUtil.parseStrictDate(dateField.getText().trim());
            } catch (ParseException ex) {
                showError("Effective date must be in yyyy-MM-dd format.");
                return;
            }
            try {
                String type = typeField.getText().trim();
                if (isEdit) {
                    rateManager.updateRate(existingRate.getRateId(), type, amount, effectiveDate);
                } else {
                    String staffId = sessionManager.getCurrentUser().getUserId();
                    rateManager.createRate(type, amount, effectiveDate, staffId);
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
    private javax.swing.JButton addButton;
    private javax.swing.JButton backButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton editButton;
    private javax.swing.JScrollPane rateScrollPane;
    private javax.swing.JTable rateTable;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JButton toggleButton;
    // End of variables declaration//GEN-END:variables
}
