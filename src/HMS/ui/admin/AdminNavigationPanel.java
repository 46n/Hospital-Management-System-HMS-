package hms.ui.admin;

import javax.swing.JButton;

/**
 * The Administrative Staff dashboard's navigation button grid.
 *
 * A small, self-contained NetBeans GUI Builder panel (see
 * AdminNavigationPanel.form). It has no dependency on any outer class, so it
 * can be safely instantiated during AbstractDashboardPanel's construction -
 * AdminDashboardPanel wires each button's action listener afterwards, once
 * its own fields are available.
 */
public class AdminNavigationPanel extends javax.swing.JPanel {

    public AdminNavigationPanel() {
        initComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        editProfileButton = new javax.swing.JButton();
        manageUsersButton = new javax.swing.JButton();
        manageDoctorsButton = new javax.swing.JButton();
        viewAppointmentsButton = new javax.swing.JButton();
        manageAssetsButton = new javax.swing.JButton();
        manageRatesButton = new javax.swing.JButton();
        manageInsuranceButton = new javax.swing.JButton();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(24, 24, 24, 24));
        setLayout(new java.awt.GridLayout(0, 1));

        editProfileButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        editProfileButton.setForeground(new java.awt.Color(255, 255, 255));
        editProfileButton.setBackground(new java.awt.Color(31, 78, 121));
        editProfileButton.setText("Edit My Profile");
        editProfileButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        editProfileButton.setBorderPainted(false);
        editProfileButton.setFocusPainted(false);
        editProfileButton.setOpaque(true);
        add(editProfileButton);

        manageUsersButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        manageUsersButton.setForeground(new java.awt.Color(255, 255, 255));
        manageUsersButton.setBackground(new java.awt.Color(31, 78, 121));
        manageUsersButton.setText("Manage Users");
        manageUsersButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        manageUsersButton.setBorderPainted(false);
        manageUsersButton.setFocusPainted(false);
        manageUsersButton.setOpaque(true);
        add(manageUsersButton);

        manageDoctorsButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        manageDoctorsButton.setForeground(new java.awt.Color(255, 255, 255));
        manageDoctorsButton.setBackground(new java.awt.Color(31, 78, 121));
        manageDoctorsButton.setText("Manage Doctors");
        manageDoctorsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        manageDoctorsButton.setBorderPainted(false);
        manageDoctorsButton.setFocusPainted(false);
        manageDoctorsButton.setOpaque(true);
        add(manageDoctorsButton);

        viewAppointmentsButton.setBackground(new java.awt.Color(31, 78, 121));
        viewAppointmentsButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        viewAppointmentsButton.setForeground(new java.awt.Color(255, 255, 255));
        viewAppointmentsButton.setText("View Appointments");
        viewAppointmentsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        viewAppointmentsButton.setBorderPainted(false);
        viewAppointmentsButton.setFocusPainted(false);
        viewAppointmentsButton.setOpaque(true);
        add(viewAppointmentsButton);

        manageAssetsButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        manageAssetsButton.setForeground(new java.awt.Color(255, 255, 255));
        manageAssetsButton.setBackground(new java.awt.Color(31, 78, 121));
        manageAssetsButton.setText("Manage Hospital Assets");
        manageAssetsButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        manageAssetsButton.setBorderPainted(false);
        manageAssetsButton.setFocusPainted(false);
        manageAssetsButton.setOpaque(true);
        add(manageAssetsButton);

        manageRatesButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        manageRatesButton.setForeground(new java.awt.Color(255, 255, 255));
        manageRatesButton.setBackground(new java.awt.Color(31, 78, 121));
        manageRatesButton.setText("Manage Consultation Rates");
        manageRatesButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        manageRatesButton.setBorderPainted(false);
        manageRatesButton.setFocusPainted(false);
        manageRatesButton.setOpaque(true);
        add(manageRatesButton);

        manageInsuranceButton.setFont(new java.awt.Font("SansSerif", 0, 14)); // NOI18N
        manageInsuranceButton.setForeground(new java.awt.Color(255, 255, 255));
        manageInsuranceButton.setBackground(new java.awt.Color(31, 78, 121));
        manageInsuranceButton.setText("Manage Insurance Networks");
        manageInsuranceButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        manageInsuranceButton.setBorderPainted(false);
        manageInsuranceButton.setFocusPainted(false);
        manageInsuranceButton.setOpaque(true);
        add(manageInsuranceButton);
    }// </editor-fold>//GEN-END:initComponents

    public JButton getEditProfileButton() {
        return editProfileButton;
    }

    public JButton getManageUsersButton() {
        return manageUsersButton;
    }

    public JButton getManageDoctorsButton() {
        return manageDoctorsButton;
    }

    public JButton getViewAppointmentsButton() {
        return viewAppointmentsButton;
    }

    public JButton getManageAssetsButton() {
        return manageAssetsButton;
    }

    public JButton getManageRatesButton() {
        return manageRatesButton;
    }

    public JButton getManageInsuranceButton() {
        return manageInsuranceButton;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton editProfileButton;
    private javax.swing.JButton manageAssetsButton;
    private javax.swing.JButton manageDoctorsButton;
    private javax.swing.JButton manageInsuranceButton;
    private javax.swing.JButton manageRatesButton;
    private javax.swing.JButton manageUsersButton;
    private javax.swing.JButton viewAppointmentsButton;
    // End of variables declaration//GEN-END:variables
}
