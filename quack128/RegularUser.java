public class RegularUser extends User {
    public RegularUser(String username, String bio, String password) {
        super(username, bio, password);
    }

    @Override
    public String getRole() {
        return "RegularUser";
    }
}
