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
import hms.model.Doctor;
import hms.model.DoctorShift;
import hms.model.MedicalManager;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
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
 * Medical Manager screen for scheduling shifts for the doctors assigned to
 * them. All rules (doctor must exist and be supervised by this manager,
 * start-before-end, no overlapping shifts) live in DoctorShiftManager -
 * this panel only collects input and displays results.
 */
public class DoctorShiftManagementPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {"ID", "Doctor", "Date", "Start", "End"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final DoctorShiftManager shiftManager;
    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final BillingManager billingManager;
    private final ReportManager reportManager;
    private final AssessmentTypeManager typeManager;
    private final ClinicalDepartmentManager departmentManager;
    private final ClinicalManager clinicalManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private DefaultTableModel tableModel;
    private JTable shiftTable;

    public DoctorShiftManagementPanel(DoctorShiftManager shiftManager, UserManager userManager,
            AppointmentManager appointmentManager, BillingManager billingManager, ReportManager reportManager,
            AssessmentTypeManager typeManager, ClinicalDepartmentManager departmentManager,
            ClinicalManager clinicalManager, SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.shiftManager = shiftManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.billingManager = billingManager;
        this.reportManager = reportManager;
        this.typeManager = typeManager;
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

        JLabel titleLabel = new JLabel("Doctor Roster / Shift Management");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        shiftTable = new JTable(tableModel);
        shiftTable.setRowHeight(24);
        shiftTable.setFont(Theme.DEFAULT_FONT);
        add(new JScrollPane(shiftTable), BorderLayout.CENTER);

        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);

        JButton addButton = new JButton("Add Shift");
        JButton editButton = new JButton("Edit Selected");
        JButton removeButton = new JButton("Remove Selected");
        JButton backButton = new JButton("Back to Dashboard");

        for (JButton button : new JButton[]{addButton, editButton, removeButton, backButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }

        addButton.addActionListener(e -> openShiftDialog(null));
        editButton.addActionListener(e -> {
            DoctorShift shift = requireSelection();
            if (shift != null) {
                openShiftDialog(shift);
            }
        });
        removeButton.addActionListener(e -> removeSelected());
        backButton.addActionListener(e -> mainFrame.showPanel(new MedicalManagerDashboardPanel(
                sessionManager, userManager, appointmentManager, billingManager, reportManager,
                typeManager, shiftManager, departmentManager, clinicalManager, mainFrame, onLogout)));

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    private MedicalManager currentManager() {
        return (MedicalManager) sessionManager.getCurrentUser();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (DoctorShift shift : shiftManager.getShiftsForManager(currentManager().getManagerId())) {
            tableModel.addRow(new Object[]{
                    shift.getShiftId(),
                    shift.getDoctor().getUsername(),
                    DATE_FORMAT.format(shift.getShiftDate()),
                    shift.getStartTime().format(TIME_FORMAT),
                    shift.getEndTime().format(TIME_FORMAT)
            });
        }
    }

    private DoctorShift requireSelection() {
        int row = shiftTable.getSelectedRow();
        if (row < 0) {
            showError("Select a shift first.");
            return null;
        }
        String shiftId = (String) tableModel.getValueAt(row, 0);
        return shiftManager.findById(shiftId);
    }

    private void removeSelected() {
        DoctorShift shift = requireSelection();
        if (shift == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove shift " + shift.getShiftId() + "? This cannot be undone.",
                "Confirm Remove", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            shiftManager.removeShift(shift.getShiftId());
            refreshTable();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void openShiftDialog(DoctorShift existingShift) {
        boolean isEdit = existingShift != null;
        List<Doctor> managedDoctors = currentManager().getManagedDoctors();
        if (!isEdit && managedDoctors.isEmpty()) {
            showError("No doctors are assigned to you yet. Ask an admin to assign one first.");
            return;
        }

        JDialog dialog = new JDialog(mainFrame, isEdit ? "Edit Shift" : "Add Shift", true);
        dialog.setLayout(new GridBagLayout());

        JComboBox<String> doctorCombo = null;
        if (!isEdit) {
            String[] doctorLabels = new String[managedDoctors.size()];
            for (int i = 0; i < managedDoctors.size(); i++) {
                Doctor doctor = managedDoctors.get(i);
                doctorLabels[i] = doctor.getUsername() + " (" + doctor.getDoctorId() + ")";
            }
            doctorCombo = new JComboBox<>(doctorLabels);
        }
        JTextField dateField = new JTextField(
                isEdit ? DATE_FORMAT.format(existingShift.getShiftDate()) : "", 16);
        JTextField startField = new JTextField(
                isEdit ? existingShift.getStartTime().format(TIME_FORMAT) : "", 16);
        JTextField endField = new JTextField(
                isEdit ? existingShift.getEndTime().format(TIME_FORMAT) : "", 16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        if (!isEdit) {
            addFormRow(dialog, gbc, "Doctor:", doctorCombo);
        } else {
            addFormRow(dialog, gbc, "Doctor:", new JLabel(existingShift.getDoctor().getUsername()));
        }
        addFormRow(dialog, gbc, "Date (yyyy-MM-dd):", dateField);
        addFormRow(dialog, gbc, "Start Time (HH:mm):", startField);
        addFormRow(dialog, gbc, "End Time (HH:mm):", endField);

        JButton saveButton = new JButton(isEdit ? "Save" : "Add");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        JComboBox<String> finalDoctorCombo = doctorCombo;
        saveButton.addActionListener(e -> {
            Date shiftDate;
            LocalTime startTime;
            LocalTime endTime;
            try {
                shiftDate = DATE_FORMAT.parse(dateField.getText().trim());
            } catch (ParseException ex) {
                showError("Date must be in yyyy-MM-dd format.");
                return;
            }
            try {
                startTime = LocalTime.parse(startField.getText().trim(), TIME_FORMAT);
                endTime = LocalTime.parse(endField.getText().trim(), TIME_FORMAT);
            } catch (DateTimeParseException ex) {
                showError("Start and end time must be in HH:mm format.");
                return;
            }
            try {
                if (isEdit) {
                    shiftManager.updateShift(existingShift.getShiftId(), shiftDate, startTime, endTime);
                } else {
                    int index = finalDoctorCombo.getSelectedIndex();
                    Doctor selectedDoctor = managedDoctors.get(index);
                    shiftManager.createShift(currentManager().getManagerId(),
                            selectedDoctor.getDoctorId(), shiftDate, startTime, endTime);
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
