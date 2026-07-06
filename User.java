package SpaceResourceBooking;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdAt;
    private int locationId;
    private String address;
    private String city;
    private String country;
    private String passwordHash;
    private String role;

    public User(int userId, String firstName, String lastName, String email, 
                String passwordHash, String role) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
    	this.createdAt = createdAt; 
    }
    
    public int getUserId() {
        return userId;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getLocationId() {
        return locationId;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + userId +
                ", name='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", location='" + (city != null ? city : "N/A") + ", " + 
                (country != null ? country : "N/A") + '\'' +
                '}';
    }
}