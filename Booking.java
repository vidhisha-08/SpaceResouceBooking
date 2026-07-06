package SpaceResourceBooking;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class Booking {

    /**
     * Calls the stored procedure to create a booking for a space, resources, or both.
     * @param userId The ID of the user creating the booking.
     * @param spaceId The ID of the space (can be null).
     * @param resourceIds Comma-separated string of resource IDs.
     * @param quantities Comma-separated string of corresponding quantities.
     * @param start The booking start time.
     * @param end The booking end time.
     * @return A success string containing the booking ID and total cost, or an error message.
     */
    public String createBooking(int userId, Integer spaceId, String resourceIds, String quantities, LocalDateTime start, LocalDateTime end) {
        String sql = "{CALL CreateCombinedBooking(?, ?, ?, ?, ?, ?)}"; 
        Connection conn = null;
        
        try {
            conn = DBManager.getConnection();
            if (conn == null) return "DATABASE ERROR: Connection failed.";
            
            // 1. Prepare the CallableStatement
            try (CallableStatement stmt = conn.prepareCall(sql)) {
                
                // 2. Set the input parameters
                stmt.setInt(1, userId);
                
                // Handle optional space_id parameter 
                if (spaceId != null) {
                    stmt.setInt(2, spaceId);
                } else {
                    stmt.setNull(2, Types.INTEGER);
                }
                
                // Resource strings
                stmt.setString(3, resourceIds);
                stmt.setString(4, quantities);
                
                // Timestamps
                stmt.setTimestamp(5, Timestamp.valueOf(start));
                stmt.setTimestamp(6, Timestamp.valueOf(end));
                
                // 3. Execute the procedure
                boolean hasResults = stmt.execute();
                
                // 4. Retrieve results (booking_id and total_cost are returned by SELECT in the procedure)
                if (hasResults) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        if (rs.next()) {
                            int newBookingId = rs.getInt("booking_id");
                            double totalCost = rs.getDouble("total_cost");
                            
                            // Successful return format
                            return String.format("SUCCESS: Booking ID %d created. Total Cost: $%.2f (Status: pending)", newBookingId, totalCost);
                        }
                    }
                }
                
                // Fallback success message if no result set was returned
                return "SUCCESS: Booking created (Cost unknown, check DB).";

            } catch (SQLException e) {
                // Catches the custom error SIGNAL from the stored procedure (SQLSTATE '45000')
                if (e.getSQLState() != null && e.getSQLState().equals("45000")) {
                    return "BOOKING FAILED: " + e.getMessage();
                }
                throw e; // Re-throw other SQL exceptions
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return "DATABASE ERROR: " + e.getMessage();
        } finally {
            DBManager.closeConnection(conn);
        }
    }
    
    /**
     * User function to mark a pending booking as 'completed' (paid).
     * @param bookingId The ID of the booking to complete.
     * @param userId The ID of the user attempting to complete the booking (security check).
     * @return true if the booking was updated, false otherwise.
     */
    public boolean completeBookingPayment(int bookingId, int userId) {
    	String sql = "UPDATE BOOKING SET payment_status = 'completed' " + 
                "WHERE booking_id = ? AND user_id = ? AND payment_status = 'pending'";
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            if (conn == null) return false;
            
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookingId);
                ps.setInt(2, userId);
                
                int rowsAffected = ps.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error completing booking payment: " + e.getMessage());
            return false;
        } finally {
            DBManager.closeConnection(conn);
        }
    }

    /**
     * Public Admin/Management function to update any status (e.g., paid, cancelled, pending).
     * @param bookingId The ID of the booking to update.
     * @param status The new status string.
     * @return true if the status was updated, false otherwise.
     */
    public boolean updatePaymentStatus(int bookingId, String status) {
        String sql = "UPDATE BOOKING SET payment_status = ? WHERE booking_id = ?";
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            if (conn == null) return false;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status);
                ps.setInt(2, bookingId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        } finally {
            DBManager.closeConnection(conn);
        }
    }
    
    /**
     * Cancels a pending or unpaid booking.
     * @param bookingId The ID of the booking to cancel.
     * @return A success or error string.
     */
    public String cancelBooking(int bookingId) {
        String sql = "UPDATE BOOKING SET payment_status = 'cancelled' WHERE booking_id = ? AND payment_status IN ('pending', 'unpaid')";
        Connection conn = null;
        
        try {
            conn = DBManager.getConnection();
            if (conn == null) return "DATABASE ERROR: Connection failed.";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookingId);
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    return "SUCCESS: Booking " + bookingId + " cancelled.";
                } else {
                    return "ERROR: Booking " + bookingId + " not found or payment status prevents cancellation (must be pending or unpaid).";
                }
            }
        } catch (SQLException e) {
            return "DATABASE ERROR during cancellation: " + e.getMessage();
        } finally {
            DBManager.closeConnection(conn);
        }
    }
}