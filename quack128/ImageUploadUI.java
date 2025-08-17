import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImageUploadUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final int NAV_ICON_SIZE = 20;
    private JLabel imagePreviewLabel;
    private JTextArea bioTextArea;
    private JButton uploadButton;
    private JButton saveButton;
    private final User currentUser;

    public ImageUploadUI(User user) {
        currentUser = user;
        setTitle("Upload Image");
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

        imagePreviewLabel = new JLabel();
        imagePreviewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imagePreviewLabel.setPreferredSize(new Dimension(WIDTH, HEIGHT / 3));
        ImageIcon emptyImageIcon = new ImageIcon();
        imagePreviewLabel.setIcon(emptyImageIcon);
        contentPanel.add(imagePreviewLabel);

        bioTextArea = new JTextArea("Enter a caption");
        bioTextArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        bioTextArea.setLineWrap(true);
        bioTextArea.setWrapStyleWord(true);
        JScrollPane bioScrollPane = new JScrollPane(bioTextArea);
        bioScrollPane.setPreferredSize(new Dimension(WIDTH - 50, HEIGHT / 6));
        contentPanel.add(bioScrollPane);

        uploadButton = new JButton("Upload Image");
        uploadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        uploadButton.addActionListener(this::uploadAction);
        contentPanel.add(uploadButton);

        saveButton = new JButton("Save Caption");
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveButton.addActionListener(this::saveBioAction);
        contentPanel.add(saveButton);

        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(navigationPanel, BorderLayout.SOUTH);
    }

    private void uploadAction(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an image file");
        fileChooser.setAcceptAllFileFilterUsed(false);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg");
        fileChooser.addChoosableFileFilter(filter);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String username = currentUser.getUsername();
                int imageId = getNextImageId(username);
                String fileExtension = getFileExtension(selectedFile);
                String newFileName = username + "_" + imageId + "." + fileExtension;

                Path destPath = Paths.get("img", "uploaded", newFileName);
                Files.createDirectories(destPath.getParent());
                Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);

                saveImageInfo(destPath.toString(), username, bioTextArea.getText());

                ImageIcon imageIcon = new ImageIcon(destPath.toString());
                if (imagePreviewLabel.getWidth() > 0 && imagePreviewLabel.getHeight() > 0) {
                    Image image = imageIcon.getImage();
                    int previewWidth = imagePreviewLabel.getWidth();
                    int previewHeight = imagePreviewLabel.getHeight();
                    int imageWidth = image.getWidth(null);
                    int imageHeight = image.getHeight(null);
                    double widthRatio = (double) previewWidth / imageWidth;
                    double heightRatio = (double) previewHeight / imageHeight;
                    double scale = Math.min(widthRatio, heightRatio);
                    int scaledWidth = (int) (scale * imageWidth);
                    int scaledHeight = (int) (scale * imageHeight);
                    imageIcon.setImage(image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH));
                }

                imagePreviewLabel.setIcon(imageIcon);
                uploadButton.setText("Upload Another Image");

                JOptionPane.showMessageDialog(this, "Image uploaded successfully!");
            } catch (IOException | SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error uploading image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error uploading image: " + ex.getMessage());
            }
        }
    }

    private int getNextImageId(String username) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM PICTURE WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) + 1;
                }
            }
        }
        return 1;
    }

    private void saveImageInfo(String imagePath, String username, String caption) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO PICTURE (imagePath, caption, username) VALUES (?, ?, ?)")) {
            stmt.setString(1, imagePath);
            stmt.setString(2, caption);
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }

    private void saveBioAction(ActionEvent event) {
        String caption = bioTextArea.getText();
        JOptionPane.showMessageDialog(this, "Caption saved: " + caption);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(51, 51, 51));
        JLabel lblRegister = new JLabel("Upload Image 🐥");
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
        navigationPanel.add(createIconButton("img/icons/add.png", ""));
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
        }
        return button;
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