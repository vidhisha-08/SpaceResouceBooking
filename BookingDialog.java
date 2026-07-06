package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for creating a new booking (space and/or resources)
 */
public class BookingDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
	private final Booking bookingDAO = new Booking();
    private final User loggedInUser;

    private JTextField spaceIdField;
    private JTextField resourceIdsField;
    private JTextField quantitiesField;
    private JTextField startTimeField;
    private JTextField endTimeField;

    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);
    // *** FIX: Changed format to include colons and be consistent with other GUIs ***
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); 
    // Format prompt text for user
    private static final String DATE_TIME_FORMAT_PROMPT = "YYYY-MM-DD HH:mm:ss";

    public BookingDialog(JFrame parent, User user) {
        super(parent, "Create New Booking", true);
        this.loggedInUser = user;
        setSize(550, 500); // Increased height slightly to accommodate clearer labels
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND_GRAY);

        add(createHeader(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createSubmitButton(), BorderLayout.SOUTH);
    }

    private JLabel createHeader() {
        JLabel header = new JLabel("Book a Space and/or Resources", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setBorder(new EmptyBorder(15, 0, 15, 0));
        return header;
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        formPanel.setBorder(new EmptyBorder(10, 30, 10, 30));
        formPanel.setBackground(BACKGROUND_GRAY);

        JLabel userIdLabel = new JLabel("Booking as User ID " + loggedInUser.getUserId() +
                " (" + loggedInUser.getFirstName() + ")", SwingConstants.CENTER);
        userIdLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        formPanel.add(userIdLabel);

        formPanel.add(new JSeparator());

        // Default to 0 for space ID
        formPanel.add(new JLabel("Space ID (Optional, 0 for resources only):"));
        spaceIdField = new JTextField("0"); 
        formPanel.add(spaceIdField);

        formPanel.add(new JLabel("Resource IDs (comma-separated, e.g. 1,3,4):"));
        resourceIdsField = new JTextField();
        formPanel.add(resourceIdsField);

        formPanel.add(new JLabel("Quantities (comma-separated, must match IDs, e.g. 2,1,1):"));
        quantitiesField = new JTextField();
        formPanel.add(quantitiesField);

        // Date/Time fields with corrected prompt
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        
        formPanel.add(new JLabel("Start Time (" + DATE_TIME_FORMAT_PROMPT + "):"));
        startTimeField = new JTextField(now.plusDays(1).format(FORMATTER));
        formPanel.add(startTimeField);

        formPanel.add(new JLabel("End Time (" + DATE_TIME_FORMAT_PROMPT + "):"));
        endTimeField = new JTextField(now.plusDays(1).plusHours(2).format(FORMATTER));
        formPanel.add(endTimeField);

        return formPanel;
    }

    private JPanel createSubmitButton() {
        JButton submitButton = new JButton("Confirm Booking");
        submitButton.setBackground(PRIMARY_NAVY);
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        submitButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        submitButton.addActionListener(e -> handleCreateBooking());

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(BACKGROUND_GRAY);
        southPanel.add(submitButton);
        return southPanel;
    }

    private void handleCreateBooking() {
        try {
            // 1. Parse Inputs
            int userId = loggedInUser.getUserId();
            Integer spaceId = null;
            String spaceIdText = spaceIdField.getText().trim();
            
            if (!(spaceIdText.isEmpty() || spaceIdText.equals("0"))) {
                spaceId = Integer.parseInt(spaceIdText);
            }
            
            String resourceIds = resourceIdsField.getText().trim();
            String quantities = quantitiesField.getText().trim();
            
            // Set to null if empty so DAO can handle it
            if (resourceIds.isEmpty()) resourceIds = null;
            if (quantities.isEmpty()) quantities = null;

            // Validate that at least a space or resources are provided
            if (spaceId == null && resourceIds == null) {
                showError("You must book either a Space OR one or more Resources.");
                return;
            }

            // Validate quantities/resources match
            if ((resourceIds != null && quantities == null) || (resourceIds == null && quantities != null)) {
                showError("If booking resources, both IDs and Quantities must be provided.");
                return;
            }
            if (resourceIds != null && quantities != null) {
                 int numIds = resourceIds.split(",").length;
                 int numQuantities = quantities.split(",").length;
                 if (numIds != numQuantities) {
                      showError("The number of Resource IDs and Quantities must match.");
                      return;
                 }
            }


            LocalDateTime start = LocalDateTime.parse(startTimeField.getText().trim(), FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endTimeField.getText().trim(), FORMATTER);

            if (start.isAfter(end) || start.isEqual(end)) {
                showError("Start time must be before end time.");
                return;
            }

            // 2. Call DAO (data access object)
            String result = bookingDAO.createBooking(userId, spaceId, resourceIds, quantities, start, end);

            // 3. Show result
            if (result.startsWith("SUCCESS")) {
                JOptionPane.showMessageDialog(this, result, "Booking Confirmed", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
            } else {
                // If the error came from the stored procedure, remove the "BOOKING FAILED: " prefix
                String cleanMessage = result.replace("BOOKING FAILED: ", "").replace("DATABASE ERROR: ", "");
                showError(cleanMessage);
            }
        } catch (NumberFormatException ex) {
            showError("Space ID must be a number, and resource lists must contain comma-separated numbers only.");
        } catch (DateTimeParseException ex) {
            showError("DateTime format error. Use " + DATE_TIME_FORMAT_PROMPT + ".");
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }
}