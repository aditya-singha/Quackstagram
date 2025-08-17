import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class ExploreUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final int NAV_ICON_SIZE = 20;
    private static final int IMAGE_SIZE = WIDTH / 3;
    private final User currentUser;

    public ExploreUI(User user) {
        currentUser = user;
        setTitle("Explore");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        JPanel headerPanel = createHeaderPanel();
        JPanel navigationPanel = createNavigationPanel();
        JPanel mainContentPanel = createMainContentPanel();

        add(headerPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JPanel createMainContentPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField(" Search Users");
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, searchField.getPreferredSize().height));

        JPanel imageGridPanel = new JPanel(new GridLayout(0, 3, 2, 2));

        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT imagePath FROM PICTURE ORDER BY createdAt DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String imagePath = rs.getString("imagePath");
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    ImageIcon imageIcon = new ImageIcon(new ImageIcon(imagePath).getImage().getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH));
                    JLabel imageLabel = new JLabel(imageIcon);
                    imageLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            displayImage(imagePath);
                        }
                    });
                    imageGridPanel.add(imageLabel);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading images: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading images: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(imageGridPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.add(searchPanel);
        mainContentPanel.add(scrollPane);
        return mainContentPanel;
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(51, 51, 51));
        JLabel lblRegister = new JLabel("Explore 🐥");
        lblRegister.setFont(new Font("Arial", Font.BOLD, 16));
        lblRegister.setForeground(Color.WHITE);
        headerPanel.add(lblRegister);
        headerPanel.setPreferredSize(new Dimension(WIDTH, 40));
        return headerPanel;
    }

    private JPanel createNavigationPanel() {
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

        return navigationPanel;
    }

    private void displayImage(String imagePath) {
        getContentPane().removeAll();
        setLayout(new BorderLayout());

        String username = "";
        String caption = "";
        String timestampString = "";
        int likes = 0;
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.username, p.caption, p.createdAt, COUNT(l.username) AS likes " +
                             "FROM PICTURE p LEFT JOIN `LIKE` l ON p.imagePath = l.imagePath " +
                             "WHERE p.imagePath = ? GROUP BY p.imagePath")) {
            stmt.setString(1, imagePath);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    username = rs.getString("username");
                    caption = rs.getString("caption");
                    timestampString = rs.getString("createdAt");
                    likes = rs.getInt("likes");
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading image details: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading image details: " + e.getMessage());
        }

        String bio = getPosterBio(username);
        String timeSincePosting = "Unknown";
        if (!timestampString.isEmpty()) {
            try {
                LocalDateTime timestamp = LocalDateTime.parse(timestampString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime now = LocalDateTime.now();
                long days = ChronoUnit.DAYS.between(timestamp, now);
                timeSincePosting = days + " day" + (days != 1 ? "s" : "") + " ago";
            } catch (Exception e) {
                System.err.println("Error parsing timestamp: " + e.getMessage());
            }
        }

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JLabel usernameLabel = new JLabel(username);
        int posterImageCount = getPosterPostCount(username);
        int posterFollowersCount = getPosterFollowerCount(username);
        int posterFollowingCount = getPosterFollowingCount(username);
        usernameLabel.setToolTipText("<html>Bio: " + bio + "<br>Posts: " + posterImageCount + "<br>Following: " + posterFollowingCount + "<br>Followers: " + posterFollowersCount + "</html>");
        JButton followButton = new JButton("Follow");
        setupFollowButton(followButton, username);
        userPanel.add(usernameLabel);
        userPanel.add(followButton);

        JLabel timeLabel = new JLabel(timeSincePosting);
        timeLabel.setHorizontalAlignment(JLabel.RIGHT);
        topPanel.add(userPanel, BorderLayout.WEST);
        topPanel.add(timeLabel, BorderLayout.EAST);

        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        try {
            BufferedImage originalImage = ImageIO.read(new File(imagePath));
            ImageIcon imageIcon = new ImageIcon(originalImage);
            imageLabel.setIcon(imageIcon);
        } catch (IOException ex) {
            imageLabel.setText("Image not found");
            System.err.println("Image not found: " + imagePath);
        }

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JTextArea captionTextArea = new JTextArea(caption);
        captionTextArea.setEditable(false);
        captionTextArea.setLineWrap(true);
        captionTextArea.setWrapStyleWord(true);
        JLabel likesLabel = new JLabel("Likes: " + likes);
        bottomPanel.add(captionTextArea, BorderLayout.CENTER);
        bottomPanel.add(likesLabel, BorderLayout.SOUTH);

        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(WIDTH - 20, backButton.getPreferredSize().height));
        backButtonPanel.add(backButton);

        backButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                getContentPane().removeAll();
                add(createHeaderPanel(), BorderLayout.NORTH);
                add(createMainContentPanel(), BorderLayout.CENTER);
                add(createNavigationPanel(), BorderLayout.SOUTH);
                revalidate();
                repaint();
            });
        });

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.add(topPanel, BorderLayout.NORTH);
        containerPanel.add(imageLabel, BorderLayout.CENTER);
        containerPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(backButtonPanel, BorderLayout.NORTH);
        add(containerPanel, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void setupFollowButton(JButton followButton, String username) {
        followButton.addActionListener(e -> handleFollowButtonClick(followButton, username));
    }

    private void handleFollowButtonClick(JButton followButton, String username) {
        boolean isFollowing = isFollowing(username);
        if (isFollowing || currentUser.getUsername().equals(username)) {
            JOptionPane.showMessageDialog(followButton, "Cannot follow " + username + ". You may already be following them or trying to follow yourself.");
        } else {
            try {
                new UserRelationshipManager().followUser(currentUser.getUsername(), username);
                JOptionPane.showMessageDialog(followButton, "Now following " + username);
                followButton.setEnabled(false);
                followButton.setText("Following");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(followButton, "Error following " + username + ": " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error following user: " + e.getMessage());
            }
        }
    }

    private boolean isFollowing(String poster) {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM FOLLOW WHERE follower = ? AND followed = ?")) {
            stmt.setString(1, currentUser.getUsername());
            stmt.setString(2, poster);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking follow status: " + e.getMessage());
            return false;
        }
    }

    private int getPosterPostCount(String poster) {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM PICTURE WHERE username = ?")) {
            stmt.setString(1, poster);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching post count: " + e.getMessage());
        }
        return 0;
    }

    private int getPosterFollowerCount(String poster) {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM FOLLOW WHERE followed = ?")) {
            stmt.setString(1, poster);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching follower count: " + e.getMessage());
        }
        return 0;
    }

    private int getPosterFollowingCount(String poster) {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM FOLLOW WHERE follower = ?")) {
            stmt.setString(1, poster);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching following count: " + e.getMessage());
        }
        return 0;
    }

    private String getPosterBio(String poster) {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT bio FROM USER WHERE username = ?")) {
            stmt.setString(1, poster);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("bio");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching bio: " + e.getMessage());
        }
        return "";
    }

    private JButton createIconButton(String iconPath, String buttonType) {
        ImageIcon iconOriginal;
        try {
            iconOriginal = new ImageIcon(iconPath);
        } catch (Exception e) {
            iconOriginal = new ImageIcon();
            System.err.println("Icon not found: " + iconPath);
        }
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
                button.addActionListener(e -> imageUploadUI(currentUser));
                break;
        }
        return button;
    }

    private void imageUploadUI(User user) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new ImageUploadUI(user).setVisible(true);
        });
    }

    private void openProfileUI(User user) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new QuakstagramProfileUI(user).setVisible(true);
        });
    }

    private void notificationsUI(User user) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new NotificationsUI(user).setVisible(true);
        });
    }

    private void openHomeUI(User user) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new QuakstagramHomeUI(user).setVisible(true);
        });
    }

    private void exploreUI(User user) {
        SwingUtilities.invokeLater(() -> {
            dispose();
            new ExploreUI(user).setVisible(true);
        });
    }
}