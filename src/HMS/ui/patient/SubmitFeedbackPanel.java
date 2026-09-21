package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.Appointment;
import hms.model.Patient;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.BorderLayout;
import java.awt.Component;
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
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

/**
 * Patient screen for submitting feedback on a completed appointment. Only
 * COMPLETED appointments are shown; ClinicalManager enforces the one-feedback-
 * per-doctor rule so this panel does not need to duplicate that check.
 */
public class SubmitFeedbackPanel extends JPanel {

    private final ClinicalManager clinicalManager;
    private final AppointmentManager appointmentManager;
    private final DoctorShiftManager shiftManager;
    private final BillingManager billingManager;
    private final UserManager userManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private List<Appointment> completedAppointments;
    private JComboBox<String> appointmentCombo;
    private JSpinner ratingSpinner;
    private JTextArea commentArea;

    public SubmitFeedbackPanel(ClinicalManager clinicalManager, AppointmentManager appointmentManager,
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

        JLabel titleLabel = new JLabel("Submit Feedback");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.BACKGROUND_COLOR);

        completedAppointments = new ArrayList<>();
        for (Appointment a : appointmentManager.listForPatient(currentPatient().getPatientId())) {
            if ("COMPLETED".equals(a.getStatus())) {
                completedAppointments.add(a);
            }
        }

        String[] labels = new String[completedAppointments.size()];
        for (int i = 0; i < completedAppointments.size(); i++) {
            Appointment a = completedAppointments.get(i);
            labels[i] = a.getAppointmentId() + " - Dr. " + a.getDoctor().getUsername();
        }
        appointmentCombo = new JComboBox<>(labels);
        ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        commentArea = new JTextArea(4, 30);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(panel, gbc, "Completed Appointment:", appointmentCombo);
        addFormRow(panel, gbc, "Rating (1-5):", ratingSpinner);
        addFormRow(panel, gbc, "Comment:", new JScrollPane(commentArea));

        JButton submitButton = new JButton("Submit Feedback");
        UIStyles.applyPrimaryButtonStyle(submitButton);
        submitButton.addActionListener(e -> submitFeedback());
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        panel.add(submitButton, gbc);

        if (completedAppointments.isEmpty()) {
            gbc.gridy++;
            JLabel noDataLabel = new JLabel("No completed appointments available for feedback.");
            noDataLabel.setFont(Theme.DEFAULT_FONT);
            panel.add(noDataLabel, gbc);
        }

        return panel;
    }

    private void submitFeedback() {
        int idx = appointmentCombo.getSelectedIndex();
        if (completedAppointments.isEmpty() || idx < 0) {
            showError("No completed appointments available for feedback.");
            return;
        }
        Appointment appointment = completedAppointments.get(idx);
        int rating = (Integer) ratingSpinner.getValue();
        String comment = commentArea.getText().trim();
        try {
            clinicalManager.submitFeedback(
                    currentPatient().getPatientId(), appointment.getAppointmentId(), rating, comment);
            completedAppointments.remove(idx);
            appointmentCombo.removeItemAt(idx);
            commentArea.setText("");
            ratingSpinner.setValue(5);
            JOptionPane.showMessageDialog(this, "Feedback submitted. Thank you!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new PatientDashboardNew(
                sessionManager, userManager, appointmentManager, shiftManager, clinicalManager, billingManager,
                mainFrame, onLogout)));
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
