package SpaceResourceBooking;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class Management {

    /**
     * User Function: Adds a review for a space or a resource using a stored procedure.
     * @param userId The ID of the user submitting the review.
     * @param spaceId The ID of the space (can be null).
     * @param resourceId The ID of the resource (can be null).
     * @param rating The rating (1-5).
     * @param comment The user's comment.
     * @return A success string or an error message (including custom DB errors).
     */
    public String addReview(int userId, Integer spaceId, Integer resourceId, int rating, String comment) {
        String sql = "{CALL AddReview(?, ?, ?, ?, ?)}";
        Connection conn = null;

        try {
            conn = DBManager.getConnection();
            if (conn == null) return "DATABASE ERROR: Connection failed.";

            try (CallableStatement stmt = conn.prepareCall(sql)) {

                stmt.setInt(1, userId);

                // Space ID (can be NULL)
                if (spaceId != null) {
                    stmt.setInt(2, spaceId);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }

                // Resource ID (can be NULL)
                if (resourceId != null) {
                    stmt.setInt(3, resourceId);
                } else {
                    stmt.setNull(3, Types.INTEGER);
                }

                stmt.setInt(4, rating);
                stmt.setString(5, comment);

                stmt.execute();
                return "SUCCESS: Review submitted!";

            } catch (SQLException e) {
                // Catches the custom error SIGNAL from the stored procedure (SQLSTATE '45000')
                if (e.getSQLState() != null && e.getSQLState().equals("45000")) {
                    return "REVIEW FAILED: " + e.getMessage();
                }
                throw e; 
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE ERROR: " + e.getMessage();
        } finally {
            DBManager.closeConnection(conn);
        }
    }

    /**
     * Admin Function: Updates the hourly rate for a specific resource.
     * @param resourceId The ID of the resource to update.
     * @param newRate The new hourly rate.
     * @return true if the rate was updated, false otherwise.
     */
    public boolean updateResourceRate(int resourceId, double newRate) {
        String sql = "UPDATE RESOURCE SET hourly_rate = ? WHERE resource_id = ?";
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDouble(1, newRate);
                ps.setInt(2, resourceId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating resource rate: " + e.getMessage());
            return false;
        } finally {
            DBManager.closeConnection(conn);
        }
    }

    /**
     * Admin Function: Deletes maintenance records older than 6 months.
     * @return The number of rows affected, or -1 on failure.
     */
    public int deleteOldMaintenance() {
        String sql = "DELETE FROM maintenance_schedule WHERE end_time < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)";
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            if (conn == null) return -1;
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                int rowsAffected = ps.executeUpdate();
                return rowsAffected;
            }
        } catch (SQLException e) {
            System.err.println("Error running maintenance cleanup: " + e.getMessage());
            return -1; // Indicate failure
        } finally {
            DBManager.closeConnection(conn);
        }
    }
     public List<Object[]> getUserBookings(int userId) {
         // FIX: This complex query uses LEFT JOINs to get details for either a Space or a Resource booking.
         // It uses COALESCE to get the correct Item Name and Item ID, avoiding the 'Unknown column' error.
         String sql = "SELECT b.booking_id, " +
                      "COALESCE(s.space_name, r.resource_name) AS ItemName, " +
                      "b.start_time, b.end_time, b.total_cost, b.payment_status " +
                      "FROM BOOKING b " +
                      "LEFT JOIN SPACE_BOOKING sb ON b.booking_id = sb.booking_id " +
                      "LEFT JOIN SPACE s ON sb.space_id = s.space_id " +
                      "LEFT JOIN RESOURCE_BOOKING rb ON b.booking_id = rb.booking_id " +
                      "LEFT JOIN RESOURCE r ON rb.resource_id = r.resource_id " +
                      "WHERE b.user_id = ? " +
                      "ORDER BY b.start_time DESC";

         List<Object[]> bookings = new ArrayList<>();
         Connection conn = null;

         try {
             conn = DBManager.getConnection();
             if (conn == null) return bookings;

             try (PreparedStatement ps = conn.prepareStatement(sql)) {
                 ps.setInt(1, userId);
                 try (ResultSet rs = ps.executeQuery()) {
                     while (rs.next()) {
                         // Note: Column index starts at 1 in JDBC
                         Object[] row = new Object[]{
                             rs.getInt(1),       // ID
                             rs.getString(2),    // ItemName
                             rs.getTimestamp(3), // Start
                             rs.getTimestamp(4), // End
                             rs.getDouble(5),    // Cost
                             rs.getString(6)     // Status
                         };
                         bookings.add(row);
                     }
                 }
             }
         } catch (SQLException e) {
             System.err.println("Database Error loading user bookings: " + e.getMessage());
             e.printStackTrace();
         } finally {
             DBManager.closeConnection(conn);
         }
         return bookings;
     }
}