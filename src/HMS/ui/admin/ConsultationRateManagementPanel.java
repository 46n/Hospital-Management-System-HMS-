package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.ConsultationRate;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

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
    private final HospitalAssetManager assetManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;

    public ConsultationRateManagementPanel(ConsultationRateManager rateManager, UserManager userManager,
            AppointmentManager appointmentManager, HospitalAssetManager assetManager,
            InsuranceNetworkManager insuranceManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.rateManager = rateManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
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
        rateScrollPane = new javax.swing.JScrollPane();
        rateTable = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        toggleButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        backButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        titleLabel.setText("Consultation Rate Management");
        add(titleLabel, java.awt.BorderLayout.NORTH);

        rateTable.setRowHeight(24);
        rateScrollPane.setViewportView(rateTable);
        add(rateScrollPane, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));

        addButton.setText("Add Rate");
        buttonPanel.add(addButton);

        editButton.setText("Edit Selected");
        buttonPanel.add(editButton);

        toggleButton.setText("Toggle Active");
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
        rateTable.setModel(tableModel);
        rateTable.setFont(Theme.DEFAULT_FONT);

        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        for (JButton button : new JButton[]{addButton, editButton, toggleButton, deleteButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

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
                sessionManager, userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                mainFrame, onLogout)));
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
                effectiveDate = DATE_FORMAT.parse(dateField.getText().trim());
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
    private javax.swing.JLabel titleLabel;
    private javax.swing.JScrollPane rateScrollPane;
    private javax.swing.JTable rateTable;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton addButton;
    private javax.swing.JButton editButton;
    private javax.swing.JButton toggleButton;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton backButton;
    // End of variables declaration//GEN-END:variables
}
