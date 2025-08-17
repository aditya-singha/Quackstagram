import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuakstagramHomeUI extends JFrame {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final int NAV_ICON_SIZE = 20;
    private static final int IMAGE_WIDTH = WIDTH - 100;
    private static final int IMAGE_HEIGHT = 150;
    private static final Color LIKE_BUTTON_COLOR = new Color(255, 90, 95);

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private JPanel homePanel;
    private JPanel imageViewPanel;
    private User currentUser;
    private List<Post> posts;

    public QuakstagramHomeUI(User user) {
        currentUser = user;
        setTitle("Quakstagram Home");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        posts = new ArrayList<>();

        try {
            loadPosts();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading posts: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        initializeUI();
    }

    private void loadPosts() throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String followingQuery = "SELECT followed FROM FOLLOW WHERE follower = ?";
            List<String> following = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(followingQuery)) {
                stmt.setString(1, currentUser.getUsername());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    following.add(rs.getString("followed"));
                }
            }

            if (following.isEmpty()) {
                return;
            }

            String postsQuery = "SELECT p.imagePath, p.caption, u.username " +
                    "FROM PICTURE p JOIN USER u ON p.username = u.username " +
                    "WHERE p.username IN (";

            StringBuilder placeholders = new StringBuilder();
            for (int i = 0; i < following.size(); i++) {
                placeholders.append("?");
                if (i < following.size() - 1) {
                    placeholders.append(",");
                }
            }
            postsQuery += placeholders + ") ORDER BY p.createdAt DESC";

            try (PreparedStatement stmt = conn.prepareStatement(postsQuery)) {
                for (int i = 0; i < following.size(); i++) {
                    stmt.setString(i + 1, following.get(i));
                }

                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String imagePath = rs.getString("imagePath");
                    String caption = rs.getString("caption");
                    String username = rs.getString("username");

                    int likes = getLikeCount(conn, imagePath);

                    boolean isLiked = isLikedByUser(conn, imagePath, currentUser.getUsername());

                    posts.add(new Post(username, caption, likes, imagePath, isLiked));
                }
            }
        }
    }

    private int getLikeCount(Connection conn, String imagePath) throws SQLException {
        String query = "SELECT COUNT(*) FROM `LIKE` WHERE imagePath = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, imagePath);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private boolean isLikedByUser(Connection conn, String imagePath, String username) throws SQLException {
        String query = "SELECT 1 FROM `LIKE` WHERE imagePath = ? AND username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, imagePath);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private void initializeUI() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        homePanel = new JPanel(new BorderLayout());
        imageViewPanel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(51, 51, 51));
        JLabel lblTitle = new JLabel("🐥 Quakstagram 🐥");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle);
        headerPanel.setPreferredSize(new Dimension(WIDTH, 40));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        for (Post post : posts) {
            JPanel postPanel = createPostPanel(post);
            contentPanel.add(postPanel);

            JPanel spacingPanel = new JPanel();
            spacingPanel.setPreferredSize(new Dimension(WIDTH-10, 5));
            spacingPanel.setBackground(new Color(230, 230, 230));
            contentPanel.add(spacingPanel);
        }

        homePanel.add(headerPanel, BorderLayout.NORTH);
        homePanel.add(scrollPane, BorderLayout.CENTER);

        JPanel navigationPanel = new JPanel();
        navigationPanel.setBackground(new Color(249, 249, 249));
        navigationPanel.setLayout(new BoxLayout(navigationPanel, BoxLayout.X_AXIS));
        navigationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        navigationPanel.add(createIconButton("img/icons/home.png", "home"));
        navigationPanel.add(Box.createHorizontalGlue());
        navigationPanel.add(createIconButton("img/icons/search.png", "explore"));
        navigationPanel.add(Box.createHorizontalGlue());
        navigationPanel.add(createIconButton("img/icons/add.png", "add"));
        navigationPanel.add(Box.createHorizontalGlue());
        navigationPanel.add(createIconButton("img/icons/heart.png", "notification"));
        navigationPanel.add(Box.createHorizontalGlue());
        navigationPanel.add(createIconButton("img/icons/profile.png", "profile"));

        homePanel.add(navigationPanel, BorderLayout.SOUTH);

        cardPanel.add(homePanel, "Home");
        cardPanel.add(imageViewPanel, "ImageView");
        add(cardPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "Home");
    }

    private JPanel createPostPanel(Post post) {
        JPanel postPanel = new JPanel();
        postPanel.setLayout(new BoxLayout(postPanel, BoxLayout.Y_AXIS));
        postPanel.setBackground(Color.WHITE);
        postPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        postPanel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel usernameLabel = new JLabel(post.getUsername());
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        imageLabel.setPreferredSize(new Dimension(IMAGE_WIDTH, IMAGE_HEIGHT));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        try {
            BufferedImage originalImage = ImageIO.read(new File(post.getImagePath()));
            BufferedImage croppedImage = originalImage.getSubimage(
                    0, 0,
                    Math.min(originalImage.getWidth(), IMAGE_WIDTH),
                    Math.min(originalImage.getHeight(), IMAGE_HEIGHT));
            imageLabel.setIcon(new ImageIcon(croppedImage));
        } catch (IOException ex) {
            imageLabel.setText("Image not found");
        }

        JLabel captionLabel = new JLabel(post.getCaption());
        captionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel likesLabel = new JLabel("Likes: " + post.getLikes());
        likesLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton likeButton = new JButton(post.isLiked() ? "❤️" : "🤍");
        likeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        likeButton.setBackground(LIKE_BUTTON_COLOR);
        likeButton.setOpaque(true);
        likeButton.setBorderPainted(false);

        likeButton.addActionListener(e -> {
            try {
                handleLikeAction(post, likesLabel, likeButton);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating like: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        postPanel.add(usernameLabel);
        postPanel.add(imageLabel);
        postPanel.add(captionLabel);
        postPanel.add(likesLabel);
        postPanel.add(likeButton);

        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                displayPostDetails(post);
            }
        });

        return postPanel;
    }

    private void handleLikeAction(Post post, JLabel likesLabel, JButton likeButton) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            if (post.isLiked()) {
                String deleteQuery = "DELETE FROM `LIKE` WHERE username = ? AND imagePath = ?";
                try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
                    stmt.setString(1, currentUser.getUsername());
                    stmt.setString(2, post.getImagePath());
                    stmt.executeUpdate();
                }
                post.setLikes(post.getLikes() - 1);
                post.setLiked(false);
                likeButton.setText("🤍");
            } else {
                String insertQuery = "INSERT INTO `LIKE` (username, imagePath) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setString(1, currentUser.getUsername());
                    stmt.setString(2, post.getImagePath());
                    stmt.executeUpdate();
                }
                post.setLikes(post.getLikes() + 1);
                post.setLiked(true);
                likeButton.setText("❤️");

                createNotification(post.getUsername(), "like", post.getImagePath());
            }
            likesLabel.setText("Likes: " + post.getLikes());
        }
    }

    private void createNotification(String username, String sourceType, String sourceId) throws SQLException {
        String text = "";
        switch (sourceType) {
            case "like":
                text = currentUser.getUsername() + " liked your post";
                break;
        }

        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "INSERT INTO NOTIFICATION (username, text, sourceType, sourceId) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, text);
                stmt.setString(3, sourceType);
                stmt.setString(4, sourceId);
                stmt.executeUpdate();
            }
        }
    }

    private void displayPostDetails(Post post) {
        imageViewPanel.removeAll();

        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        JLabel usernameLabel = new JLabel(post.getUsername());
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        userPanel.add(usernameLabel);

        JLabel fullSizeImageLabel = new JLabel();
        fullSizeImageLabel.setHorizontalAlignment(JLabel.CENTER);
        try {
            BufferedImage originalImage = ImageIO.read(new File(post.getImagePath()));
            BufferedImage croppedImage = originalImage.getSubimage(
                    0, 0,
                    Math.min(originalImage.getWidth(), WIDTH-20),
                    Math.min(originalImage.getHeight(), HEIGHT-40));
            fullSizeImageLabel.setIcon(new ImageIcon(croppedImage));
        } catch (IOException ex) {
            fullSizeImageLabel.setText("Image not found");
        }

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.add(new JLabel(post.getCaption()));
        JLabel likesLabel = new JLabel("Likes: " + post.getLikes());
        infoPanel.add(likesLabel);

        JButton likeButton = new JButton(post.isLiked() ? "❤️" : "🤍");
        likeButton.setBackground(LIKE_BUTTON_COLOR);
        likeButton.setOpaque(true);
        likeButton.setBorderPainted(false);
        likeButton.addActionListener(e -> {
            try {
                handleLikeAction(post, likesLabel, likeButton);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error updating like: " + ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        infoPanel.add(likeButton);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Home"));

        imageViewPanel.add(userPanel, BorderLayout.NORTH);
        imageViewPanel.add(fullSizeImageLabel, BorderLayout.CENTER);
        imageViewPanel.add(infoPanel, BorderLayout.SOUTH);
        imageViewPanel.add(backButton, BorderLayout.PAGE_END);

        imageViewPanel.revalidate();
        imageViewPanel.repaint();
        cardLayout.show(cardPanel, "ImageView");
    }

    private JButton createIconButton(String iconPath, String buttonType) {
        ImageIcon iconOriginal = new ImageIcon(iconPath);
        Image iconScaled = iconOriginal.getImage().getScaledInstance(NAV_ICON_SIZE, NAV_ICON_SIZE, Image.SCALE_SMOOTH);
        JButton button = new JButton(new ImageIcon(iconScaled));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);

        switch (buttonType) {
            case "home":
                button.addActionListener(e -> openHomeUI(currentUser));
                break;
            case "profile":
                button.addActionListener(e -> openProfileUI(currentUser));
                break;
            case "notification":
                button.addActionListener(e -> notificationsUI(currentUser));
                break;
            case "explore":
                button.addActionListener(e -> exploreUI(currentUser));
                break;
            case "add":
                button.addActionListener(e -> ImageUploadUI(currentUser));
                break;
        }
        return button;
    }

    private void openProfileUI(User user) {
        this.dispose();
        new QuakstagramProfileUI(user).setVisible(true);
    }

    private void notificationsUI(User user) {
        this.dispose();
        new NotificationsUI(user).setVisible(true);
    }

    private void ImageUploadUI(User user) {
        this.dispose();
        new ImageUploadUI(user).setVisible(true);
    }

    private void openHomeUI(User user) {
        this.dispose();
        new QuakstagramHomeUI(user).setVisible(true);
    }

    private void exploreUI(User user) {
        this.dispose();
        new ExploreUI(user).setVisible(true);
    }

    private static class Post {
        private final String username;
        private final String caption;
        private int likes;
        private final String imagePath;
        private boolean isLiked;

        public Post(String username, String caption, int likes, String imagePath, boolean isLiked) {
            this.username = username;
            this.caption = caption;
            this.likes = likes;
            this.imagePath = imagePath;
            this.isLiked = isLiked;
        }

        public String getUsername() { return username; }
        public String getCaption() { return caption; }
        public int getLikes() { return likes; }
        public String getImagePath() { return imagePath; }
        public boolean isLiked() { return isLiked; }

        public void setLikes(int likes) { this.likes = likes; }
        public void setLiked(boolean liked) { isLiked = liked; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new RegularUser("testuser", "Test Bio", "password");
            new QuakstagramHomeUI(testUser).setVisible(true);
        });
    }
}