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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
 * Admin screen for consultation rate configuration. All rules (required
 * fields, non-negative amount) live in ConsultationRateManager - this panel
 * only collects input and displays results.
 */
public class ConsultationRateManagementPanel extends JPanel {

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
    private JTable rateTable;

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
        buildLayout();
        refreshTable();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("Consultation Rate Management");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rateTable = new JTable(tableModel);
        rateTable.setRowHeight(24);
        rateTable.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(rateTable), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton addButton = new JButton("Add Rate");
        JButton editButton = new JButton("Edit Selected");
        JButton toggleButton = new JButton("Toggle Active");
        JButton deleteButton = new JButton("Delete Selected");
        JButton backButton = new JButton("Back to Dashboard");

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

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        return buttonPanel;
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
}
