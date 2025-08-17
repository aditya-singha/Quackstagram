public class RegularUserFactory extends UserFactory {
    @Override
    public User createUser(String username, String bio, String password) {
        return new RegularUser(username, bio, password);
    }
    
}
