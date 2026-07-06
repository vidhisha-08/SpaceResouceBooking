package SpaceResourceBooking;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ReportPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private final String reportTitle;
    
    public interface ReportExecutor {
        ResultSet execute(Connection conn) throws SQLException;
    }

    private final ReportExecutor reportExecutor;
    private static final Color BACKGROUND_GRAY = new Color(195, 199, 206);

    public ReportPanel(String title, ReportExecutor executor) {
        this.reportTitle = title;
        this.reportExecutor = executor;
        
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_GRAY);

        JLabel header = new JLabel(title, SwingConstants.CENTER);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(header, BorderLayout.NORTH);

        initializeReportTable();
        
        JButton refreshButton = new JButton("Refresh Data");
        refreshButton.addActionListener(e -> initializeReportTable());
        add(refreshButton, BorderLayout.SOUTH);
    }
    
    private void initializeReportTable() {
        Component centerComponent = ((BorderLayout)getLayout()).getLayoutComponent(BorderLayout.CENTER);
        if (centerComponent != null) {
            remove(centerComponent);
        }
        
        try (Connection conn = DBManager.getConnection();
             ResultSet rs = reportExecutor.execute(conn)) { 
            
            DefaultTableModel model = buildTableModel(rs);
            
            JTable reportTable = new JTable(model);
            reportTable.setFillsViewportHeight(true);
            reportTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
            reportTable.setRowHeight(25);
            
            JScrollPane scrollPane = new JScrollPane(reportTable);
            
            add(scrollPane, BorderLayout.CENTER);
            revalidate();
            repaint();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading report '" + reportTitle + "': " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
  
    public static DefaultTableModel buildTableModel(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();

        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for (int column = 1; column <= columnCount; column++) {
            columnNames.add(metaData.getColumnLabel(column));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (rs.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                vector.add(rs.getObject(columnIndex));
            }
            data.add(vector);
        }

        return new DefaultTableModel(data, columnNames);
    }
}