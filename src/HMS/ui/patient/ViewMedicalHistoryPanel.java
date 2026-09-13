package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.AssessmentResult;
import hms.model.MedicalRecord;
import hms.model.Patient;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Patient screen showing all completed medical records. Selecting a row
 * expands the full details (vitals, diagnosis, notes, assessment results)
 * in the panel below the table.
 */
public class ViewMedicalHistoryPanel extends JPanel {

    private static final String[] COLUMNS = {"Record ID", "Doctor", "Appointment Date", "Diagnosis"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final BillingManager billingManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private List<MedicalRecord> records;
    private DefaultTableModel tableModel;
    private JTextArea detailArea;

    public ViewMedicalHistoryPanel(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
            DoctorShiftManager shiftManager, BillingManager billingManager, UserManager userManager,
            SessionManager sessionManager, MainFrame mainFrame, Runnable onLogout) {
        this.clinicalManager = clinicalManager;
        this.appointmentManager = appointmentManager;
        this.shiftManager = shiftManager;
        this.billingManager = billingManager;
        this.userManager = userManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        buildLayout();
        refresh();
    }

    private Patient currentPatient() {
        return (Patient) sessionManager.getCurrentUser();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("My Medical History");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(tableModel);
        table.setFont(Theme.DEFAULT_FONT);
        table.setRowHeight(24);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showDetail(table.getSelectedRow());
            }
        });

        detailArea = new JTextArea(8, 50);
        detailArea.setEditable(false);
        detailArea.setFont(Theme.DEFAULT_FONT);
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);

        JPanel centerPanel = new JPanel(new BorderLayout(0, Theme.SPACING_MEDIUM));
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);

        JScrollPane tableScroll = new JScrollPane(table);
        tableScroll.setPreferredSize(new Dimension(600, 200));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        JScrollPane detailScroll = new JScrollPane(detailArea);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Record Details (select a row above)"));
        centerPanel.add(detailScroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new PatientDashboardPanel(
                sessionManager, userManager, appointmentManager, shiftManager, clinicalManager, billingManager,
                mainFrame, onLogout)));
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refresh() {
        records = clinicalManager.getPatientHistory(currentPatient().getPatientId());
        tableModel.setRowCount(0);
        for (MedicalRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.getRecordId(),
                    record.getDoctor() != null ? record.getDoctor().getUsername() : "",
                    record.getAppointment() != null
                            ? DATE_FORMAT.format(record.getAppointment().getAppointmentDate()) : "",
                    record.getDiagnosis() == null ? "" : record.getDiagnosis()
            });
        }
    }

    private void showDetail(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= records.size()) {
            detailArea.setText("");
            return;
        }
        MedicalRecord record = records.get(rowIndex);
        StringBuilder sb = new StringBuilder();
        sb.append("Record ID:           ").append(record.getRecordId()).append("\n");
        sb.append("Doctor:              ")
                .append(record.getDoctor() != null ? record.getDoctor().getUsername() : "N/A").append("\n");
        sb.append("Date:                ").append(DATE_FORMAT.format(record.getRecordDate())).append("\n");
        sb.append("Vital Signs:         ")
                .append(record.getVitalSigns() == null ? "" : record.getVitalSigns()).append("\n");
        sb.append("Diagnosis:           ")
                .append(record.getDiagnosis() == null ? "" : record.getDiagnosis()).append("\n");
        sb.append("Consultation Notes:  ")
                .append(record.getConsultationNotes() == null ? "" : record.getConsultationNotes()).append("\n");
        if (!record.getAssessmentResults().isEmpty()) {
            sb.append("\nAssessment Results:\n");
            for (AssessmentResult result : record.getAssessmentResults()) {
                sb.append("  ").append(result.getAssessmentName()).append(": ").append(result.getValue());
                if (result.getGrade() != null && !result.getGrade().isEmpty()) {
                    sb.append(" [").append(result.getGrade()).append("]");
                }
                sb.append("\n");
            }
        }
        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }
}
