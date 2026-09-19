package hms.ui.admin;

import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

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
        customizeComponents();
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

        setLayout(new java.awt.GridLayout(0, 1, Theme.SPACING_MEDIUM, Theme.SPACING_MEDIUM));

        editProfileButton.setText("Edit My Profile");
        add(editProfileButton);

        manageUsersButton.setText("Manage Users");
        add(manageUsersButton);

        manageDoctorsButton.setText("Manage Doctors");
        add(manageDoctorsButton);

        viewAppointmentsButton.setText("View Appointments");
        add(viewAppointmentsButton);

        manageAssetsButton.setText("Manage Hospital Assets");
        add(manageAssetsButton);

        manageRatesButton.setText("Manage Consultation Rates");
        add(manageRatesButton);

        manageInsuranceButton.setText("Manage Insurance Networks");
        add(manageInsuranceButton);
    }// </editor-fold>//GEN-END:initComponents

    private void customizeComponents() {
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        for (JButton button : new JButton[]{editProfileButton, manageUsersButton, manageDoctorsButton,
                viewAppointmentsButton, manageAssetsButton, manageRatesButton, manageInsuranceButton}) {
            UIStyles.applyPrimaryButtonStyle(button);
        }
    }

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
    private javax.swing.JButton manageUsersButton;
    private javax.swing.JButton manageDoctorsButton;
    private javax.swing.JButton viewAppointmentsButton;
    private javax.swing.JButton manageAssetsButton;
    private javax.swing.JButton manageRatesButton;
    private javax.swing.JButton manageInsuranceButton;
    // End of variables declaration//GEN-END:variables
}
