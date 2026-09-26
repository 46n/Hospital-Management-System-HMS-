/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hms.ui.patient;
import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;

import hms.model.Appointment;

import hms.ui.common.MainFrame;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import hms.ui.common.UIStyles;

/**
 *
 * @author andli
 */
public class SubmitFeedbackNew extends javax.swing.JPanel {
    private FeedbackController feedbackController;

    private List<Appointment> completedAppointments =
        new ArrayList<>();

    /**
     * Creates new form SubmitFeedbackNew
     */
public SubmitFeedbackNew() {
    initComponents();
    applyStyles();
}

public SubmitFeedbackNew(
        ClinicalManager clinicalManager,
        AppointmentManager appointmentManager,
        DoctorShiftManager shiftManager,
        BillingManager billingManager,
        UserManager userManager,
        SessionManager sessionManager,
        MainFrame mainFrame,
        Runnable onLogout) {

    initComponents();
    applyStyles();

    this.feedbackController =
            new FeedbackController(
                    clinicalManager,
                    appointmentManager,
                    shiftManager,
                    billingManager,
                    userManager,
                    sessionManager,
                    mainFrame,
                    onLogout
            );

    loadCompletedAppointments();
    
}
    private void loadCompletedAppointments() {

    cmbAppointment.removeAllItems();

    try {

        completedAppointments =
                feedbackController.getCompletedAppointments();

        for (Appointment appointment
                : completedAppointments) {

            String doctor =
                    appointment.getDoctor() != null
                            ? appointment.getDoctor().getUsername()
                            : "Unknown";

            cmbAppointment.addItem(
                    appointment.getAppointmentId()
                    + " - Dr. "
                    + doctor
            );
        }

        if (completedAppointments.isEmpty()) {

            cmbAppointment.addItem(
                    "No completed appointments"
            );

            btnSubmit.setEnabled(false);

        } else {

            btnSubmit.setEnabled(true);
        }

    } catch (IllegalStateException ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Unable to Load Appointments",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
    private void submitFeedback() {

    if (completedAppointments.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "No completed appointments available for feedback.",
                "Unable to Submit Feedback",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    int index =
            cmbAppointment.getSelectedIndex();

    if (index < 0
            || index >= completedAppointments.size()) {

        JOptionPane.showMessageDialog(
                this,
                "Please select an appointment.",
                "Unable to Submit Feedback",
                JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    Appointment appointment =
            completedAppointments.get(index);

    int rating =
            (Integer) spnRating.getValue();

    String comment =
            txtComment.getText().trim();

    int confirm =
            JOptionPane.showConfirmDialog(
                    this,
                    "Submit feedback for "
                    + appointment.getAppointmentId()
                    + "?\nRating: "
                    + rating
                    + "/5",
                    "Confirm Feedback",
                    JOptionPane.YES_NO_OPTION
            );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {

        feedbackController.submitFeedback(
                appointment,
                rating,
                comment
        );

        JOptionPane.showMessageDialog(
                this,
                "Feedback submitted successfully.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE
        );

        txtComment.setText("");
        spnRating.setValue(5);

        completedAppointments.remove(index);
        cmbAppointment.removeItemAt(index);

        if (completedAppointments.isEmpty()) {

            cmbAppointment.addItem(
                    "No completed appointments"
            );

            btnSubmit.setEnabled(false);
        }

    } catch (IllegalArgumentException
            | IllegalStateException ex) {

        JOptionPane.showMessageDialog(
                this,
                ex.getMessage(),
                "Unable to Submit Feedback",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
       
    
private void applyStyles() {

    UIStyles.applyHeadingStyle(lblTitle);

    UIStyles.applyPrimaryButtonStyle(btnSubmit);
    UIStyles.applyPrimaryButtonStyle(btnBack);
}    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblTitle = new javax.swing.JLabel();
        lblAppointment = new javax.swing.JLabel();
        cmbAppointment = new javax.swing.JComboBox<>();
        lblRating = new javax.swing.JLabel();
        spnRating = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtComment = new javax.swing.JTextArea();
        btnSubmit = new javax.swing.JButton();
        btnBack = new javax.swing.JButton();
        lblComment = new javax.swing.JLabel();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(850, 500));
        setLayout(null);

        lblTitle.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        lblTitle.setText("Submit Feedback");
        add(lblTitle);
        lblTitle.setBounds(320, 50, 189, 32);

        lblAppointment.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        lblAppointment.setText("Completed Appointment:");
        add(lblAppointment);
        lblAppointment.setBounds(200, 140, 143, 18);

        cmbAppointment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        add(cmbAppointment);
        cmbAppointment.setBounds(210, 160, 130, 25);

        lblRating.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        lblRating.setText("Rating (1 - 5):");
        add(lblRating);
        lblRating.setBounds(510, 140, 79, 18);

        spnRating.setModel(new javax.swing.SpinnerNumberModel(5, 1, 5, 1));
        add(spnRating);
        spnRating.setBounds(510, 160, 130, 28);

        txtComment.setColumns(20);
        txtComment.setLineWrap(true);
        txtComment.setRows(5);
        txtComment.setWrapStyleWord(true);
        jScrollPane1.setViewportView(txtComment);

        add(jScrollPane1);
        jScrollPane1.setBounds(320, 250, 194, 76);

        btnSubmit.setForeground(new java.awt.Color(51, 51, 51));
        btnSubmit.setText("Submit Feedback");
        btnSubmit.addActionListener(this::btnSubmitActionPerformed);
        add(btnSubmit);
        btnSubmit.setBounds(330, 340, 180, 37);

        btnBack.setForeground(new java.awt.Color(51, 51, 51));
        btnBack.setText("Back to Dashboard");
        btnBack.addActionListener(this::btnBackActionPerformed);
        add(btnBack);
        btnBack.setBounds(330, 420, 180, 37);

        lblComment.setFont(new java.awt.Font("Dialog", 0, 13)); // NOI18N
        lblComment.setText("Comment:");
        add(lblComment);
        lblComment.setBounds(320, 230, 60, 18);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSubmitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSubmitActionPerformed
        submitFeedback();
    }//GEN-LAST:event_btnSubmitActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        feedbackController.backToDashboard();
    }//GEN-LAST:event_btnBackActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnSubmit;
    private javax.swing.JComboBox<String> cmbAppointment;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAppointment;
    private javax.swing.JLabel lblComment;
    private javax.swing.JLabel lblRating;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JSpinner spnRating;
    private javax.swing.JTextArea txtComment;
    // End of variables declaration//GEN-END:variables
}
