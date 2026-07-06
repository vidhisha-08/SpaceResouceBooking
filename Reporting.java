package SpaceResourceBooking;

import java.sql.*;

public class Reporting {

    // 1. viewBookingSummary - Refactored for GUI display
    public ResultSet viewBookingSummary(Connection conn) throws SQLException {
        // Query remains similar, but we remove connection logic and printing
        String sql = "SELECT * FROM booking_summary ORDER BY booking_id DESC LIMIT 20";
        // Do NOT close the statement/result set here; ReportPanel handles that.
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 2. viewTopRatedSpaces - Refactored for GUI display
    public ResultSet viewTopRatedSpaces(Connection conn) throws SQLException {
        String sql = "SELECT s.space_name AS 'Space Name', AVG(r.rating) AS 'Avg Rating' " +
                     "FROM SPACE s " +
                     "JOIN REVIEW r ON s.space_id = r.space_id " +
                     "GROUP BY s.space_name " +
                     "ORDER BY 2 DESC, 1 ASC " +
                     "LIMIT 5";
        
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 3. viewMonthlyRevenue - Refactored for GUI display
    public ResultSet viewMonthlyRevenue(Connection conn) throws SQLException {
        String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS 'Month/Year', SUM(amount) AS 'Total Revenue' " +
                     "FROM PAYMENT " +
                     "GROUP BY 1 " +
                     "ORDER BY 1 DESC";
        
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // 4. viewUnusedResources - Refactored for GUI display
    public ResultSet viewUnusedResources(Connection conn) throws SQLException {
        String sql = "SELECT resource_id AS ID, resource_name AS Name " +
                     "FROM RESOURCE " +
                     "WHERE resource_id NOT IN (SELECT DISTINCT resource_id FROM RESOURCE_BOOKING)";
        
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }
}