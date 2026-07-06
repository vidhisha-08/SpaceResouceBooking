package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class AuthGUI extends JFrame {

    /**
	 *	
	 */
	private static final long serialVersionUID = 1L;
	private final Auth auth = new Auth();
    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);
    
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);

    public AuthGUI() {
        setTitle("Book YOUR Space With US");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createSignupPanel(), "Signup");
        
        add(mainPanel);
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY); 
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // --- Components ---
        // Changed label from "Username" to "Email" for clarity since Auth uses email
        JTextField emailField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Get Started");
        JButton createAccountButton = new JButton("Create Account");

        // UI styling
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), new EmptyBorder(10, 30, 10, 10)));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK), new EmptyBorder(10, 30, 10, 10)));
        loginButton.setBackground(PRIMARY_NAVY);
        loginButton.setForeground(Color.WHITE);
        createAccountButton.setContentAreaFilled(false);
        createAccountButton.setBorderPainted(false);
        createAccountButton.setForeground(PRIMARY_NAVY);
        
        // Login Action
        loginButton.addActionListener(e -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            User user = auth.login(email, password);
            
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Login Successful! Welcome, " + user.getFirstName() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose(); // Close login frame
                try {
                    // *** FIX: Handled the SQLException required by MainApplicationGUI constructor ***
					new MainApplicationGUI(user).setVisible(true);
				} catch (SQLException e1) {
                    System.err.println("Failed to start Main Application due to database error: " + e1.getMessage());
					JOptionPane.showMessageDialog(this, "A database error occurred while starting the main application.", "Error", JOptionPane.ERROR_MESSAGE);
				} 
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        createAccountButton.addActionListener(e -> cardLayout.show(mainPanel, "Signup"));

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; panel.add(passwordField, gbc);
        gbc.gridx = 1; gbc.gridy = 2; panel.add(loginButton, gbc);
        gbc.gridx = 1; gbc.gridy = 3; panel.add(createAccountButton, gbc);
        
        return panel;
    }

    private JPanel createSignupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.LIGHT_GRAY); 	 	
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // --- Components ---
        JTextField firstNameField = new JTextField(15);
        JTextField lastNameField = new JTextField(15);
        JTextField emailField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPasswordField confirmPasswordField = new JPasswordField(15);
        
        JButton signupButton = new JButton("Sign Up"); // Changed button text for clarity
        JButton backToLoginButton = new JButton("Back to Login");
        
        // UI Styling
        signupButton.setBackground(PRIMARY_NAVY);
        signupButton.setForeground(Color.WHITE);
        backToLoginButton.setContentAreaFilled(false);
        backToLoginButton.setBorderPainted(false);
        backToLoginButton.setForeground(PRIMARY_NAVY);

        signupButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();
            
            if (email.isEmpty() || password.isEmpty() || firstNameField.getText().trim().isEmpty() || lastNameField.getText().trim().isEmpty()) {
                 JOptionPane.showMessageDialog(this, "All fields are required.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = auth.signUp(
                firstNameField.getText().trim(), 
                lastNameField.getText().trim(), 
                email, 
                password
            );
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Signup Successful! Please log in.", "Success", JOptionPane.INFORMATION_MESSAGE);
                // Clear fields
                firstNameField.setText("");
                lastNameField.setText("");
                emailField.setText("");
                passwordField.setText("");
                confirmPasswordField.setText("");
                
                cardLayout.show(mainPanel, "Login");
            } else {
                JOptionPane.showMessageDialog(this, "Signup Failed. This email may already be in use.", "Signup Failed", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Switch to Login
        backToLoginButton.addActionListener(e -> cardLayout.show(mainPanel, "Login"));

        // Layout
        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("First Name:"), gbc); gbc.gridx = 1; panel.add(firstNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Last Name:"), gbc); gbc.gridx = 1; panel.add(lastNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email:"), gbc); gbc.gridx = 1; panel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Password:"), gbc); gbc.gridx = 1; panel.add(passwordField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Confirm Password:"), gbc); gbc.gridx = 1; panel.add(confirmPasswordField, gbc);
        gbc.gridx = 1; gbc.gridy = 5; panel.add(signupButton, gbc);
        gbc.gridx = 1; gbc.gridy = 6; panel.add(backToLoginButton, gbc);

        return panel;
    }

    /**
     * The main entry point for the GUI application.
     */
    public static void main(String[] args) {
        // Start the Swing GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            AuthGUI authFrame = new AuthGUI(); 
            authFrame.setVisible(true);
        });
    }
}