package hms.ui.common;

import hms.manager.SessionManager;
import hms.model.User;

import java.awt.Color;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Presents the login form. All credential checking is delegated to
 * SessionManager (which delegates to UserManager); this panel only converts
 * input, calls it, and displays the result.
 *
 * Visual structure is managed by the NetBeans GUI Builder (see LoginPanel.form).
 * Only initComponents() is generated/guarded; everything else is hand-written.
 */
public class LoginPanel extends javax.swing.JPanel {

    private final SessionManager sessionManager;
    private final LoginListener listener;

    /** Notifies the caller once SessionManager confirms a successful login. */
    public interface LoginListener {
        void onLoginSuccess(User user);
    }

    public LoginPanel(SessionManager sessionManager, LoginListener listener) {
        if (sessionManager == null || listener == null) {
            throw new IllegalArgumentException("Session manager and listener are required");
        }
        this.sessionManager = sessionManager;
        this.listener = listener;
        initComponents();
        customizeComponents();
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        formPanel = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        usernameLabel = new javax.swing.JLabel();
        usernameField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        loginButton = new javax.swing.JButton();
        messageLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        formPanel.setLayout(new java.awt.GridBagLayout());

        titleLabel.setText("Hospital Management System - Login");
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(titleLabel, gridBagConstraints);

        usernameLabel.setText("Username:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(usernameLabel, gridBagConstraints);

        usernameField.setColumns(18);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(usernameField, gridBagConstraints);

        passwordLabel.setText("Password:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(passwordLabel, gridBagConstraints);

        passwordField.setColumns(18);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(passwordField, gridBagConstraints);

        loginButton.setText("Log In");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(loginButton, gridBagConstraints);

        messageLabel.setText(" ");
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        formPanel.add(messageLabel, gridBagConstraints);

        add(formPanel, new java.awt.GridBagConstraints());
    }// </editor-fold>//GEN-END:initComponents

    /** Applies HMS theming, wires listeners, and finishes anything initComponents() cannot express declaratively. */
    private void customizeComponents() {
        setBackground(Theme.BACKGROUND_COLOR);

        formPanel.setBackground(Theme.BACKGROUND_COLOR);
        formPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(Theme.PRIMARY_COLOR),
                javax.swing.BorderFactory.createEmptyBorder(
                        Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE)));

        UIStyles.applyHeadingStyle(titleLabel);

        usernameLabel.setFont(Theme.DEFAULT_FONT);
        passwordLabel.setFont(Theme.DEFAULT_FONT);

        UIStyles.applyPrimaryButtonStyle(loginButton);
        loginButton.addActionListener(e -> attemptLogin());

        messageLabel.setForeground(new Color(180, 0, 0));
        messageLabel.setFont(Theme.DEFAULT_FONT);

        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> attemptLogin());
    }

    /** Focuses the username field once this panel is actually showing, so ENTER-to-advance works immediately. */
    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        try {
            User user = sessionManager.login(username, password);
            messageLabel.setText(" ");
            passwordField.setText("");
            listener.onLoginSuccess(user);
        } catch (IllegalArgumentException ex) {
            messageLabel.setText(ex.getMessage());
            passwordField.setText("");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel formPanel;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel usernameLabel;
    private javax.swing.JTextField usernameField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JButton loginButton;
    private javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
}
