package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.MedicalRecord;
import hms.model.Patient;
import hms.model.Payment;
import hms.model.Prescription;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * Patient screen showing all prescriptions across completed records and all
 * payment records. Read-only — no actions available from here.
 */
public class ViewPrescriptionsBillsPanel extends JPanel {

    private static final String[] RX_COLUMNS = {"Rx ID", "Medication", "Dosage", "Instructions", "Doctor", "Date"};
    private static final String[] BILL_COLUMNS = {"Payment ID", "Appointment", "Amount", "Status", "Date"};
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final BillingManager billingManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public ViewPrescriptionsBillsPanel(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
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
    }

    private Patient currentPatient() {
        return (Patient) sessionManager.getCurrentUser();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("Prescriptions & Bills");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));
        centerPanel.setBackground(Theme.BACKGROUND_COLOR);
        centerPanel.add(createPrescriptionsPanel());
        centerPanel.add(createBillsPanel());
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

    private JPanel createPrescriptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("My Prescriptions"));

        DefaultTableModel rxModel = new DefaultTableModel(RX_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Patient patient = currentPatient();
        List<MedicalRecord> records = clinicalManager.getPatientHistory(patient.getPatientId());
        for (MedicalRecord record : records) {
            for (Prescription rx : record.getPrescriptions()) {
                rxModel.addRow(new Object[]{
                        rx.getPrescriptionId(),
                        rx.getMedicationDetails(),
                        rx.getDosage(),
                        rx.getInstructions(),
                        rx.getDoctor() != null ? rx.getDoctor().getUsername() : "",
                        rx.getPrescribedDate() != null ? DATE_FORMAT.format(rx.getPrescribedDate()) : ""
                });
            }
        }

        JTable rxTable = new JTable(rxModel);
        rxTable.setFont(Theme.DEFAULT_FONT);
        rxTable.setRowHeight(24);
        panel.add(new JScrollPane(rxTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createBillsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder("My Bills"));

        DefaultTableModel billModel = new DefaultTableModel(BILL_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        List<Payment> payments = billingManager.getPatientBills(currentPatient().getPatientId());
        for (Payment payment : payments) {
            Appointment appt = appointmentManager.findById(payment.getAppointmentId());
            String apptDisplay;
            if (appt != null) {
                apptDisplay = payment.getAppointmentId()
                        + " | Dr. " + (appt.getDoctor() != null ? appt.getDoctor().getUsername() : "?")
                        + " | " + DATE_FORMAT.format(appt.getAppointmentDate())
                        + " " + appt.getAppointmentTime().format(TIME_FORMAT);
            } else {
                apptDisplay = payment.getAppointmentId();
            }
            billModel.addRow(new Object[]{
                    payment.getPaymentId(),
                    apptDisplay,
                    String.format("%.2f", payment.getAmount()),
                    payment.getStatus(),
                    payment.getPaymentDate() != null ? DATE_FORMAT.format(payment.getPaymentDate()) : ""
            });
        }

        JTable billTable = new JTable(billModel);
        billTable.setFont(Theme.DEFAULT_FONT);
        billTable.setRowHeight(24);
        panel.add(new JScrollPane(billTable), BorderLayout.CENTER);
        return panel;
    }
}
