import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordAuthentication implements UserAuthentication {
    @Override
    public boolean authenticate(String username, String password) {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT 1 FROM USER WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}