/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.AssetAllocationManager;
import hms.manager.ClinicalManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.ui.common.EditProfileNew;
import hms.ui.common.MainFrame;
import hms.ui.common.UIStyles;

/**
 * Landing screen for Administrative Staff, built the same way as the group's
 * other role dashboards (DoctorDashboardNew / MedicalManagerDashboardNew): a
 * single self-contained NetBeans GUI Builder JPanel with its own title label
 * and button grid, rather than a shared AbstractDashboardPanel/navigation-
 * panel pair.
 *
 * @author Abdulaziz Qaderi
 */
public class AdminDashboardNew extends javax.swing.JPanel {

    private SessionManager sessionManager;
    private UserManager userManager;
    private AppointmentManager appointmentManager;
    private ClinicalManager clinicalManager;
    private HospitalAssetManager assetManager;
    private ConsultationRateManager rateManager;
    private InsuranceNetworkManager insuranceManager;
    private AssetAllocationManager allocationManager;
    private MainFrame mainFrame;
    private Runnable onLogout;

    /**
     * Creates new form AdminDashboardNew
     */
    public AdminDashboardNew() {
        initComponents();
    }

    public AdminDashboardNew(SessionManager sessionManager, UserManager userManager,
            AppointmentManager appointmentManager, ClinicalManager clinicalManager,
            HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, AssetAllocationManager allocationManager,
            MainFrame mainFrame, Runnable onLogout) {

        initComponents();
        applyStyles();

        this.sessionManager = sessionManager;
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.clinicalManager = clinicalManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.allocationManager = allocationManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;

        if (sessionManager != null && sessionManager.getCurrentUser() != null) {
            lblWelcome.setText(
                "Welcome, "
                + sessionManager.getCurrentUser().getUsername()
                + " (Administrative Staff)");
        }
    }

    private void openProfile() {
        mainFrame.showPanel(new EditProfileNew(sessionManager, userManager, () -> mainFrame.showPanel(this)));
    }

    private void openUsers() {
        mainFrame.showPanel(new UserManagementPanel(userManager, appointmentManager, clinicalManager, assetManager,
                rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void openDoctors() {
        mainFrame.showPanel(new DoctorManagementPanel(userManager, appointmentManager, clinicalManager, assetManager,
                rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void openAppointments() {
        mainFrame.showPanel(new AppointmentManagementPanel(appointmentManager, userManager, clinicalManager,
                assetManager, rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void openMedicalRequests() {
        mainFrame.showPanel(new MedicalRequestManagementPanel(clinicalManager, userManager, appointmentManager,
                assetManager, rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void openHospitalAssets() {
        mainFrame.showPanel(new HospitalAssetManagementPanel(assetManager, userManager, appointmentManager,
                clinicalManager, rateManager, insuranceManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void openConsultationRates() {
        mainFrame.showPanel(new ConsultationRateManagementPanel(rateManager, userManager, appointmentManager,
                clinicalManager, assetManager, insuranceManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void openInsuranceNetworks() {
        mainFrame.showPanel(new InsuranceNetworkManagementPanel(insuranceManager, userManager, appointmentManager,
                clinicalManager, assetManager, rateManager, allocationManager, sessionManager, mainFrame, onLogout));
    }

    private void logout() {
        sessionManager.logout();
        if (onLogout != null) {
            onLogout.run();
        }
    }

    private void applyStyles() {
        UIStyles.applyHeadingStyle(lblWelcome);

        UIStyles.applyPrimaryButtonStyle(btnEditProfile);
        UIStyles.applyPrimaryButtonStyle(btnManageUsers);
        UIStyles.applyPrimaryButtonStyle(btnManageDoctors);
        UIStyles.applyPrimaryButtonStyle(btnViewAppointments);
        UIStyles.applyPrimaryButtonStyle(btnMedicalRequests);
        UIStyles.applyPrimaryButtonStyle(btnManageAssets);
        UIStyles.applyPrimaryButtonStyle(btnManageRates);
        UIStyles.applyPrimaryButtonStyle(btnManageInsurance);
        UIStyles.applyPrimaryButtonStyle(btnLogout);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblWelcome = new javax.swing.JLabel();
        btnEditProfile = new javax.swing.JButton();
        btnManageUsers = new javax.swing.JButton();
        btnManageDoctors = new javax.swing.JButton();
        btnViewAppointments = new javax.swing.JButton();
        btnMedicalRequests = new javax.swing.JButton();
        btnManageAssets = new javax.swing.JButton();
        btnManageRates = new javax.swing.JButton();
        btnManageInsurance = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();

        setBackground(new java.awt.Color(204, 204, 204));

        lblWelcome.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        lblWelcome.setText("Welcome, admin (Administrative Staff)");

        btnEditProfile.setBackground(new java.awt.Color(51, 51, 51));
        btnEditProfile.setForeground(new java.awt.Color(255, 255, 255));
        btnEditProfile.setText("Edit My Profile");
        btnEditProfile.addActionListener(this::btnEditProfileActionPerformed);

        btnManageUsers.setBackground(new java.awt.Color(51, 51, 51));
        btnManageUsers.setForeground(new java.awt.Color(255, 255, 255));
        btnManageUsers.setText("Manage Users");
        btnManageUsers.addActionListener(this::btnManageUsersActionPerformed);

        btnManageDoctors.setBackground(new java.awt.Color(51, 51, 51));
        btnManageDoctors.setForeground(new java.awt.Color(255, 255, 255));
        btnManageDoctors.setText("Manage Doctors");
        btnManageDoctors.addActionListener(this::btnManageDoctorsActionPerformed);

        btnViewAppointments.setBackground(new java.awt.Color(51, 51, 51));
        btnViewAppointments.setForeground(new java.awt.Color(255, 255, 255));
        btnViewAppointments.setText("View Appointments");
        btnViewAppointments.addActionListener(this::btnViewAppointmentsActionPerformed);

        btnMedicalRequests.setBackground(new java.awt.Color(51, 51, 51));
        btnMedicalRequests.setForeground(new java.awt.Color(255, 255, 255));
        btnMedicalRequests.setText("Medical Requests");
        btnMedicalRequests.addActionListener(this::btnMedicalRequestsActionPerformed);

        btnManageAssets.setBackground(new java.awt.Color(51, 51, 51));
        btnManageAssets.setForeground(new java.awt.Color(255, 255, 255));
        btnManageAssets.setText("Manage Hospital Assets");
        btnManageAssets.addActionListener(this::btnManageAssetsActionPerformed);

        btnManageRates.setBackground(new java.awt.Color(51, 51, 51));
        btnManageRates.setForeground(new java.awt.Color(255, 255, 255));
        btnManageRates.setText("Manage Consultation Rates");
        btnManageRates.addActionListener(this::btnManageRatesActionPerformed);

        btnManageInsurance.setBackground(new java.awt.Color(51, 51, 51));
        btnManageInsurance.setForeground(new java.awt.Color(255, 255, 255));
        btnManageInsurance.setText("Manage Insurance Networks");
        btnManageInsurance.addActionListener(this::btnManageInsuranceActionPerformed);

        btnLogout.setBackground(new java.awt.Color(51, 51, 51));
        btnLogout.setForeground(new java.awt.Color(255, 255, 255));
        btnLogout.setText("Log Out");
        btnLogout.addActionListener(this::btnLogoutActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblWelcome)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnEditProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnManageDoctors, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMedicalRequests, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnManageRates, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnManageUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnViewAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnManageAssets, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnManageInsurance, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(btnLogout, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(lblWelcome)
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManageUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnManageDoctors, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnViewAppointments, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMedicalRequests, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManageAssets, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnManageRates, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnManageInsurance, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnEditProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditProfileActionPerformed
        // TODO add your handling code here:
        openProfile();
    }//GEN-LAST:event_btnEditProfileActionPerformed

    private void btnManageUsersActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageUsersActionPerformed
        // TODO add your handling code here:
        openUsers();
    }//GEN-LAST:event_btnManageUsersActionPerformed

    private void btnManageDoctorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageDoctorsActionPerformed
        // TODO add your handling code here:
        openDoctors();
    }//GEN-LAST:event_btnManageDoctorsActionPerformed

    private void btnViewAppointmentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnViewAppointmentsActionPerformed
        // TODO add your handling code here:
        openAppointments();
    }//GEN-LAST:event_btnViewAppointmentsActionPerformed

    private void btnMedicalRequestsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMedicalRequestsActionPerformed
        // TODO add your handling code here:
        openMedicalRequests();
    }//GEN-LAST:event_btnMedicalRequestsActionPerformed

    private void btnManageAssetsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageAssetsActionPerformed
        // TODO add your handling code here:
        openHospitalAssets();
    }//GEN-LAST:event_btnManageAssetsActionPerformed

    private void btnManageRatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageRatesActionPerformed
        // TODO add your handling code here:
        openConsultationRates();
    }//GEN-LAST:event_btnManageRatesActionPerformed

    private void btnManageInsuranceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManageInsuranceActionPerformed
        // TODO add your handling code here:
        openInsuranceNetworks();
    }//GEN-LAST:event_btnManageInsuranceActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        // TODO add your handling code here:
        logout();
    }//GEN-LAST:event_btnLogoutActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditProfile;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnManageAssets;
    private javax.swing.JButton btnManageDoctors;
    private javax.swing.JButton btnManageInsurance;
    private javax.swing.JButton btnManageRates;
    private javax.swing.JButton btnManageUsers;
    private javax.swing.JButton btnMedicalRequests;
    private javax.swing.JButton btnViewAppointments;
    private javax.swing.JLabel lblWelcome;
    // End of variables declaration//GEN-END:variables
}
