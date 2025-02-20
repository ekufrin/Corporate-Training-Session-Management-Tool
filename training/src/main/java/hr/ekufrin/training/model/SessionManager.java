package hr.ekufrin.training.model;

/**
 * Represents a session manager for storing user data for current login session.
 * Singleton class.
 */
public class SessionManager {
    public static final SessionManager INSTANCE = getInstance();
    private static SessionManager singletonInstance;
    private User user;

    private SessionManager() {}

    private static SessionManager getInstance() {
        if(INSTANCE == null) {
            singletonInstance = new SessionManager();
        }
        return singletonInstance;
    }

    /**
     * Sets the current session with the given user.
     * @param user - user who is currently logged in
     */
    public void setSession(User user) {
        this.user = user;
    }

    /**
     * Returns the user who is currently logged in.
     * @return - user
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the id of the user who is currently logged in.
     * @return - id of the user
     */
    public Long getuserId() {
        return user.getId();
    }

    /**
     * Returns the role of the user who is currently logged in.
     * @return - role of the user
     */
    public String getUserRole() {
        return user.getRole().getName();
    }
}
