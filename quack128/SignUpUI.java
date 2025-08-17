import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignUpUI extends JFrame {

    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final String PROFILE_PHOTO_PATH = "img/storage/profile/";

    private JTextField txtUsername, txtPassword, txtBio;
    private JLabel lblPhoto;
    private File selectedProfilePicture;

    public SignUpUI() {
        setTitle("Quackstagram - Register");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        add(createHeader(), BorderLayout.NORTH);
        add(createFieldsPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.CENTER));
        header.setBackground(new Color(51, 51, 51));

        JLabel lblTitle = new JLabel("Quackstagram 🐥");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitle.setForeground(Color.WHITE);

        header.add(lblTitle);
        header.setPreferredSize(new Dimension(WIDTH, 40));
        return header;
    }

    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        lblPhoto = new JLabel(new ImageIcon(new ImageIcon("img/logos/DACS.png")
                .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        lblPhoto.setPreferredSize(new Dimension(80, 80));

        txtUsername = createTextField("Username");
        txtPassword = createTextField("Password");
        txtBio = createTextField("Bio");

        JButton btnUploadPhoto = createButton("Upload Photo", this::handleProfilePictureUpload);

        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.add(lblPhoto);

        panel.add(Box.createVerticalStrut(10));
        panel.add(photoPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(txtUsername);
        panel.add(Box.createVerticalStrut(10));
        panel.add(txtPassword);
        panel.add(Box.createVerticalStrut(10));
        panel.add(txtBio);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btnUploadPhoto);

        return panel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Color.WHITE);

        JButton btnRegister = createButton("Register", this::handleRegistration);
        JButton btnSignIn = createButton("Already have an account? Sign In", this::openSignInUI);

        footer.add(btnRegister, BorderLayout.CENTER);
        footer.add(btnSignIn, BorderLayout.SOUTH);
        return footer;
    }

    private void handleRegistration(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String bio = txtBio.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and password are required", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (doesUsernameExist(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists. Choose a different username.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            saveUserToDatabase(username, password, bio);

            if (selectedProfilePicture != null) {
                saveProfilePicture(selectedProfilePicture, username);
            }

            dispose();

            SwingUtilities.invokeLater(() -> {
                SignInUI signInFrame = new SignInUI();
                signInFrame.setVisible(true);
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private boolean doesUsernameExist(String username) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "SELECT 1 FROM USER WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        }
    }

    private void saveUserToDatabase(String username, String password, String bio) throws SQLException {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String userQuery = "INSERT INTO USER (username, bio, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(userQuery)) {
                stmt.setString(1, username);
                stmt.setString(2, bio);
                stmt.setString(3, password);
                stmt.executeUpdate();
            }

            String regularUserQuery = "INSERT INTO REGULAR_USER (username) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(regularUserQuery)) {
                stmt.setString(1, username);
                stmt.executeUpdate();
            }
        }
    }

    private void handleProfilePictureUpload(ActionEvent event) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedProfilePicture = fileChooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedProfilePicture);
                ImageIcon icon = new ImageIcon(image.getScaledInstance(80, 80, Image.SCALE_SMOOTH));
                lblPhoto.setIcon(icon);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveProfilePicture(File file, String username) {
        try {
            new File(PROFILE_PHOTO_PATH).mkdirs();

            BufferedImage image = ImageIO.read(file);
            File outputFile = new File(PROFILE_PHOTO_PATH + username + ".png");
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving profile picture: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openSignInUI(ActionEvent event) {
        dispose();
        SwingUtilities.invokeLater(() -> {
            SignInUI signInFrame = new SignInUI();
            signInFrame.setVisible(true);
        });
    }

    private JTextField createTextField(String placeholder) {
        JTextField field = new JTextField(placeholder);
        field.setForeground(Color.GRAY);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        return field;
    }

    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        button.setBackground(new Color(255, 90, 95));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SignUpUI signUpFrame = new SignUpUI();
            signUpFrame.setVisible(true);
        });
    }
}