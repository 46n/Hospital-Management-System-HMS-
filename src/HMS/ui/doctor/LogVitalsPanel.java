package hms.ui.doctor;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.ClinicalManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.Doctor;
import hms.model.MedicalRecord;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Doctor screen for logging vital signs, consultation notes, and diagnosis
 * against one of their appointments. Also provides a Finalize action that
 * marks the appointment COMPLETED (requires consultation notes to be non-empty).
 * All persistence goes through ClinicalManager; this panel never touches
 * data files directly.
 */
public class LogVitalsPanel extends JPanel {

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final AssessmentTypeManager typeManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private List<Appointment> doctorAppointments;
    private JComboBox<String> appointmentCombo;
    private JTextField vitalsField;
    private JTextArea notesArea;
    private JTextField diagnosisField;

    public LogVitalsPanel(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
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

        JLabel titleLabel = new JLabel("Log Vitals & Consultation Notes");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);

        doctorAppointments = appointmentManager.listForDoctor(currentDoctor().getDoctorId());
        String[] labels = new String[doctorAppointments.size()];
        for (int i = 0; i < doctorAppointments.size(); i++) {
            Appointment a = doctorAppointments.get(i);
            labels[i] = a.getAppointmentId() + " - " + a.getPatient().getUsername() + " (" + a.getStatus() + ")";
        }
        appointmentCombo = new JComboBox<>(labels);
        appointmentCombo.addActionListener(e -> loadExistingRecord());

        vitalsField = new JTextField(30);
        notesArea = new JTextArea(4, 30);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        diagnosisField = new JTextField(30);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(panel, gbc, "Appointment:", appointmentCombo);
        addFormRow(panel, gbc, "Vital Signs:", vitalsField);
        addFormRow(panel, gbc, "Consultation Notes:", new JScrollPane(notesArea));
        addFormRow(panel, gbc, "Diagnosis:", diagnosisField);

        JButton saveButton = new JButton("Save Vitals & Notes");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        saveButton.addActionListener(e -> saveVitals());
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(saveButton, gbc);

        gbc.gridy++;
        JButton finalizeButton = new JButton("Finalize Record (Mark Completed)");
        UIStyles.applyPrimaryButtonStyle(finalizeButton);
        finalizeButton.addActionListener(e -> finalizeRecord());
        panel.add(finalizeButton, gbc);

        if (!doctorAppointments.isEmpty()) {
            loadExistingRecord();
        }

        return panel;
    }

    private void loadExistingRecord() {
        int idx = appointmentCombo.getSelectedIndex();
        if (idx < 0 || idx >= doctorAppointments.size()) return;
        MedicalRecord record = doctorAppointments.get(idx).getMedicalRecord();
        if (record != null) {
            vitalsField.setText(record.getVitalSigns() == null ? "" : record.getVitalSigns());
            notesArea.setText(record.getConsultationNotes() == null ? "" : record.getConsultationNotes());
            diagnosisField.setText(record.getDiagnosis() == null ? "" : record.getDiagnosis());
        } else {
            vitalsField.setText("");
            notesArea.setText("");
            diagnosisField.setText("");
        }
    }

    private void saveVitals() {
        if (doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }
        Appointment appointment = doctorAppointments.get(appointmentCombo.getSelectedIndex());
        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                    appointment.getAppointmentId(), currentDoctor().getDoctorId());
            clinicalManager.updateVitalsAndNotes(record.getRecordId(),
                    vitalsField.getText().trim(),
                    diagnosisField.getText().trim(),
                    notesArea.getText().trim());
            JOptionPane.showMessageDialog(this, "Vitals and notes saved.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void finalizeRecord() {
        if (doctorAppointments.isEmpty()) {
            showError("You have no appointments.");
            return;
        }
        Appointment appointment = doctorAppointments.get(appointmentCombo.getSelectedIndex());
        if ("COMPLETED".equals(appointment.getStatus())) {
            showError("This appointment is already completed.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Finalize record for appointment " + appointment.getAppointmentId() + "?\n"
                + "This marks the appointment COMPLETED and cannot be undone.",
                "Confirm Finalize", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                    appointment.getAppointmentId(), currentDoctor().getDoctorId());
            clinicalManager.updateVitalsAndNotes(record.getRecordId(),
                    vitalsField.getText().trim(),
                    diagnosisField.getText().trim(),
                    notesArea.getText().trim());
            clinicalManager.finalizeRecord(record.getRecordId());
            JOptionPane.showMessageDialog(this, "Record finalized. Appointment marked COMPLETED.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshAppointmentCombo();
        } catch (IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAppointmentCombo() {
        doctorAppointments = appointmentManager.listForDoctor(currentDoctor().getDoctorId());
        appointmentCombo.removeAllItems();
        for (Appointment a : doctorAppointments) {
            appointmentCombo.addItem(
                    a.getAppointmentId() + " - " + a.getPatient().getUsername() + " (" + a.getStatus() + ")");
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
