package hms.ui.patient;

import hms.manager.AppointmentManager;
import hms.manager.BillingManager;
import hms.manager.ClinicalManager;
import hms.manager.DoctorShiftManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.common.MainFrame;
import hms.ui.common.UIStyles;

public class PatientDashboardNew extends javax.swing.JPanel {

    private SessionManager sessionManager;
    private UserManager userManager;
    private AppointmentManager appointmentManager;
    private DoctorShiftManager shiftManager;
    private ClinicalManager clinicalManager;
    private BillingManager billingManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    private PatientController patientController;

    /**
     * Constructor used by NetBeans GUI Builder.
     */
    public PatientDashboardNew() {
        initComponents();
        applyStyles();
        setPreferredSize(new java.awt.Dimension(850, 500));
    }

    /**
     * Constructor used by the real HMS application.
     */
public PatientDashboardNew(
        SessionManager sessionManager,
        UserManager userManager,
        AppointmentManager appointmentManager,
        DoctorShiftManager shiftManager,
        ClinicalManager clinicalManager,
        BillingManager billingManager,
        MainFrame mainFrame,
        Runnable onLogout) {

    initComponents();
    applyStyles();
    setPreferredSize(new java.awt.Dimension(850, 500));
    



    this.sessionManager = sessionManager;
    this.userManager = userManager;
    this.appointmentManager = appointmentManager;
    this.shiftManager = shiftManager;
    this.clinicalManager = clinicalManager;
    this.billingManager = billingManager;
    this.mainFrame = mainFrame;
    this.onLogout = onLogout;

    this.patientController = new PatientController(
            sessionManager,
            userManager,
            appointmentManager,
            shiftManager,
            clinicalManager,
            billingManager,
            mainFrame,
            onLogout
    );

    if (sessionManager != null
            && sessionManager.getCurrentUser() != null) {

        lblWelcome.setText(
                "Welcome, "
                + sessionManager.getCurrentUser().getUsername()
        );
    }
}
private void applyStyles() {

    UIStyles.applyHeadingStyle(lblTitle);
    UIStyles.applyHeadingStyle(lblWelcome);

    UIStyles.applyPrimaryButtonStyle(btnProfile);
    UIStyles.applyPrimaryButtonStyle(btnAppointments);
    UIStyles.applyPrimaryButtonStyle(btnMedicalHistory);
    UIStyles.applyPrimaryButtonStyle(btnPrescriptionsBills);
    UIStyles.applyPrimaryButtonStyle(btnFeedback);
    UIStyles.applyPrimaryButtonStyle(btnLogout);
}
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnLogout = new javax.swing.JButton();
        lblTitle = new javax.swing.JLabel();
        lblWelcome = new javax.swing.JLabel();
        btnProfile = new javax.swing.JButton();
        btnAppointments = new javax.swing.JButton();
        btnPrescriptionsBills = new javax.swing.JButton();
        btnMedicalHistory = new javax.swing.JButton();
        btnFeedback = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));
        setPreferredSize(new java.awt.Dimension(850, 500));

        btnLogout.setForeground(new java.awt.Color(51, 51, 51));
        btnLogout.setText("LOGOUT");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        lblTitle.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        lblTitle.setText("Patient Dashboard");

        lblWelcome.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        lblWelcome.setText("Welcome,");

        btnProfile.setBackground(new java.awt.Color(51, 51, 51));
        btnProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnProfile.setText("My Profile");
        btnProfile.addActionListener(this::btnProfileActionPerformed);

        btnAppointments.setBackground(new java.awt.Color(51, 51, 51));
        btnAppointments.setForeground(new java.awt.Color(255, 255, 255));
        btnAppointments.setText("Book Appointment");
        btnAppointments.addActionListener(this::btnAppointmentsActionPerformed);

        btnPrescriptionsBills.setBackground(new java.awt.Color(51, 51, 51));
        btnPrescriptionsBills.setForeground(new java.awt.Color(255, 255, 255));
        btnPrescriptionsBills.setText("Prescriptions & Bills");
        btnPrescriptionsBills.addActionListener(this::btnPrescriptionsBillsActionPerformed);

        btnMedicalHistory.setBackground(new java.awt.Color(51, 51, 51));
        btnMedicalHistory.setForeground(new java.awt.Color(255, 255, 255));
        btnMedicalHistory.setText("Medical History");
        btnMedicalHistory.addActionListener(this::btnMedicalHistoryActionPerformed);

        btnFeedback.setBackground(new java.awt.Color(51, 51, 51));
        btnFeedback.setForeground(new java.awt.Color(255, 255, 255));
        btnFeedback.setText("Submit Feedback");
        btnFeedback.addActionListener(this::btnFeedbackActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(167, 167, 167)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(39, 39, 39)
                                .addComponent(lblTitle, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(15, 15, 15)
                                .addComponent(lblWelcome, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnMedicalHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(19, 19, 19)
                                .addComponent(btnPrescriptionsBills, javax.swing.GroupLayout.PREFERRED_SIZE, 236, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(133, 133, 133)
                                .addComponent(btnFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, 235, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(357, 357, 357)
                        .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(193, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblTitle)
                    .addComponent(lblWelcome))
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnMedicalHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnPrescriptionsBills, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(btnFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(155, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAppointmentsActionPerformed
        patientController.openAppointments();
    }//GEN-LAST:event_btnAppointmentsActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        patientController.logout();
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProfileActionPerformed
        patientController.openProfile();
    }//GEN-LAST:event_btnProfileActionPerformed

    private void btnMedicalHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMedicalHistoryActionPerformed
        patientController.openMedicalHistory();
    }//GEN-LAST:event_btnMedicalHistoryActionPerformed

    private void btnPrescriptionsBillsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrescriptionsBillsActionPerformed
        patientController.openPrescriptionsBills();
    }//GEN-LAST:event_btnPrescriptionsBillsActionPerformed

    private void btnFeedbackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFeedbackActionPerformed
        patientController.openFeedback();
    }//GEN-LAST:event_btnFeedbackActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAppointments;
    private javax.swing.JButton btnFeedback;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnMedicalHistory;
    private javax.swing.JButton btnPrescriptionsBills;
    private javax.swing.JButton btnProfile;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblWelcome;
    // End of variables declaration//GEN-END:variables
}
