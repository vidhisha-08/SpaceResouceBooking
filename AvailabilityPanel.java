package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.SQLException;

public class AvailabilityPanel extends JPanel { 

    /**
	 *	
	 */
	private static final long serialVersionUID = 1L;
	private final SpaceResource sr = new SpaceResource();
    private JTextField startTimeField;
    private JTextField endTimeField;
    private JPanel spacesPanel;
    private JPanel resourcesPanel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);
    private static final Color PRIMARY_NAVY = new Color(15, 44, 89);

    public AvailabilityPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 30, 20, 30));
        setBackground(BACKGROUND_GRAY);

        // Header and Input
        add(createInputPanel(), BorderLayout.NORTH);

        // Results Display
        add(createResultsPanel(), BorderLayout.CENTER);
        
        // Initial load using default times
        SwingUtilities.invokeLater(this::initialLoad);
    }
    
    private void initialLoad() {
         try {
            updateAvailability();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Initial load failed: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            System.err.println("Initial Availability Load Error: " + e.getMessage());
        }
    }

    private JPanel createInputPanel() {
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        inputPanel.setBackground(BACKGROUND_GRAY);
        
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        
        startTimeField = new JTextField(now.format(FORMATTER), 18);
        endTimeField = new JTextField(now.plusHours(2).format(FORMATTER), 18);
        
        JButton searchButton = new JButton("Check Availability");
        searchButton.setBackground(PRIMARY_NAVY);
        searchButton.setForeground(Color.WHITE);
        searchButton.addActionListener(e -> {
			try {
				updateAvailability();
			} catch (SQLException e1) {
				JOptionPane.showMessageDialog(this, "Error checking availability: " + e1.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
			}
		});

        inputPanel.add(new JLabel("Start Time (YYYY-MM-DD HH:mm:ss):"));
        inputPanel.add(startTimeField);
        inputPanel.add(new JLabel("End Time (YYYY-MM-DD HH:mm:ss):"));
        inputPanel.add(endTimeField);
        inputPanel.add(searchButton);
        
        return inputPanel;
    }

    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        resultsPanel.setBackground(BACKGROUND_GRAY);
        
        spacesPanel = createReportContainer("Available Spaces");
        resourcesPanel = createReportContainer("Available Resources");
        
        resultsPanel.add(spacesPanel);
        resultsPanel.add(resourcesPanel);
        
        return resultsPanel;
    }

    private JPanel createReportContainer(String title) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_NAVY, 2), 
            title, 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 16), PRIMARY_NAVY
        ));
        container.setBackground(Color.WHITE);
        return container;
    }

    private void updateAvailability() throws SQLException {
        try {
            LocalDateTime start = LocalDateTime.parse(startTimeField.getText().trim(), FORMATTER);
            LocalDateTime end = LocalDateTime.parse(endTimeField.getText().trim(), FORMATTER);
            
            if (start.isAfter(end) || start.isEqual(end)) {
                 JOptionPane.showMessageDialog(this, "Start time must be before end time.", "Time Error", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            // --- 1. Update Spaces (using the List return from DAO) ---
            List<Space> availableSpaces = sr.findAvailableSpaces(start, end);
            updateSpaceTable(availableSpaces);

            // --- 2. Update Resources (direct query in GUI or refactor to DAO, keeping direct query for now) ---
            updateResourceTable(start, end);

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Date/Time format error. Use YYYY-MM-DD HH:mm:ss.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Helper to run the existing space logic against a JTable
    private void updateSpaceTable(List<Space> spaces) {
        String[] columnNames = {"ID", "Name", "City", "Rate (per hr)", "Capacity"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Assuming Space class has public getters (or fields based on previous code)
        for (Space space : spaces) {
            model.addRow(new Object[]{
                space.getSpaceId(),
                space.getSpaceName(),
                space.getLocationCity(),
                String.format("$%.2f", space.getHourlyRate()),
                space.getCapacity()
            });
        }
        
        refreshPanel(spacesPanel, model);
    }

    // Helper to query and display available resources using the DAO logic.
    private void updateResourceTable(LocalDateTime start, LocalDateTime end) {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            if (conn == null) return;
           
         // Inside AvailabilityPanel.java, in the updateResourceTable() method:

            String sql = "SELECT r.resource_id AS ID, r.resource_name AS Name, r.hourly_rate AS Rate, " +
                         "r.quantity_available AS TotalQty, " + 
                         "(r.quantity_available - COALESCE(SUM(rb.quantity), 0)) AS 'Remaining Qty' " +
                         "FROM RESOURCE r " +
                         "LEFT JOIN RESOURCE_BOOKING rb ON r.resource_id = rb.resource_id " +
                         "LEFT JOIN BOOKING b ON rb.booking_id = b.booking_id AND b.payment_status NOT IN ('cancelled', 'completed') AND " +
                         "b.end_time > CAST(? AS DATETIME) AND b.start_time < CAST(? AS DATETIME) " + 
                         "WHERE r.status = 'Available' " +
                         "AND NOT EXISTS (SELECT 1 FROM MAINTENANCE_SCHEDULE ms WHERE ms.resource_id = r.resource_id AND " +
                         "ms.end_time > CAST(? AS DATETIME) AND ms.start_time < CAST(? AS DATETIME)) " +
                         "GROUP BY r.resource_id, r.resource_name, r.hourly_rate, r.quantity_available " +
                         // FIX: Use the correct column name 'quantity_available'
                         "HAVING r.quantity_available - COALESCE(SUM(rb.quantity), 0) > 0";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                Timestamp startTs = Timestamp.valueOf(start);
                Timestamp endTs = Timestamp.valueOf(end);
                
                ps.setTimestamp(1, startTs); ps.setTimestamp(2, endTs);
                ps.setTimestamp(3, startTs); ps.setTimestamp(4, endTs);

                try (ResultSet rs = ps.executeQuery()) {
                    // ReportPanel.buildTableModel is assumed to exist and work
                    DefaultTableModel model = ReportPanel.buildTableModel(rs); 
                    refreshPanel(resourcesPanel, model);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error querying available resources: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
             System.err.println("Availability Panel Resource Query Error: " + e.getMessage());
        } finally {
            DBManager.closeConnection(conn);
        }
    }
    
    // Utility to clear and redraw the table in a panel
    private void refreshPanel(JPanel panel, DefaultTableModel model) {
        panel.removeAll();
        if (model.getRowCount() == 0) {
             panel.add(new JLabel("No items available in the selected time range.", SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
             JTable table = new JTable(model);
             table.setFillsViewportHeight(true);
             table.setFont(new Font("SansSerif", Font.PLAIN, 14));
             table.setRowHeight(25);
             panel.add(new JScrollPane(table), BorderLayout.CENTER);
        }
        
        panel.revalidate();
        panel.repaint();
    }
}