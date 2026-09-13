package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.InsuranceNetwork;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Admin screen for accepted insurance network configuration. All rules
 * (required fields, coverage range 0-100) live in InsuranceNetworkManager -
 * this panel only collects input and displays results.
 */
public class InsuranceNetworkManagementPanel extends JPanel {

    private static final String[] COLUMN_NAMES =
            {"ID", "Provider", "Plan", "Coverage %", "Active", "Managed By"};

    private final InsuranceNetworkManager insuranceManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;
    private JTable networkTable;

    public InsuranceNetworkManagementPanel(InsuranceNetworkManager insuranceManager, UserManager userManager,
            AppointmentManager appointmentManager, HospitalAssetManager assetManager,
            ConsultationRateManager rateManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.insuranceManager = insuranceManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
        refreshTable();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("Insurance Network Management");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        networkTable = new JTable(tableModel);
        networkTable.setRowHeight(24);
        networkTable.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(networkTable), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton addButton = new JButton("Add Network");
        JButton editButton = new JButton("Edit Selected");
        JButton toggleButton = new JButton("Toggle Active");
        JButton deleteButton = new JButton("Delete Selected");
        JButton backButton = new JButton("Back to Dashboard");

        for (JButton button : new JButton[]{addButton, editButton, toggleButton, deleteButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

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
                sessionManager, userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                mainFrame, onLogout)));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        return buttonPanel;
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
        } catch (IllegalArgumentException ex) {
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
        } catch (IllegalArgumentException ex) {
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
}
