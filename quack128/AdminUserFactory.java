public class AdminUserFactory extends UserFactory {
    @Override
    public User createUser(String username, String bio, String password) {
        return new AdminUser(username, bio, password);
    }
    
}
