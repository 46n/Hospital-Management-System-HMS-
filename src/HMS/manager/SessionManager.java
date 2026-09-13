package hms.manager;

import hms.model.User;

/**
 * Coordinates the single active login session: who is currently logged in,
 * logging out, and role checks. Credential validation itself stays in
 * UserManager — this class only stores the result and answers "who is
 * logged in" / "are they allowed to do this" questions for the GUI.
 */
public class SessionManager {

    private final UserManager userManager;
    private User currentUser;

    public SessionManager(UserManager userManager) {
        if (userManager == null) {
            throw new IllegalArgumentException("User manager is required");
        }
        this.userManager = userManager;
    }

    /** Delegates credential checking to UserManager and stores the result as the active session. */
    public User login(String username, String password) {
        User user = userManager.login(username, password);
        this.currentUser = user;
        return user;
    }

    public void logout() {
        this.currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /** Throws if nobody is logged in; for managers/screens that require an active session. */
    public void requireLogin() {
        if (currentUser == null) {
            throw new IllegalStateException("No user is currently logged in");
        }
    }

    public boolean hasRole(String userType) {
        return currentUser != null && currentUser.getUserType().equals(userType);
    }

    /** Throws if nobody is logged in, or the logged-in user is not the expected role. */
    public void requireRole(String userType) {
        requireLogin();
        if (!currentUser.getUserType().equals(userType)) {
            throw new IllegalStateException("This action requires role: " + userType);
        }
    }
}
