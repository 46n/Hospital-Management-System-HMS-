package hms.ui.common;

import hms.manager.SessionManager;
import hms.model.User;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Shared chassis for the four role dashboards: a welcome/role header, a
 * role-specific navigation area supplied by each subclass, and a logout
 * footer. Holds no business logic - it only displays session state and
 * delegates logging out to SessionManager, the same way every dashboard
 * needs to.
 *
 * The header and footer are managed by the NetBeans GUI Builder (see
 * AbstractDashboardPanel.form). The CENTER navigation area is supplied by
 * each subclass at runtime via createNavigationPanel(), so it stays out of
 * the generated block - the same contract every dashboard subclass already
 * relied on before this conversion, unchanged.
 */
public abstract class AbstractDashboardPanel extends javax.swing.JPanel {

    protected AbstractDashboardPanel(SessionManager sessionManager, Runnable onLogout, String roleDisplayName) {
        if (sessionManager == null || onLogout == null) {
            throw new IllegalArgumentException("Session manager and logout callback are required");
        }
        User user = sessionManager.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        initComponents();
        customizeComponents(user, roleDisplayName, sessionManager, onLogout);
        add(createNavigationPanel(), BorderLayout.CENTER);
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        welcomeLabel = new javax.swing.JLabel();
        footerPanel = new javax.swing.JPanel();
        logoutButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        welcomeLabel.setText("Welcome");
        add(welcomeLabel, java.awt.BorderLayout.NORTH);

        logoutButton.setText("Log Out");
        footerPanel.add(logoutButton);
        add(footerPanel, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Applies HMS theming and wires the one listener the generated block cannot express. */
    private void customizeComponents(User user, String roleDisplayName, SessionManager sessionManager, Runnable onLogout) {
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        welcomeLabel.setText("Welcome, " + user.getUsername() + " (" + roleDisplayName + ")");
        UIStyles.applyHeadingStyle(welcomeLabel);

        footerPanel.setBackground(Theme.BACKGROUND_COLOR);

        UIStyles.applyPrimaryButtonStyle(logoutButton);
        logoutButton.addActionListener(e -> {
            sessionManager.logout();
            onLogout.run();
        });
    }

    /** Each role dashboard supplies its own navigation content here. */
    protected abstract JPanel createNavigationPanel();

    /** Shared helper so every dashboard's placeholder buttons look and behave the same. */
    protected static JButton createPlaceholderButton(String label) {
        JButton button = new JButton(label);
        UIStyles.applyPrimaryButtonStyle(button);
        button.setEnabled(false);
        button.setToolTipText("Not implemented yet");
        return button;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel welcomeLabel;
    private javax.swing.JPanel footerPanel;
    private javax.swing.JButton logoutButton;
    // End of variables declaration//GEN-END:variables
}
