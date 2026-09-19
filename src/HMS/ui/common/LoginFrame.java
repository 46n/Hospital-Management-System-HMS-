package hms.ui.common;

import hms.manager.SessionManager;

/**
 * The only window shown before authentication. Holds nothing but the login
 * form - no navigation bar, reports, system menu, or user area - so the
 * application's workspace chrome (MainFrame) never exists until after a
 * successful login.
 *
 * Frame-level properties are managed by the NetBeans GUI Builder (see
 * LoginFrame.form). The content pane itself is a runtime-constructed
 * LoginPanel, so it is wired up in hand-written code after initComponents().
 */
public class LoginFrame extends javax.swing.JFrame {

    public LoginFrame(SessionManager sessionManager, LoginPanel.LoginListener onSuccess) {
        if (onSuccess == null) {
            throw new IllegalArgumentException("Success callback is required");
        }
        initComponents();
        LoginPanel loginPanel = new LoginPanel(sessionManager, user -> {
            dispose();
            onSuccess.onLoginSuccess(user);
        });
        setContentPane(loginPanel);
        setLocationRelativeTo(null);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Hospital Management System - Login");
        setMinimumSize(new java.awt.Dimension(480, 380));
        setSize(new java.awt.Dimension(520, 420));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
