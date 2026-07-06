package SpaceResourceBooking;

public class Resource {
    
     int resourceId;
     String resourceName;
     String description;
     double hourlyRate;
     int quantityAvailable;
     String status;
     
    public Resource(int resourceId, String resourceName, String description, double hourlyRate, int quantityAvailable, String status) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.description = description;
        this.hourlyRate = hourlyRate;
        this.quantityAvailable = quantityAvailable;
        this.status = status;
    }

    public Resource(int resourceId, String resourceName, double hourlyRate, int remainingQuantity) {
        this.resourceId = resourceId;
        this.resourceName = resourceName;
        this.hourlyRate = hourlyRate;
        this.quantityAvailable = remainingQuantity;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getDescription() {
        return description;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public int getQuantityAvailable() {
        return quantityAvailable;
    }
    
    public String getStatus() {
        return status;
    }
    
    @Override
    public String toString() {
        return "Resource{" +
                "id=" + resourceId +
                ", name='" + resourceName + '\'' +
                ", rate=" + hourlyRate +
                ", qty=" + quantityAvailable +
                ", status='" + status + '\'' +
                '}';
    }
}