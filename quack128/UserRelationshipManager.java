import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRelationshipManager {

    public void followUser(String follower, String followed) throws SQLException {
        if (!isAlreadyFollowing(follower, followed)) {
            try (Connection conn = new DatabaseConnector().getConnection()) {
                String query = "INSERT INTO FOLLOW (follower, followed) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setString(1, follower);
                    stmt.setString(2, followed);
                    stmt.executeUpdate();
                }
            }
        }
    }

    private boolean isAlreadyFollowing(String follower, String followed) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT 1 FROM FOLLOW WHERE follower = ? AND followed = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, follower);
                stmt.setString(2, followed);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        }
    }

    public List<String> getFollowers(String username) throws SQLException {
        List<String> followers = new ArrayList<>();
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT follower FROM FOLLOW WHERE followed = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    followers.add(rs.getString("follower"));
                }
            }
        }
        return followers;
    }

    public List<String> getFollowing(String username) throws SQLException {
        List<String> following = new ArrayList<>();
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT followed FROM FOLLOW WHERE follower = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    following.add(rs.getString("followed"));
                }
            }
        }
        return following;
    }
}