import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Represents a user on Quackstagram
public abstract class User {
    private String username;
    private String bio;
    private String password;
    private int postsCount;
    private int followersCount;
    private int followingCount;
    private List<Picture> pictures;

    public User(String username, String bio, String password) {
        this.username = username;
        this.bio = bio;
        this.password = password;
        this.pictures = new ArrayList<>();
        this.postsCount = 0;
        this.followersCount = 0;
        this.followingCount = 0;
    }
    public void addPicture(Picture picture) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "INSERT INTO PICTURE (imagePath, caption, username) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, picture.getImagePath());
                stmt.setString(2, picture.getCaption());
                stmt.setString(3, this.username);
                stmt.executeUpdate();
            }
            pictures.add(picture);
            postsCount++;
        }
    }

    public void loadPictures() throws SQLException {
        pictures.clear();
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT * FROM PICTURE WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, this.username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    Picture picture = new Picture(
                            rs.getString("imagePath"),
                            rs.getString("caption")
                    );
                    pictures.add(picture);
                }
            }
            postsCount = pictures.size();
        }
    }

    public void loadCounts() throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String followersQuery = "SELECT COUNT(*) FROM FOLLOW WHERE followed = ?";
            try (PreparedStatement stmt = conn.prepareStatement(followersQuery)) {
                stmt.setString(1, this.username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    followersCount = rs.getInt(1);
                }
            }

            String followingQuery = "SELECT COUNT(*) FROM FOLLOW WHERE follower = ?";
            try (PreparedStatement stmt = conn.prepareStatement(followingQuery)) {
                stmt.setString(1, this.username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    followingCount = rs.getInt(1);
                }
            }
        }
    }

    public String getUsername() { return username; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public int getPostsCount() { return postsCount; }
    public int getFollowersCount() { return followersCount; }
    public int getFollowingCount() { return followingCount; }
    public List<Picture> getPictures() { return pictures; }

    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
    public void setPostsCount(int postCount) { this.postsCount = postCount; }

    public abstract String getRole();

    public String getPassword() {
        return password;
    }
}