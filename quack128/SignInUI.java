import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SignInUI extends JFrame {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 500;
    private static final String USERS_FILE = "data/users.txt";

    private JTextField txtUsername;
    private JTextField txtPassword;
    private JButton btnSignIn, btnRegisterNow;
    private JLabel lblPhoto;
    private User newUser;
    private PasswordManager passwordManager;
    private UserFactory regularUserFactory = new RegularUserFactory();

    public SignInUI() {
        setTitle("Quackstagram - Sign In");
        setSize(WIDTH, HEIGHT);
        setMinimumSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        passwordManager = new PasswordManager(new PasswordAuthentication());
        initializeUI();
    }

    private void initializeUI() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(51, 51, 51));
        JLabel lblRegister = new JLabel("Quackstagram 🐥");
        lblRegister.setFont(new Font("Arial", Font.BOLD, 16));
        lblRegister.setForeground(Color.WHITE);
        headerPanel.add(lblRegister);
        headerPanel.setPreferredSize(new Dimension(WIDTH, 40));

        lblPhoto = new JLabel(new ImageIcon(new ImageIcon("img/logos/DACS.png")
                .getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        JPanel photoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        photoPanel.add(lblPhoto);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        fieldsPanel.add(photoPanel);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(txtUsername);
        fieldsPanel.add(Box.createVerticalStrut(10));
        fieldsPanel.add(txtPassword);

        btnSignIn = new JButton("Sign-In");
        btnSignIn.addActionListener(this::onSignInClicked);
        btnSignIn.setBackground(new Color(255, 90, 95));
        btnSignIn.setForeground(Color.BLACK);
        styleButton(btnSignIn);

        btnRegisterNow = new JButton("No Account? Register Now");
        btnRegisterNow.addActionListener(this::onRegisterNowClicked);
        btnRegisterNow.setBackground(Color.WHITE);
        styleButton(btnRegisterNow);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(btnSignIn);
        buttonPanel.add(btnRegisterNow);

        add(headerPanel, BorderLayout.NORTH);
        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onSignInClicked(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (passwordManager.verifyCredentials(username, password)) {
            try {
                try (Connection conn = new DatabaseConnector().getConnection()) {
                    String query = "SELECT bio FROM USER WHERE username = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setString(1, username);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            String bio = rs.getString("bio");
                            newUser = regularUserFactory.createUser(username, bio, password);
                            newUser.loadPictures();
                            newUser.loadCounts();
                        }
                    }
                }

                dispose();
                SwingUtilities.invokeLater(() -> new QuakstagramProfileUI(newUser).setVisible(true));
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegisterNowClicked(ActionEvent event) {
        dispose();
        SwingUtilities.invokeLater(() -> new SignUpUI().setVisible(true));
    }

    private void saveUserInformation(User user) {
        try (Connection conn = new DatabaseConnector().getConnection()) {
            String query = "INSERT INTO USER (username, bio, password) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getBio());
                stmt.setString(3, user.getPassword());
                stmt.executeUpdate();
            }
            String regularUserQuery = "INSERT INTO REGULAR_USER (username) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(regularUserQuery)) {
                stmt.setString(1, user.getUsername());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void styleButton(JButton button) {
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SignInUI().setVisible(true));
    }
}
