public class AdminUser extends User {
    public AdminUser(String username, String bio, String password) {
        super(username, bio, password);
    }

    @Override
    public String getRole() {
        return "AdminUser";
    }
    
}
