package hms.ui.common;

import hms.manager.SessionManager;

import java.awt.Dimension;
import javax.swing.JFrame;

/**
 * The only window shown before authentication. Holds nothing but the login
 * form - no navigation bar, reports, system menu, or user area - so the
 * application's workspace chrome (MainFrame) never exists until after a
 * successful login.
 */
public class LoginFrame extends JFrame {

    public LoginFrame(SessionManager sessionManager, LoginPanel.LoginListener onSuccess) {
        super("Hospital Management System - Login");
        if (onSuccess == null) {
            throw new IllegalArgumentException("Success callback is required");
        }
        LoginPanel loginPanel = new LoginPanel(sessionManager, user -> {
            dispose();
            onSuccess.onLoginSuccess(user);
        });
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(loginPanel);
        setSize(520, 420);
        setMinimumSize(new Dimension(480, 380));
        setLocationRelativeTo(null);
    }
}
