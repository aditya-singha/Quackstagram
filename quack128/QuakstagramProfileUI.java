import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuakstagramProfileUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final int PROFILE_IMAGE_SIZE = 80;
    private static final int GRID_IMAGE_SIZE = WIDTH / 3;
    private static final int NAV_ICON_SIZE = 20;
    private JPanel contentPanel;
    private JPanel headerPanel;
    private JPanel navigationPanel;
    private User currentUser;

    public QuakstagramProfileUI(User user) {
        this.currentUser = user;
        setTitle("Quakstagram Profile");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            loadUserData();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        contentPanel = new JPanel();
        headerPanel = createHeaderPanel();
        navigationPanel = createNavigationPanel();

        initializeUI();
    }

    private void loadUserData() throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String userQuery = "SELECT bio FROM USER WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, currentUser.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentUser.setBio(rs.getString("bio"));
                }
            }
            String followersQuery = "SELECT COUNT(*) FROM FOLLOW WHERE followed = ?";
            try (PreparedStatement stmt = conn.prepareStatement(followersQuery)) {
                stmt.setString(1, currentUser.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentUser.setFollowersCount(rs.getInt(1));
                }
            }

            String followingQuery = "SELECT COUNT(*) FROM FOLLOW WHERE follower = ?";
            try (PreparedStatement stmt = conn.prepareStatement(followingQuery)) {
                stmt.setString(1, currentUser.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentUser.setFollowingCount(rs.getInt(1));
                }
            }

            String postsQuery = "SELECT COUNT(*) FROM PICTURE WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(postsQuery)) {
                stmt.setString(1, currentUser.getUsername());
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    currentUser.setPostsCount(rs.getInt(1));
                }
            }
        }
    }

    private void initializeUI() {
        getContentPane().removeAll();
        add(headerPanel, BorderLayout.NORTH);
        add(navigationPanel, BorderLayout.SOUTH);
        initializeImageGrid();
        revalidate();
        repaint();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(249, 249, 249));

        JPanel topHeaderPanel = new JPanel(new BorderLayout(10, 0));
        topHeaderPanel.setBackground(new Color(249, 249, 249));

        ImageIcon profileIcon = new ImageIcon(new ImageIcon("img/storage/profile/" +
                currentUser.getUsername() + ".png").getImage()
                .getScaledInstance(PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE, Image.SCALE_SMOOTH));
        JLabel profileImage = new JLabel(profileIcon);
        profileImage.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topHeaderPanel.add(profileImage, BorderLayout.WEST);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
        statsPanel.setBackground(new Color(249, 249, 249));
        statsPanel.add(createStatLabel(Integer.toString(currentUser.getPostsCount()), "Posts"));
        statsPanel.add(createStatLabel(Integer.toString(currentUser.getFollowersCount()), "Followers"));
        statsPanel.add(createStatLabel(Integer.toString(currentUser.getFollowingCount()), "Following"));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 0));

        topHeaderPanel.add(statsPanel, BorderLayout.CENTER);
        headerPanel.add(topHeaderPanel);

        JPanel profileNameAndBioPanel = new JPanel();
        profileNameAndBioPanel.setLayout(new BorderLayout());
        profileNameAndBioPanel.setBackground(new Color(249, 249, 249));

        JLabel profileNameLabel = new JLabel(currentUser.getUsername());
        profileNameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        profileNameLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        JTextArea profileBio = new JTextArea(currentUser.getBio());
        profileBio.setEditable(false);
        profileBio.setFont(new Font("Arial", Font.PLAIN, 12));
        profileBio.setBackground(new Color(249, 249, 249));
        profileBio.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        profileNameAndBioPanel.add(profileNameLabel, BorderLayout.NORTH);
        profileNameAndBioPanel.add(profileBio, BorderLayout.CENTER);
        headerPanel.add(profileNameAndBioPanel);

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

    private void initializeImageGrid() {
        contentPanel.removeAll();
        contentPanel.setLayout(new GridLayout(0, 3, 5, 5));

        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT imagePath FROM PICTURE WHERE username = ? ORDER BY createdAt DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, currentUser.getUsername());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String imagePath = rs.getString("imagePath");
                    ImageIcon imageIcon = new ImageIcon(new ImageIcon(imagePath).getImage()
                            .getScaledInstance(GRID_IMAGE_SIZE, GRID_IMAGE_SIZE, Image.SCALE_SMOOTH));
                    JLabel imageLabel = new JLabel(imageIcon);

                    imageLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            displayImage(imageIcon);
                        }
                    });
                    contentPanel.add(imageLabel);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading images: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void displayImage(ImageIcon imageIcon) {
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

        JLabel fullSizeImageLabel = new JLabel(imageIcon);
        fullSizeImageLabel.setHorizontalAlignment(JLabel.CENTER);
        contentPanel.add(fullSizeImageLabel, BorderLayout.CENTER);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> initializeUI());
        contentPanel.add(backButton, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private JLabel createStatLabel(String number, String text) {
        JLabel label = new JLabel("<html><div style='text-align: center;'>" +
                number + "<br/>" + text + "</div></html>", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.BLACK);
        return label;
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

    private void ImageUploadUI(User user) {
        this.dispose();
        new ImageUploadUI(user).setVisible(true);
    }

    private void openProfileUI(User user) {
        this.dispose();
        new QuakstagramProfileUI(user).setVisible(true);
    }

    private void notificationsUI(User user) {
        this.dispose();
        new NotificationsUI(user).setVisible(true);
    }

    private void openHomeUI(User user) {
        this.dispose();
        new QuakstagramHomeUI(user).setVisible(true);
    }

    private void exploreUI(User user) {
        this.dispose();
        new ExploreUI(user).setVisible(true);
    }
}