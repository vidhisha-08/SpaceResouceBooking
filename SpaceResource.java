package SpaceResourceBooking;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class Space {
    int spaceId;
    String spaceName;
    String description;
    int capacity;
    double hourlyRate;
    String locationCity;
    
    // Main constructor used for retrieving available spaces
    public Space(int id, String name, String desc, int cap, double rate, String city) {
        this.spaceId = id;
        this.spaceName = name;
        this.description = desc;
        this.capacity = cap;
        this.hourlyRate = rate;
        this.locationCity = city;
    }

    public int getSpaceId() { return spaceId; }
    public String getSpaceName() { return spaceName; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public double getHourlyRate() { return hourlyRate; }
    public String getLocationCity() { return locationCity; }

   
    public String toString() {
        return String.format("| ID: %-3d | Name: %-20s | City: %-15s | Rate: $%-7.2f | Capacity: %-3d |", 
            spaceId, spaceName, locationCity, hourlyRate, capacity);
    }
}

public class SpaceResource {

    public List<Space> findAvailableSpaces(LocalDateTime start, LocalDateTime end) throws SQLException {
        List<Space> availableSpaces = new ArrayList<>();
        Connection conn = null;

        // --- SQL QUERY ---
        // This query finds spaces that are 'Available' AND do NOT have an overlapping booking
        // in the 'SPACE_BOOKING' join table.
        String sql = "SELECT s.space_id, s.space_name, s.description, s.capacity, s.hourly_rate, l.city " +
                     "FROM SPACE s JOIN LOCATION l ON s.location_id = l.location_id " +
                     "WHERE s.status = 'Available' AND s.space_id NOT IN (" +
                     "  SELECT sb.space_id FROM SPACE_BOOKING sb " + 
                     "  JOIN BOOKING b ON sb.booking_id = b.booking_id " + 
                     "  WHERE b.payment_status NOT IN ('cancelled', 'completed') " + 
                     "  AND b.end_time > ? " +   
                     "  AND b.start_time < ? " + 
                     ")";

        try {
            conn = DBManager.getConnection();
            if (conn == null) throw new SQLException("Database connection failed.");
      
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                Timestamp startTs = Timestamp.valueOf(start);
                Timestamp endTs = Timestamp.valueOf(end);
                
                ps.setTimestamp(1, startTs); 
                ps.setTimestamp(2, endTs);   

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Space space = new Space(
                            rs.getInt("space_id"),
                            rs.getString("space_name"),
                            rs.getString("description"),
                            rs.getInt("capacity"),
                            rs.getDouble("hourly_rate"),
                            rs.getString("city")
                        );
                        availableSpaces.add(space);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error finding available spaces: " + e.getMessage());
            throw e; // Re-throw to be handled by the GUI layer (AvailabilityPanel)
        } finally {
            DBManager.closeConnection(conn);
        }
        return availableSpaces;
    }
}