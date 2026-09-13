package hms.ui.doctor;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.ClinicalManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.MedicalRecord;
import hms.model.Prescription;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Doctor screen for issuing prescriptions against one of their appointments.
 * Selecting an appointment shows any prescriptions already recorded for that
 * appointment's medical record. All persistence goes through ClinicalManager.
 */
public class IssuePrescriptionPanel extends JPanel {

    private static final String[] COLUMNS = {"Rx ID", "Medication", "Dosage", "Instructions", "Date"};

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final AssessmentTypeManager typeManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private List<Appointment> doctorAppointments;
    private JComboBox<String> appointmentCombo;
    private JTextField medicationField;
    private JTextField dosageField;
    private JTextField instructionsField;
    private DefaultTableModel tableModel;

    public IssuePrescriptionPanel(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
            AssessmentTypeManager typeManager, UserManager userManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.clinicalManager = clinicalManager;
        this.appointmentManager = appointmentManager;
        this.typeManager = typeManager;
        this.userManager = userManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
    }

    private Doctor currentDoctor() {
        return (Doctor) sessionManager.getCurrentUser();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("Issue Prescription");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Theme.BACKGROUND_COLOR);

        doctorAppointments = appointmentManager.listForDoctor(currentDoctor().getDoctorId());
        String[] labels = new String[doctorAppointments.size()];
        for (int i = 0; i < doctorAppointments.size(); i++) {
            Appointment a = doctorAppointments.get(i);
            labels[i] = a.getAppointmentId() + " - " + a.getPatient().getUsername() + " (" + a.getStatus() + ")";
        }
        appointmentCombo = new JComboBox<>(labels);
        appointmentCombo.addActionListener(e -> refreshTable());

        medicationField = new JTextField(20);
        dosageField = new JTextField(20);
        instructionsField = new JTextField(20);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(fieldsPanel, gbc, "Appointment:", appointmentCombo);
        addFormRow(fieldsPanel, gbc, "Medication:", medicationField);
        addFormRow(fieldsPanel, gbc, "Dosage:", dosageField);
        addFormRow(fieldsPanel, gbc, "Instructions:", instructionsField);

        JButton issueButton = new JButton("Issue Prescription");
        UIStyles.applyPrimaryButtonStyle(issueButton);
        issueButton.addActionListener(e -> issuePrescription());
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        fieldsPanel.add(issueButton, gbc);

        panel.add(fieldsPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(Theme.DEFAULT_FONT);
        table.setRowHeight(24);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        panel.add(scrollPane, BorderLayout.CENTER);

        if (!doctorAppointments.isEmpty()) {
            refreshTable();
        }

        return panel;
    }

    private void issuePrescription() {
        if (doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }
        Appointment appointment = doctorAppointments.get(appointmentCombo.getSelectedIndex());
        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                    appointment.getAppointmentId(), currentDoctor().getDoctorId());
            clinicalManager.addPrescription(record.getRecordId(),
                    medicationField.getText().trim(),
                    dosageField.getText().trim(),
                    instructionsField.getText().trim());
            medicationField.setText("");
            dosageField.setText("");
            instructionsField.setText("");
            refreshTable();
            JOptionPane.showMessageDialog(this, "Prescription issued.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        int idx = appointmentCombo.getSelectedIndex();
        if (idx < 0 || idx >= doctorAppointments.size()) return;
        MedicalRecord record = doctorAppointments.get(idx).getMedicalRecord();
        if (record == null) return;
        for (Prescription rx : record.getPrescriptions()) {
            tableModel.addRow(new Object[]{
                    rx.getPrescriptionId(),
                    rx.getMedicationDetails(),
                    rx.getDosage(),
                    rx.getInstructions(),
                    rx.getPrescribedDate()
            });
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new DoctorDashboardPanel(
                sessionManager, userManager, appointmentManager, clinicalManager, typeManager, mainFrame, onLogout)));
        panel.add(backButton);
        return panel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
    }
}
