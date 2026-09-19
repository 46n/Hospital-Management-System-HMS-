package hms.ui.admin;

import hms.manager.AppointmentManager;
import hms.manager.ConsultationRateManager;
import hms.manager.HospitalAssetManager;
import hms.manager.InsuranceNetworkManager;
import hms.manager.SessionManager;
import hms.manager.UserManager;
import hms.model.User;
import hms.ui.common.MainFrame;
import hms.ui.common.Theme;
import hms.ui.common.UIStyles;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

/**
 * Self-service profile screen for the logged-in Admin. User ID, username,
 * and role are shown but never editable - only phone and email can change.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see
 * EditProfilePanel.form). Only initComponents() is generated/guarded; the
 * live user data, the change-password dialog, and navigation stay
 * hand-written.
 */
public class EditProfilePanel extends javax.swing.JPanel {

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    public EditProfilePanel(UserManager userManager, AppointmentManager appointmentManager,
            HospitalAssetManager assetManager, ConsultationRateManager rateManager,
            InsuranceNetworkManager insuranceManager, SessionManager sessionManager,
            MainFrame mainFrame, Runnable onLogout) {
        this.userManager = userManager;
        this.appointmentManager = appointmentManager;
        this.assetManager = assetManager;
        this.rateManager = rateManager;
        this.insuranceManager = insuranceManager;
        this.sessionManager = sessionManager;
        this.mainFrame = mainFrame;
        this.onLogout = onLogout;
        initComponents();
        customizeComponents();
    }

    private User currentUser() {
        return sessionManager.getCurrentUser();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        titleLabel = new javax.swing.JLabel();
        formPanel = new javax.swing.JPanel();
        userIdLabel = new javax.swing.JLabel();
        userIdValueLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        usernameValueLabel = new javax.swing.JLabel();
        roleLabel = new javax.swing.JLabel();
        roleValueLabel = new javax.swing.JLabel();
        phoneLabel = new javax.swing.JLabel();
        phoneField = new javax.swing.JTextField();
        emailLabel = new javax.swing.JLabel();
        emailField = new javax.swing.JTextField();
        saveButton = new javax.swing.JButton();
        changePasswordButton = new javax.swing.JButton();
        buttonPanel = new javax.swing.JPanel();
        backButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        titleLabel.setText("My Profile");
        add(titleLabel, java.awt.BorderLayout.NORTH);

        formPanel.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints gridBagConstraints;

        userIdLabel.setText("User ID:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(userIdLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(userIdValueLabel, gridBagConstraints);

        usernameLabel.setText("Username:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(usernameLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(usernameValueLabel, gridBagConstraints);

        roleLabel.setText("Role:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(roleLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(roleValueLabel, gridBagConstraints);

        phoneLabel.setText("Phone:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(phoneLabel, gridBagConstraints);

        phoneField.setColumns(18);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(phoneField, gridBagConstraints);

        emailLabel.setText("Email:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(emailLabel, gridBagConstraints);

        emailField.setColumns(18);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(emailField, gridBagConstraints);

        saveButton.setText("Save Changes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(saveButton, gridBagConstraints);

        changePasswordButton.setText("Change Password");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(changePasswordButton, gridBagConstraints);

        add(formPanel, java.awt.BorderLayout.CENTER);

        backButton.setText("Back to Dashboard");
        buttonPanel.add(backButton);
        add(buttonPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Applies HMS theming, fills in the live user data, and wires listeners. */
    private void customizeComponents() {
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        UIStyles.applyHeadingStyle(titleLabel);
        formPanel.setBackground(Theme.BACKGROUND_COLOR);

        User user = currentUser();
        userIdValueLabel.setText(user.getUserId());
        usernameValueLabel.setText(user.getUsername());
        roleValueLabel.setText(user.getUserType());
        phoneField.setText(user.getPhone());
        emailField.setText(user.getEmail());

        UIStyles.applyPrimaryButtonStyle(saveButton);
        saveButton.addActionListener(e -> saveProfile());

        UIStyles.applyPrimaryButtonStyle(changePasswordButton);
        changePasswordButton.addActionListener(e -> openChangePasswordDialog());

        buttonPanel.setBackground(Theme.BACKGROUND_COLOR);
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new AdminDashboardPanel(
                sessionManager, userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                mainFrame, onLogout)));
    }

    private void saveProfile() {
        try {
            userManager.updateProfile(currentUser().getUserId(),
                    phoneField.getText().trim(), emailField.getText().trim());
            JOptionPane.showMessageDialog(this, "Profile updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void openChangePasswordDialog() {
        JDialog dialog = new JDialog(mainFrame, "Change Password", true);
        dialog.setLayout(new GridBagLayout());

        JPasswordField currentPasswordField = new JPasswordField(16);
        JPasswordField newPasswordField = new JPasswordField(16);
        JPasswordField confirmPasswordField = new JPasswordField(16);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(dialog, gbc, "Current Password:", currentPasswordField);
        addFormRow(dialog, gbc, "New Password:", newPasswordField);
        addFormRow(dialog, gbc, "Confirm New Password:", confirmPasswordField);

        JButton saveButton = new JButton("Change Password");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        JButton cancelButton = new JButton("Cancel");
        UIStyles.applyPrimaryButtonStyle(cancelButton);
        cancelButton.addActionListener(e -> dialog.dispose());

        saveButton.addActionListener(e -> {
            String current = new String(currentPasswordField.getPassword());
            String next = new String(newPasswordField.getPassword());
            String confirm = new String(confirmPasswordField.getPassword());
            if (!next.equals(confirm)) {
                showError("New passwords do not match.");
                return;
            }
            try {
                userManager.changePassword(currentUser().getUserId(), current, next);
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Password changed successfully.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(cancelButton, gbc);
        gbc.gridx = 1;
        dialog.add(saveButton, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void addFormRow(JDialog dialog, GridBagConstraints gbc, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
        gbc.gridy++;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel titleLabel;
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel userIdLabel;
    private javax.swing.JLabel userIdValueLabel;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JLabel usernameValueLabel;
    private javax.swing.JLabel roleLabel;
    private javax.swing.JLabel roleValueLabel;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JTextField phoneField;
    private javax.swing.JLabel emailLabel;
    private javax.swing.JTextField emailField;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton changePasswordButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton backButton;
    // End of variables declaration//GEN-END:variables
}
