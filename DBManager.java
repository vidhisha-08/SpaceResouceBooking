package SpaceResourceBooking;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {

    // 1. Database Credentials
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/spacebooking"; // Use your actual database name
    private static final String USERNAME = "root"; // e.g., "root"
    private static final String PASSWORD = "Shreya@11"; // e.g., "password123"
    
    // The driver class name (required for older versions, good practice to include)
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    public static Connection getConnection() {
        Connection connection = null;
        
        try {
            // Load the MySQL JDBC driver
            Class.forName(DRIVER_CLASS);
            
            // Establish the connection
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
            
            // System.out.println("Database connection established successfully.");
            
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found. Ensure the MySQL Connector/J JAR is in the classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database.");
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return connection;
    }
    
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                // System.out.println("Connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing the connection.");
                e.printStackTrace();
            }
        }
    }
}