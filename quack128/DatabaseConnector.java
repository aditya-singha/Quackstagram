import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/quack?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "BCS1510";
    private static final String DB_PASSWORD = "BCS1510";

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }
}