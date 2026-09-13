package hms.ui.common;

import hms.manager.SessionManager;
import hms.model.User;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Presents the login form. All credential checking is delegated to
 * SessionManager (which delegates to UserManager); this panel only converts
 * input, calls it, and displays the result.
 */
public class LoginPanel extends JPanel {

    /** Notifies the caller once SessionManager confirms a successful login. */
    public interface LoginListener {
        void onLoginSuccess(User user);
    }

    private final SessionManager sessionManager;
    private final LoginListener listener;

    private final JTextField usernameField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel messageLabel = new JLabel(" ");

    public LoginPanel(SessionManager sessionManager, LoginListener listener) {
        if (sessionManager == null || listener == null) {
            throw new IllegalArgumentException("Session manager and listener are required");
        }
        this.sessionManager = sessionManager;
        this.listener = listener;
        buildLayout();
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());
        setBackground(Theme.BACKGROUND_COLOR);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Theme.BACKGROUND_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(
                        Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL, Theme.SPACING_SMALL);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel titleLabel = new JLabel("Hospital Management System - Login");
        UIStyles.applyHeadingStyle(titleLabel);
        formPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(Theme.DEFAULT_FONT);
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(usernameField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(Theme.DEFAULT_FONT);
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Log In");
        UIStyles.applyPrimaryButtonStyle(loginButton);
        loginButton.addActionListener(e -> attemptLogin());
        formPanel.add(loginButton, gbc);

        gbc.gridy++;
        messageLabel.setForeground(new Color(180, 0, 0));
        messageLabel.setFont(Theme.DEFAULT_FONT);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(messageLabel, gbc);

        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        passwordField.addActionListener(e -> attemptLogin());

        add(formPanel);
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
}
