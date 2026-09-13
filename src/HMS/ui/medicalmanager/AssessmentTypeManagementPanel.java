package hms.ui.medicalmanager;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalDepartmentManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.ReportManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.AssessmentType;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
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
 * Medical Manager screen for assessment type configuration. All rules
 * (required fields, valid category/grading rule, non-negative charge) live
 * in AssessmentTypeManager - this panel only collects input and displays
 * results.
 */
public class AssessmentTypeManagementPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {"ID", "Name", "Category", "Grading Rule", "Base Charge", "Active"};

    private final AssessmentTypeManager typeManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final BillingManager billingManager;
    private final ReportManager reportManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalDepartmentManager departmentManager;
    private final ClinicalManager clinicalManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;
    private JTable typeTable;

    public AssessmentTypeManagementPanel(AssessmentTypeManager typeManager, UserManager userManager,
            AppointmentManager appointmentManager, BillingManager billingManager, ReportManager reportManager,
            DoctorShiftManager shiftManager, ClinicalDepartmentManager departmentManager,
            ClinicalManager clinicalManager, SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.typeManager = typeManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.billingManager = billingManager;
        this.reportManager = reportManager;
        this.shiftManager = shiftManager;
        this.departmentManager = departmentManager;
        this.clinicalManager = clinicalManager;
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

        JLabel titleLabel = new JLabel("Assessment Type Management");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        typeTable = new JTable(tableModel);
        typeTable.setRowHeight(24);
        typeTable.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(typeTable), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton addButton = new JButton("Add Type");
        JButton editButton = new JButton("Edit Selected");
        JButton toggleButton = new JButton("Toggle Active");
        JButton deleteButton = new JButton("Delete Selected");
        JButton backButton = new JButton("Back to Dashboard");

        for (JButton button : new JButton[]{addButton, editButton, toggleButton, deleteButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

        addButton.addActionListener(e -> openTypeDialog(null));
        editButton.addActionListener(e -> {
            AssessmentType type = requireSelection();
            if (type != null) {
                openTypeDialog(type);
            }
        });
        toggleButton.addActionListener(e -> toggleSelectedActive());
        deleteButton.addActionListener(e -> deleteSelected());
        backButton.addActionListener(e -> mainFrame.showPanel(new MedicalManagerDashboardPanel(
                sessionManager, userManager, appointmentManager, billingManager, reportManager,
                typeManager, shiftManager, departmentManager, clinicalManager, mainFrame, onLogout)));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (AssessmentType type : typeManager.getTypes()) {
            tableModel.addRow(new Object[]{
                    type.getAssessmentTypeId(),
                    type.getName(),
                    type.getCategory(),
                    type.getGradingRule(),
                    type.getBaseCharge(),
                    type.isActive() ? "Yes" : "No"
            });
        }
    }

    private AssessmentType requireSelection() {
        int row = typeTable.getSelectedRow();
        if (row < 0) {
            showError("Select an assessment type first.");
            return null;
        }
        String typeId = (String) tableModel.getValueAt(row, 0);
        return typeManager.findById(typeId);
    }

    private void toggleSelectedActive() {
        AssessmentType type = requireSelection();
        if (type == null) {
            return;
        }
        try {
            typeManager.setActive(type.getAssessmentTypeId(), !type.isActive());
            refreshTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        AssessmentType type = requireSelection();
        if (type == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete assessment type " + type.getAssessmentTypeId() + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            typeManager.deleteType(type.getAssessmentTypeId());
            refreshTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openTypeDialog(AssessmentType existingType) {
        boolean isEdit = existingType != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Assessment Type" : "Add Assessment Type", true);
        dialog.setLayout(new GridBagLayout());

        JTextField nameField = new JTextField(isEdit ? existingType.getName() : "", 16);
        List<String> categories = AssessmentTypeManager.getValidCategories();
        JComboBox<String> categoryCombo = new JComboBox<>(categories.toArray(new String[0]));
        List<String> gradingRules = AssessmentTypeManager.getValidGradingRules();
        JComboBox<String> gradingRuleCombo = new JComboBox<>(gradingRules.toArray(new String[0]));
        JTextField chargeField = new JTextField(isEdit ? String.valueOf(existingType.getBaseCharge()) : "", 16);
        if (isEdit) {
            categoryCombo.setSelectedItem(existingType.getCategory());
            gradingRuleCombo.setSelectedItem(existingType.getGradingRule());
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Name:", nameField);
        addFormRow(dialog, gbc, "Type (Category):", categoryCombo);
        addFormRow(dialog, gbc, "Grading Rule:", gradingRuleCombo);
        addFormRow(dialog, gbc, "Base Charge:", chargeField);

        JButton saveButton = new JButton(isEdit ? "Save" : "Add");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            double charge;
            try {
                charge = Double.parseDouble(chargeField.getText().trim());
            } catch (NumberFormatException ex) {
                showError("Base charge must be a number.");
                return;
            }
            try {
                String name = nameField.getText().trim();
                String category = (String) categoryCombo.getSelectedItem();
                String gradingRule = (String) gradingRuleCombo.getSelectedItem();
                if (isEdit) {
                    typeManager.updateType(existingType.getAssessmentTypeId(), name, category, gradingRule, charge);
                } else {
                    String managerId = sessionManager.getCurrentUser().getUserId();
                    typeManager.createType(name, category, gradingRule, charge, managerId);
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
