import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

class Picture {
    private String imagePath;
    private String caption;
    private int likesCount;
    private List<String> comments;

    public Picture(String imagePath, String caption) {
        this.imagePath = imagePath;
        this.caption = caption;
        this.likesCount = 0;
        this.comments = new ArrayList<>();
    }

    public void loadLikesCount() throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT COUNT(*) FROM `LIKE` WHERE imagePath = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, this.imagePath);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    likesCount = rs.getInt(1);
                }
            }
        }
    }

    public void loadComments() throws SQLException {
        comments.clear();
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT text FROM COMMENT WHERE imagePath = ? ORDER BY createdAt";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, this.imagePath);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    comments.add(rs.getString("text"));
                }
            }
        }
    }

    public void addComment(String username, String comment) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "INSERT INTO COMMENT (text, username, imagePath) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, comment);
                stmt.setString(2, username);
                stmt.setString(3, this.imagePath);
                stmt.executeUpdate();
            }
            comments.add(comment);
        }
    }

    public void like(String username) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "INSERT INTO `LIKE` (username, imagePath) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, this.imagePath);
                stmt.executeUpdate();
            }
            likesCount++;
        }
    }

    public String getImagePath() { return imagePath; }
    public String getCaption() { return caption; }
    public int getLikesCount() { return likesCount; }
    public List<String> getComments() { return comments; }
}