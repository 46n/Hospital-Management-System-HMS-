package hms.ui.doctor;

import hms.manager.AppointmentManager;
import hms.manager.AssessmentTypeManager;
import hms.manager.ClinicalManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.AssessmentResult;
import hms.model.AssessmentType;
import hms.model.Doctor;
import hms.model.MedicalRecord;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
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
 * Doctor screen for recording a clinical assessment result against one of
 * their own appointments. AssessmentType (configured by a Medical Manager)
 * is the source of truth for category/grading rule/charge - this panel only
 * lets the doctor pick an active type and a value; ClinicalManager and
 * AssessmentType do all the validation.
 */
public class RecordAssessmentPanel extends JPanel {

    private static final String[] COLUMN_NAMES = {"Result ID", "Assessment", "Value", "Grade", "Date"};

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final AssessmentTypeManager typeManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private List<Appointment> doctorAppointments;
    private List<AssessmentType> activeTypes;
    private JComboBox<String> appointmentCombo;
    private JComboBox<String> typeCombo;
    private JTextField valueField;
    private JTextField gradeField;
    private DefaultTableModel resultsTableModel;

    public RecordAssessmentPanel(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
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

        JLabel titleLabel = new JLabel("Record Assessment Result");
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
        String[] appointmentLabels = new String[doctorAppointments.size()];
        for (int i = 0; i < doctorAppointments.size(); i++) {
            Appointment appointment = doctorAppointments.get(i);
            appointmentLabels[i] = appointment.getAppointmentId() + " - " + appointment.getPatient().getUsername()
                    + " (" + appointment.getStatus() + ")";
        }
        appointmentCombo = new JComboBox<>(appointmentLabels);

        activeTypes = new ArrayList<>();
        for (AssessmentType type : typeManager.getTypes()) {
            if (type.isActive()) {
                activeTypes.add(type);
            }
        }
        String[] typeLabels = new String[activeTypes.size()];
        for (int i = 0; i < activeTypes.size(); i++) {
            AssessmentType type = activeTypes.get(i);
            typeLabels[i] = type.getName() + " (" + type.getCategory() + ", " + type.getGradingRule() + ")";
        }
        typeCombo = new JComboBox<>(typeLabels);

        valueField = new JTextField(16);
        gradeField = new JTextField(16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(fieldsPanel, gbc, "Appointment:", appointmentCombo);
        addFormRow(fieldsPanel, gbc, "Assessment Type:", typeCombo);
        addFormRow(fieldsPanel, gbc, "Value:", valueField);
        addFormRow(fieldsPanel, gbc, "Grade / Interpretation (optional):", gradeField);

        JButton recordButton = new JButton("Record Result");
        UIStyles.applyPrimaryButtonStyle(recordButton);
        recordButton.addActionListener(e -> recordResult());
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        fieldsPanel.add(recordButton, gbc);

        panel.add(fieldsPanel, BorderLayout.NORTH);

        resultsTableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable resultsTable = new JTable(resultsTableModel);
        resultsTable.setFont(Theme.DEFAULT_FONT);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        scrollPane.setPreferredSize(new Dimension(500, 150));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
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

    private void recordResult() {
        if (doctorAppointments.isEmpty()) {
            showError("You have no appointments to record a result against.");
            return;
        }
        if (activeTypes.isEmpty()) {
            showError("No active assessment types are available. Ask a Medical Manager to add one.");
            return;
        }
        Appointment appointment = doctorAppointments.get(appointmentCombo.getSelectedIndex());
        AssessmentType type = activeTypes.get(typeCombo.getSelectedIndex());
        try {
            MedicalRecord record = clinicalManager.getOrOpenRecord(
                    appointment.getAppointmentId(), currentDoctor().getDoctorId());
            clinicalManager.addAssessmentResult(record.getRecordId(), type.getAssessmentTypeId(),
                    valueField.getText().trim(), gradeField.getText().trim());
            valueField.setText("");
            gradeField.setText("");
            refreshResultsTable(record);
            JOptionPane.showMessageDialog(this, "Assessment result recorded.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshResultsTable(MedicalRecord record) {
        resultsTableModel.setRowCount(0);
        for (AssessmentResult result : record.getAssessmentResults()) {
            resultsTableModel.addRow(new Object[]{
                    result.getResultId(),
                    result.getAssessmentName(),
                    result.getValue(),
                    result.getGrade(),
                    result.getResultDate()
            });
        }
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
