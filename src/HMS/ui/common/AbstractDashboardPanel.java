package hms.ui.common;

import hms.manager.SessionManager;
import hms.model.User;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Shared chassis for the four role dashboards: a welcome/role header, a
 * role-specific navigation area supplied by each subclass, and a logout
 * footer. Holds no business logic - it only displays session state and
 * delegates logging out to SessionManager, the same way every dashboard
 * needs to.
 */
public abstract class AbstractDashboardPanel extends JPanel {

    protected AbstractDashboardPanel(SessionManager sessionManager, Runnable onLogout, String roleDisplayName) {
        if (sessionManager == null || onLogout == null) {
            throw new IllegalArgumentException("Session manager and logout callback are required");
        }
        User user = sessionManager.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
        buildLayout(user, roleDisplayName, sessionManager, onLogout);
    }

    private void buildLayout(User user, String roleDisplayName, SessionManager sessionManager, Runnable onLogout) {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(
                Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE, Theme.SPACING_LARGE));

        JLabel welcomeLabel = new JLabel("Welcome, " + user.getUsername() + " (" + roleDisplayName + ")");
        UIStyles.applyHeadingStyle(welcomeLabel);
        add(welcomeLabel, BorderLayout.NORTH);

        add(createNavigationPanel(), BorderLayout.CENTER);

        JButton logoutButton = new JButton("Log Out");
        UIStyles.applyPrimaryButtonStyle(logoutButton);
        logoutButton.addActionListener(e -> {
            sessionManager.logout();
            onLogout.run();
        });
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Theme.BACKGROUND_COLOR);
        footerPanel.add(logoutButton);
        add(footerPanel, BorderLayout.SOUTH);
    }

    /** Each role dashboard supplies its own navigation placeholders here. */
    protected abstract JPanel createNavigationPanel();

    /** Shared helper so every dashboard's placeholder buttons look and behave the same. */
    protected static JButton createPlaceholderButton(String label) {
        JButton button = new JButton(label);
        UIStyles.applyPrimaryButtonStyle(button);
        button.setEnabled(false);
        button.setToolTipText("Not implemented yet");
        return button;
    }
}
