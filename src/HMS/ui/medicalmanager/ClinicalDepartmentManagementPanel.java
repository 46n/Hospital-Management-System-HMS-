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
import hms.model.ClinicalDepartment;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
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
 * Medical Manager screen for clinical department management. All rules
 * (required name, unique name) live in ClinicalDepartmentManager - this
 * panel only collects input and displays results.
 */
public class ClinicalDepartmentManagementPanel extends JPanel {

    private static final String[] COLUMN_NAMES =
            {"ID", "Department Name", "Description", "Assigned Manager", "Active"};

    private final ClinicalDepartmentManager departmentManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final BillingManager billingManager;
    private final ReportManager reportManager;
    private final AssessmentTypeManager typeManager;
    private final DoctorShiftManager shiftManager;
    private final ClinicalManager clinicalManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;
    private JTable departmentTable;
    private JTextField searchField;

    public ClinicalDepartmentManagementPanel(ClinicalDepartmentManager departmentManager, UserManager userManager,
            AppointmentManager appointmentManager, BillingManager billingManager, ReportManager reportManager,
            AssessmentTypeManager typeManager, DoctorShiftManager shiftManager, ClinicalManager clinicalManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.departmentManager = departmentManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.billingManager = billingManager;
        this.reportManager = reportManager;
        this.typeManager = typeManager;
        this.shiftManager = shiftManager;
        this.clinicalManager = clinicalManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
        refreshTable(departmentManager.getDepartments());
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        add(createTopPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        departmentTable = new JTable(tableModel);
        departmentTable.setRowHeight(24);
        departmentTable.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(departmentTable), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.BACKGROUND_COLOR);

        JLabel titleLabel = new JLabel("Clinical Department Management");
        UIStyles.applyHeadingStyle(titleLabel);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Theme.BACKGROUND_COLOR);
        searchField = new JTextField(16);
        JButton searchButton = new JButton("Search");
        UIStyles.applyPrimaryButtonStyle(searchButton);
        searchButton.addActionListener(e -> applySearch());
        JButton clearButton = new JButton("Clear");
        UIStyles.applyPrimaryButtonStyle(clearButton);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refreshTable(departmentManager.getDepartments());
        });
        searchPanel.add(new JLabel("Search (name or description):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        topPanel.add(searchPanel, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton addButton = new JButton("Add Department");
        JButton editButton = new JButton("Edit Selected");
        JButton toggleButton = new JButton("Toggle Active Status");
        JButton deleteButton = new JButton("Delete Department");
        JButton backButton = new JButton("Back to Dashboard");

        for (JButton button : new JButton[]{addButton, editButton, toggleButton, deleteButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

        addButton.addActionListener(e -> openDepartmentDialog(null));
        editButton.addActionListener(e -> {
            ClinicalDepartment department = requireSelection();
            if (department != null) {
                openDepartmentDialog(department);
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

    private void applySearch() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(departmentManager.getDepartments());
            return;
        }
        List<ClinicalDepartment> matches = new ArrayList<>();
        for (ClinicalDepartment department : departmentManager.getDepartments()) {
            String name = department.getDepartmentName() == null ? "" : department.getDepartmentName().toLowerCase();
            String description = department.getDescription() == null ? "" : department.getDescription().toLowerCase();
            if (name.contains(query) || description.contains(query)) {
                matches.add(department);
            }
        }
        refreshTable(matches);
    }

    private void refreshTable(List<ClinicalDepartment> departments) {
        tableModel.setRowCount(0);
        for (ClinicalDepartment department : departments) {
            tableModel.addRow(new Object[]{
                    department.getDepartmentId(),
                    department.getDepartmentName(),
                    department.getDescription(),
                    department.getMedicalManager() == null ? "" : department.getMedicalManager().getUsername(),
                    department.isActive() ? "Yes" : "No"
            });
        }
    }

    private ClinicalDepartment requireSelection() {
        int row = departmentTable.getSelectedRow();
        if (row < 0) {
            showError("Select a department first.");
            return null;
        }
        String departmentId = (String) tableModel.getValueAt(row, 0);
        return departmentManager.findById(departmentId);
    }

    private void toggleSelectedActive() {
        ClinicalDepartment department = requireSelection();
        if (department == null) {
            return;
        }
        try {
            departmentManager.setActive(department.getDepartmentId(), !department.isActive());
            refreshTable(departmentManager.getDepartments());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteSelected() {
        ClinicalDepartment department = requireSelection();
        if (department == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete department " + department.getDepartmentId() + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            departmentManager.deleteDepartment(department.getDepartmentId());
            refreshTable(departmentManager.getDepartments());
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openDepartmentDialog(ClinicalDepartment existingDepartment) {
        boolean isEdit = existingDepartment != null;
        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Department" : "Add Department", true);
        dialog.setLayout(new GridBagLayout());

        JTextField nameField = new JTextField(isEdit ? existingDepartment.getDepartmentName() : "", 18);
        JTextField descriptionField = new JTextField(isEdit ? existingDepartment.getDescription() : "", 18);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Department Name:", nameField);
        addFormRow(dialog, gbc, "Description:", descriptionField);

        JButton saveButton = new JButton(isEdit ? "Save" : "Add");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                if (isEdit) {
                    departmentManager.updateDepartment(existingDepartment.getDepartmentId(), name, description);
                } else {
                    String managerId = sessionManager.getCurrentUser().getUserId();
                    departmentManager.createDepartment(name, description, managerId);
                }
                dialog.dispose();
                refreshTable(departmentManager.getDepartments());
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
