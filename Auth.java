package SpaceResourceBooking;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Auth {

    /**
     * Attempts to log in a user by verifying credentials against the stored hash.
     * @param email The user's email.
     * @param password The raw password input.
     * @return The User object if successful, null otherwise.
     */
    public User login(String email, String password) {
        String hashedInput = AuthUtil.hashPassword(password);
        if (hashedInput == null) {
            System.err.println("Login Failed: Hashing algorithm issue.");
            return null;
        }
        
        // Removed password_hash from SELECT as it's not needed for the constructor, 
        // but included for clarity in the refactored User constructor.
        String sql = "SELECT user_id, first_name, last_name, email, password_hash, role FROM USER WHERE email = ?";
        Connection conn = null;
        User user = null;

        try {
            conn = DBManager.getConnection();
            if (conn == null) return null;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password_hash");
                        
                        // Compare the calculated hash of the input password with the stored hash
                        if (storedHash.equals(hashedInput)) {
                            // Successfully authenticated. Create and return the User object.
                            user = new User(
                                rs.getInt("user_id"), 
                                rs.getString("first_name"), 
                                rs.getString("last_name"), 
                                rs.getString("email"), 
                                storedHash, // passwordHash
                                rs.getString("role")
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Login Database Error: " + e.getMessage());
        } finally {
            DBManager.closeConnection(conn);
        }
        return user;
    }

    /**
     * Registers a new user with the default 'user' role.
     * @param firstName User's first name.
     * @param lastName User's last name.
     * @param email User's email (must be unique).
     * @param password User's raw password.
     * @return true if sign up was successful, false otherwise.
     */
    public boolean signUp(String firstName, String lastName, String email, String password) {
        String hashedPwd = AuthUtil.hashPassword(password);
        if (hashedPwd == null) return false;

        // Note: The database should handle setting the default role and creation date.
        String sql = "INSERT INTO USER (first_name, last_name, email, password_hash, role) VALUES (?, ?, ?, ?, 'user')";
        Connection conn = null;
        
        try {
            conn = DBManager.getConnection();
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, firstName);
                ps.setString(2, lastName);
                ps.setString(3, email);
                ps.setString(4, hashedPwd);
                
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            // MySQL error code for duplicate entry (Unique constraint violation)
            if (e.getErrorCode() == 1062) { 
                System.out.println("Signup Failed: That email address is already registered.");
            } else {
                System.err.println("Signup Database Error: " + e.getMessage());
            }
            return false;
        } finally {
            DBManager.closeConnection(conn);
        }
    }
}

/**
 * Utility class for secure password hashing using SHA-256.
 */
class AuthUtil {

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Convert byte array to a 64-character hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                // Ensure the byte is treated as unsigned and formatted with leading zero if necessary
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // This should ideally never happen on modern JVMs
            System.err.println("Fatal Hashing Error: SHA-256 algorithm not found.");
            e.printStackTrace();
            return null;
        }
    }
}