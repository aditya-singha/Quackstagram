

public class PasswordManager {
    private UserAuthentication authenticationStrategy;

    public PasswordManager(UserAuthentication authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    public void setAuthenticationStrategy(UserAuthentication authenticationStrategy) {
        this.authenticationStrategy = authenticationStrategy;
    }

    public boolean verifyCredentials(String username, String input) {
        return authenticationStrategy.authenticate(username, input);
    }
}