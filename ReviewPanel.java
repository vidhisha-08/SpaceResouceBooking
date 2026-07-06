package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ReviewPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    
    private final int userId; 
    
    private final Management management = new Management(); 
    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);

    // Form Components
    private JTextField itemIdField;
    private JComboBox<String> itemTypeComboBox;
    private JComboBox<Integer> ratingComboBox;
    private JTextArea commentArea;

    public ReviewPanel(int userId) {
      
        this.userId = userId;
        
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 30, 20, 30));
        setBackground(BACKGROUND_GRAY);

        JLabel header = new JLabel("Submit a Review (Space or Resource)", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        add(createReviewForm(), BorderLayout.CENTER);
    }
    
   
    private JPanel createReviewForm() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE); 
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // 1. Item Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.2; // Give label column less width
        formPanel.add(new JLabel("Type of Item:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.8; // Give field column more width
        itemTypeComboBox = new JComboBox<>(new String[]{"Space", "Resource"});
        formPanel.add(itemTypeComboBox, gbc);

        // 2. Item ID
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Item ID:"), gbc);
        
        gbc.gridx = 1;
        itemIdField = new JTextField(10);
        formPanel.add(itemIdField, gbc);

        // 3. Rating
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Rating (1-5):"), gbc);
        
        gbc.gridx = 1;
        ratingComboBox = new JComboBox<>(new Integer[]{5, 4, 3, 2, 1});
        formPanel.add(ratingComboBox, gbc);

        // 4. Comment Area
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        formPanel.add(new JLabel("Comment:"), gbc);

        gbc.gridy = 4;
        commentArea = new JTextArea(5, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        formPanel.add(scrollPane, gbc);

        // 5. Submit Button
        gbc.gridy = 5;
        gbc.insets = new Insets(20, 8, 8, 8);
        JButton submitButton = new JButton("Submit Review");
        submitButton.setBackground(PRIMARY_NAVY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        submitButton.addActionListener(e -> submitReview());
        formPanel.add(submitButton, gbc);
        
        // Add the formPanel to the center of the main panel
        // This ensures it doesn't stretch to fill the whole window
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(BACKGROUND_GRAY);
        centerWrapper.add(formPanel);
        return centerWrapper;
    }
    
    private void submitReview() {
        // *** This check is now valid because userId is set in the constructor ***
        if (this.userId <= 0) {
             JOptionPane.showMessageDialog(this, "User ID not set. Cannot submit review.", "Internal Error", JOptionPane.ERROR_MESSAGE);
             return;
        }
        
        try {
            int itemId = Integer.parseInt(itemIdField.getText().trim());
            String itemType = (String) itemTypeComboBox.getSelectedItem();
            int rating = (Integer) ratingComboBox.getSelectedItem();
            String comment = commentArea.getText().trim();
            
            String result;

            if (comment.isEmpty()) {
                 JOptionPane.showMessageDialog(this, "A comment is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            if ("Space".equals(itemType)) {
                result = management.addReview(userId, itemId, null, rating, comment);
            } else { // "Resource"
                result = management.addReview(userId, null, itemId, rating, comment);
            }

            if (result.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(this, result, "Success", JOptionPane.INFORMATION_MESSAGE);
                itemIdField.setText("");
                commentArea.setText("");
                ratingComboBox.setSelectedIndex(0);
            } else {
                String displayMessage = result.replace("REVIEW FAILED: ", "Error: ").replace("DATABASE ERROR: ", "Error: ");
                JOptionPane.showMessageDialog(this, 
                    displayMessage, 
                    "Submission Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for the Item ID.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) { 
             JOptionPane.showMessageDialog(this, "Error submitting review: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
        }
    }
}