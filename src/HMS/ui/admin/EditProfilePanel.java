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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Self-service profile screen for the logged-in Admin. User ID, username,
 * and role are shown but never editable - only phone and email can change.
 */
public class EditProfilePanel extends JPanel {

    private final UserManager userManager;
    private final AppointmentManager appointmentManager;
    private final HospitalAssetManager assetManager;
    private final ConsultationRateManager rateManager;
    private final InsuranceNetworkManager insuranceManager;
    private final SessionManager sessionManager;
    private final MainFrame mainFrame;
    private final Runnable onLogout;

    private JTextField phoneField;
    private JTextField emailField;

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
        buildLayout();
    }

    private User currentUser() {
        return sessionManager.getCurrentUser();
    }

    private void buildLayout() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel titleLabel = new JLabel("My Profile");
        UIStyles.applyHeadingStyle(titleLabel);
        add(titleLabel, BorderLayout.NORTH);

        User user = currentUser();
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        addFormRow(formPanel, gbc, "User ID:", new JLabel(user.getUserId()));
        addFormRow(formPanel, gbc, "Username:", new JLabel(user.getUsername()));
        addFormRow(formPanel, gbc, "Role:", new JLabel(user.getUserType()));

        phoneField = new JTextField(user.getPhone(), 18);
        emailField = new JTextField(user.getEmail(), 18);
        addFormRow(formPanel, gbc, "Phone:", phoneField);
        addFormRow(formPanel, gbc, "Email:", emailField);

        JButton saveButton = new JButton("Save Changes");
        UIStyles.applyPrimaryButtonStyle(saveButton);
        saveButton.addActionListener(e -> saveProfile());
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        formPanel.add(saveButton, gbc);

        gbc.gridy++;
        JButton changePasswordButton = new JButton("Change Password");
        UIStyles.applyPrimaryButtonStyle(changePasswordButton);
        changePasswordButton.addActionListener(e -> openChangePasswordDialog());
        formPanel.add(changePasswordButton, gbc);

        add(formPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Theme.BACKGROUND_COLOR);
        JButton backButton = new JButton("Back to Dashboard");
        UIStyles.applyPrimaryButtonStyle(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(new AdminDashboardPanel(
                sessionManager, userManager, appointmentManager, assetManager, rateManager, insuranceManager,
                mainFrame, onLogout)));
        panel.add(backButton);
        return panel;
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

    private void addFormRow(JPanel panel, GridBagConstraints gbc, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private void addFormRow(JDialog dialog, GridBagConstraints gbc, String labelText, Component field) {
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        dialog.add(new JLabel(labelText), gbc);
        gbc.gridx = 1;
        dialog.add(field, gbc);
        gbc.gridy++;
    }
}
