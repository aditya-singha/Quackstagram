import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class NotificationsUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final int NAV_ICON_SIZE = 20;
    private final User currentUser;

    public NotificationsUI(User user) {
        currentUser = user;
        setTitle("Notifications");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        initializeUI();
    }

    private void initializeUI() {
        JPanel headerPanel = createHeaderPanel();
        JPanel navigationPanel = createNavigationPanel();

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT text, sourceType, sourceId, timestamp FROM NOTIFICATION WHERE username = ? ORDER BY timestamp DESC")) {
            stmt.setString(1, currentUser.getUsername());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String text = rs.getString("text");
                    String sourceType = rs.getString("sourceType");
                    String sourceId = rs.getString("sourceId");
                    String timestamp = rs.getString("timestamp");
                    String notificationMessage = formatNotificationMessage(text, sourceType, sourceId, timestamp);

                    JPanel notificationPanel = new JPanel(new BorderLayout());
                    notificationPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    JLabel notificationLabel = new JLabel(notificationMessage);
                    notificationPanel.add(notificationLabel, BorderLayout.CENTER);

                    contentPanel.add(notificationPanel);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading notifications: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Error loading notifications: " + e.getMessage());
        }

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);
    }

    private String formatNotificationMessage(String text, String sourceType, String sourceId, String timestamp) {
        String elapsedTime = getElapsedTime(timestamp);
        switch (sourceType) {
            case "like":
                return sourceId + " liked your picture - " + elapsedTime + " ago";
            case "comment":
                return sourceId + " commented on your picture - " + elapsedTime + " ago";
            case "follow":
                return text + " - " + elapsedTime + " ago";
            default:
                return text + " - " + elapsedTime + " ago";
        }
    }

    private String getElapsedTime(String timestamp) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime timeOfNotification = LocalDateTime.parse(timestamp, formatter);
            LocalDateTime currentTime = LocalDateTime.now();

            long daysBetween = ChronoUnit.DAYS.between(timeOfNotification, currentTime);
            long minutesBetween = ChronoUnit.MINUTES.between(timeOfNotification, currentTime) % 60;

            StringBuilder timeElapsed = new StringBuilder();
            if (daysBetween > 0) {
                timeElapsed.append(daysBetween).append(" day").append(daysBetween > 1 ? "s" : "");
            }
            if (minutesBetween > 0) {
                if (daysBetween > 0) {
                    timeElapsed.append(" and ");
                }
                timeElapsed.append(minutesBetween).append(" minute").append(minutesBetween > 1 ? "s" : "");
            }
            if (timeElapsed.length() == 0) {
                timeElapsed.append("just now");
            }
            return timeElapsed.toString();
        } catch (Exception e) {
            return "unknown time";
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(51, 51, 51));
        JLabel lblRegister = new JLabel("Notifications 🐥");
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