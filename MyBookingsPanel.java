package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MyBookingsPanel extends JPanel {

    private static final long serialVersionUID = 1L;
	private final int userId;
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);
    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);

    public MyBookingsPanel(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 30, 20, 30));
        setBackground(BACKGROUND_GRAY);

        JLabel header = new JLabel("My Current and Past Bookings (User ID: " + userId + ")", SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 24));
        header.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(header, BorderLayout.NORTH);

        // Initial load
        initializeBookingsTable();
        
        JButton refreshButton = new JButton("Refresh List");
        refreshButton.setBackground(PRIMARY_NAVY);
        refreshButton.setForeground(Color.WHITE);
        refreshButton.addActionListener(e -> initializeBookingsTable());
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setBackground(BACKGROUND_GRAY);
        southPanel.add(refreshButton);
        
        add(southPanel, BorderLayout.SOUTH);
    }

    void initializeBookingsTable() {
        Component centerComponent = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            remove(centerComponent);
        }

        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            if (conn == null) {
                add(new JLabel("Database connection failed.", SwingConstants.CENTER), BorderLayout.CENTER);
                revalidate();
                repaint();
                return;
            }
            
            // --- SQL Query: Joins BOOKING to get space details if available ---
            String sql = "SELECT b.booking_id AS ID, " +
                         "COALESCE(s.space_name, 'Resource-Only Booking') AS ItemName, " +
                         "b.start_time AS Start, b.end_time AS End, " +
                         "b.total_cost AS Cost, b.payment_status AS Status " +
                         "FROM BOOKING b " +
                         "LEFT JOIN SPACE_BOOKING sb ON b.booking_id = sb.booking_id " +
                         "LEFT JOIN SPACE s ON sb.space_id = s.space_id " +
                         "WHERE b.user_id = ? " +
                         "ORDER BY b.start_time DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    DefaultTableModel model = ReportPanel.buildTableModel(rs); 
                    
                    if (model.getRowCount() == 0) {
                         add(new JLabel("You have no bookings.", SwingConstants.CENTER), BorderLayout.CENTER);
                    } else {
                         JTable bookingsTable = new JTable(model);
                         bookingsTable.setFillsViewportHeight(true);
                         bookingsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
                         bookingsTable.setRowHeight(25);
                         
                         
                         add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
                    }
                    
                    revalidate();
                    repaint();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading bookings: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            DBManager.closeConnection(conn);
        }
    }
}