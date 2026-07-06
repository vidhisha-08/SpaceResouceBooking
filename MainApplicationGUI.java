package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

public class MainApplicationGUI extends JFrame {

    /**
	 *	
	 */
	private static final long serialVersionUID = 1L;
	private final User loggedInUser; 
    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);

    // Initialize DAO objects
    final Booking booking = new Booking();
    final SpaceResource sr = new SpaceResource();
    // Assuming Management, Reporting classes exist
    final Management management = new Management(); 
    final Reporting reporting = new Reporting();
    
    private JTabbedPane mainTabbedPane;

    public MainApplicationGUI(User user) throws SQLException {
        this.loggedInUser = user;
        setTitle("Booking System - " + user.getRole().toUpperCase() + " Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 750);
        setMinimumSize(new Dimension(900, 700));
        setLocationRelativeTo(null);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logoutAndExit();
            }
        });
        
        setLayout(new BorderLayout());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContentArea(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY_NAVY);
        header.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Book YOUR Space With US");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.add(title, BorderLayout.WEST);

        JButton logoutButton = new JButton("Logout (" + loggedInUser.getFirstName() + ")");
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(PRIMARY_NAVY);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.addActionListener(e -> logoutAndExit());
        header.add(logoutButton, BorderLayout.EAST);

        return header;
    }
    
    private JComponent createMainContentArea() throws SQLException {
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        // 1. Initial Dashboard (Index 0)
        mainTabbedPane.addTab("Home", createRoleDashboardPanel());

        // 2. Availability Check (Index 1)
        mainTabbedPane.addTab("Availability Check", new AvailabilityPanel()); 
        
        // 3. Role-Specific Tabs
        if (loggedInUser.getRole().equals("admin")) {
            // Admin Tabs (Indices 2 & 3)
            mainTabbedPane.addTab("Management", new AdminManagementPanel());
            mainTabbedPane.addTab("Reports", createReportsTabbedPane());
        } else {
            // User-specific tabs (Indices 2 & 3)
            mainTabbedPane.addTab("My Bookings", new MyBookingsPanel(loggedInUser.getUserId())); 
            mainTabbedPane.addTab("Add Review", new ReviewPanel(loggedInUser.getUserId()));
        } 

        return mainTabbedPane;
    }

    // --- Dashboard Panel ---
    private JPanel createRoleDashboardPanel() {
        JPanel dashboard = new JPanel(new GridBagLayout());
        dashboard.setBackground(BACKGROUND_GRAY);
        dashboard.setBorder(new EmptyBorder(50, 50, 50, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        
        // "Welcome" Header
        JLabel welcomeLabel = new JLabel("Welcome, " + loggedInUser.getFirstName() + "!", SwingConstants.CENTER);
        welcomeLabel.setBackground(PRIMARY_NAVY);
        welcomeLabel.setOpaque(true);
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        welcomeLabel.setBorder(new EmptyBorder(10, 50, 10, 50));
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dashboard.add(welcomeLabel, gbc);
        
        // --- Navigation Buttons ---
        gbc.gridwidth = 1;
        gbc.ipadx = 50; 
        gbc.ipady = 15;
        
        // Review Tab Index (User: index 3, Admin: N/A)
        final int userMyBookingTabIndex = 2; 
        final int userReviewTabIndex = 3; 

        gbc.gridy = 1;
        JButton checkSpacesBtn = createDashboardButton("Check Availability");
        checkSpacesBtn.addActionListener(e -> mainTabbedPane.setSelectedIndex(1)); // Availability Check is index 1
        dashboard.add(checkSpacesBtn, gbc);

        gbc.gridy = 2;
        JButton createBookingBtn = createDashboardButton("Create a Booking");
        createBookingBtn.addActionListener(e -> {
            BookingDialog dialog = new BookingDialog(this, loggedInUser);
            dialog.setVisible(true);
        }); 
        dashboard.add(createBookingBtn, gbc);

        // Button 3: Review a product (User Only) / Management Tab (Admin Only)
        gbc.gridy = 3;
        if (!loggedInUser.getRole().equals("admin")) {
            JButton reviewBtn = createDashboardButton("Review a Space");
            reviewBtn.addActionListener(e -> mainTabbedPane.setSelectedIndex(userReviewTabIndex));
            dashboard.add(reviewBtn, gbc);
        } else {
             JButton manageBtn = createDashboardButton("Go to Management Panel");
             manageBtn.addActionListener(e -> mainTabbedPane.setSelectedIndex(2)); // Management is index 2 for admin
             dashboard.add(manageBtn, gbc);
        }

        // Button 4: Pay for a booking (User Only) / Reporting (Admin Only)
        gbc.gridy = 4;
        String buttonText;
        JButton actionButton;
        
        if (loggedInUser.getRole().equals("admin")) {
             buttonText = "View Reports";
             actionButton = createDashboardButton(buttonText);
             actionButton.addActionListener(e -> mainTabbedPane.setSelectedIndex(3)); // Reports is index 3 for admin
        } else {
             buttonText = "View & Pay Bookings";
             actionButton = createDashboardButton(buttonText);
             actionButton.addActionListener(e -> {
                mainTabbedPane.setSelectedIndex(userMyBookingTabIndex); // My Bookings is index 2 for user
                
                String bookingIdInput = JOptionPane.showInputDialog(
                    this, 
                    "Enter the Booking ID you wish to pay for (Leave blank to just view):", 
                    "Payment Action", 
                    JOptionPane.PLAIN_MESSAGE
                );
                
                if (bookingIdInput != null && !bookingIdInput.trim().isEmpty()) {
                    try {
                        int bookingId = Integer.parseInt(bookingIdInput.trim());
                        
                        // *** Corrected call to completeBookingPayment ***
                        if (booking.completeBookingPayment(bookingId, loggedInUser.getUserId())) {
                            JOptionPane.showMessageDialog(this, "Booking " + bookingId + " marked as 'completed' (paid)!", "Payment Successful", JOptionPane.INFORMATION_MESSAGE);
                            // Refresh the My Bookings tab by re-initializing its data
                            if (mainTabbedPane.getComponentAt(userMyBookingTabIndex) instanceof MyBookingsPanel) {
                                ((MyBookingsPanel) mainTabbedPane.getComponentAt(userMyBookingTabIndex)).initializeBookingsTable(); 
                            }
                        } else {
                            JOptionPane.showMessageDialog(this, "Payment failed. Check if the ID is correct, if it's yours, and if the status is 'pending'.", "Payment Failed", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid Booking ID. Please enter a number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
             });
        }
        dashboard.add(actionButton, gbc);
        
        return dashboard;
    }
    
    private JButton createDashboardButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY_NAVY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
    
    private JComponent createReportsTabbedPane() {
        JTabbedPane reportsTabbedPane = new JTabbedPane();
        reportsTabbedPane.setFont(new Font("SansSerif", Font.PLAIN, 14));
        
        // ReportPanel requires a title and a method reference (Supplier<ResultSet>)
        
        reportsTabbedPane.addTab("Booking Summary", new ReportPanel(
            "Recent Booking Summary (Top 20)", 
            reporting::viewBookingSummary
        ));

        reportsTabbedPane.addTab("Monthly Revenue", new ReportPanel(
            "Monthly Revenue Report", 
            reporting::viewMonthlyRevenue
        ));
        
        reportsTabbedPane.addTab("Unused Resources", new ReportPanel(
            "Resources Never Booked", 
            reporting::viewUnusedResources
        ));
        
        reportsTabbedPane.addTab("Top Rated Spaces", new ReportPanel(
            "Top 5 Highest Rated Spaces", 
            reporting::viewTopRatedSpaces
        ));

        return reportsTabbedPane;
    }

    private void logoutAndExit() {
        if (JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to log out?", "Confirm Logout", 
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            
            this.dispose(); 
            new AuthGUI().setVisible(true);
        }
    }
}